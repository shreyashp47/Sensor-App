package com.example.sensorapp.di

import android.content.Context
import androidx.room.Room
import com.example.sensorapp.data.local.AppDatabase
import com.example.sensorapp.data.local.SessionDao
import com.example.sensorapp.data.local.SensorDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "sensorapp.db"
        ).fallbackToDestructiveMigration().build()
    }

    @Provides
    fun provideSensorDao(database: AppDatabase): SensorDao = database.sensorDao()

    @Provides
    fun provideSessionDao(database: AppDatabase): SessionDao = database.sessionDao()
}
