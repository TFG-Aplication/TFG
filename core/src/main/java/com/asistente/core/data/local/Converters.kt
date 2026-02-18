package com.asistente.core.data.local

import androidx.room.TypeConverter
import java.util.Date

class Converters {

//fechas en room
    @TypeConverter
    fun fromTimestamp(value: Long?): Date? {
        return value?.let { Date(it) }
    }

    @TypeConverter
    fun dateToTimestamp(date: Date?): Long? {
        return date?.time
    }

//list en room (usuarios)
    @TypeConverter
    fun fromStringList(value: List<String>?): String {
        return value?.joinToString(",") ?: ""
    }

    @TypeConverter
    fun toStringList(value: String?): List<String> {
        return value?.split(",")?.filter { it.isNotEmpty() } ?: emptyList()
    }



//list en room (alert)
@TypeConverter
fun fromLongList(value: List<Long>?): String {
    return value?.joinToString(separator = ",")?: ""
}

@TypeConverter
fun toLongList(value: String?): List<Long> {
    return value?.split(",")?.mapNotNull { it.toLongOrNull() } ?: emptyList() // ⬅️ Corregido
}
}