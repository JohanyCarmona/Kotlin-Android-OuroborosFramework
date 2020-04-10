package com.example.ouroboros.model.firebase.users

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.ouroboros.model.TableCodes.OuroborosCodes.Companion.OUROBOROS_INIT

@Entity(tableName = "user_table")
class User (
    @PrimaryKey @ColumnInfo(name = "idUser") val idUser : String = "",
    @ColumnInfo(name ="email") val email : String = "",
    @ColumnInfo(name ="username") val username : String = "",
    @ColumnInfo(name ="ouroboros") val ouroboros : Double = OUROBOROS_INIT
)