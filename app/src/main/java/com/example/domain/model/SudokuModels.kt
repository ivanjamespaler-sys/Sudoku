package com.example.domain.model

enum class GameMode(val gridSize: Int, val boxRows: Int, val boxCols: Int, val label: String) {
    CLASSIC_9X9(gridSize = 9, boxRows = 3, boxCols = 3, label = "Classic 9×9"),
    JOINERY_6X6(gridSize = 6, boxRows = 2, boxCols = 3, label = "Joinery 6×6")
}

enum class Difficulty(val displayName: String, val subtitle: String, val clues9x9: Int, val clues6x6: Int) {
    APPRENTICE("Apprentice", "Gentle grain • Ideal warm-up", 42, 18),
    ARTISAN("Artisan", "Balanced joinery • Steady logic", 34, 14),
    MASTER("Master", "Tightly fitted • Deep deduction", 28, 11),
    GRANDMASTER("Grandmaster", "Pure hardwood • Minimal clues", 23, 9);

    fun clueCount(mode: GameMode): Int = when (mode) {
        GameMode.CLASSIC_9X9 -> clues9x9
        GameMode.JOINERY_6X6 -> clues6x6
    }
}

enum class WoodTheme(
    val displayName: String,
    val originNote: String,
    val isDarkSurface: Boolean
) {
    HONEY_OAK("Honey Oak", "Warm golden grain with walnut ink", false),
    SMOKED_WALNUT("Smoked Walnut", "Deep espresso timber with brass inlay", true),
    SAKURA_CHERRY("Sakura Cherry", "Rosy heartwood with mahogany lines", false),
    HINOKI_CYPRESS("Hinoki Cypress", "Pale temple wood with sumi charcoal", false)
}

enum class GameStatus {
    LOADING,
    PLAYING,
    PAUSED,
    COMPLETED,
    GAME_OVER
}

data class SudokuCell(
    val row: Int,
    val col: Int,
    val value: Int = 0,
    val solution: Int = 0,
    val isGiven: Boolean = false,
    val notes: Set<Int> = emptySet(),
    val isConflict: Boolean = false
)

data class UndoSnapshot(
    val values: List<Int>,
    val notes: List<Set<Int>>,
    val selectedRow: Int,
    val selectedCol: Int
)

data class HintResult(
    val row: Int,
    val col: Int,
    val value: Int,
    val technique: String,
    val explanation: String
)

data class CompletedPuzzleSummary(
    val id: Long,
    val mode: GameMode,
    val difficulty: Difficulty,
    val elapsedSeconds: Int,
    val mistakes: Int,
    val hintsUsed: Int,
    val isDailyChallenge: Boolean,
    val completedAtMillis: Long
)

data class DifficultyBestTime(
    val difficulty: Difficulty,
    val bestSeconds: Int?,
    val averageSeconds: Int?,
    val solvedCount: Int
)

data class WorkshopStats(
    val totalSolved: Int = 0,
    val cleanSolves: Int = 0,
    val currentDailyStreak: Int = 0,
    val bestClassicSeconds: Int? = null,
    val difficultyBreakdown: List<DifficultyBestTime> = emptyList(),
    val recentCarvings: List<CompletedPuzzleSummary> = emptyList()
)

data class ResumePreview(
    val gameId: String,
    val mode: GameMode,
    val difficulty: Difficulty,
    val elapsedSeconds: Int,
    val completionPercent: Int,
    val isDailyChallenge: Boolean
)

data class UserPreferences(
    val woodTheme: WoodTheme = WoodTheme.HONEY_OAK,
    val zenMode: Boolean = false,
    val autoCleanNotes: Boolean = true,
    val highlightSameDigits: Boolean = true,
    val hapticsEnabled: Boolean = true
)
