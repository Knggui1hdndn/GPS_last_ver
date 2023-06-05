package com.example.gps.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.io.Serializable

@Entity(tableName = "MovementData")
class MovementData(
    @PrimaryKey(autoGenerate = true)
    val id: Int,
    val date: Long,
    var startLatitude: Double,
    var startLongitude: Double,
    var endLatitude: Double,
    var endLongitude: Double,
    var maxSpeed: Double,
    var averageSpeed: Double,
    var distance: Double,
    var time: Long,

    ) :Serializable{

}