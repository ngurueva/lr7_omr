package com.example.lr7_omr.daos

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.example.lr7_omr.models.Album

@Dao
interface AlbumDao {
    @Insert
    suspend fun insert(album: Album)

    @Update
    suspend fun update(album: Album)

    @Delete
    suspend fun delete(album: Album)

    @Query("SELECT * FROM Album")
    suspend fun getAllAlbums(): List<Album>
}