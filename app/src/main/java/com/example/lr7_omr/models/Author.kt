package com.example.lr7_omr.models

import android.os.Parcel
import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class Author(
    @PrimaryKey(autoGenerate = true) val idAuthor: Int = 0,
    val name: String
)