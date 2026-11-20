package com.example.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.local.SudokuRepository
import com.example.domain.engine.SudokuEngine
import com.example.domain.model.Difficulty
import com.example.domain.model.GameMode
import com.example.domain.model.GameStatus
import com.example.domain.model.HintResult
import com.example.domain.model.ResumePreview
import com.example.domain.model.SudokuCell
import com.example.domain.model.UndoSnapshot
import com.example.domain.model.UserPreferences
import com.example.domain.model.WoodTheme
import com.example.domain.model.WorkshopStats
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.UUID

data class GameUiState(
    val gameId: String = "",
    val mode: GameMode = GameMode.CLASSIC_9X9,
    val difficulty: Difficulty = Difficulty.ARTISAN,
    val isDailyChallenge: Boolean = false,
    val dailyDateKey: String? = null,
    val cells: List<SudokuCell> = emptyList(),
    val selectedRow: Int = -1,
    val selectedCol: Int = -1,
    val isNotesMode: Boolean = false,
    val elapsedSeconds: Int = 0,
    val mistakes: Int = 0,
    val maxMistakes: Int = 3,
    val hintsUsed: Int = 0,
    val status: GameStatus = GameStatus.LOADING,
    val canUndo: Boolean = false,
    val activeHint: HintResult? = null,
    val celebratingIndices: Set<Int> = emptySet(),
    val isNewBestTime: Boolean = false,
    val remainingCounts: Map<Int, Int> = emptyMap(),
    val hapticTrigger: Long = 0L,
    val errorHapticTrigger: Long = 0L
)

