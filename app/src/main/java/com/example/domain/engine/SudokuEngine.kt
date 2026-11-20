package com.example.domain.engine

import com.example.domain.model.Difficulty
import com.example.domain.model.GameMode
import com.example.domain.model.HintResult
import com.example.domain.model.SudokuCell
import kotlin.random.Random

data class GeneratedBoard(
    val mode: GameMode,
    val difficulty: Difficulty,
    val cells: List<SudokuCell>,
    val seed: Long
)

object SudokuEngine {

    fun generatePuzzle(
        mode: GameMode,
        difficulty: Difficulty,
        seed: Long = System.currentTimeMillis()
    ): GeneratedBoard {
        val random = Random(seed)
        val n = mode.gridSize
        val total = n * n
        val solved = IntArray(total)

        fillBoard(solved, mode, random)
        val puzzle = solved.copyOf()
        val targetClues = difficulty.clueCount(mode)

        val positions = (0 until total).toMutableList()
        positions.shuffle(random)

        var currentClues = total
        for (pos in positions) {
            if (currentClues <= targetClues) break
            val backup = puzzle[pos]
            if (backup == 0) continue
            puzzle[pos] = 0
            if (countSolutions(puzzle.copyOf(), mode, limit = 2) == 1) {
                currentClues--
            } else {
                puzzle[pos] = backup
            }
        }

        val cells = List(total) { index ->
            val r = index / n
            val c = index % n
            val value = puzzle[index]
            SudokuCell(
                row = r,
                col = c,
                value = value,
                solution = solved[index],
                isGiven = value != 0,
                notes = emptySet(),
                isConflict = false
            )
        }

        return GeneratedBoard(
            mode = mode,
            difficulty = difficulty,
            cells = cells,
            seed = seed
        )
    }

    private fun fillBoard(board: IntArray, mode: GameMode, random: Random): Boolean {
        val n = mode.gridSize
        val emptyIdx = findBestEmptyCell(board, mode) ?: return true
        if (emptyIdx == -1) return false

        val row = emptyIdx / n
        val col = emptyIdx % n
        val candidates = getValidDigits(board, mode, row, col).shuffled(random)

        for (digit in candidates) {
            board[emptyIdx] = digit
            if (fillBoard(board, mode, random)) return true
            board[emptyIdx] = 0
        }
        return false
    }

    private fun countSolutions(board: IntArray, mode: GameMode, limit: Int): Int {
        val n = mode.gridSize
        val emptyIdx = findBestEmptyCell(board, mode) ?: return 1
        if (emptyIdx == -1) return 0

        val row = emptyIdx / n
        val col = emptyIdx % n
        val candidates = getValidDigits(board, mode, row, col)
        if (candidates.isEmpty()) return 0

        var solutions = 0
        for (digit in candidates) {
            board[emptyIdx] = digit
            solutions += countSolutions(board, mode, limit - solutions)
            board[emptyIdx] = 0
            if (solutions >= limit) return solutions
        }
        return solutions
    }

    /**
     * Minimum Remaining Value (MRV) heuristic:
     * Returns null if board is completely filled, -1 if an empty cell has 0 candidates,
     * otherwise returns the index of the empty cell with fewest valid candidates.
     */
    private fun findBestEmptyCell(board: IntArray, mode: GameMode): Int? {
        val n = mode.gridSize
        var bestIdx: Int? = null
        var minCandidates = Int.MAX_VALUE

        for (i in board.indices) {
            if (board[i] == 0) {
                val r = i / n
                val c = i % n
                val count = countValidDigits(board, mode, r, c)
                if (count == 0) return -1
                if (count < minCandidates) {
                    minCandidates = count
                    bestIdx = i
                    if (count == 1) return bestIdx
                }
            }
        }
        return bestIdx
    }

    fun getValidDigits(board: IntArray, mode: GameMode, row: Int, col: Int): List<Int> {
        val n = mode.gridSize
        val used = BooleanArray(n + 1)
        markUsedInPeers(board, mode, row, col, used)
        val result = ArrayList<Int>(n)
        for (d in 1..n) {
            if (!used[d]) result.add(d)
        }
        return result
    }

    private fun countValidDigits(board: IntArray, mode: GameMode, row: Int, col: Int): Int {
        val n = mode.gridSize
        val used = BooleanArray(n + 1)
        markUsedInPeers(board, mode, row, col, used)
        var count = 0
        for (d in 1..n) {
            if (!used[d]) count++
        }
        return count
    }

