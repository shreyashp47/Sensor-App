package com.example.sensorapp.di

import android.content.Context
import android.hardware.SensorManager
import com.example.sensorapp.data.sensor.SensorDataSource
import com.example.sensorapp.data.repository.SensorRepositoryImpl
import com.example.sensorapp.domain.repository.SensorRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object SensorModule {

    @Provides
    @Singleton
    fun provideSensorManager(@ApplicationContext context: Context): SensorManager {
        return context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    }

    @Provides
    @Singleton
    fun provideSensorRepository(impl: SensorRepositoryImpl): SensorRepository = impl
}
