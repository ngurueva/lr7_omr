package com.example.lr7_omr.viewmodels

import android.app.Application
import android.content.ActivityNotFoundException
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
import kotlinx.coroutines.*
import com.example.lr7_omr.threads.TaskManager
import android.content.Context
import android.content.Intent
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import android.net.Uri
import android.widget.Toast
import androidx.core.content.FileProvider
import java.io.File


//TODO: Сделать отмену потоков!!!

class MainViewModel(application: Application) : AndroidViewModel(application) {
    //переменные DAO - интерфейсы для работы с room-базой
    private val albumDao: AlbumDao
    private val authorDao: AuthorDao
    private val songDao: SongDao

    //LiveData позволяют UI автоматически обновляться.
    private val _allAlbums = MutableLiveData<List<Album>>()
    val allAlbums: LiveData<List<Album>> = _allAlbums

    private val _allAuthors = MutableLiveData<List<Author>>()
    val allAuthors: LiveData<List<Author>> = _allAuthors

    private val _allSongsWithDetails = MutableLiveData<List<SongWithDetails>>()
    val allSongsWithDetails: LiveData<List<SongWithDetails>> get() = _allSongsWithDetails

    private val job = SupervisorJob() // Управляет всеми корутинами

    // Создаем свою область выполнения корутин (coroutineScope), в которой:
    // - все корутины будут выполняться в Dispatchers.IO (фоновый поток)
    // - все корутины будут "привязаны" к SupervisorJob, чтобы управлять их жизненным циклом
    private val coroutineScope = CoroutineScope(Dispatchers.IO + job)



    init {
        val db = DatabaseClient.getInstance(application)
        albumDao = db.albumDao()
        authorDao = db.authorDao()
        songDao = db.songDao()

        // Загружаем данные при инициализации ViewModel
        loadAlbums()
        loadAuthors()
        loadSongs()

        loadSongsInThread()

        exportSongsToCsvInThread(getApplication())
        exportSongsToPdfInThread(getApplication())
    }

    // Получение всех альбомов
    fun loadAlbums() {
        // Запускаем в корутине для работы с базой данных
        viewModelScope.launch {
            val albums = withContext(Dispatchers.IO) { albumDao.getAllAlbums() }
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
        viewModelScope.launch(Dispatchers.IO) { // Фоновый поток для базы данных
            songDao.insert(song)
            withContext(Dispatchers.Main) { // Возвращаемся в UI поток
                loadSongs() // Обновляем UI после вставки
            }
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

        coroutineScope.launch {
            val song = songDao.getById(songId)
            songLiveData.postValue(song)
        }

        return songLiveData
    }

    override fun onCleared() {
        super.onCleared()
        job.cancel()
        TaskManager.instance.cancelAllThreads()
    }

    fun loadSongsInThread() {
        TaskManager.instance.runInThread {
            try {
                val songs = runBlocking { songDao.getSongsWithDetails() }
                _allSongsWithDetails.postValue(songs)

                println("Загружено песен: ${songs.size}")
                for (songWithDetails in songs) {
                    val song = songWithDetails.song
                    val album = songWithDetails.album
                    val author = songWithDetails.author

                    println("Песня: ${song.title}, Автор: ${author.name}, Альбом: ${album.title}")
                }

            } catch (e: Exception) {
                println("Ошибка при чтении песен в потоке: ${e.message}")
            }
        }
    }

    fun openCSV(context: Context, filename: String = "songs_export.csv") {
        val file = File(context.filesDir, filename)
        if (!file.exists()) {
            println("CSV-файл не найден: ${file.absolutePath}")
            return
        }

        val uri = FileProvider.getUriForFile(
            context,
            context.packageName + ".provider",
            file
        )

        val intent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(uri, "text/csv")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }

        context.startActivity(Intent.createChooser(intent, "Открыть CSV-файл с помощью..."))
    }

    fun openPDF(context: Context, filename: String = "songs_export.pdf") {
        val file = File(context.filesDir, filename)
        if (!file.exists()) {
            println("PDF-файл не найден: ${file.absolutePath}")
            return
        }

        val uri = FileProvider.getUriForFile(
            context,
            context.packageName + ".provider",
            file
        )

        val intent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(uri, "application/pdf")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }

        context.startActivity(Intent.createChooser(intent, "Открыть PDF-файл с помощью..."))
    }



    fun exportSongsToCsvInThread(context: Context, filename: String = "songs_export.csv") {
        TaskManager.instance.runInThread {
            try {
                val songs = runBlocking { songDao.getSongsWithDetails() }
                val csvBuilder = StringBuilder()
                csvBuilder.append("ID,Title,Album,Author\n")

                for (song in songs) {
                    csvBuilder.append("${song.song.idSong},${song.song.title},${song.album.title},${song.author.name}\n")
                }

                val file = File(context.filesDir, filename)
                file.writeText(csvBuilder.toString())

                println("CSV-файл успешно создан: ${file.absolutePath}")
            } catch (e: Exception) {
                println("Ошибка при экспорте CSV: ${e.message}")
            }
        }
    }

    fun exportSongsToPdfInThread(context: Context, filename: String = "songs_export.pdf") {
        TaskManager.instance.runInThread {
            try {
                val songs = runBlocking { songDao.getSongsWithDetails() }

                val pdfDocument = PdfDocument()
                val paint = Paint()
                val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create()
                val page = pdfDocument.startPage(pageInfo)
                val canvas = page.canvas

                var y = 50
                paint.textSize = 14f
                paint.isFakeBoldText = true
                canvas.drawText("Список песен", 220f, y.toFloat(), paint)
                y += 30
                paint.isFakeBoldText = false

                for (song in songs) {
                    val line = "${song.song.idSong}. ${song.song.title} — ${song.author.name} [${song.album.title}]"
                    canvas.drawText(line, 30f, y.toFloat(), paint)
                    y += 20
                    if (y > 800) break // ограничение страницы (одна страница)
                }

                pdfDocument.finishPage(page)

                val file = File(context.filesDir, filename)
                file.outputStream().use {
                    pdfDocument.writeTo(it)
                }
                pdfDocument.close()

                println("PDF-файл успешно создан: ${file.absolutePath}")
            } catch (e: Exception) {
                println("Ошибка при экспорте PDF: ${e.message}")
            }
        }
    }

}