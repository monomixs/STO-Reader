package com.wedley.storeader.data

data class Chapter(
    var number: String = "",
    var name: String = "",
    var episodes: MutableList<Episode> = mutableListOf(),
    var isExpanded: Boolean = false
)