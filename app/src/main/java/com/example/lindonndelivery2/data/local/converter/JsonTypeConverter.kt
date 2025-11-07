package com.example.lindonndelivery2.data.local.converter

import androidx.room.TypeConverter
import org.json.JSONArray
import org.json.JSONObject

class JsonTypeConverter {
    @TypeConverter
    fun fromJsonString(value: String?): String? {
        return value
    }

    @TypeConverter
    fun toJsonString(value: String?): String? {
        return value
    }
}

