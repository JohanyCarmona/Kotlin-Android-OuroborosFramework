package com.example.ouroboros.model.room.topics
import androidx.room.*

@Dao
interface TopicRoomDAO {
    @Insert
    fun insertTopic(topicRoom: TopicRoom)

    @Query("SELECT * FROM topicRoom_table WHERE title LIKE :title")
    fun searchTopic(title: String): TopicRoom

    @Query("SELECT * FROM topicRoom_table WHERE idTopic LIKE :idTopic")
    fun searchIdTopic(idTopic : Int): TopicRoom

    @Update
    fun updateTopic(topicRoom: TopicRoom)

    @Delete
    fun deleteTopic(topicRoom: TopicRoom)

    @Query("SELECT * FROM topicRoom_table")
    fun getTopics() : List<TopicRoom>
}
