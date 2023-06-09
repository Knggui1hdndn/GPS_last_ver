package com.example.gps.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Update
import com.example.gps.model.Vehicle

@Dao
interface VehicleDao {
    @Update
    fun updateVehicle(vehicle: Vehicle)

    @Query("Update Vehicle set isChecked=0    ")
    fun updateUnChecked( )

    @Query("Update Vehicle set clockSpeed= :clockSpeed  where  isChecked=1  ")
    fun updateMaxSpeed(    clockSpeed: Int)

    @Query("Update Vehicle set limitWarning= :limitWarning  where isChecked=1  ")
    fun updateWarning( limitWarning: Int )

    @Query("Update Vehicle set isChecked=1  where   type= :type")
    fun updateVehicle(  type: Int)

    @Query("SELECT * FROM Vehicle where isChecked = 1 ")
    fun getVehicleChecked( ): Vehicle

    @Query("SELECT * FROM Vehicle  ")
    fun getAllVehicle(): MutableList<Vehicle>

    @Query("INSERT INTO Vehicle (clockSpeed, limitWarning,type,isChecked ) VALUES (:clockSpeed, :limitWarning,  :type,:isChecked)")
    fun insertVehicle(
        clockSpeed: Int,
        limitWarning: Int,
        type: Int,
        isChecked: Int

    )


}