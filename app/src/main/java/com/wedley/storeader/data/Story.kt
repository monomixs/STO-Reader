package com.wedley.storeader.data

data class Story(
    var title: String = "",
    var chapters: MutableList<Chapter> = mutableListOf()
)