package com.wedley.storeader.data

data class Story(
    var id: String = java.util.UUID.randomUUID().toString(),
    var title: String = "",
    var author: String = "",
    var genre: String = "",
    var description: String = "",
    var chapters: MutableList<Chapter> = mutableListOf()
)