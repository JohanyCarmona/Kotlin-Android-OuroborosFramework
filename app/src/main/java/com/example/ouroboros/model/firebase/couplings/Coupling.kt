package com.example.ouroboros.model.firebase.couplings

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "coupling_table")
class Coupling (
    @PrimaryKey @ColumnInfo(name = "idCoupling") val idCoupling: String = "",
    @ColumnInfo(name ="idHelperTopic") val idHelperTopic : String = "",
    @ColumnInfo(name ="idApplicantTopic") val idApplicantTopic: String = "",
    @ColumnInfo(name ="roleDispatcher") val roleDispatcher: Int = 0,
    @ColumnInfo(name ="ouroboros") val ouroboros: Double = 0.0,
    @ColumnInfo(name ="coupledDate") val coupledDate: Long = 0,
    @ColumnInfo(name ="coupledState") val coupledState: Int = 0
)
