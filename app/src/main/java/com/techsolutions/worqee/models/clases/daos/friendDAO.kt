package com.techsolutions.worqee.models.clases.daos

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.techsolutions.worqee.models.clases.entities.AmigoEntity

@Dao
interface AmigoDao {

    @Query("SELECT * FROM amigos")
    suspend fun getAll(): List<AmigoEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(amigos: List<AmigoEntity>)

    @Query("DELETE FROM amigos")
    suspend fun deleteAll()
}