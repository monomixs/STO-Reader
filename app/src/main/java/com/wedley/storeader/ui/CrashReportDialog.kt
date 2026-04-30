package com.wedley.storeader.ui

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import com.wedley.storeader.CrashHandler
import com.wedley.storeader.R

class CrashReportDialog : DialogFragment() {

    companion object {
        private const val ARG_LOG = "crash_log"

        fun newInstance(log: String): CrashReportDialog {
            return CrashReportDialog().apply {
                arguments = Bundle().apply { putString(ARG_LOG, log) }
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View = inflater.inflate(R.layout.dialog_crash_report, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val log = arguments?.getString(ARG_LOG) ?: "No log available"
        view.findViewById<TextView>(R.id.crashLog).text = log

        // Copy to clipboard
        view.findViewById<View>(R.id.btnCopyCrash).setOnClickListener {
            val clipboard = requireContext()
                .getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            clipboard.setPrimaryClip(ClipData.newPlainText("Crash Log", log))
            Toast.makeText(requireContext(), "Copied to clipboard", Toast.LENGTH_SHORT).show()
        }

        // Dismiss + clear the saved crash file so it doesn't show again
        view.findViewById<View>(R.id.btnDismissCrash).setOnClickListener {
            CrashHandler.clearLastCrash(requireContext())
            dismiss()
        }
    }

    override fun onStart() {
        super.onStart()
        dialog?.window?.apply {
            setBackgroundDrawableResource(android.R.color.transparent)
            setLayout(
                (resources.displayMetrics.widthPixels * 0.92).toInt(),
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
        }
    }

    // Prevent dismissing by tapping outside — user must tap Got it to clear the log
    override fun isCancelable() = false
}