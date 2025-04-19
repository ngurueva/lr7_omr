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
import com.example.lr7_omr.adapters.AlbumAdapter
import com.example.lr7_omr.models.Album
import com.example.lr7_omr.viewmodels.MainViewModel
import com.google.android.material.floatingactionbutton.FloatingActionButton

class AlbumListActivity : AppCompatActivity() {
    private lateinit var albumViewModel: MainViewModel
    private lateinit var albumAdapter: AlbumAdapter
    private lateinit var albumRecyclerView: RecyclerView

    // Регистрируем ActivityResultLauncher
    private val startActivityForResult = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == RESULT_OK) {
            // Если результат успешен, обновляем список альбомов
            albumViewModel.loadAlbums()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_album_list)
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

        albumRecyclerView = findViewById(R.id.album_recycler_view)

        albumAdapter = AlbumAdapter(mutableListOf(), object : AlbumAdapter.OnAlbumClickListener {
            override fun onAlbumClick(album: Album) {
                // Открытие экрана редактирования альбома
                val intent = Intent(this@AlbumListActivity, EditAuthorAlbumActivity::class.java)
                intent.putExtra("type", "album")
//                intent.putExtra("album", album) // передаем альбом для редактирования
                startActivityForResult.launch(intent)
            }
        })

        albumRecyclerView.layoutManager = LinearLayoutManager(this)
        albumRecyclerView.adapter = albumAdapter

        // Получение ViewModel
        albumViewModel = ViewModelProvider(this)[MainViewModel::class.java]

        // Наблюдаем за данными альбомов
        albumViewModel.allAlbums.observe(this, Observer { albums ->
            albums?.let {
                albumAdapter.updateData(it) // Обновление данных в адаптере
            }
        })

        val fab: FloatingActionButton = findViewById(R.id.fab)
        fab.setOnClickListener {
            val intent = Intent(this, EditAuthorAlbumActivity::class.java)
            intent.putExtra("type", "album")
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
                val album = albumAdapter.getItemAt(position)

                // Подтверждение удаления
                AlertDialog.Builder(this@AlbumListActivity)
                    .setMessage("Вы уверены, что хотите удалить этот альбом?")
                    .setPositiveButton("Удалить") { _, _ ->
                        // Удаление альбома из базы данных через ViewModel
                        albumViewModel.deleteAlbum(album)
                        albumAdapter.removeItem(position)
                    }
                    .setNegativeButton("Отмена") { dialog, _ ->
                        albumAdapter.notifyItemChanged(position) // Восстанавливаем элемент
                        dialog.dismiss()
                    }
                    .show()
            }
        }

        val itemTouchHelper = ItemTouchHelper(swipeCallback)
        itemTouchHelper.attachToRecyclerView(albumRecyclerView)
    }
}
