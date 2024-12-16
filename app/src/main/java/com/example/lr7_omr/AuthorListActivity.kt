package com.example.lr7_omr

import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.lr7_omr.adapters.AuthorAdapter
import com.example.lr7_omr.models.Author
import com.example.lr7_omr.viewmodels.MainViewModel
import com.google.android.material.floatingactionbutton.FloatingActionButton

class AuthorListActivity : AppCompatActivity() {
    private lateinit var authorViewModel: MainViewModel
    private lateinit var authorAdapter: AuthorAdapter
    private lateinit var authorRecyclerView: RecyclerView

    // Регистрируем ActivityResultLauncher
    private val startActivityForResult = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == RESULT_OK) {
            // Если результат успешен, обновляем список альбомов
            authorViewModel.loadAuthors()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_author_list)
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

        authorRecyclerView = findViewById(R.id.author_recycler_view)

        authorAdapter = AuthorAdapter(mutableListOf(), object : AuthorAdapter.OnAuthorClickListener {
            override fun onAuthorClick(author: Author) {
                // Открытие экрана редактирования альбома
                val intent = Intent(this@AuthorListActivity, EditAuthorAlbumActivity::class.java)
                intent.putExtra("type", "author")
                intent.putExtra("author", author) // передаем альбом для редактирования
                startActivityForResult.launch(intent)
            }
        })

        authorRecyclerView.layoutManager = LinearLayoutManager(this)
        authorRecyclerView.adapter = authorAdapter

        // Получение ViewModel
        authorViewModel = ViewModelProvider(this)[MainViewModel::class.java]

        // Наблюдаем за данными авторов
        authorViewModel.allAuthors.observe(this, Observer { authors ->
            authors?.let {
                authorAdapter.updateData(it) // Обновление данных в адаптере
            }
        })

        val fab: FloatingActionButton = findViewById(R.id.fab)
        fab.setOnClickListener {
            val intent = Intent(this, EditAuthorAlbumActivity::class.java)
            intent.putExtra("type", "author")
            startActivityForResult.launch(intent)
        }

        // Добавляем поддержку свайпов
        val swipeCallback = object : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {
            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean = false

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val position = viewHolder.adapterPosition
                val author = authorAdapter.getItemAt(position)

                // Подтверждение удаления
                AlertDialog.Builder(this@AuthorListActivity)
                    .setMessage("Вы уверены, что хотите удалить этого автора?")
                    .setPositiveButton("Удалить") { _, _ ->
                        // Удаление автора из базы данных через ViewModel
                        authorViewModel.deleteAuthor(author)
                        authorAdapter.removeItem(position)
                    }
                    .setNegativeButton("Отмена") { dialog, _ ->
                        authorAdapter.notifyItemChanged(position) // Восстанавливаем элемент
                        dialog.dismiss()
                    }
                    .show()
            }
        }

        val itemTouchHelper = ItemTouchHelper(swipeCallback)
        itemTouchHelper.attachToRecyclerView(authorRecyclerView)
    }
}