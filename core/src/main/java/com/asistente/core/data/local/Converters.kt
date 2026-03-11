package com.asistente.core.data.local

import androidx.room.TypeConverter
import com.asistente.core.domain.models.RecurrenceType
import com.asistente.core.domain.models.SlotType
import java.util.Date

class Converters {

    // ── Fechas ────────────────────────────────────────────────────────────────
    @TypeConverter
    fun fromTimestamp(value: Long?): Date? {
        return value?.let { Date(it) }
    }

    @TypeConverter
    fun dateToTimestamp(date: Date?): Long? {
        return date?.time
    }

    // ── List<String> (owners, etc.) ───────────────────────────────────────────
    @TypeConverter
    fun fromStringList(value: List<String>?): String {
        return value?.joinToString(",") ?: ""
    }

    @TypeConverter
    fun toStringList(value: String?): List<String> {
        return value?.split(",")?.filter { it.isNotEmpty() } ?: emptyList()
    }

    // ── List<Long> (alerts) ───────────────────────────────────────────────────
    @TypeConverter
    fun fromLongList(value: List<Long>?): String {
        return value?.joinToString(separator = ",") ?: ""
    }

    @TypeConverter
    fun toLongList(value: String?): List<Long> {
        return value?.split(",")?.mapNotNull { it.toLongOrNull() } ?: emptyList()
    }

    // ── List<Int> (daysOfWeek en TimeSlot) ────────────────────────────────────
    @TypeConverter
    fun fromIntList(value: List<Int>?): String {
        return value?.joinToString(",") ?: ""
    }

    @TypeConverter
    fun toIntList(value: String?): List<Int> {
        return value?.split(",")?.mapNotNull { it.trim().toIntOrNull() } ?: emptyList()
    }

    // ── RecurrenceType (TimeSlot) ─────────────────────────────────────────────
    @TypeConverter
    fun fromRecurrenceType(value: RecurrenceType?): String {
        return value?.name ?: RecurrenceType.WEEKLY.name
    }

    @TypeConverter
    fun toRecurrenceType(value: String?): RecurrenceType {
        return try {
            RecurrenceType.valueOf(value ?: RecurrenceType.WEEKLY.name)
        } catch (e: IllegalArgumentException) {
            RecurrenceType.WEEKLY
        }
    }

    // ── SlotType (TimeSlot) ───────────────────────────────────────────────────
    @TypeConverter
    fun fromSlotType(value: SlotType?): String {
        return value?.name ?: SlotType.BLOCKED.name
    }

    @TypeConverter
    fun toSlotType(value: String?): SlotType {
        return try {
            SlotType.valueOf(value ?: SlotType.BLOCKED.name)
        } catch (e: IllegalArgumentException) {
            SlotType.BLOCKED
        }
    }
}