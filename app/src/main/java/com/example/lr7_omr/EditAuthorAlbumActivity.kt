package com.example.lr7_omr

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.example.lr7_omr.database.DatabaseClient
import com.example.lr7_omr.models.Album
import com.example.lr7_omr.models.Author
import com.example.lr7_omr.viewmodels.MainViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class EditAuthorAlbumActivity : AppCompatActivity() {

    private lateinit var mainViewModel: MainViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_edit_author_album)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val toolbar: Toolbar? = findViewById(R.id.my_toolbar)
        setSupportActionBar(toolbar)

        // Получаем тип действия из Intent
        val type = intent.getStringExtra("type") ?: "author"

        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            setDisplayShowHomeEnabled(true)
            when (type) {
                "author" -> {
                    title = "Автор"
                }
                "album" -> {
                    title = "Альбом"
                }
            }
        }

        toolbar?.setNavigationOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        // Получаем ViewModel
        mainViewModel = ViewModelProvider(this)[MainViewModel::class.java]

        // Настройка экрана в зависимости от типа
        val editText = findViewById<EditText>(R.id.nameEdit)
        val button = findViewById<Button>(R.id.addButton)

        when (type) {
            "author" -> {
                toolbar?.title = "Автор"
                editText.hint = "Имя автора"

                // Получаем объект для редактирования
                val currentAuthor: Author? = intent.getParcelableExtra("author")
                if (currentAuthor != null) {
                    editText.setText(currentAuthor.name)
                    button.text = "Изменить"
                }

                button.setOnClickListener {
                    val name = editText.text.toString()
                    if (name.isNotEmpty()) {
                        if (currentAuthor == null) {
                            mainViewModel.addAuthor(name)  // Добавление нового автора
                        } else {
                            mainViewModel.updateAuthor(currentAuthor.copy(name = name))  // Обновление автора
                        }
                        setResult(RESULT_OK)
                        finish()
                    }
                }
            }
            "album" -> {
                toolbar?.title = "Альбом"
                editText.hint = "Название альбома"

                // Получаем объект для редактирования
                val currentAlbum: Album? = intent.getParcelableExtra("album")
                if (currentAlbum != null) {
                    editText.setText(currentAlbum.title)
                    button.text = "Изменить"
                }

                button.setOnClickListener {
                    val title = editText.text.toString()
                    if (title.isNotEmpty()) {
                        if (currentAlbum == null) {
                            mainViewModel.addAlbum(title)  // Добавление нового альбома
                        } else {
                            mainViewModel.updateAlbum(currentAlbum.copy(title = title))  // Обновление альбома
                        }
                        setResult(RESULT_OK)
                        finish()
                    }
                }
            }
        }
    }
}
