package com.asistente.core.data.local

import androidx.room.TypeConverter
import com.asistente.core.domain.models.Categoria
import java.util.Date

class Converters {

//categorias enum en room
    @TypeConverter
    fun fromPriority(categoria: Categoria): String {
        return categoria.name
    }

    @TypeConverter
    fun toPriority(categoria: String): Categoria {
        return Categoria.valueOf(categoria)
    }

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
}