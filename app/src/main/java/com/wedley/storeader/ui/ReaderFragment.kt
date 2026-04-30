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
import com.wedley.storeader.MainActivity
import com.wedley.storeader.R
import com.wedley.storeader.storage.RecentStoriesManager

class ReaderFragment : Fragment() {

    private val appViewModel get() = (requireActivity() as MainActivity).viewModel

    private lateinit var tvStoryTitle   : TextView
    private lateinit var tvChapterLabel : TextView
    private lateinit var tvContent      : TextView
    private lateinit var btnNext        : TextView
    private lateinit var scrollView     : NestedScrollView
    private lateinit var recentManager  : RecentStoriesManager

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View = inflater.inflate(R.layout.fragment_reader, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        recentManager  = RecentStoriesManager(requireContext())
        tvStoryTitle   = view.findViewById(R.id.readerStoryTitle)
        tvChapterLabel = view.findViewById(R.id.readerChapterLabel)
        tvContent      = view.findViewById(R.id.readerContent)
        btnNext        = view.findViewById(R.id.btnNext)
        scrollView     = view.findViewById(R.id.scrollView)

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
            insets
        }

        view.findViewById<View>(R.id.btnBack).setOnClickListener {
            requireActivity().onBackPressedDispatcher.onBackPressed()
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
                Toast.makeText(requireContext(), "Saved to recent ✦", Toast.LENGTH_SHORT).show()
            }

            btnEditFile.setOnClickListener {
                if (!isAdded) return@setOnClickListener
                appViewModel.openedFromFileManager = false
                parentFragmentManager.beginTransaction()
                    .setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out,
                        android.R.anim.fade_in, android.R.anim.fade_out)
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

        renderChapter()
    }

    private fun renderChapter() {
        if (!isAdded) return
        val story   = appViewModel.currentStory
        val index   = appViewModel.readingChapterIndex
        val chapter = story.chapters.getOrNull(index)

        tvStoryTitle.text = story.title.ifBlank { "Untitled Story" }

        if (chapter == null) {
            tvChapterLabel.text = "No chapters yet"
            tvContent.text      = ""
            btnNext.visibility  = View.GONE
            return
        }

        val num  = chapter.number.ifBlank { (index + 1).toString() }
        val name = chapter.name.ifBlank { "" }
        tvChapterLabel.text = if (name.isNotBlank()) "Chapter $num — $name" else "Chapter $num"
        tvContent.text      = chapter.story.trim()
        btnNext.visibility  = if (index < story.chapters.size - 1) View.VISIBLE else View.GONE
    }

    private fun dpToPx(dp: Int) = (dp * resources.displayMetrics.density).toInt()
}