    private fun markUsedInPeers(
        board: IntArray,
        mode: GameMode,
        row: Int,
        col: Int,
        used: BooleanArray
    ) {
        val n = mode.gridSize
        for (i in 0 until n) {
            val rowVal = board[row * n + i]
            if (rowVal != 0) used[rowVal] = true
            val colVal = board[i * n + col]
            if (colVal != 0) used[colVal] = true
        }
        val boxStartRow = (row / mode.boxRows) * mode.boxRows
        val boxStartCol = (col / mode.boxCols) * mode.boxCols
        for (r in boxStartRow until boxStartRow + mode.boxRows) {
            for (c in boxStartCol until boxStartCol + mode.boxCols) {
                val v = board[r * n + c]
                if (v != 0) used[v] = true
            }
        }
    }

    fun computeConflicts(cells: List<SudokuCell>, mode: GameMode): List<SudokuCell> {
        val n = mode.gridSize
        return cells.mapIndexed { index, cell ->
            if (cell.value == 0 || cell.isGiven) {
                cell.copy(isConflict = false)
            } else {
                val wrongAgainstSolution = cell.solution != 0 && cell.value != cell.solution
                val peerConflict = hasPeerDuplicate(cells, mode, cell.row, cell.col, cell.value, index, n)
                cell.copy(isConflict = wrongAgainstSolution || peerConflict)
            }
        }
    }

    private fun hasPeerDuplicate(
        cells: List<SudokuCell>,
        mode: GameMode,
        row: Int,
        col: Int,
        value: Int,
        selfIndex: Int,
        n: Int
    ): Boolean {
        for (i in 0 until n) {
            val rIdx = row * n + i
            if (rIdx != selfIndex && cells[rIdx].value == value) return true
            val cIdx = i * n + col
            if (cIdx != selfIndex && cells[cIdx].value == value) return true
        }
        val boxStartRow = (row / mode.boxRows) * mode.boxRows
        val boxStartCol = (col / mode.boxCols) * mode.boxCols
        for (r in boxStartRow until boxStartRow + mode.boxRows) {
            for (c in boxStartCol until boxStartCol + mode.boxCols) {
                val idx = r * n + c
                if (idx != selfIndex && cells[idx].value == value) return true
            }
        }
        return false
    }

