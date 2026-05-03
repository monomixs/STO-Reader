package com.wedley.storeader.data

data class RecentStory(
    val id: String,
    val title: String,
    val timestamp: Long,
    val storyJson: String,
    val chapterCount: Int = 0,
    val episodeCount: Int = 0
)