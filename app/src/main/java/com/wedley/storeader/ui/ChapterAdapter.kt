package com.wedley.storeader.ui

import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.wedley.storeader.R
import com.wedley.storeader.data.Chapter

class ChapterAdapter(
    private val chapters: MutableList<Chapter>,
    private val onDelete: (Int) -> Unit
) : RecyclerView.Adapter<ChapterAdapter.VH>() {

    inner class VH(view: View) : RecyclerView.ViewHolder(view) {
        val header      : View     = view.findViewById(R.id.chapterHeader)
        val body        : View     = view.findViewById(R.id.chapterBody)
        val badge       : TextView = view.findViewById(R.id.chapterBadge)
        val namePreview : TextView = view.findViewById(R.id.chapterNamePreview)
        val toggle      : TextView = view.findViewById(R.id.chapterToggle)
        val inputNumber : EditText = view.findViewById(R.id.inputNumber)
        val inputName   : EditText = view.findViewById(R.id.inputName)
        val inputStory  : EditText = view.findViewById(R.id.inputStory)
        val btnDelete   : View     = view.findViewById(R.id.btnDelete)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_chapter_card, parent, false)
        return VH(view)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        val ch = chapters[position]

        // Remove all watchers before touching text fields
        holder.inputNumber.tag = null
        holder.inputName.tag   = null
        holder.inputStory.tag  = null

        (holder.inputNumber.getTag(R.id.inputNumber) as? TextWatcher)
            ?.let { holder.inputNumber.removeTextChangedListener(it) }
        (holder.inputName.getTag(R.id.inputName) as? TextWatcher)
            ?.let { holder.inputName.removeTextChangedListener(it) }
        (holder.inputStory.getTag(R.id.inputStory) as? TextWatcher)
            ?.let { holder.inputStory.removeTextChangedListener(it) }

        // Set values
        holder.badge.text        = "Ch. ${position + 1}"
        holder.namePreview.text  = ch.name.ifBlank { "Untitled Chapter" }
        holder.namePreview.alpha = if (ch.name.isBlank()) 0.4f else 1f
        holder.body.visibility   = if (ch.isExpanded) View.VISIBLE else View.GONE
        holder.toggle.rotation   = if (ch.isExpanded) 180f else 0f

        holder.inputNumber.setText(ch.number)
        holder.inputName.setText(ch.name)
        holder.inputStory.setText(ch.story)

        // Attach fresh watchers and store them in tags
        val numWatcher = watcher { ch.number = it }
        val nameWatcher = watcher {
            ch.name = it
            holder.namePreview.text  = it.ifBlank { "Untitled Chapter" }
            holder.namePreview.alpha = if (it.isBlank()) 0.4f else 1f
        }
        val storyWatcher = watcher { ch.story = it }

        holder.inputNumber.addTextChangedListener(numWatcher)
        holder.inputName.addTextChangedListener(nameWatcher)
        holder.inputStory.addTextChangedListener(storyWatcher)

        holder.inputNumber.setTag(R.id.inputNumber, numWatcher)
        holder.inputName.setTag(R.id.inputName, nameWatcher)
        holder.inputStory.setTag(R.id.inputStory, storyWatcher)

        // Expand/collapse
        holder.header.setOnClickListener {
            ch.isExpanded = !ch.isExpanded
            holder.body.visibility = if (ch.isExpanded) View.VISIBLE else View.GONE
            holder.toggle.animate()
                .rotation(if (ch.isExpanded) 180f else 0f)
                .setDuration(200)
                .start()
        }

        // Delete — use bindingAdapterPosition to avoid stale index
        holder.btnDelete.setOnClickListener {
            val pos = holder.bindingAdapterPosition
            if (pos != RecyclerView.NO_POSITION) onDelete(pos)
        }
    }

    override fun getItemCount() = chapters.size

    // Stable IDs = RecyclerView won't unnecessarily rebind unchanged items
    override fun getItemId(position: Int) = position.toLong()

    init { setHasStableIds(true) }

    private fun watcher(onChange: (String) -> Unit) = object : TextWatcher {
        override fun afterTextChanged(s: Editable?) { onChange(s?.toString() ?: "") }
        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
    }
}