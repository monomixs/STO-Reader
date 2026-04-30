package com.wedley.storeader.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.wedley.storeader.R

class NewStoryBottomSheet : BottomSheetDialogFragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View = inflater.inflate(R.layout.bottom_sheet_new_story, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val input = view.findViewById<EditText>(R.id.newTitleInput)

        view.findViewById<View>(R.id.btnCancel).setOnClickListener { dismiss() }

        view.findViewById<View>(R.id.btnCreate).setOnClickListener {
            val title = input.text.toString().trim().ifBlank { "Untitled Story" }
            val result = Bundle().apply { putString("title", title) }
            parentFragmentManager.setFragmentResult("new_story", result)
            dismiss()
        }
    }
}