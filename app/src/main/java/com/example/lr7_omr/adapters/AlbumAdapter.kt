package com.example.lr7_omr.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.lr7_omr.R
import com.example.lr7_omr.models.Album

class AlbumAdapter(
    private var albums: MutableList<Album>,
    private val listener: OnAlbumClickListener
) : RecyclerView.Adapter<AlbumAdapter.AlbumViewHolder>() {

    interface OnAlbumClickListener {
        fun onAlbumClick(album: Album)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AlbumViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_album, parent, false)
        return AlbumViewHolder(view)
    }

    override fun onBindViewHolder(holder: AlbumViewHolder, position: Int) {
        val album = albums[position]
        holder.bind(album)
    }

    override fun getItemCount(): Int {
        return albums.size
    }

    fun updateData(newAlbums: List<Album>) {
        albums = newAlbums.toMutableList()
        notifyDataSetChanged()
    }

    fun removeItem(position: Int) {
        albums.removeAt(position)
        notifyItemRemoved(position)
    }

    fun getItemAt(position: Int): Album {
        return albums[position]
    }

    inner class AlbumViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val albumTitle: TextView = itemView.findViewById(R.id.album_title)

        fun bind(album: Album) {
            albumTitle.text = album.title
            itemView.setOnClickListener {
                listener.onAlbumClick(album) // Обработчик клика по элементу
            }
        }
    }
}