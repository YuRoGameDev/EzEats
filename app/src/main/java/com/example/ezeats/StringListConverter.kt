package com.example.ezeats

import androidx.room.TypeConverter

class StringListConverter {

    @TypeConverter
    fun fromList(list: List<String>): String {
        return list.filter { it.isNotBlank() }.joinToString("||")
    }

    @TypeConverter
    fun toList(data: String): List<String> {
        return data.split("||").filter { it.isNotBlank() }
    }
}