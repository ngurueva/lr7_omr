package com.example.lr7_omr.models

import androidx.room.Embedded
import androidx.room.Relation

data class SongWithDetails(
    @Embedded val song: Song,
    @Relation(parentColumn = "albumIdAlbum", entityColumn = "idAlbum")
    val album: Album,
    @Relation(parentColumn = "authorIdAuthor", entityColumn = "idAuthor")
    val author: Author
)