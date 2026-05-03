package com.wedley.storeader.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.wedley.storeader.R
import com.wedley.storeader.data.Episode

class EpisodeReaderAdapter(private val episodes: List<Episode>) :
    RecyclerView.Adapter<EpisodeReaderAdapter.VH>() {

    class VH(view: View) : RecyclerView.ViewHolder(view) {
        val header: TextView = view.findViewById(R.id.tvEpisodeHeader)
        val content: TextView = view.findViewById(R.id.tvEpisodeContent)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_episode_reader, parent, false)
        return VH(view)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        val ep = episodes[position]
        
        val epNum = ep.number.ifBlank { "" }
        val epName = ep.name.ifBlank { "" }
        
        val headerText = if (epName.isNotBlank() && epNum.isNotBlank()) "◈ Episode $epNum: $epName ◈"
        else if (epNum.isNotBlank()) "◈ Episode $epNum ◈"
        else if (epName.isNotBlank()) "◈ $epName ◈"
        else ""

        if (headerText.isNotBlank()) {
            holder.header.text = headerText
            holder.header.visibility = View.VISIBLE
        } else {
            holder.header.visibility = View.GONE
        }

        holder.content.text = ep.content.trim()
    }

    override fun getItemCount() = episodes.size
}