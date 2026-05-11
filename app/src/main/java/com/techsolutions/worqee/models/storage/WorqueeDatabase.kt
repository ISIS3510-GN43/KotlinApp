package com.techsolutions.worqee.models.storage

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.techsolutions.worqee.models.clases.daos.FriendDao
import com.techsolutions.worqee.models.clases.entities.FriendEntity

@Database(entities = [FriendEntity::class], version = 1, exportSchema = false)
abstract class WorqeeDatabase : RoomDatabase() {

    abstract fun friendDao(): FriendDao

    companion object {
        @Volatile
        private var INSTANCE: WorqeeDatabase? = null

        fun getInstance(context: Context): WorqeeDatabase {
            return INSTANCE ?: synchronized(this) {
                Room.databaseBuilder(
                    context.applicationContext,
                    WorqeeDatabase::class.java,
                    "worqee_db"
                ).build().also { INSTANCE = it }
            }
        }
    }
}