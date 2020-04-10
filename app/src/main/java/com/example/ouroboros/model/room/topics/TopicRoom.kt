package com.example.ouroboros.model.room.topics

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "topicRoom_table")
class TopicRoom (
    @PrimaryKey(autoGenerate = true) @ColumnInfo(name = "idTopic") val idTopic: Int,
    @ColumnInfo(name ="idUser") val idUser : String = "",
    @ColumnInfo(name ="role_type") val role_type: Int = 0,
    @ColumnInfo(name ="publication_type") val publication_type: Int = 0,
    @ColumnInfo(name ="title") val title: String = "",
    @ColumnInfo(name ="resource_category") val resource_category: Int = 0,
    @ColumnInfo(name ="image") val image: String = "",
    @ColumnInfo(name ="description") val description: String = "",
    @ColumnInfo(name ="publication_date") val publication_date: Long = 0,
    @ColumnInfo(name ="latitude") val latitude: Double = 0.0,
    @ColumnInfo(name ="longitude") val longitude: Double = 0.0,
    @ColumnInfo(name ="enable") val enable: Boolean = true
)