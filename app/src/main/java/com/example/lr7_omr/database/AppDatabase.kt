package com.example.lr7_omr.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.lr7_omr.daos.AlbumDao
import com.example.lr7_omr.daos.AuthorDao
import com.example.lr7_omr.daos.SongDao
import com.example.lr7_omr.models.Album
import com.example.lr7_omr.models.Author
import com.example.lr7_omr.models.Song

@Database(entities = [Author::class, Album::class, Song::class], version = 1)
abstract class AppDatabase : RoomDatabase() {
    abstract fun authorDao(): AuthorDao
    abstract fun albumDao(): AlbumDao
    abstract fun songDao(): SongDao
}