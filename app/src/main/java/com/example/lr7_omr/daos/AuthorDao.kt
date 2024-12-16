package com.example.lr7_omr.daos

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.example.lr7_omr.models.Author

@Dao
interface AuthorDao {
    @Insert
    suspend fun insert(author: Author)

    @Update
    suspend fun update(author: Author)

    @Delete
    suspend fun delete(author: Author)

    @Query("SELECT * FROM Author")
    suspend fun getAllAuthors(): List<Author>
}