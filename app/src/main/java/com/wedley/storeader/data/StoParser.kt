package com.wedley.storeader.data

object StoParser {

    fun parse(text: String): Story {
        val story = Story()
        var currentChapter: Chapter? = null
        var currentEpisode: Episode? = null
        var storyMode = false

        for (raw in text.lines()) {
            val line = raw.trim()
            
            // Handle tags first
            if (line == "[chapter]") {
                currentChapter = Chapter()
                story.chapters.add(currentChapter)
                currentEpisode = null
                storyMode = false
                continue
            }
            if (line == "[episode]") {
                if (currentChapter != null) {
                    currentEpisode = Episode()
                    currentChapter.episodes.add(currentEpisode)
                    storyMode = false
                }
                continue
            }
            if (line == "[story]") {
                storyMode = true
                continue
            }
            if (line == "[meta]") {
                storyMode = false
                continue
            }

            // Handle content
            if (storyMode) {
                currentEpisode?.let { it.content += raw + "\n" }
                continue
            }

            when {
                line.startsWith("id=") -> story.id = line.removePrefix("id=")
                line.startsWith("title=") -> story.title = line.removePrefix("title=")
                line.startsWith("author=") -> story.author = line.removePrefix("author=")
                line.startsWith("genre=") -> story.genre = line.removePrefix("genre=")
                line.startsWith("description=") -> story.description = line.removePrefix("description=")
                
                currentEpisode != null -> when {
                    line.startsWith("number=") -> currentEpisode.number = line.removePrefix("number=")
                    line.startsWith("name=")   -> currentEpisode.name   = line.removePrefix("name=")
                }
                
                currentChapter != null -> when {
                    line.startsWith("number=") -> currentChapter.number = line.removePrefix("number=")
                    line.startsWith("name=")   -> currentChapter.name   = line.removePrefix("name=")
                }
            }
        }
        return story
    }

    fun serialize(story: Story): String = buildString {
        append("[meta]\nid=${story.id}\ntitle=${story.title}\nauthor=${story.author}\ngenre=${story.genre}\ndescription=${story.description}\n\n")
        story.chapters.forEach { ch ->
            append("[chapter]\nnumber=${ch.number}\nname=${ch.name}\n\n")
            ch.episodes.forEach { ep ->
                append("[episode]\nnumber=${ep.number}\nname=${ep.name}\n\n[story]\n${ep.content}\n\n")
            }
        }
    }

    fun toPlainText(story: Story): String = buildString {
        append(story.title.ifBlank { "Untitled Story" }).append("\n")
        if (story.author.isNotBlank()) append("By ").append(story.author).append("\n")
        if (story.genre.isNotBlank()) append("Genre: ").append(story.genre).append("\n")
        if (story.description.isNotBlank()) append("\n").append(story.description).append("\n")
        
        append("\n" + "=".repeat(20) + "\n\n")
        
        story.chapters.forEach { ch ->
            append("Chapter ").append(ch.number)
            if (ch.name.isNotBlank()) append(": ").append(ch.name)
            append("\n\n")
            
            ch.episodes.forEach { ep ->
                if (ep.name.isNotBlank()) append("Episode: ").append(ep.name).append("\n\n")
                append(ep.content.trim()).append("\n\n")
            }
            append("-".repeat(10)).append("\n\n")
        }
    }
}