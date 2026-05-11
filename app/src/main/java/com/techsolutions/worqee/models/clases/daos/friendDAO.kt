package com.techsolutions.worqee.models.clases.daos

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.techsolutions.worqee.models.clases.entities.FriendEntity

@Dao
interface FriendDao {

    @Query("SELECT * FROM amigos")
    suspend fun getAll(): List<FriendEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(friends: List<FriendEntity>)

    @Query("DELETE FROM amigos")
    suspend fun deleteAll()
}