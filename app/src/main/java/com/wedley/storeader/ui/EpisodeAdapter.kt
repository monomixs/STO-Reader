package com.wedley.storeader.ui

import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import androidx.recyclerview.widget.RecyclerView
import com.wedley.storeader.R
import com.wedley.storeader.data.Episode

class EpisodeAdapter(
    private val episodes: MutableList<Episode>,
    private val onDelete: (Int) -> Unit
) : RecyclerView.Adapter<EpisodeAdapter.VH>() {

    inner class VH(view: View) : RecyclerView.ViewHolder(view) {
        val inputNumber : EditText = view.findViewById(R.id.inputEpNumber)
        val inputName   : EditText = view.findViewById(R.id.inputEpName)
        val inputContent: EditText = view.findViewById(R.id.inputEpContent)
        val btnDelete   : View     = view.findViewById(R.id.btnDeleteEp)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_episode_edit, parent, false)
        return VH(view)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        val ep = episodes[position]

        holder.inputNumber.setText(ep.number)
        holder.inputName.setText(ep.name)
        holder.inputContent.setText(ep.content)

        // Fresh watchers
        holder.inputNumber.addTextChangedListener(watcher { ep.number = it })
        holder.inputName.addTextChangedListener(watcher { ep.name = it })
        holder.inputContent.addTextChangedListener(watcher { ep.content = it })

        holder.btnDelete.setOnClickListener {
            val pos = holder.bindingAdapterPosition
            if (pos != RecyclerView.NO_POSITION) onDelete(pos)
        }
    }

    override fun getItemCount() = episodes.size

    private fun watcher(onChange: (String) -> Unit) = object : TextWatcher {
        override fun afterTextChanged(s: Editable?) { onChange(s?.toString() ?: "") }
        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
    }
}