package com.example

import com.example.domain.engine.SudokuEngine
import com.example.domain.model.Difficulty
import com.example.domain.model.GameMode
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class SudokuEngineTest {

    @Test
    fun generateClassic9x9HasValidSolutionAndClues() {
        val board = SudokuEngine.generatePuzzle(
            mode = GameMode.CLASSIC_9X9,
            difficulty = Difficulty.ARTISAN,
            seed = 123456L
        )
        assertEquals(81, board.cells.size)
        val givenCount = board.cells.count { it.isGiven }
        assertTrue("Expected at least 30 clues, got $givenCount", givenCount >= 30)

        // Check every row, col, and 3x3 box of solution contains digits 1..9
        for (r in 0 until 9) {
            val rowVals = (0 until 9).map { c -> board.cells[r * 9 + c].solution }.toSet()
            assertEquals((1..9).toSet(), rowVals)
        }
        for (c in 0 until 9) {
            val colVals = (0 until 9).map { r -> board.cells[r * 9 + c].solution }.toSet()
            assertEquals((1..9).toSet(), colVals)
        }
        for (br in 0 until 3) {
            for (bc in 0 until 3) {
                val boxVals = mutableSetOf<Int>()
                for (r in br * 3 until br * 3 + 3) {
                    for (c in bc * 3 until bc * 3 + 3) {
                        boxVals.add(board.cells[r * 9 + c].solution)
                    }
                }
                assertEquals((1..9).toSet(), boxVals)
            }
        }
    }

    @Test
    fun generateJoinery6x6HasValidSolutionAndBlocks() {
        val board = SudokuEngine.generatePuzzle(
            mode = GameMode.JOINERY_6X6,
            difficulty = Difficulty.APPRENTICE,
            seed = 98765L
        )
        assertEquals(36, board.cells.size)
        for (r in 0 until 6) {
            val rowVals = (0 until 6).map { c -> board.cells[r * 6 + c].solution }.toSet()
            assertEquals((1..6).toSet(), rowVals)
        }
        for (br in 0 until 3) {
            for (bc in 0 until 2) {
                val boxVals = mutableSetOf<Int>()
                for (r in br * 2 until br * 2 + 2) {
                    for (c in bc * 3 until bc * 3 + 3) {
                        boxVals.add(board.cells[r * 6 + c].solution)
                    }
                }
                assertEquals((1..6).toSet(), boxVals)
            }
        }
    }

    @Test
    fun smartHintFindsValidMove() {
        val board = SudokuEngine.generatePuzzle(
            mode = GameMode.CLASSIC_9X9,
            difficulty = Difficulty.APPRENTICE,
            seed = 42L
        )
        assertFalse(SudokuEngine.isBoardSolved(board.cells))
        val hint = SudokuEngine.findSmartHint(board.cells, GameMode.CLASSIC_9X9, -1, -1)
        assertNotNull(hint)
        val target = board.cells[hint!!.row * 9 + hint.col]
        assertEquals(target.solution, hint.value)
    }
}
