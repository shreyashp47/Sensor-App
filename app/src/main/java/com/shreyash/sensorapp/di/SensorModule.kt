package com.shreyash.sensorapp.di

import android.content.Context
import android.hardware.SensorManager
import com.shreyash.sensorapp.data.sensor.SensorDataSource
import com.shreyash.sensorapp.data.repository.SensorRepositoryImpl
import com.shreyash.sensorapp.domain.repository.SensorRepository
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
