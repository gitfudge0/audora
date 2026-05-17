package dev.gitfudge.musicworkbench.data.db

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [TrackEntity::class],
    version = 1,
    exportSchema = false,
)
abstract class LibraryDatabase : RoomDatabase() {
    abstract fun trackDao(): TrackDao
}
