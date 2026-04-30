package com.wedley.storeader

import androidx.lifecycle.ViewModel
import com.wedley.storeader.data.Story

class AppViewModel : ViewModel() {
    var currentStory: Story = Story()
    var readingChapterIndex: Int = 0
    var openedFromFileManager: Boolean = false
}