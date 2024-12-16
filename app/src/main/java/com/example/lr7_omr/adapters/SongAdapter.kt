package com.example.lr7_omr.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.lr7_omr.R
import com.example.lr7_omr.models.SongWithDetails
import android.graphics.Color;


class SongAdapter(
    private var songs: MutableList<SongWithDetails>,
    private val listener: OnSongClickListener
) : RecyclerView.Adapter<SongAdapter.SongViewHolder>() {

    interface OnSongClickListener {
        fun onSongClick(song: SongWithDetails)
        fun onSongSwipe(song: SongWithDetails)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SongViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_song, parent, false)
        return SongViewHolder(view)
    }

    override fun onBindViewHolder(holder: SongViewHolder, position: Int) {
        val song = songs[position]
        holder.bind(song)
    }

    override fun getItemCount(): Int = songs.size

    fun updateData(newSongs: List<SongWithDetails>) {
        songs = newSongs.toMutableList()
        notifyDataSetChanged()
    }

    fun removeItem(position: Int) {
        val song = songs[position]
        songs.removeAt(position)
        notifyItemRemoved(position)
        listener.onSongSwipe(song)
    }

    inner class SongViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val songTitle: TextView = itemView.findViewById(R.id.song_title)
        private val songAuthor: TextView = itemView.findViewById(R.id.song_author)
        private val songAlbum: TextView = itemView.findViewById(R.id.song_album)

        fun bind(songWithDetails: SongWithDetails) {
            songTitle.text = songWithDetails.song.title
            songAuthor.text = "Автор: ${songWithDetails.author.name}"
            songAlbum.text = "Альбом: ${songWithDetails.album.title}"

            // Устанавливаем цвет названия в зависимости от isFavorite
            if (songWithDetails.song.isFavorite) {
                songTitle.setTextColor(Color.GREEN);
            }

            itemView.setOnClickListener {
                listener.onSongClick(songWithDetails)
            }
        }
    }
}