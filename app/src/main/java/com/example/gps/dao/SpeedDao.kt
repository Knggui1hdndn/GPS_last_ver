package com.example.gps.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.gps.model.LocationData
import com.example.gps.model.MovementData
import com.example.gps.model.Speed
import com.example.gps.model.Vehicle

@Dao
interface SpeedDao {
    @Query("Update Speed set isChecked=0  where type!=:i")
    fun updateUnChecked(i: Int)

    @Query("Update Speed set isChecked=1  where   type==:i")
    fun updateChecked(i: Int)

    @Update
    fun update(speed: Speed)

    @Query("SELECT * FROM Speed where isChecked = 1  ")
    fun getChecked(): Speed

    @Insert
    fun insertSpeed(
        speed: Speed
    )


}