package com.example.lr7_omr.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.lr7_omr.R
import com.example.lr7_omr.models.Author

class AuthorAdapter(
    private var authors: MutableList<Author>,
    private val listener: OnAuthorClickListener
) : RecyclerView.Adapter<AuthorAdapter.AuthorViewHolder>() {

    interface OnAuthorClickListener {
        fun onAuthorClick(author: Author)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AuthorViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_author, parent, false)
        return AuthorViewHolder(view)
    }

    override fun onBindViewHolder(holder: AuthorViewHolder, position: Int) {
        val author = authors[position]
        holder.bind(author)
    }

    override fun getItemCount(): Int {
        return authors.size
    }

    // Обновление данных в адаптере
    fun updateData(newAuthors: List<Author>) {
        authors = newAuthors.toMutableList()
        notifyDataSetChanged()
    }

    // Удаление элемента по позиции
    fun removeItem(position: Int) {
        authors.removeAt(position)
        notifyItemRemoved(position)
    }

    // Получение элемента по позиции
    fun getItemAt(position: Int): Author {
        return authors[position]
    }

    inner class AuthorViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val authorName: TextView = itemView.findViewById(R.id.author_name)

        fun bind(author: Author) {
            authorName.text = author.name
            itemView.setOnClickListener {
                listener.onAuthorClick(author) // Обработчик клика по элементу
            }
        }
    }
}

