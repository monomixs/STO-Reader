package com.wedley.storeader.storage

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.wedley.storeader.data.RecentStory
import com.wedley.storeader.data.Story

class RecentStoriesManager(context: Context) {

    private val prefs = context.getSharedPreferences("sto_prefs", Context.MODE_PRIVATE)
    private val gson = Gson()

    fun getAll(): List<RecentStory> {
        val json = prefs.getString("recent", "[]") ?: "[]"
        val type = object : TypeToken<List<RecentStory>>() {}.type
        return gson.fromJson(json, type)
    }

    fun save(story: Story) {
        val list = getAll().toMutableList()
        val entry = RecentStory(
            title = story.title.ifBlank { "Untitled" },
            timestamp = System.currentTimeMillis(),
            storyJson = gson.toJson(story),
            chapterCount = story.chapters.size
        )
        list.removeAll { it.title == entry.title }
        list.add(0, entry)
        prefs.edit()
            .putString("recent", gson.toJson(list.take(8)))
            .apply()
    }

    fun delete(title: String) {
        val list = getAll().toMutableList()
        list.removeAll { it.title == title }
        prefs.edit()
            .putString("recent", gson.toJson(list))
            .apply()
    }

    fun getStory(index: Int): Story? {
        return getAll().getOrNull(index)?.let {
            gson.fromJson(it.storyJson, Story::class.java)
        }
    }
}