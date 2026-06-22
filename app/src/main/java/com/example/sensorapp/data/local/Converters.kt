package com.example.sensorapp.data.local

import androidx.room.TypeConverter

class Converters {

    @TypeConverter
    fun fromFloatList(value: List<Float>): String = value.joinToString(",")

    @TypeConverter
    fun toFloatList(value: String): List<Float> {
        if (value.isEmpty()) return emptyList()
        return value.split(",").map { it.toFloat() }
    }
}
