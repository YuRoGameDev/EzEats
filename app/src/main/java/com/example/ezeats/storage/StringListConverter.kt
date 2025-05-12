package com.example.ezeats.storage

import androidx.room.TypeConverter

//This is only for extracting the list of recipes from the Room Data
//Recipes are stored as a string, then converted to a list
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