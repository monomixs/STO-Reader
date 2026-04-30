package com.wedley.storeader.data

object StoParser {

    fun parse(text: String): Story {
        val story = Story()
        var current: Chapter? = null
        var storyMode = false

        for (raw in text.lines()) {
            val line = raw.trim()
            when {
                line.startsWith("title=") -> story.title = line.removePrefix("title=")
                line == "[chapter]" -> {
                    current?.let { story.chapters.add(it) }
                    current = Chapter()
                    storyMode = false
                }
                current != null -> when {
                    line.startsWith("number=") -> current.number = line.removePrefix("number=")
                    line.startsWith("name=")   -> current.name   = line.removePrefix("name=")
                    line == "[story]"           -> storyMode = true
                    storyMode                  -> current.story += raw + "\n"
                }
            }
        }
        current?.let { story.chapters.add(it) }
        return story
    }

    fun serialize(story: Story): String = buildString {
        append("[meta]\ntitle=${story.title}\n\n")
        story.chapters.forEach { ch ->
            append("[chapter]\nnumber=${ch.number}\nname=${ch.name}\n\n[story]\n${ch.story}\n\n")
        }
    }
}