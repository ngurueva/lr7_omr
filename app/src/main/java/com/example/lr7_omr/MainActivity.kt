package com.example.lr7_omr

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.Spinner
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.lr7_omr.adapters.AlbumAdapter
import com.example.lr7_omr.adapters.SongAdapter
import com.example.lr7_omr.models.Song
import com.example.lr7_omr.models.SongWithDetails
import com.example.lr7_omr.viewmodels.MainViewModel
import com.google.android.material.floatingactionbutton.FloatingActionButton

class MainActivity : AppCompatActivity() {
    private lateinit var recyclerView: RecyclerView
    private lateinit var songAdapter: SongAdapter
    private lateinit var mainViewModel: MainViewModel
    private lateinit var authorSpinner: Spinner
    private lateinit var albumSpinner: Spinner
    private lateinit var songTitleEditText: EditText
    private lateinit var favoriteCheckBox: CheckBox
    private lateinit var actionButton: Button

    private var isEditing = false
    private var songId: Int = 0 // id песни для редактирования (если передан)

    // Регистрируем ActivityResultLauncher
    private val startActivityForResult = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == RESULT_OK) {
            // Если результат успешен, обновляем список альбомов
            mainViewModel.loadSongs()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        val btnOpnCSV = findViewById<Button>(R.id.btnOpnCSV)
        val btnOpnPDF = findViewById<Button>(R.id.btnOpnPDF)

        btnOpnCSV.setOnClickListener {
            mainViewModel.openCSV(this)
        }

        btnOpnPDF.setOnClickListener {
            mainViewModel.openPDF(this)
        }





        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        setSupportActionBar(findViewById(R.id.my_toolbar))

        mainViewModel = ViewModelProvider(this)[MainViewModel::class.java]

        // Инициализация UI
        authorSpinner = findViewById(R.id.spinner)
        albumSpinner = findViewById(R.id.spinner2)
        songTitleEditText = findViewById(R.id.editTextText)
        favoriteCheckBox = findViewById(R.id.checkBox)
        actionButton = findViewById(R.id.button)

        // Проверяем, передан ли songId
        songId = intent.getIntExtra("songId", 0)
        isEditing = songId != 0

        // Заполняем спиннеры данными
        loadAuthorsAndAlbums()

        // Обработка кнопки сохранения/добавления
        actionButton.setOnClickListener {
            saveOrUpdateSong()
        }

        recyclerView = findViewById(R.id.recycler_view)
        recyclerView.layoutManager = LinearLayoutManager(this)

        songAdapter = SongAdapter(mutableListOf(), object : SongAdapter.OnSongClickListener{
            override fun onSongClick(song: SongWithDetails){
                // Переход к редактированию
                val intent = Intent(this@MainActivity, EditSongActivity::class.java)
                intent.putExtra("songId", song.song.idSong)
                startActivityForResult.launch(intent)
            }

            override fun onSongSwipe(song: SongWithDetails) {
                // Удаление песни
                mainViewModel.deleteSong(song.song)
            }
        } )
        recyclerView.adapter = songAdapter


        // Настраиваем свайпы для удаления
        val itemTouchHelper = ItemTouchHelper(object : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {
            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean = false

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val position = viewHolder.adapterPosition
                songAdapter.removeItem(position)
            }
        })
        itemTouchHelper.attachToRecyclerView(recyclerView)

        // Наблюдаем за изменениями данных
        mainViewModel.allSongsWithDetails.observe(this) { songs ->
            songAdapter.updateData(songs)
        }


    }

    // Подключаем меню
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu) // Подключаем файл меню
        return true
    }

    // Обрабатываем нажатие на элементы меню
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.item_author -> {
                // Реакция на нажатие "Авторы"
                showAuthors()
                true
            }
            R.id.item_album -> {
                // Реакция на нажатие "Альбомы"
                showAlbums()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun showAuthors() {
        val intent = Intent(this, AuthorListActivity::class.java)
        startActivity(intent)
    }

    private fun showAlbums() {
        val intent = Intent(this, AlbumListActivity::class.java)
        startActivity(intent)
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
    private fun saveOrUpdateSong() {
        val title = songTitleEditText.text.toString().trim()
        val isFavorite = favoriteCheckBox.isChecked
        val authorPosition = authorSpinner.selectedItemPosition
        val albumPosition = albumSpinner.selectedItemPosition

        val authors = mainViewModel.allAuthors.value
        val albums = mainViewModel.allAlbums.value

        if (title.isNotEmpty() && authors != null && albums != null &&
            authorPosition in authors.indices && albumPosition in albums.indices) {

            val selectedAuthor = authors[authorPosition]
            val selectedAlbum = albums[albumPosition]

            val song = Song(
                title = title,
                isFavorite = isFavorite,
                authorIdAuthor = selectedAuthor.idAuthor,
                albumIdAlbum = selectedAlbum.idAlbum
            )
            mainViewModel.addSong(song)
            setResult(RESULT_OK)
            songTitleEditText?.text?.clear()
        }
    }
}