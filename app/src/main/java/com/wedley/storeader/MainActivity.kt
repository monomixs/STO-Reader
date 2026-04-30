package com.wedley.storeader

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
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
            showFragment(HomeFragment())

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
                            viewModel.readingChapterIndex = 0
                            viewModel.openedFromFileManager = true
                            showFragment(ReaderFragment())
                        }
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                    e.printStackTrace()
                }
            }
        } else {
            showFragment(HomeFragment())
        }
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

    fun showFragment(fragment: androidx.fragment.app.Fragment) {
        if (!isFinishing && !isDestroyed) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.container, fragment)
                .commit() // removed commitAllowingStateLoss
        }
    }
}