package com.wedley.storeader.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.widget.NestedScrollView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.wedley.storeader.MainActivity
import com.wedley.storeader.R
import com.wedley.storeader.storage.RecentStoriesManager

class ReaderFragment : Fragment() {

    private val appViewModel get() = (requireActivity() as MainActivity).viewModel

    private lateinit var mainColumn     : View
    private lateinit var coverLayout    : View
    private lateinit var tvStoryTitle   : TextView
    private lateinit var tvChapterLabel : TextView
    private lateinit var rvEpisodes     : RecyclerView
    private lateinit var btnNext        : TextView
    private lateinit var btnPrev        : TextView
    private lateinit var scrollView     : NestedScrollView
    private lateinit var recentManager  : RecentStoriesManager

    // Cover views
    private lateinit var coverTitle      : TextView
    private lateinit var coverAuthor     : TextView
    private lateinit var coverGenre      : TextView
    private lateinit var coverChapters   : TextView
    private lateinit var coverEpisodes   : TextView
    private lateinit var coverDescription: TextView
    private lateinit var btnStartReading : View

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View = inflater.inflate(R.layout.fragment_reader, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        recentManager  = RecentStoriesManager(requireContext())
        mainColumn     = view.findViewById(R.id.mainColumn)
        coverLayout    = view.findViewById(R.id.coverLayout)
        tvStoryTitle   = view.findViewById(R.id.readerStoryTitle)
        tvChapterLabel = view.findViewById(R.id.readerChapterLabel)
        rvEpisodes     = view.findViewById(R.id.rvEpisodes)
        btnNext        = view.findViewById(R.id.btnNext)
        btnPrev        = view.findViewById(R.id.btnPrev)
        scrollView     = view.findViewById(R.id.scrollView)

        rvEpisodes.layoutManager = LinearLayoutManager(requireContext())

        // Cover views
        coverTitle       = view.findViewById(R.id.coverTitle)
        coverAuthor      = view.findViewById(R.id.coverAuthor)
        coverGenre       = view.findViewById(R.id.coverGenre)
        coverChapters    = view.findViewById(R.id.coverChapters)
        coverEpisodes    = view.findViewById(R.id.coverEpisodes)
        coverDescription = view.findViewById(R.id.coverDescription)
        btnStartReading  = view.findViewById(R.id.btnStartReading)

        // Status bar inset
        val topBar = view.findViewById<View>(R.id.topBar)
        ViewCompat.setOnApplyWindowInsetsListener(topBar) { v, insets ->
            val bars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(v.paddingLeft, bars.top + dpToPx(14), v.paddingRight, v.paddingBottom)
            insets
        }

        // Nav bar inset
        ViewCompat.setOnApplyWindowInsetsListener(view.findViewById(R.id.navBarSpacer)) { v, insets ->
            val bars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.layoutParams?.let { lp ->
                lp.height = bars.bottom
                v.layoutParams = lp
            }
            btnNext.layoutParams?.let { lp ->
                (lp as? ViewGroup.MarginLayoutParams)?.bottomMargin = bars.bottom + dpToPx(20)
                btnNext.layoutParams = lp
            }
            btnPrev.layoutParams?.let { lp ->
                (lp as? ViewGroup.MarginLayoutParams)?.bottomMargin = bars.bottom + dpToPx(20)
                btnPrev.layoutParams = lp
            }
            insets
        }

        // Show save/edit only if opened from file manager
        val fromFileManager = appViewModel.openedFromFileManager
        val btnSaveToRecent = view.findViewById<TextView>(R.id.btnSaveToRecent)
        val btnEditFile     = view.findViewById<TextView>(R.id.btnEditFile)

        if (fromFileManager) {
            btnSaveToRecent.visibility = View.VISIBLE
            btnEditFile.visibility     = View.VISIBLE

            btnSaveToRecent.setOnClickListener {
                if (!isAdded) return@setOnClickListener
                recentManager.save(appViewModel.currentStory)
                appViewModel.openedFromFileManager = false
                btnSaveToRecent.visibility = View.GONE
                btnEditFile.visibility     = View.GONE
                Toast.makeText(requireContext(), "Saved to recent", Toast.LENGTH_SHORT).show()
            }

            btnEditFile.setOnClickListener {
                if (!isAdded) return@setOnClickListener
                appViewModel.openedFromFileManager = true
                parentFragmentManager.beginTransaction()
                    .setReorderingAllowed(true)
                    .setCustomAnimations(
                        R.anim.slide_in_right, R.anim.slide_out_left,
                        R.anim.slide_in_left, R.anim.slide_out_right
                    )
                    .replace(R.id.container, EditorFragment())
                    .addToBackStack(null)
                    .commitAllowingStateLoss()
            }
        }

        btnNext.setOnClickListener {
            val story = appViewModel.currentStory
            if (appViewModel.readingChapterIndex < story.chapters.size - 1) {
                appViewModel.readingChapterIndex++
                renderChapter()
                scrollView.smoothScrollTo(0, 0)
            }
        }

        btnPrev.setOnClickListener {
            if (appViewModel.readingChapterIndex > -1) {
                appViewModel.readingChapterIndex--
                renderChapter()
                scrollView.smoothScrollTo(0, 0)
            }
        }

        btnStartReading.setOnClickListener {
            if (appViewModel.currentStory.chapters.isNotEmpty()) {
                appViewModel.readingChapterIndex = 0
                renderChapter()
            }
        }

        renderChapter()
    }

