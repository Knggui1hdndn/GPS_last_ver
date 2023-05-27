package com.example.gps.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(
    tableName = "Vehicle",
    foreignKeys = [ForeignKey(
        entity = Speed::class,
        parentColumns = ["type"],
        childColumns = ["typeID"],
        onDelete = ForeignKey.CASCADE
    )]
)
class Vehicle(
    @PrimaryKey(autoGenerate = true)
    val id:Int,
    val typeID: Int,
    var type: Int,
    var limitWarning: Int,
    var clockSpeed: Int,
    var isChecked: Boolean
)

