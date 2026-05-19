package dev.gitfudge.audora.di

import android.content.Context
import androidx.room.Room
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import dev.gitfudge.audora.data.db.LibraryDatabase
import dev.gitfudge.audora.data.db.TrackDao
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): LibraryDatabase =
        Room.databaseBuilder(context, LibraryDatabase::class.java, "library.db")
            .fallbackToDestructiveMigration()
            .build()

    @Provides
    fun provideTrackDao(db: LibraryDatabase): TrackDao = db.trackDao()
}
