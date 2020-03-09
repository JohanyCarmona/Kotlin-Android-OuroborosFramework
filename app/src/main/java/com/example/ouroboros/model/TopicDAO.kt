package com.example.ouroboros.model
import androidx.room.*

@Dao
interface TopicDAO {
    @Insert
    fun insertTopic(topic: Topic)

    @Query("SELECT * FROM topic_table WHERE title LIKE :title")
    fun searchTopic(title: String): Topic

    @Update
    fun updateTopic(topic: Topic)

    @Delete
    fun deleteTopic(topic: Topic)

    @Query("SELECT * FROM topic_table")
    fun getTopics() : List<Topic>
}