    private fun renderChapter() {
        if (!isAdded) return
        val story   = appViewModel.currentStory
        val index   = appViewModel.readingChapterIndex
        
        if (index == -1) {
            // SHOW COVER
            coverLayout.visibility = View.VISIBLE
            mainColumn.visibility  = View.GONE
            btnNext.visibility     = View.GONE
            btnPrev.visibility     = View.GONE

            coverTitle.text     = story.title.ifBlank { "Untitled Story" }
            coverAuthor.text    = if (story.author.isNotBlank()) "Author: ${story.author}" else "Author: Unknown"
            coverGenre.text     = if (story.genre.isNotBlank()) "Genre: ${story.genre}" else "Genre: Unknown"
            coverChapters.text  = "Chapters: ${story.chapters.size}"
            coverEpisodes.text  = "Episodes: ${story.chapters.sumOf { it.episodes.size }}"
            
            if (story.description.isNotBlank()) {
                coverDescription.text = story.description
                coverDescription.visibility = View.VISIBLE
            } else {
                coverDescription.visibility = View.GONE
            }
            return
        }

        // SHOW READING CONTENT
        coverLayout.visibility = View.GONE
        mainColumn.visibility  = View.VISIBLE

        tvStoryTitle.text = story.title.ifBlank { "Untitled Story" }

        val chapter = story.chapters.getOrNull(index)

        if (chapter == null) {
            tvChapterLabel.text = "No chapters yet"
            rvEpisodes.adapter  = EpisodeReaderAdapter(emptyList())
            btnNext.visibility  = View.GONE
            btnPrev.visibility  = View.GONE
            return
        }

        val num  = chapter.number.ifBlank { (index + 1).toString() }
        val name = chapter.name.ifBlank { "" }
        tvChapterLabel.text = if (name.isNotBlank()) "Chapter $num — $name" else "Chapter $num"

        rvEpisodes.adapter = EpisodeReaderAdapter(chapter.episodes)
        
        btnNext.text = "Next"
        btnNext.visibility  = if (index < story.chapters.size - 1) View.VISIBLE else View.GONE
        btnPrev.visibility  = if (index >= 1) View.VISIBLE else View.GONE
    }

    private fun dpToPx(dp: Int) = (dp * resources.displayMetrics.density).toInt()
}