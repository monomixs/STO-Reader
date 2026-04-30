package com.wedley.storeader.data

data class RecentStory(
    val title: String,
    val timestamp: Long,
    val storyJson: String,
    val chapterCount: Int = 0
)