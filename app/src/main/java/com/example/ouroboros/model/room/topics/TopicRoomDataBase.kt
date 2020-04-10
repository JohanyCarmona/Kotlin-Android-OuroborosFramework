package com.example.ouroboros.model.room.topics

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [TopicRoom::class], version = 1)
abstract class TopicRoomDataBase : RoomDatabase() {
    abstract fun TopicRoomDAO(): TopicRoomDAO
}