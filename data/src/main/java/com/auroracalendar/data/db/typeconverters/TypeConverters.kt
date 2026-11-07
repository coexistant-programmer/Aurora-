package com.auroracalendar.data.db.typeconverters

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.lang.reflect.Type
import java.time.Instant
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter

/**
 * TypeConverters map Kotlin types to SQLite-supported columns.
 *
 * Decision: store all instants as epoch milliseconds (UTC) and convert to local
 * time only at the UI/domain layer. This avoids timezone-storage bugs and makes
 * timer math (elapsed = now - start) trivial and correct across DST/zone changes.
 */
class TypeConverters {

    private val gson = Gson()

    // ---------- Instant <-> Long (epoch ms) ----------

    @TypeConverter
    fun instantToMillis(instant: Instant?): Long? = instant?.toEpochMilli()

    @TypeConverter
    fun millisToInstant(millis: Long?): Instant? = millis?.let { Instant.ofEpochMilli(it) }

    // ---------- List<Int> <-> JSON (reminder minutes) ----------

    @TypeConverter
    fun intListToJson(list: List<Int>?): String? =
        list?.let { gson.toJson(it) }

    @TypeConverter
    fun jsonToIntList(json: String?): List<Int>? {
        if (json.isNullOrBlank()) return emptyList()
        val type: Type = object : TypeToken<List<Int>>() {}.type
        return gson.fromJson(json, type) ?: emptyList()
    }

    // ---------- List<String> <-> JSON (subtasks, exception dates) ----------

    @TypeConverter
    fun stringListToJson(list: List<String>?): String? =
        list?.let { gson.toJson(it) }

    @TypeConverter
    fun jsonToStringList(json: String?): List<String>? {
        if (json.isNullOrBlank()) return emptyList()
        val type: Type = object : TypeToken<List<String>>() {}.type
        return gson.fromJson(json, type) ?: emptyList()
    }

    // ---------- Zone offset (for display) ----------

    @TypeConverter
    fun zoneOffsetToString(offset: ZoneOffset?): String? = offset?.id

    @TypeConverter
    fun stringToZoneOffset(id: String?): ZoneOffset? = id?.let { ZoneOffset.of(id) }
}
