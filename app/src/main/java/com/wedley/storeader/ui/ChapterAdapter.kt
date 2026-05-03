package com.wedley.storeader.ui

import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.wedley.storeader.R
import com.wedley.storeader.data.Chapter
import com.wedley.storeader.data.Episode

class ChapterAdapter(
    private val chapters: MutableList<Chapter>,
    private val onDelete: (Int) -> Unit
) : RecyclerView.Adapter<ChapterAdapter.VH>() {

    inner class VH(view: View) : RecyclerView.ViewHolder(view) {
        val header       : View     = view.findViewById(R.id.chapterHeader)
        val body         : View     = view.findViewById(R.id.chapterBody)
        val badge        : TextView = view.findViewById(R.id.chapterBadge)
        val namePreview  : TextView = view.findViewById(R.id.chapterNamePreview)
        val toggle       : TextView = view.findViewById(R.id.chapterToggle)
        val inputNumber  : EditText = view.findViewById(R.id.inputNumber)
        val inputName    : EditText = view.findViewById(R.id.inputName)
        val episodesList : RecyclerView = view.findViewById(R.id.episodesList)
        val btnAddEpisode: View     = view.findViewById(R.id.btnAddEpisode)
        val btnDelete    : View     = view.findViewById(R.id.btnDelete)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_chapter_card, parent, false)
        return VH(view)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        val ch = chapters[position]

        // Set values
        holder.badge.text        = "Ch. ${position + 1}"
        holder.namePreview.text  = ch.name.ifBlank { "Untitled Chapter" }
        holder.namePreview.alpha = if (ch.name.isBlank()) 0.4f else 1f
        holder.body.visibility   = if (ch.isExpanded) View.VISIBLE else View.GONE
        holder.toggle.rotation   = if (ch.isExpanded) 180f else 0f

        holder.inputNumber.setText(ch.number)
        holder.inputName.setText(ch.name)

        // Clear existing watchers (simplified for this update)
        holder.inputNumber.addTextChangedListener(watcher { ch.number = it })
        holder.inputName.addTextChangedListener(watcher {
            ch.name = it
            holder.namePreview.text  = it.ifBlank { "Untitled Chapter" }
            holder.namePreview.alpha = if (it.isBlank()) 0.4f else 1f
        })

        // Episodes Adapter
        val epAdapter = EpisodeAdapter(ch.episodes) { epIndex ->
            ch.episodes.removeAt(epIndex)
            notifyItemChanged(position) // Refresh nested list
        }
        holder.episodesList.layoutManager = LinearLayoutManager(holder.itemView.context)
        holder.episodesList.adapter = epAdapter

        holder.btnAddEpisode.setOnClickListener {
            ch.episodes.add(Episode(number = (ch.episodes.size + 1).toString()))
            epAdapter.notifyItemInserted(ch.episodes.size - 1)
        }

        // Expand/collapse
        holder.header.setOnClickListener {
            ch.isExpanded = !ch.isExpanded
            holder.body.visibility = if (ch.isExpanded) View.VISIBLE else View.GONE
            holder.toggle.animate()
                .rotation(if (ch.isExpanded) 180f else 0f)
                .setDuration(200)
                .start()
        }

        // Delete
        holder.btnDelete.setOnClickListener {
            val pos = holder.bindingAdapterPosition
            if (pos != RecyclerView.NO_POSITION) onDelete(pos)
        }
    }

    override fun getItemCount() = chapters.size

    override fun getItemId(position: Int) = position.toLong()

    init { setHasStableIds(true) }

    private fun watcher(onChange: (String) -> Unit) = object : TextWatcher {
        override fun afterTextChanged(s: Editable?) { onChange(s?.toString() ?: "") }
        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
    }
}