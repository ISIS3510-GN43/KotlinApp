package com.techsolutions.worqee.models.storage

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.techsolutions.worqee.models.clases.daos.AmigoDao
import com.techsolutions.worqee.models.clases.entities.AmigoEntity


@Database(entities = [AmigoEntity::class], version = 1, exportSchema = false)
abstract class WorqeeDatabase : RoomDatabase() {

    abstract fun amigoDao(): AmigoDao

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