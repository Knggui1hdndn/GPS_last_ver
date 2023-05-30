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
interface VehicleDao {
    @Update
    fun updateVehicle(vehicle: Vehicle)

    @Query("Update Vehicle set isChecked=0  where typeID =  :i  ")
    fun updateUnChecked(i: Int)

    @Query("Update Vehicle set clockSpeed= :clockSpeed  where typeID =  :typeID and type = :type  ")
    fun updateMaxSpeed(typeID: Int,type: Int,clockSpeed:Int)
    @Query("Update Vehicle set isChecked=1  where   typeID = :i and type= :type")
    fun updateVehicle(i: Int,type: Int)

    @Query("SELECT * FROM Vehicle where isChecked = 1 and typeID= :typeId")
    fun getVehicleChecked(typeId: Int): Vehicle

    @Query("SELECT * FROM Vehicle  ")
    fun getAllVehicle(): MutableList<Vehicle>

    @Query("INSERT INTO Vehicle (clockSpeed, limitWarning,type,isChecked,typeID) VALUES (:clockSpeed, :limitWarning,  :type,:isChecked,:typeID)")
    fun insertVehicle(
        clockSpeed: Int,
        limitWarning: Int,
        type: Int,
        isChecked: Int,
        typeID: Int
    )


}