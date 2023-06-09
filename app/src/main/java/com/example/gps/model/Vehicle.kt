package com.example.gps.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(
    tableName = "Vehicle",

)
class Vehicle(
    @PrimaryKey(autoGenerate = true)
    val id:Int,
     var type: Int,
    var limitWarning: Int,
    var clockSpeed: Int,
    var isChecked: Boolean
){
    override fun toString(): String {
        return "Vehicle(id=$id,   type=$type, limitWarning=$limitWarning, clockSpeed=$clockSpeed, isChecked=$isChecked)"
    }
}

