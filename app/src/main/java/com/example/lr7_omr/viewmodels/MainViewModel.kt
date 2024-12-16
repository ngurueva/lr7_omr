package com.example.lr7_omr.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.lr7_omr.daos.AlbumDao
import com.example.lr7_omr.daos.AuthorDao
import com.example.lr7_omr.daos.SongDao
import com.example.lr7_omr.database.DatabaseClient
import com.example.lr7_omr.models.Album
import com.example.lr7_omr.models.Author
import com.example.lr7_omr.models.Song
import com.example.lr7_omr.models.SongWithDetails
import kotlinx.coroutines.launch

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val albumDao: AlbumDao
    private val authorDao: AuthorDao
    private val songDao: SongDao

    private val _allAlbums = MutableLiveData<List<Album>>()
    val allAlbums: LiveData<List<Album>> = _allAlbums

    private val _allAuthors = MutableLiveData<List<Author>>()
    val allAuthors: LiveData<List<Author>> = _allAuthors

    private val _allSongsWithDetails = MutableLiveData<List<SongWithDetails>>()
    val allSongsWithDetails: LiveData<List<SongWithDetails>> get() = _allSongsWithDetails

    init {
        val db = DatabaseClient.getInstance(application)
        albumDao = db.albumDao()
        authorDao = db.authorDao()
        songDao = db.songDao()

        // Загружаем данные при инициализации ViewModel
        loadAlbums()
        loadAuthors()
        loadSongs()
    }

    // Получение всех альбомов
    fun loadAlbums() {
        viewModelScope.launch {
            val albums = albumDao.getAllAlbums()
            _allAlbums.postValue(albums)
        }
    }

    // Получение всех авторов
    fun loadAuthors() {
        viewModelScope.launch {
            val authors = authorDao.getAllAuthors()
            _allAuthors.postValue(authors)
        }
    }

    fun loadSongs() {
        viewModelScope.launch {
            val songs = songDao.getSongsWithDetails()
            _allSongsWithDetails.postValue(songs)
        }
    }

    // Добавление альбома
    fun addAlbum(title: String) {
        viewModelScope.launch {
            albumDao.insert(Album(title = title))
            loadAlbums()  // Перезагружаем список альбомов
        }
    }

    // Добавление автора
    fun addAuthor(name: String) {
        viewModelScope.launch {
            authorDao.insert(Author(name = name))
            loadAuthors()  // Перезагружаем список авторов
        }
    }

    // Добавление песни
    fun addSong(song: Song) {
        viewModelScope.launch {
            songDao.insert(song)
            loadSongs()  // Перезагружаем список песен
        }
    }

    // Обновление альбома
    fun updateAlbum(album: Album) {
        viewModelScope.launch {
            albumDao.update(album)
            loadAlbums()  // Перезагружаем список альбомов
        }
    }

    // Обновление автора
    fun updateAuthor(author: Author) {
        viewModelScope.launch {
            authorDao.update(author)
            loadAuthors()  // Перезагружаем список авторов
        }
    }

    // Обновление песни
    fun updateSong(song: Song) {
        viewModelScope.launch {
            songDao.update(song)  // Обновляем песню в базе данных
            loadSongs()  // Перезагружаем список песен
        }
    }

    // Удаление альбома
    fun deleteAlbum(album: Album) {
        viewModelScope.launch {
            albumDao.delete(album)
            loadAlbums()  // Перезагружаем список альбомов
        }
    }

    // Удаление автора
    fun deleteAuthor(author: Author) {
        viewModelScope.launch {
            authorDao.delete(author)
            loadAuthors()  // Перезагружаем список авторов
        }
    }

    // Удаление песни
    fun deleteSong(song: Song) {
        viewModelScope.launch {
            songDao.delete(song)  // Удаляем песню из базы данных
            loadSongs()  // Перезагружаем список песен
        }
    }

    fun getSongById(songId: Int): LiveData<Song?> {
        val songLiveData = MutableLiveData<Song?>()

        viewModelScope.launch {
            val song = songDao.getById(songId)
            songLiveData.postValue(song)
        }

        return songLiveData
    }
}

