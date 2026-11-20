package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [SavedGameEntity::class, CompletedPuzzleEntity::class],
    version = 1,
    exportSchema = false
)
abstract class SudokuDatabase : RoomDatabase() {
    abstract fun sudokuDao(): SudokuDao

    companion object {
        @Volatile
        private var INSTANCE: SudokuDatabase? = null

        fun getInstance(context: Context): SudokuDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    SudokuDatabase::class.java,
                    "timber_sudoku.db"
                ).build().also { INSTANCE = it }
            }
        }
    }
}
