package com.example.lr7_omr

import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.Spinner
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.example.lr7_omr.models.Song
import com.example.lr7_omr.viewmodels.MainViewModel

class EditSongActivity : AppCompatActivity() {
    private lateinit var authorSpinner: Spinner
    private lateinit var albumSpinner: Spinner
    private lateinit var songTitleEditText: EditText
    private lateinit var favoriteCheckBox: CheckBox
    private lateinit var actionButton: Button
    private lateinit var mainViewModel: MainViewModel

    private var songId: Int = 0 // id песни для редактирования (если передан)


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_edit_song)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val toolbar: Toolbar? = findViewById(R.id.my_toolbar)
        setSupportActionBar(toolbar)

        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            setDisplayShowHomeEnabled(true)
        }

        toolbar?.setNavigationOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        // Получаем ViewModel
        mainViewModel = ViewModelProvider(this)[MainViewModel::class.java]

        // Инициализация UI
        authorSpinner = findViewById(R.id.spinner)
        albumSpinner = findViewById(R.id.spinner2)
        songTitleEditText = findViewById(R.id.editTextText)
        favoriteCheckBox = findViewById(R.id.checkBox)
        actionButton = findViewById(R.id.button)

        // Проверяем, передан ли songId
        songId = intent.getIntExtra("songId", 0)

        // Заполняем спиннеры данными
        loadAuthorsAndAlbums()

        // Если редактирование, заполняем поля
        loadSongDetails(songId)
        actionButton.text = "Сохранить изменения"


        // Обработка кнопки сохранения/добавления
        actionButton.setOnClickListener {
            saveOrUpdateSong()
        }
    }

    private fun loadAuthorsAndAlbums() {
        // Загружаем авторов
        mainViewModel.allAuthors.observe(this, Observer { authors ->
            val authorNames = authors.map { it.name }
            val authorAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, authorNames)
            authorAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            authorSpinner.adapter = authorAdapter
        })

        // Загружаем альбомы
        mainViewModel.allAlbums.observe(this, Observer { albums ->
            val albumNames = albums.map { it.title }
            val albumAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, albumNames)
            albumAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            albumSpinner.adapter = albumAdapter
        })
    }

    private fun loadSongDetails(songId: Int) {
        mainViewModel.getSongById(songId).observe(this, Observer { song ->
            song?.let {
                songTitleEditText.setText(it.title)
                favoriteCheckBox.isChecked = it.isFavorite

                // Устанавливаем выбранного автора и альбом
                val authors = mainViewModel.allAuthors.value ?: return@Observer
                val albums = mainViewModel.allAlbums.value ?: return@Observer

                val authorIndex = authors.indexOfFirst { author -> author.idAuthor == it.authorIdAuthor }
                if (authorIndex != -1) authorSpinner.setSelection(authorIndex)

                val albumIndex = albums.indexOfFirst { album -> album.idAlbum == it.albumIdAlbum }
                if (albumIndex != -1) albumSpinner.setSelection(albumIndex)
            }
        })
    }

    private fun saveOrUpdateSong() {
        val title = songTitleEditText.text.toString().trim()
        val isFavorite = favoriteCheckBox.isChecked

        // Получаем выбранные автор и альбом
        val authorPosition = authorSpinner.selectedItemPosition
        val albumPosition = albumSpinner.selectedItemPosition

        val authors = mainViewModel.allAuthors.value
        val albums = mainViewModel.allAlbums.value

        if (title.isNotEmpty() && authors != null && albums != null &&
            authorPosition in authors.indices && albumPosition in albums.indices) {

            val selectedAuthor = authors[authorPosition]
            val selectedAlbum = albums[albumPosition]

            val song = Song(
                idSong = songId,
                title = title,
                isFavorite = isFavorite,
                authorIdAuthor = selectedAuthor.idAuthor,
                albumIdAlbum = selectedAlbum.idAlbum
            )
            mainViewModel.updateSong(song)
            setResult(RESULT_OK)
            finish() // Закрываем Activity
        } else {
            Toast.makeText(this, "Заполните все поля", Toast.LENGTH_SHORT).show()
        }
    }
}