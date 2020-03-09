package com.example.ouroboros.model

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [Topic::class], version = 1)
abstract class TopicDataBase : RoomDatabase() {
    abstract fun TopicDAO(): TopicDAO
}