package com.example.android.politicalpreparedness.database

import androidx.room.TypeConverter
import com.example.android.politicalpreparedness.representative.model.Representative
import com.example.android.politicalpreparedness.representative.model.Representatives
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.util.Date

class Converters {
    @TypeConverter
    fun fromTimestamp(value: Long?): Date? {
        return value?.let { Date(it) }
    }

    @TypeConverter
    fun dateToTimestamp(date: Date?): Long? {
        return date?.time?.toLong()
    }

    @TypeConverter
    fun toRepresentatives(representative: String):List<Representative>?{
        val listType = object : TypeToken<ArrayList<Representative>>(){}.type
        return Gson().fromJson<List<Representative>>(representative,listType)
    }

    @TypeConverter
    fun fromRepresentatives(representatives: List<Representative>):String?{
        return Gson().toJson(representatives)
    }


}