package com.example.lr7_omr.database

import android.content.Context
import androidx.room.Room

object DatabaseClient {
    private var instance: AppDatabase? = null

    fun getInstance(context: Context): AppDatabase {
        if (instance == null) {
            instance = Room.databaseBuilder(
                context.applicationContext,
                AppDatabase::class.java,
                "music_db"
            ).build()
        }
        return instance!!
    }
}