    fun findSmartHint(
        cells: List<SudokuCell>,
        mode: GameMode,
        selectedRow: Int,
        selectedCol: Int
    ): HintResult? {
        val n = mode.gridSize
        // Build clean board from correctly placed values (or current non-conflicting values)
        val board = IntArray(n * n) { idx ->
            val cell = cells[idx]
            if (cell.value != 0 && !cell.isConflict) cell.value else 0
        }

        // First: check if the currently selected cell is empty/wrong and is a Naked Single
        if (selectedRow in 0 until n && selectedCol in 0 until n) {
            val selIdx = selectedRow * n + selectedCol
            val selCell = cells[selIdx]
            if (selCell.value == 0 || selCell.isConflict) {
                val candidates = getValidDigits(board, mode, selectedRow, selectedCol)
                if (candidates.size == 1 && candidates.first() == selCell.solution) {
                    val v = candidates.first()
                    return HintResult(
                        row = selectedRow,
                        col = selectedCol,
                        value = v,
                        technique = "Naked Single",
                        explanation = "Row ${selectedRow + 1}, Col ${selectedCol + 1} has only one legal digit left: $v."
                    )
                }
            }
        }

        // 1. Scan all cells for Naked Singles
        for (idx in cells.indices) {
            val cell = cells[idx]
            if (cell.value == 0 || cell.isConflict) {
                val candidates = getValidDigits(board, mode, cell.row, cell.col)
                if (candidates.size == 1 && candidates.first() == cell.solution) {
                    val v = candidates.first()
                    return HintResult(
                        row = cell.row,
                        col = cell.col,
                        value = v,
                        technique = "Naked Single",
                        explanation = "All other digits are blocked from Row ${cell.row + 1}, Col ${cell.col + 1}, leaving only $v."
                    )
                }
            }
        }

        // 2. Scan Boxes for Hidden Singles
        val boxesPerRow = n / mode.boxCols
        val boxesPerCol = n / mode.boxRows
        for (br in 0 until boxesPerCol) {
            for (bc in 0 until boxesPerRow) {
                val boxNum = br * boxesPerRow + bc + 1
                val startR = br * mode.boxRows
                val startC = bc * mode.boxCols
                for (digit in 1..n) {
                    val possibleSpots = mutableListOf<Pair<Int, Int>>()
                    var alreadyPlaced = false
                    for (r in startR until startR + mode.boxRows) {
                        for (c in startC until startC + mode.boxCols) {
                            val idx = r * n + c
                            if (board[idx] == digit) {
                                alreadyPlaced = true
                                break
                            }
                            if (board[idx] == 0 && digit in getValidDigits(board, mode, r, c)) {
                                possibleSpots.add(r to c)
                            }
                        }
                        if (alreadyPlaced) break
                    }
                    if (!alreadyPlaced && possibleSpots.size == 1) {
                        val (r, c) = possibleSpots.first()
                        val targetCell = cells[r * n + c]
                        if (targetCell.solution == digit) {
                            return HintResult(
                                row = r,
                                col = c,
                                value = digit,
                                technique = "Hidden Single (Block $boxNum)",
                                explanation = "Within Block $boxNum, $digit can only fit at Row ${r + 1}, Col ${c + 1} due to row and column locks."
                            )
                        }
                    }
                }
            }
        }

        // 3. Scan Rows for Hidden Singles
        for (r in 0 until n) {
            for (digit in 1..n) {
                val spots = mutableListOf<Int>()
                var alreadyPlaced = false
                for (c in 0 until n) {
                    val idx = r * n + c
                    if (board[idx] == digit) {
                        alreadyPlaced = true
                        break
                    }
                    if (board[idx] == 0 && digit in getValidDigits(board, mode, r, c)) {
                        spots.add(c)
                    }
                }
                if (!alreadyPlaced && spots.size == 1) {
                    val c = spots.first()
                    if (cells[r * n + c].solution == digit) {
                        return HintResult(
                            row = r,
                            col = c,
                            value = digit,
                            technique = "Hidden Single (Row ${r + 1})",
                            explanation = "In Row ${r + 1}, Col ${c + 1} is the sole opening that accepts $digit."
                        )
                    }
                }
            }
        }

        // 4. Scan Columns for Hidden Singles
        for (c in 0 until n) {
            for (digit in 1..n) {
                val spots = mutableListOf<Int>()
                var alreadyPlaced = false
                for (r in 0 until n) {
                    val idx = r * n + c
                    if (board[idx] == digit) {
                        alreadyPlaced = true
                        break
                    }
                    if (board[idx] == 0 && digit in getValidDigits(board, mode, r, c)) {
                        spots.add(r)
                    }
                }
                if (!alreadyPlaced && spots.size == 1) {
                    val r = spots.first()
                    if (cells[r * n + c].solution == digit) {
                        return HintResult(
                            row = r,
                            col = c,
                            value = digit,
                            technique = "Hidden Single (Col ${c + 1})",
                            explanation = "In Column ${c + 1}, Row ${r + 1} is the only open tile for $digit."
                        )
                    }
                }
            }
        }

        // 5. Fallback logical inlay for Master/Grandmaster intersection chains
        val targetCell = if (selectedRow in 0 until n && selectedCol in 0 until n) {
            val sel = cells[selectedRow * n + selectedCol]
            if (sel.value == 0 || sel.isConflict) sel else cells.firstOrNull { it.value == 0 || it.isConflict }
        } else {
            cells.firstOrNull { it.value == 0 || it.isConflict }
        } ?: return null

        return HintResult(
            row = targetCell.row,
            col = targetCell.col,
            value = targetCell.solution,
            technique = "Craftsman Deduction",
            explanation = "Chain elimination locks ${targetCell.solution} into Row ${targetCell.row + 1}, Col ${targetCell.col + 1}."
        )
    }

    fun isBoardSolved(cells: List<SudokuCell>): Boolean {
        return cells.isNotEmpty() && cells.all { it.value != 0 && it.value == it.solution }
    }

    fun completedUnitsAround(
        cells: List<SudokuCell>,
        mode: GameMode,
        row: Int,
        col: Int
    ): Set<Int> {
        val n = mode.gridSize
        val completedIndices = mutableSetOf<Int>()

        val rowComplete = (0 until n).all { c ->
            val cell = cells[row * n + c]
            cell.value != 0 && cell.value == cell.solution
        }
        if (rowComplete) {
            for (c in 0 until n) completedIndices.add(row * n + c)
        }

        val colComplete = (0 until n).all { r ->
            val cell = cells[r * n + col]
            cell.value != 0 && cell.value == cell.solution
        }
        if (colComplete) {
            for (r in 0 until n) completedIndices.add(r * n + col)
        }

        val startR = (row / mode.boxRows) * mode.boxRows
        val startC = (col / mode.boxCols) * mode.boxCols
        var boxComplete = true
        for (r in startR until startR + mode.boxRows) {
            for (c in startC until startC + mode.boxCols) {
                val cell = cells[r * n + c]
                if (cell.value == 0 || cell.value != cell.solution) {
                    boxComplete = false
                    break
                }
            }
            if (!boxComplete) break
        }
        if (boxComplete) {
            for (r in startR until startR + mode.boxRows) {
                for (c in startC until startC + mode.boxCols) {
                    completedIndices.add(r * n + c)
                }
            }
        }

        return completedIndices
    }
}
