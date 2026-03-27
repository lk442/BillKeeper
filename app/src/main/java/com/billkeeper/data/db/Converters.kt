package com.billkeeper.data.db

import androidx.room.TypeConverter

class Converters {

    @TypeConverter
    fun fromStringList(value: List<String>): String {
        return org.json.JSONArray(value).toString()
    }

    @TypeConverter
    fun toStringList(value: String): List<String> {
        if (value.isBlank()) return emptyList()
        val array = org.json.JSONArray(value)
        return (0 until array.length()).map { array.getString(it) }
    }
}
