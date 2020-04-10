package com.example.ouroboros.model.firebase.interactions

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "interaction_table")
class Interaction (
    @PrimaryKey @ColumnInfo(name = "idInteraction") val idInteraction: String = "",
    @ColumnInfo(name ="idCoupling") val idCoupling : String = "",
    @ColumnInfo(name ="startDate") val startDate: Long = 0,
    @ColumnInfo(name ="startLatitudeDispatcher") val startLatitudeDispatcher: Double = 0.0,
    @ColumnInfo(name ="startLongitudeDispatcher") val startLongitudeDispatcher: Double = 0.0,
    @ColumnInfo(name ="LatitudeDispatcher") val LatitudeDispatcher: Double = 0.0,
    @ColumnInfo(name ="LongitudeDispatcher") val LongitudeDispatcher: Double = 0.0,
    @ColumnInfo(name ="endDate") val endDate: Long = 0,
    @ColumnInfo(name ="interactionState") val interactionState: Int = 0
)
