package com.wedley.storeader.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import com.wedley.storeader.R

class ExportOptionsDialog : DialogFragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View = inflater.inflate(R.layout.dialog_export_options, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        view.findViewById<View>(R.id.btnCancelExport).setOnClickListener { dismiss() }
        
        view.findViewById<View>(R.id.btnExportSto).setOnClickListener {
            parentFragmentManager.setFragmentResult("export_format", Bundle().apply { putString("format", "sto") })
            dismiss()
        }
        
        view.findViewById<View>(R.id.btnExportTxt).setOnClickListener {
            parentFragmentManager.setFragmentResult("export_format", Bundle().apply { putString("format", "txt") })
            dismiss()
        }
    }

    override fun onStart() {
        super.onStart()
        dialog?.window?.apply {
            setBackgroundDrawableResource(android.R.color.transparent)
            setLayout(
                (resources.displayMetrics.widthPixels * 0.9).toInt(),
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
        }
    }
}