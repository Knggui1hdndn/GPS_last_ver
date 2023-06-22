package com.gps.speedometer.odometer.gpsspeedtracker.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.gps.speedometer.odometer.gpsspeedtracker.model.MovementData

@Dao
interface MovementDataDao {
    @Query("SELECT * FROM MovementData where startLatitude!=0  ")
    fun getAllMovementData(): MutableList<MovementData>


    @Delete
    fun delete(model: MovementData)
    @Query("SELECT id FROM MovementData ORDER BY id DESC LIMIT 1")
    fun getLastMovementDataId(): Int

    @Query("SELECT * FROM MovementData ORDER BY id DESC LIMIT 1")
    fun getLastMovementData(): MovementData
    @Query("SELECT * FROM MovementData where id= :id")
    fun getMovementDataById(id:Int): MovementData
    @Update
    fun updateMovementData(movementData: MovementData)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insertMovementData(users: MovementData)
    @Query("Delete from MovementData")
    fun deleteAll()

    @Query("SELECT SUM(MovementData.distance)  FROM MovementData")
    fun getDistance():Int
}