package com.wedley.storeader.ui

import android.net.Uri
import android.os.Bundle
import android.provider.OpenableColumns
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.wedley.storeader.MainActivity
import com.wedley.storeader.R
import com.wedley.storeader.data.RecentStory
import com.wedley.storeader.data.Story
import com.wedley.storeader.data.StoParser
import com.wedley.storeader.storage.RecentStoriesManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class HomeFragment : Fragment() {

    private val appViewModel get() = (requireActivity() as MainActivity).viewModel
    private var recentManager: RecentStoriesManager? = null
    private lateinit var storyAdapter: StoryAdapter

    private val openFileLauncher = registerForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri -> uri?.let { loadFile(it) } }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View = inflater.inflate(R.layout.fragment_home, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Init RecentStoriesManager on background thread — it touches disk
        CoroutineScope(Dispatchers.IO).launch {
            val manager = RecentStoriesManager(requireContext().applicationContext)
            withContext(Dispatchers.Main) {
                if (!isAdded) return@withContext
                recentManager = manager
                refreshRecentList() // load list only after manager is ready
            }
        }

        // Fragment result listener — registered once here, not in click
        parentFragmentManager.setFragmentResultListener(
            "new_story", viewLifecycleOwner
        ) { _, bundle ->
            val title = bundle.getString("title") ?: "Untitled Story"
            appViewModel.currentStory = Story(title = title)
            navigateToEditor()
        }

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
                lp.height = bars.bottom + dpToPx(20)
                v.layoutParams = lp
            }
            insets
        }

        view.findViewById<View>(R.id.cardNewStory).setOnClickListener {
            if (isAdded) NewStoryBottomSheet().show(parentFragmentManager, "new_story_sheet")
        }

        view.findViewById<View>(R.id.cardOpenFile).setOnClickListener {
            openFileLauncher.launch(arrayOf("*/*"))
        }

        storyAdapter = StoryAdapter(
            items = emptyList(),
            onRead = { index ->
                if (!isAdded) return@StoryAdapter
                recentManager?.getStory(index)?.let { story ->
                    appViewModel.currentStory = story
                    appViewModel.readingChapterIndex = -1
                    navigateToReader()
                }
            },
            onEdit = { index ->
                if (!isAdded) return@StoryAdapter
                recentManager?.getStory(index)?.let { story ->
                    appViewModel.currentStory = story
                    navigateToEditor()
                }
            }
        )

        view.findViewById<RecyclerView>(R.id.recentList).apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = storyAdapter
            isNestedScrollingEnabled = false
            itemAnimator = null
        }
    }

    override fun onResume() {
        super.onResume()
        // Only refresh if manager is already ready, otherwise
        // the init coroutine above will call refreshRecentList() itself
        if (recentManager != null) refreshRecentList()
    }

    private fun refreshRecentList() {
        if (!isAdded) return

        // Read SharedPreferences off the main thread
        CoroutineScope(Dispatchers.IO).launch {
            val recent: List<RecentStory> = try {
                recentManager?.getAll() ?: emptyList()
            } catch (e: Exception) {
                emptyList()
            }

            withContext(Dispatchers.Main) {
                if (!isAdded) return@withContext
                storyAdapter.updateList(recent)
                view?.findViewById<TextView>(R.id.emptyState)?.visibility =
                    if (recent.isEmpty()) View.VISIBLE else View.GONE
            }
        }
    }

    private fun loadFile(uri: Uri) {
        if (!isAdded) return

        // Check if file has .sto extension
        val fileName = getFileName(uri)
        if (fileName != null && !fileName.lowercase().endsWith(".sto")) {
            Toast.makeText(requireContext(), "Only .sto files are supported", Toast.LENGTH_SHORT).show()
            return
        }

        // Read file off the main thread
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val text = requireContext().applicationContext
                    .contentResolver
                    .openInputStream(uri)
                    ?.bufferedReader()
                    ?.readText() ?: return@launch

                val story = StoParser.parse(text)

                withContext(Dispatchers.Main) {
                    if (!isAdded) return@withContext
                    appViewModel.currentStory = story
                    appViewModel.readingChapterIndex = -1
                    appViewModel.openedFromFileManager = true
                    navigateToReader()
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    if (!isAdded) return@withContext
                    Toast.makeText(requireContext(), "Failed to read file", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun getFileName(uri: Uri): String? {
        var result: String? = null
        if (uri.scheme == "content") {
            val cursor = requireContext().contentResolver.query(uri, null, null, null, null)
            try {
                if (cursor != null && cursor.moveToFirst()) {
                    val index = cursor.getColumnIndex(android.provider.OpenableColumns.DISPLAY_NAME)
                    if (index != -1) result = cursor.getString(index)
                }
            } finally {
                cursor?.close()
            }
        }
        if (result == null) {
            result = uri.path
            val cut = result?.lastIndexOf('/') ?: -1
            if (cut != -1) {
                result = result?.substring(cut + 1)
            }
        }
        return result
    }

    private fun navigateToEditor() {
        if (!isAdded) return
        parentFragmentManager.beginTransaction()
            .setReorderingAllowed(true)
            .setCustomAnimations(
                R.anim.slide_in_right, R.anim.slide_out_left,
                R.anim.slide_in_left, R.anim.slide_out_right
            )
            .replace(R.id.container, EditorFragment(), "editor")
            .addToBackStack(null)
            .commitAllowingStateLoss()
    }

    private fun navigateToReader() {
        if (!isAdded) return
        parentFragmentManager.beginTransaction()
            .setReorderingAllowed(true)
            .setCustomAnimations(
                R.anim.slide_in_right, R.anim.slide_out_left,
                R.anim.slide_in_left, R.anim.slide_out_right
            )
            .replace(R.id.container, ReaderFragment(), "reader")
            .addToBackStack(null)
            .commitAllowingStateLoss()
    }

    private fun dpToPx(dp: Int) = (dp * resources.displayMetrics.density).toInt()
}