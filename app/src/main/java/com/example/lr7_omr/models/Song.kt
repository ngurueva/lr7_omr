package com.example.lr7_omr.models

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class Song(
    @PrimaryKey(autoGenerate = true) val idSong: Int = 0,
    val albumIdAlbum: Int, // FK на Album
    val authorIdAuthor: Int, // FK на Author
    val title: String,
    val isFavorite: Boolean = false
)