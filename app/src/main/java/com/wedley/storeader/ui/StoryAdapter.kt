package com.wedley.storeader.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.wedley.storeader.R
import com.wedley.storeader.data.RecentStory

class StoryAdapter(
    private var items: List<RecentStory>,
    private val onRead: (Int) -> Unit,
    private val onEdit: (Int) -> Unit
) : RecyclerView.Adapter<StoryAdapter.VH>() {

    class VH(view: View) : RecyclerView.ViewHolder(view) {
        val title        : TextView = view.findViewById(R.id.storyTitle)
        val meta         : TextView = view.findViewById(R.id.storyMeta)
        val chapterCount : TextView = view.findViewById(R.id.storyChapterCount)
        val btnRead      : View     = view.findViewById(R.id.btnRead)
        val btnEdit      : View     = view.findViewById(R.id.btnEdit)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_story_card, parent, false)
        return VH(view)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        val item = items[position]
        holder.title.text = item.title
        holder.meta.text  = timeAgo(item.timestamp)
        val chaptersText = when (item.chapterCount) {
            1    -> "1 chapter"
            else -> "${item.chapterCount} chapters"
        }
        val episodesText = when (item.episodeCount) {
            1    -> "1 episode"
            else -> "${item.episodeCount} episodes"
        }
        holder.chapterCount.text = "$chaptersText • $episodesText"

        holder.btnRead.setOnClickListener {
            val pos = holder.bindingAdapterPosition
            if (pos != RecyclerView.NO_POSITION) onRead(pos)
        }
        holder.btnEdit.setOnClickListener {
            val pos = holder.bindingAdapterPosition
            if (pos != RecyclerView.NO_POSITION) onEdit(pos)
        }
    }

    override fun getItemCount() = items.size

    fun updateList(newItems: List<RecentStory>) {
        items = newItems
        notifyDataSetChanged()
    }

    private fun timeAgo(ts: Long): String {
        val diff = System.currentTimeMillis() - ts
        return when {
            diff < 60_000L     -> "Just now"
            diff < 3_600_000L  -> "${diff / 60_000}m ago"
            diff < 86_400_000L -> "${diff / 3_600_000}h ago"
            else               -> "${diff / 86_400_000}d ago"
        }
    }
}