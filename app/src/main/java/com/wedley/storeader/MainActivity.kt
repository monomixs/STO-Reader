package com.wedley.storeader

import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.provider.OpenableColumns
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.wedley.storeader.data.StoParser
import com.wedley.storeader.ui.CrashReportDialog
import com.wedley.storeader.ui.HomeFragment
import com.wedley.storeader.ui.ReaderFragment
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainActivity : AppCompatActivity() {

    lateinit var viewModel: AppViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Install crash handler FIRST
        CrashHandler.install(applicationContext)

        viewModel = ViewModelProvider(this)[AppViewModel::class.java]

        WindowCompat.setDecorFitsSystemWindows(window, false)
        window.statusBarColor = Color.TRANSPARENT
        window.navigationBarColor = Color.TRANSPARENT
        WindowInsetsControllerCompat(window, window.decorView).apply {
            isAppearanceLightStatusBars = false
            isAppearanceLightNavigationBars = false
        }

        setContentView(R.layout.activity_main)

        if (savedInstanceState == null) {
            handleIntent(intent)
            checkForLastCrash()
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handleIntent(intent)
    }

    private fun handleIntent(intent: Intent) {
        val uri = intent.data

        if (intent.action == Intent.ACTION_VIEW && uri != null) {
            val fileName = getFileName(uri)
            if (fileName != null && !fileName.lowercase().endsWith(".sto")) {
                showFragment(HomeFragment(), animate = false)
                return
            }

            showFragment(HomeFragment(), animate = false)

            lifecycleScope.launch(Dispatchers.IO) {
                try {
                    val text = contentResolver
                        .openInputStream(uri)
                        ?.bufferedReader()
                        ?.readText() ?: ""

                    val story = StoParser.parse(text)

                    withContext(Dispatchers.Main) {
                        if (!isFinishing && !isDestroyed) {
                            viewModel.currentStory = story
                            viewModel.readingChapterIndex = -1
                            viewModel.openedFromFileManager = true
                            showFragment(ReaderFragment(), animate = true)
                        }
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        } else {
            showFragment(HomeFragment(), animate = false)
        }
    }

    private fun getFileName(uri: Uri): String? {
        var result: String? = null
        if (uri.scheme == "content") {
            val cursor = contentResolver.query(uri, null, null, null, null)
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

    private fun checkForLastCrash() {
        lifecycleScope.launch(Dispatchers.IO) {
            val log = CrashHandler.getLastCrash(applicationContext)
            if (log != null) {
                withContext(Dispatchers.Main) {
                    if (!isFinishing && !isDestroyed) {
                        supportFragmentManager.executePendingTransactions()
                        CrashReportDialog.newInstance(log)
                            .show(supportFragmentManager, "crash_report")
                    }
                }
            }
        }
    }

    fun showFragment(fragment: androidx.fragment.app.Fragment, animate: Boolean = true) {
        if (!isFinishing && !isDestroyed) {
            val transaction = supportFragmentManager.beginTransaction()
                .setReorderingAllowed(true)
            
            if (animate) {
                transaction.setCustomAnimations(
                    R.anim.slide_in_right, R.anim.slide_out_left,
                    R.anim.slide_in_left, R.anim.slide_out_right
                )
            }
            
            transaction.replace(R.id.container, fragment)
                .commit()
        }
    }
}