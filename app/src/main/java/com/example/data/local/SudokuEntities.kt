package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "saved_games")
data class SavedGameEntity(
    @PrimaryKey val gameId: String,
    val modeName: String,
    val difficultyName: String,
    val valuesCsv: String,
    val solutionCsv: String,
    val givenMaskCsv: String,
    val notesEncoded: String,
    val elapsedSeconds: Int,
    val mistakes: Int,
    val hintsUsed: Int,
    val isDailyChallenge: Boolean,
    val dailyDateKey: String?,
    val updatedAtMillis: Long
)

@Entity(tableName = "completed_puzzles")
data class CompletedPuzzleEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val modeName: String,
    val difficultyName: String,
    val elapsedSeconds: Int,
    val mistakes: Int,
    val hintsUsed: Int,
    val isDailyChallenge: Boolean,
    val dailyDateKey: String?,
    val completedAtMillis: Long
)
