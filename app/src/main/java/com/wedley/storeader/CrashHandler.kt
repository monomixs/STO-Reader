package com.wedley.storeader

import android.content.Context
import java.io.File
import java.io.PrintWriter
import java.io.StringWriter

class CrashHandler(
    private val context: Context,
    private val defaultHandler: Thread.UncaughtExceptionHandler?
) : Thread.UncaughtExceptionHandler {

    override fun uncaughtException(thread: Thread, throwable: Throwable) {
        try {
            // Convert the full stack trace to a string
            val sw = StringWriter()
            throwable.printStackTrace(PrintWriter(sw))
            val stackTrace = sw.toString()

            val timestamp = java.text.SimpleDateFormat(
                "yyyy-MM-dd HH:mm:ss", java.util.Locale.getDefault()
            ).format(java.util.Date())

            val log = "Crashed at: $timestamp\n\n$stackTrace"

            // Save to internal storage — no permissions needed
            File(context.filesDir, CRASH_FILE).writeText(log)
        } catch (e: Exception) {
            // Don't crash the crash handler
        }

        // Let Android do its normal crash handling after we've saved the log
        defaultHandler?.uncaughtException(thread, throwable)
    }

    companion object {
        const val CRASH_FILE = "last_crash.txt"

        fun install(context: Context) {
            val default = Thread.getDefaultUncaughtExceptionHandler()
            Thread.setDefaultUncaughtExceptionHandler(CrashHandler(context, default))
        }

        fun getLastCrash(context: Context): String? {
            val file = File(context.filesDir, CRASH_FILE)
            return if (file.exists()) file.readText() else null
        }

        fun clearLastCrash(context: Context) {
            File(context.filesDir, CRASH_FILE).delete()
        }
    }
}