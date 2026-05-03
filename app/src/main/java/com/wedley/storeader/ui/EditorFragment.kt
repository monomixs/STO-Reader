package com.wedley.storeader.ui

import android.net.Uri
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.wedley.storeader.MainActivity
import com.wedley.storeader.R
import com.wedley.storeader.data.Chapter
import com.wedley.storeader.data.StoParser
import com.wedley.storeader.storage.RecentStoriesManager

class EditorFragment : Fragment() {

    private val appViewModel get() = (requireActivity() as MainActivity).viewModel
    private lateinit var chapterAdapter: ChapterAdapter
    private lateinit var recentManager: RecentStoriesManager
    private var exportFormat: String = "sto"

    private val saveFileLauncher = registerForActivityResult(
        ActivityResultContracts.CreateDocument("text/plain")
    ) { uri -> uri?.let { exportToUri(it) } }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View = inflater.inflate(R.layout.fragment_editor, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        recentManager = RecentStoriesManager(requireContext())
        val story = appViewModel.currentStory

        // Status bar inset
        val topBar = view.findViewById<View>(R.id.topBar)
        ViewCompat.setOnApplyWindowInsetsListener(topBar) { v, insets ->
            val bars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(v.paddingLeft, bars.top + dpToPx(14), v.paddingRight, v.paddingBottom)
            insets
        }

        // Nav bar inset
        val spacer = view.findViewById<View>(R.id.navBarSpacer)
        ViewCompat.setOnApplyWindowInsetsListener(spacer) { v, insets ->
            val bars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.layoutParams?.let { lp ->
                lp.height = bars.bottom + dpToPx(32)
                v.layoutParams = lp
            }
            insets
        }

        // Title
        val titleInput = view.findViewById<EditText>(R.id.storyTitle)
        titleInput.setText(story.title)
        titleInput.addTextChangedListener(watcher { story.title = it })

        // Author
        val authorInput = view.findViewById<EditText>(R.id.storyAuthor)
        authorInput.setText(story.author)
        authorInput.addTextChangedListener(watcher { story.author = it })

        // Genre
        val genreInput = view.findViewById<EditText>(R.id.storyGenre)
        genreInput.setText(story.genre)
        genreInput.addTextChangedListener(watcher { story.genre = it })

        // Description
        val descInput = view.findViewById<EditText>(R.id.storyDescription)
        descInput.setText(story.description)
        descInput.addTextChangedListener(watcher { story.description = it })

        // Chapters
        chapterAdapter = ChapterAdapter(story.chapters) { index ->
            if (index in story.chapters.indices) {
                story.chapters.removeAt(index)
                chapterAdapter.notifyItemRemoved(index)
                chapterAdapter.notifyItemRangeChanged(index, story.chapters.size)
                if (isAdded) Toast.makeText(requireContext(), "Chapter removed", Toast.LENGTH_SHORT).show()
            }
        }

        view.findViewById<RecyclerView>(R.id.chaptersList).apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = chapterAdapter
            isNestedScrollingEnabled = false
            itemAnimator = null
            setHasFixedSize(false)
        }

        view.findViewById<View>(R.id.btnAddChapter).setOnClickListener {
            story.chapters.add(Chapter(number = (story.chapters.size + 1).toString()))
            chapterAdapter.notifyItemInserted(story.chapters.size - 1)
        }

        // Delete story — show warning dialog
        parentFragmentManager.setFragmentResultListener(
            "delete_confirmed", viewLifecycleOwner
        ) { _, _ ->
            recentManager.delete(story.id, story.title)
            if (isAdded) {
                Toast.makeText(requireContext(), "Story deleted", Toast.LENGTH_SHORT).show()
                // Pop back to home
                parentFragmentManager.popBackStack(null,
                    androidx.fragment.app.FragmentManager.POP_BACK_STACK_INCLUSIVE)
                // (requireActivity() as MainActivity).showFragment(HomeFragment()) -- removed duplicate nav
            }
        }

        view.findViewById<View>(R.id.btnDeleteStory).setOnClickListener {
            if (isAdded) {
                DeleteWarningDialog().show(parentFragmentManager, "delete_warning")
            }
        }

        // Save
        view.findViewById<View>(R.id.btnSave).setOnClickListener {
            story.title = titleInput.text.toString()
            recentManager.save(story)
            if (isAdded) Toast.makeText(requireContext(), "Saved ✦", Toast.LENGTH_SHORT).show()
        }

        // Export
        parentFragmentManager.setFragmentResultListener("export_format", viewLifecycleOwner) { _, bundle ->
            val format = bundle.getString("format") ?: "sto"
            exportFormat = format
            val title = view.findViewById<EditText>(R.id.storyTitle).text.toString()
            val name = (if (title.isBlank()) "story" else title).replace(" ", "_") + (if (format == "sto") ".sto" else ".txt")
            saveFileLauncher.launch(name)
        }

        view.findViewById<View>(R.id.btnExport).setOnClickListener {
            if (isAdded) {
                ExportOptionsDialog().show(parentFragmentManager, "export_options")
            }
        }
    }

    private fun exportToUri(uri: Uri) {
        if (!isAdded) return
        try {
            val story = appViewModel.currentStory
            val content = if (exportFormat == "sto") {
                StoParser.serialize(story)
            } else {
                StoParser.toPlainText(story)
            }
            requireContext().contentResolver.openOutputStream(uri)?.use {
                it.write(content.toByteArray())
            }
            recentManager.save(story)
            Toast.makeText(requireContext(), "Exported", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            Toast.makeText(requireContext(), "Export failed", Toast.LENGTH_SHORT).show()
        }
    }

    private fun dpToPx(dp: Int) = (dp * resources.displayMetrics.density).toInt()

    private fun watcher(onChange: (String) -> Unit) = object : TextWatcher {
        override fun afterTextChanged(s: Editable?) { onChange(s?.toString() ?: "") }
        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
    }
}