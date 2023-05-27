package com.example.gps.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.gps.model.LocationData
import com.example.gps.model.MovementData
import com.example.gps.model.Vehicle

@Dao
interface VehicleDao {
    @Update
    fun updateVehicle(vehicle: Vehicle)


    @Query("SELECT * FROM Vehicle where isChecked = 1 and typeID= :typeId")
    fun getVehicleChecked(typeId:Int):  Vehicle

    @Query("INSERT INTO Vehicle (clockSpeed, limitWarning,type,isChecked,typeID) VALUES (:clockSpeed, :limitWarning,  :type,:isChecked,:typeID)")
    fun insertVehicle(
        clockSpeed: Int,
        limitWarning: Int,
        type: Int,
        isChecked: Int,
        typeID:Int)


}