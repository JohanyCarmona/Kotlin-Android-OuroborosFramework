package com.example.ouroboros.model.room
import android.app.Application
import androidx.room.Room
import com.example.ouroboros.model.room.topics.TopicRoomDataBase

class SesionRoom : Application() {

    companion object {
        lateinit var database: TopicRoomDataBase
    }

    override fun onCreate() {
        super.onCreate()

        database = Room.databaseBuilder(
                this,
                TopicRoomDataBase::class.java,
                "topicRoom_DB"
            )
            .allowMainThreadQueries()
            .build()
    }
}