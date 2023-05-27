package com.example.gps.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(
    tableName = "LocationData",
    foreignKeys = [ForeignKey(
        entity = MovementData::class,
        parentColumns = ["id"],
        childColumns = ["movementDataId"],
        onDelete = ForeignKey.CASCADE
    )]
)
class LocationData (
    @PrimaryKey(autoGenerate = true)
    val id: Int,
    @ColumnInfo(name = "latitude")
    val latitude: Double,

    @ColumnInfo(name = "longitude")
    val longitude: Double,

    @ColumnInfo(name = "movementDataId")
    val movementDataId: Int
){
    override fun toString(): String {
        return "LocationData(id=$id, latitude=$latitude, longitude=$longitude, movementDataId=$movementDataId)"
    }
}