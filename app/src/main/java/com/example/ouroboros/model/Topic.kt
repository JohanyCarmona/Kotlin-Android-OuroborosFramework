package com.example.ouroboros.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "topic_table")
class Topic (
    @PrimaryKey @ColumnInfo(name = "idTopic") val idTopic: String = "",
    @ColumnInfo(name ="idUser") val idUser : String = "",
    @ColumnInfo(name ="role_type") val role_type: Int = 0,
    @ColumnInfo(name ="publication_type") val publication_type: Int = 0,
    @ColumnInfo(name ="title") val title: String = "",
    @ColumnInfo(name ="resource_category") val resource_category: Int = 0,
    @ColumnInfo(name ="image") val image: String = "",
    @ColumnInfo(name ="description") val description: String = "",
    @ColumnInfo(name ="publication_date") val publication_date: String = "",
    @ColumnInfo(name ="latitude") val latitude: Double = 0.0,
    @ColumnInfo(name ="longitude") val longitude: Double = 0.0,
    @ColumnInfo(name ="enable") val enable: Boolean = true
    )
