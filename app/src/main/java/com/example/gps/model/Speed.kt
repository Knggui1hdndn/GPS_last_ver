package com.example.gps.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "Speed")
class Speed(@PrimaryKey val type: Int, var isChecked: Boolean)
