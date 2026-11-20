package com.example.data.local

import com.example.domain.engine.SudokuEngine
import com.example.domain.model.CompletedPuzzleSummary
import com.example.domain.model.Difficulty
import com.example.domain.model.DifficultyBestTime
import com.example.domain.model.GameMode
import com.example.domain.model.ResumePreview
import com.example.domain.model.SudokuCell
import com.example.domain.model.UserPreferences
import com.example.domain.model.WoodTheme
import com.example.domain.model.WorkshopStats
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

data class LoadedSavedGame(
    val gameId: String,
    val mode: GameMode,
    val difficulty: Difficulty,
    val cells: List<SudokuCell>,
    val elapsedSeconds: Int,
    val mistakes: Int,
    val hintsUsed: Int,
    val isDailyChallenge: Boolean,
    val dailyDateKey: String?
)

class SudokuRepository(
    private val dao: SudokuDao,
    private val prefsRepo: PreferencesRepository
) {
    val preferences: Flow<UserPreferences> = prefsRepo.preferences

    fun todayDateKey(): String {
        return SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())
    }

    fun todaySeed(): Long {
        val key = SimpleDateFormat("yyyyMMdd", Locale.US).format(Date())
        return key.toLongOrNull() ?: 20261120L
    }

    fun observeDailySolved(): Flow<Boolean> = dao.observeDailySolved(todayDateKey())

    fun observeLatestResumePreview(): Flow<ResumePreview?> = dao.observeLatestSavedGame().map { entity ->
        entity?.let {
            val mode = GameMode.entries.firstOrNull { m -> m.name == it.modeName } ?: GameMode.CLASSIC_9X9
            val diff = Difficulty.entries.firstOrNull { d -> d.name == it.difficultyName } ?: Difficulty.ARTISAN
            val values = it.valuesCsv.split(",").mapNotNull { s -> s.toIntOrNull() }
            val filledCount = values.count { v -> v != 0 }
            val total = mode.gridSize * mode.gridSize
            val pct = if (total > 0) ((filledCount * 100f) / total).toInt().coerceIn(0, 100) else 0
            ResumePreview(
                gameId = it.gameId,
                mode = mode,
                difficulty = diff,
                elapsedSeconds = it.elapsedSeconds,
                completionPercent = pct,
                isDailyChallenge = it.isDailyChallenge
            )
        }
    }

    fun observeStats(): Flow<WorkshopStats> = dao.observeCompletedPuzzles().map { records ->
        val summaries = records.mapNotNull { rec ->
            val mode = GameMode.entries.firstOrNull { it.name == rec.modeName } ?: return@mapNotNull null
            val diff = Difficulty.entries.firstOrNull { it.name == rec.difficultyName } ?: return@mapNotNull null
            CompletedPuzzleSummary(
                id = rec.id,
                mode = mode,
                difficulty = diff,
                elapsedSeconds = rec.elapsedSeconds,
                mistakes = rec.mistakes,
                hintsUsed = rec.hintsUsed,
                isDailyChallenge = rec.isDailyChallenge,
                completedAtMillis = rec.completedAtMillis
            )
        }

        val totalSolved = summaries.size
        val cleanSolves = summaries.count { it.mistakes == 0 && it.hintsUsed == 0 }
        val bestClassic = summaries
            .filter { it.mode == GameMode.CLASSIC_9X9 }
            .minOfOrNull { it.elapsedSeconds }

        val breakdown = Difficulty.entries.map { diff ->
            val matching = summaries.filter { it.difficulty == diff && it.mode == GameMode.CLASSIC_9X9 }
            DifficultyBestTime(
                difficulty = diff,
                bestSeconds = matching.minOfOrNull { it.elapsedSeconds },
                averageSeconds = if (matching.isEmpty()) null else matching.sumOf { it.elapsedSeconds } / matching.size,
                solvedCount = matching.size
            )
        }

        val dailyDates = records
            .filter { it.isDailyChallenge && !it.dailyDateKey.isNullOrBlank() }
            .mapNotNull { it.dailyDateKey }
            .toSet()

        val streak = calculateDailyStreak(dailyDates)

        WorkshopStats(
            totalSolved = totalSolved,
            cleanSolves = cleanSolves,
            currentDailyStreak = streak,
            bestClassicSeconds = bestClassic,
            difficultyBreakdown = breakdown,
            recentCarvings = summaries.take(15)
        )
    }

    private fun calculateDailyStreak(solvedDateKeys: Set<String>): Int {
        if (solvedDateKeys.isEmpty()) return 0
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        val cal = Calendar.getInstance()
        val todayKey = sdf.format(cal.time)
        var streak = 0

        if (!solvedDateKeys.contains(todayKey)) {
            cal.add(Calendar.DAY_OF_YEAR, -1)
        }

        while (true) {
            val key = sdf.format(cal.time)
            if (solvedDateKeys.contains(key)) {
                streak++
                cal.add(Calendar.DAY_OF_YEAR, -1)
            } else {
                break
            }
        }
        return streak
    }

    suspend fun loadSavedGame(gameId: String): LoadedSavedGame? {
        val entity = dao.getSavedGame(gameId) ?: return null
        val mode = GameMode.entries.firstOrNull { it.name == entity.modeName } ?: GameMode.CLASSIC_9X9
        val diff = Difficulty.entries.firstOrNull { it.name == entity.difficultyName } ?: Difficulty.ARTISAN
        val n = mode.gridSize
        val total = n * n
        val values = entity.valuesCsv.split(",").mapNotNull { it.toIntOrNull() }
        val solutions = entity.solutionCsv.split(",").mapNotNull { it.toIntOrNull() }
        val givens = entity.givenMaskCsv.split(",").map { it == "1" }
        val notesGroups = entity.notesEncoded.split("|")

        if (values.size != total || solutions.size != total || givens.size != total) return null

        val rawCells = List(total) { idx ->
            val noteSet = notesGroups.getOrNull(idx)
                ?.split(",")
                ?.mapNotNull { it.toIntOrNull() }
                ?.toSet()
                ?: emptySet()
            SudokuCell(
                row = idx / n,
                col = idx % n,
                value = values[idx],
                solution = solutions[idx],
                isGiven = givens[idx],
                notes = noteSet
            )
        }
        val cells = SudokuEngine.computeConflicts(rawCells, mode)
        return LoadedSavedGame(
            gameId = entity.gameId,
            mode = mode,
            difficulty = diff,
            cells = cells,
            elapsedSeconds = entity.elapsedSeconds,
            mistakes = entity.mistakes,
            hintsUsed = entity.hintsUsed,
            isDailyChallenge = entity.isDailyChallenge,
            dailyDateKey = entity.dailyDateKey
        )
    }

    suspend fun saveProgress(
        gameId: String,
        mode: GameMode,
        difficulty: Difficulty,
        cells: List<SudokuCell>,
        elapsedSeconds: Int,
        mistakes: Int,
        hintsUsed: Int,
        isDailyChallenge: Boolean,
        dailyDateKey: String?
    ) {
        if (cells.isEmpty()) return
        val valuesCsv = cells.joinToString(",") { it.value.toString() }
        val solutionCsv = cells.joinToString(",") { it.solution.toString() }
        val givenMaskCsv = cells.joinToString(",") { if (it.isGiven) "1" else "0" }
        val notesEncoded = cells.joinToString("|") { cell ->
            cell.notes.sorted().joinToString(",")
        }
        dao.upsertSavedGame(
            SavedGameEntity(
                gameId = gameId,
                modeName = mode.name,
                difficultyName = difficulty.name,
                valuesCsv = valuesCsv,
                solutionCsv = solutionCsv,
                givenMaskCsv = givenMaskCsv,
                notesEncoded = notesEncoded,
                elapsedSeconds = elapsedSeconds,
                mistakes = mistakes,
                hintsUsed = hintsUsed,
                isDailyChallenge = isDailyChallenge,
                dailyDateKey = dailyDateKey,
                updatedAtMillis = System.currentTimeMillis()
            )
        )
    }

    suspend fun recordCompletion(
        gameId: String,
        mode: GameMode,
        difficulty: Difficulty,
        elapsedSeconds: Int,
        mistakes: Int,
        hintsUsed: Int,
        isDailyChallenge: Boolean,
        dailyDateKey: String?
    ) {
        dao.deleteSavedGame(gameId)
        dao.insertCompletedPuzzle(
            CompletedPuzzleEntity(
                modeName = mode.name,
                difficultyName = difficulty.name,
                elapsedSeconds = elapsedSeconds,
                mistakes = mistakes,
                hintsUsed = hintsUsed,
                isDailyChallenge = isDailyChallenge,
                dailyDateKey = dailyDateKey,
                completedAtMillis = System.currentTimeMillis()
            )
        )
    }

    suspend fun deleteSavedGame(gameId: String) = dao.deleteSavedGame(gameId)

    suspend fun setWoodTheme(theme: WoodTheme) = prefsRepo.setWoodTheme(theme)
    suspend fun setZenMode(enabled: Boolean) = prefsRepo.setZenMode(enabled)
    suspend fun setAutoCleanNotes(enabled: Boolean) = prefsRepo.setAutoCleanNotes(enabled)
    suspend fun setHighlightSameDigits(enabled: Boolean) = prefsRepo.setHighlightSameDigits(enabled)
    suspend fun setHapticsEnabled(enabled: Boolean) = prefsRepo.setHapticsEnabled(enabled)
}