class SudokuViewModel(
    private val repository: SudokuRepository
) : ViewModel() {

    val preferences: StateFlow<UserPreferences> = repository.preferences
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), UserPreferences())

    val resumePreview: StateFlow<ResumePreview?> = repository.observeLatestResumePreview()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val dailySolvedToday: StateFlow<Boolean> = repository.observeDailySolved()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    val stats: StateFlow<WorkshopStats> = repository.observeStats()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), WorkshopStats())

    private val _selectedHomeMode = MutableStateFlow(GameMode.CLASSIC_9X9)
    val selectedHomeMode: StateFlow<GameMode> = _selectedHomeMode.asStateFlow()

    private val _gameState = MutableStateFlow(GameUiState())
    val gameState: StateFlow<GameUiState> = _gameState.asStateFlow()

    private val undoStack = ArrayDeque<UndoSnapshot>()
    private var timerJob: Job? = null
    private var celebrationJob: Job? = null

    fun selectHomeMode(mode: GameMode) {
        _selectedHomeMode.value = mode
    }

    fun setWoodTheme(theme: WoodTheme) {
        viewModelScope.launch { repository.setWoodTheme(theme) }
    }

    fun setZenMode(enabled: Boolean) {
        viewModelScope.launch { repository.setZenMode(enabled) }
    }

    fun setAutoCleanNotes(enabled: Boolean) {
        viewModelScope.launch { repository.setAutoCleanNotes(enabled) }
    }

    fun setHighlightSameDigits(enabled: Boolean) {
        viewModelScope.launch { repository.setHighlightSameDigits(enabled) }
    }

    fun setHapticsEnabled(enabled: Boolean) {
        viewModelScope.launch { repository.setHapticsEnabled(enabled) }
    }

    fun startNewGame(
        mode: GameMode,
        difficulty: Difficulty,
        isDailyChallenge: Boolean = false
    ) {
        stopTimer()
        undoStack.clear()
        val dailyKey = if (isDailyChallenge) repository.todayDateKey() else null
        val gameId = if (isDailyChallenge) "daily_$dailyKey" else UUID.randomUUID().toString()
        val seed = if (isDailyChallenge) repository.todaySeed() else System.nanoTime()

        _gameState.value = GameUiState(
            gameId = gameId,
            mode = mode,
            difficulty = difficulty,
            isDailyChallenge = isDailyChallenge,
            dailyDateKey = dailyKey,
            status = GameStatus.LOADING
        )

        viewModelScope.launch {
            val generated = withContext(Dispatchers.Default) {
                SudokuEngine.generatePuzzle(mode = mode, difficulty = difficulty, seed = seed)
            }
            val firstEditable = generated.cells.firstOrNull { !it.isGiven }
            _gameState.value = GameUiState(
                gameId = gameId,
                mode = mode,
                difficulty = difficulty,
                isDailyChallenge = isDailyChallenge,
                dailyDateKey = dailyKey,
                cells = generated.cells,
                selectedRow = firstEditable?.row ?: 0,
                selectedCol = firstEditable?.col ?: 0,
                status = GameStatus.PLAYING,
                remainingCounts = computeRemainingCounts(generated.cells, mode)
            )
            persistCurrentGame()
            startTimer()
        }
    }

    fun resumeSavedGame(gameId: String) {
        stopTimer()
        undoStack.clear()
        _gameState.update { it.copy(status = GameStatus.LOADING) }

        viewModelScope.launch {
            val loaded = repository.loadSavedGame(gameId)
            if (loaded == null) {
                startNewGame(GameMode.CLASSIC_9X9, Difficulty.ARTISAN, false)
                return@launch
            }
            val firstEditable = loaded.cells.firstOrNull { !it.isGiven && it.value == 0 }
                ?: loaded.cells.firstOrNull { !it.isGiven }
            _gameState.value = GameUiState(
                gameId = loaded.gameId,
                mode = loaded.mode,
                difficulty = loaded.difficulty,
                isDailyChallenge = loaded.isDailyChallenge,
                dailyDateKey = loaded.dailyDateKey,
                cells = loaded.cells,
                selectedRow = firstEditable?.row ?: 0,
                selectedCol = firstEditable?.col ?: 0,
                elapsedSeconds = loaded.elapsedSeconds,
                mistakes = loaded.mistakes,
                hintsUsed = loaded.hintsUsed,
                status = GameStatus.PLAYING,
                remainingCounts = computeRemainingCounts(loaded.cells, loaded.mode)
            )
            startTimer()
        }
    }

    fun selectCell(row: Int, col: Int) {
        val current = _gameState.value
        if (current.status != GameStatus.PLAYING) return
        _gameState.update {
            it.copy(
                selectedRow = row,
                selectedCol = col,
                hapticTrigger = System.nanoTime()
            )
        }
    }

    fun toggleNotesMode() {
        _gameState.update {
            it.copy(
                isNotesMode = !it.isNotesMode,
                hapticTrigger = System.nanoTime()
            )
        }
    }

    fun enterDigit(digit: Int) {
        val current = _gameState.value
        if (current.status != GameStatus.PLAYING) return
        val n = current.mode.gridSize
        if (current.selectedRow !in 0 until n || current.selectedCol !in 0 until n) return
        val idx = current.selectedRow * n + current.selectedCol
        val targetCell = current.cells.getOrNull(idx) ?: return
        if (targetCell.isGiven) return

        pushUndoSnapshot(current)

        if (current.isNotesMode) {
            if (targetCell.value != 0) return
            val updatedNotes = if (digit in targetCell.notes) {
                targetCell.notes - digit
            } else {
                targetCell.notes + digit
            }
            val updatedCells = current.cells.toMutableList()
            updatedCells[idx] = targetCell.copy(notes = updatedNotes)
            _gameState.update {
                it.copy(
                    cells = updatedCells,
                    canUndo = undoStack.isNotEmpty(),
                    activeHint = null,
                    hapticTrigger = System.nanoTime()
                )
            }
            persistCurrentGameAsync()
            return
        }

        // Inlay digit mode
        val updatedCells = current.cells.toMutableList()
        val isMistake = digit != targetCell.solution
        updatedCells[idx] = targetCell.copy(
            value = digit,
            notes = emptySet()
        )

        val cleanedCells = if (!isMistake && preferences.value.autoCleanNotes) {
            cleanPeerNotes(updatedCells, current.mode, targetCell.row, targetCell.col, digit)
        } else {
            updatedCells
        }

        val checkedCells = SudokuEngine.computeConflicts(cleanedCells, current.mode)
        val newMistakes = if (isMistake) current.mistakes + 1 else current.mistakes
        val zenActive = preferences.value.zenMode
        val lost = !zenActive && newMistakes >= current.maxMistakes
        val won = !lost && SudokuEngine.isBoardSolved(checkedCells)

        if (won) {
            onPuzzleSolved(current, checkedCells, newMistakes, current.hintsUsed)
        } else if (lost) {
            stopTimer()
            _gameState.update {
                it.copy(
                    cells = checkedCells,
                    mistakes = newMistakes,
                    status = GameStatus.GAME_OVER,
                    canUndo = undoStack.isNotEmpty(),
                    activeHint = null,
                    remainingCounts = computeRemainingCounts(checkedCells, current.mode),
                    errorHapticTrigger = System.nanoTime()
                )
            }
            persistCurrentGameAsync()
        } else {
            if (!isMistake) {
                val completedUnits = SudokuEngine.completedUnitsAround(
                    checkedCells, current.mode, targetCell.row, targetCell.col
                )
                if (completedUnits.isNotEmpty()) {
                    triggerUnitCelebration(completedUnits)
                }
            }
            _gameState.update {
                it.copy(
                    cells = checkedCells,
                    mistakes = newMistakes,
                    canUndo = undoStack.isNotEmpty(),
                    activeHint = null,
                    remainingCounts = computeRemainingCounts(checkedCells, current.mode),
                    hapticTrigger = if (!isMistake) System.nanoTime() else it.hapticTrigger,
                    errorHapticTrigger = if (isMistake) System.nanoTime() else it.errorHapticTrigger
                )
            }
            persistCurrentGameAsync()
        }
    }

    fun eraseSelectedCell() {
        val current = _gameState.value
        if (current.status != GameStatus.PLAYING) return
        val n = current.mode.gridSize
        if (current.selectedRow !in 0 until n || current.selectedCol !in 0 until n) return
        val idx = current.selectedRow * n + current.selectedCol
        val targetCell = current.cells.getOrNull(idx) ?: return
        if (targetCell.isGiven || (targetCell.value == 0 && targetCell.notes.isEmpty())) return

        pushUndoSnapshot(current)
        val updatedCells = current.cells.toMutableList()
        updatedCells[idx] = targetCell.copy(value = 0, notes = emptySet(), isConflict = false)
        val checked = SudokuEngine.computeConflicts(updatedCells, current.mode)
        _gameState.update {
            it.copy(
                cells = checked,
                canUndo = undoStack.isNotEmpty(),
                activeHint = null,
                remainingCounts = computeRemainingCounts(checked, current.mode),
                hapticTrigger = System.nanoTime()
            )
        }
        persistCurrentGameAsync()
    }

    fun undo() {
        val current = _gameState.value
        if (current.status != GameStatus.PLAYING || undoStack.isEmpty()) return
        val snap = undoStack.removeLast()
        val restored = current.cells.mapIndexed { idx, cell ->
            cell.copy(
                value = snap.values.getOrElse(idx) { cell.value },
                notes = snap.notes.getOrElse(idx) { cell.notes }
            )
        }
        val checked = SudokuEngine.computeConflicts(restored, current.mode)
        _gameState.update {
            it.copy(
                cells = checked,
                selectedRow = snap.selectedRow,
                selectedCol = snap.selectedCol,
                canUndo = undoStack.isNotEmpty(),
                activeHint = null,
                remainingCounts = computeRemainingCounts(checked, current.mode),
                hapticTrigger = System.nanoTime()
            )
        }
        persistCurrentGameAsync()
    }

    fun fillAllCandidates() {
        val current = _gameState.value
        if (current.status != GameStatus.PLAYING) return
        pushUndoSnapshot(current)

        val n = current.mode.gridSize
        val board = IntArray(n * n) { idx ->
            val cell = current.cells[idx]
            if (cell.value != 0 && !cell.isConflict) cell.value else 0
        }
        val updated = current.cells.map { cell ->
            if (cell.value == 0) {
                val candidates = SudokuEngine.getValidDigits(board, current.mode, cell.row, cell.col).toSet()
                cell.copy(notes = candidates)
            } else {
                cell
            }
        }
        _gameState.update {
            it.copy(
                cells = updated,
                canUndo = undoStack.isNotEmpty(),
                hapticTrigger = System.nanoTime()
            )
        }
        persistCurrentGameAsync()
    }

    fun requestSmartHint() {
        val current = _gameState.value
        if (current.status != GameStatus.PLAYING) return

        val hint = SudokuEngine.findSmartHint(
            cells = current.cells,
            mode = current.mode,
            selectedRow = current.selectedRow,
            selectedCol = current.selectedCol
        ) ?: return

        pushUndoSnapshot(current)
        val n = current.mode.gridSize
        val idx = hint.row * n + hint.col
        val updatedCells = current.cells.toMutableList()
        val target = updatedCells[idx]
        updatedCells[idx] = target.copy(value = hint.value, notes = emptySet(), isConflict = false)

        val cleaned = if (preferences.value.autoCleanNotes) {
            cleanPeerNotes(updatedCells, current.mode, hint.row, hint.col, hint.value)
        } else {
            updatedCells
        }
        val checked = SudokuEngine.computeConflicts(cleaned, current.mode)
        val newHintsUsed = current.hintsUsed + 1

        if (SudokuEngine.isBoardSolved(checked)) {
            onPuzzleSolved(current, checked, current.mistakes, newHintsUsed, hint)
        } else {
            val completedUnits = SudokuEngine.completedUnitsAround(checked, current.mode, hint.row, hint.col)
            if (completedUnits.isNotEmpty()) triggerUnitCelebration(completedUnits)

            _gameState.update {
                it.copy(
                    cells = checked,
                    selectedRow = hint.row,
                    selectedCol = hint.col,
                    hintsUsed = newHintsUsed,
                    activeHint = hint,
                    canUndo = undoStack.isNotEmpty(),
                    remainingCounts = computeRemainingCounts(checked, current.mode),
                    hapticTrigger = System.nanoTime()
                )
            }
            persistCurrentGameAsync()
        }
    }

    fun dismissHint() {
        _gameState.update { it.copy(activeHint = null) }
    }

    fun pauseGame() {
        if (_gameState.value.status == GameStatus.PLAYING) {
            stopTimer()
            _gameState.update { it.copy(status = GameStatus.PAUSED) }
            persistCurrentGameAsync()
        }
    }

    fun resumeGameFromPause() {
        if (_gameState.value.status == GameStatus.PAUSED) {
            _gameState.update { it.copy(status = GameStatus.PLAYING) }
            startTimer()
        }
    }

    fun continueInZenMode() {
        val current = _gameState.value
        if (current.status != GameStatus.GAME_OVER) return
        viewModelScope.launch {
            repository.setZenMode(true)
            _gameState.update { it.copy(status = GameStatus.PLAYING) }
            startTimer()
        }
    }

    private fun onPuzzleSolved(
        current: GameUiState,
        solvedCells: List<SudokuCell>,
        finalMistakes: Int,
        finalHints: Int,
        hintResult: HintResult? = null
    ) {
        stopTimer()
        val prevBest = stats.value.difficultyBreakdown
            .firstOrNull { it.difficulty == current.difficulty }?.bestSeconds
        val isNewBest = current.mode == GameMode.CLASSIC_9X9 &&
            (prevBest == null || current.elapsedSeconds < prevBest)

        _gameState.update {
            it.copy(
                cells = solvedCells,
                mistakes = finalMistakes,
                hintsUsed = finalHints,
                status = GameStatus.COMPLETED,
                activeHint = hintResult,
                isNewBestTime = isNewBest,
                remainingCounts = computeRemainingCounts(solvedCells, current.mode),
                celebratingIndices = solvedCells.indices.toSet(),
                hapticTrigger = System.nanoTime()
            )
        }

        viewModelScope.launch {
            repository.recordCompletion(
                gameId = current.gameId,
                mode = current.mode,
                difficulty = current.difficulty,
                elapsedSeconds = current.elapsedSeconds,
                mistakes = finalMistakes,
                hintsUsed = finalHints,
                isDailyChallenge = current.isDailyChallenge,
                dailyDateKey = current.dailyDateKey
            )
        }
    }

    private fun pushUndoSnapshot(state: GameUiState) {
        undoStack.addLast(
            UndoSnapshot(
                values = state.cells.map { it.value },
                notes = state.cells.map { it.notes },
                selectedRow = state.selectedRow,
                selectedCol = state.selectedCol
            )
        )
        if (undoStack.size > 60) undoStack.removeFirst()
    }

    private fun cleanPeerNotes(
        cells: MutableList<SudokuCell>,
        mode: GameMode,
        row: Int,
        col: Int,
        digit: Int
    ): MutableList<SudokuCell> {
        val n = mode.gridSize
        val boxStartR = (row / mode.boxRows) * mode.boxRows
        val boxStartC = (col / mode.boxCols) * mode.boxCols
        for (idx in cells.indices) {
            val cell = cells[idx]
            if (cell.notes.isNotEmpty() && digit in cell.notes) {
                val sameRow = cell.row == row
                val sameCol = cell.col == col
                val sameBox = cell.row in boxStartR until (boxStartR + mode.boxRows) &&
                    cell.col in boxStartC until (boxStartC + mode.boxCols)
                if (sameRow || sameCol || sameBox) {
                    cells[idx] = cell.copy(notes = cell.notes - digit)
                }
            }
        }
        return cells
    }

    private fun computeRemainingCounts(cells: List<SudokuCell>, mode: GameMode): Map<Int, Int> {
        val n = mode.gridSize
        val counts = mutableMapOf<Int, Int>()
        for (d in 1..n) {
            val placedCorrectly = cells.count { it.value == d && !it.isConflict }
            counts[d] = (n - placedCorrectly).coerceAtLeast(0)
        }
        return counts
    }

    private fun triggerUnitCelebration(indices: Set<Int>) {
        celebrationJob?.cancel()
        _gameState.update { it.copy(celebratingIndices = indices) }
        celebrationJob = viewModelScope.launch {
            delay(850L)
            _gameState.update { it.copy(celebratingIndices = emptySet()) }
        }
    }

    private fun startTimer() {
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            while (true) {
                delay(1000L)
                val state = _gameState.value
                if (state.status == GameStatus.PLAYING) {
                    val nextSec = state.elapsedSeconds + 1
                    _gameState.update { it.copy(elapsedSeconds = nextSec) }
                    if (nextSec % 5 == 0) {
                        persistCurrentGame()
                    }
                }
            }
        }
    }

    private fun stopTimer() {
        timerJob?.cancel()
        timerJob = null
    }

    private fun persistCurrentGameAsync() {
        viewModelScope.launch { persistCurrentGame() }
    }

    private suspend fun persistCurrentGame() {
        val state = _gameState.value
        if (state.gameId.isBlank() || state.cells.isEmpty()) return
        if (state.status == GameStatus.COMPLETED) return
        repository.saveProgress(
            gameId = state.gameId,
            mode = state.mode,
            difficulty = state.difficulty,
            cells = state.cells,
            elapsedSeconds = state.elapsedSeconds,
            mistakes = state.mistakes,
            hintsUsed = state.hintsUsed,
            isDailyChallenge = state.isDailyChallenge,
            dailyDateKey = state.dailyDateKey
        )
    }

    override fun onCleared() {
        super.onCleared()
        stopTimer()
    }
}

class SudokuViewModelFactory(
    private val repository: SudokuRepository
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(SudokuViewModel::class.java)) {
            return SudokuViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}

fun formatDuration(totalSeconds: Int): String {
    val mins = totalSeconds / 60
    val secs = totalSeconds % 60
    return "%02d:%02d".format(mins, secs)
}
