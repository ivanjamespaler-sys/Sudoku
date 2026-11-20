package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface SudokuDao {
    @Query("SELECT * FROM saved_games ORDER BY updatedAtMillis DESC LIMIT 1")
    fun observeLatestSavedGame(): Flow<SavedGameEntity?>

    @Query("SELECT * FROM saved_games WHERE gameId = :gameId LIMIT 1")
    suspend fun getSavedGame(gameId: String): SavedGameEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertSavedGame(game: SavedGameEntity)

    @Query("DELETE FROM saved_games WHERE gameId = :gameId")
    suspend fun deleteSavedGame(gameId: String)

    @Query("SELECT * FROM completed_puzzles ORDER BY completedAtMillis DESC")
    fun observeCompletedPuzzles(): Flow<List<CompletedPuzzleEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCompletedPuzzle(record: CompletedPuzzleEntity)

    @Query("SELECT EXISTS(SELECT 1 FROM completed_puzzles WHERE isDailyChallenge = 1 AND dailyDateKey = :dateKey)")
    fun observeDailySolved(dateKey: String): Flow<Boolean>
}
