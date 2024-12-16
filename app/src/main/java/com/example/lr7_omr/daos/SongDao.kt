package com.example.lr7_omr.daos

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.example.lr7_omr.models.Song
import com.example.lr7_omr.models.SongWithDetails

@Dao
interface SongDao {
    @Insert
    suspend fun insert(song: Song)

    @Update
    suspend fun update(song: Song)

    @Delete
    suspend fun delete(song: Song)

    @Query("SELECT * FROM Song")
    suspend fun getAllSongs(): List<Song>

    @Query("SELECT * FROM Song WHERE isFavorite = 1")
    suspend fun getFavoriteSongs(): List<Song>

    @Query("SELECT * FROM Song")
    suspend fun getSongsWithDetails(): List<SongWithDetails>

    @Query("SELECT * FROM song WHERE idSong = :id LIMIT 1")
    suspend fun getById(id: Int): Song?
}