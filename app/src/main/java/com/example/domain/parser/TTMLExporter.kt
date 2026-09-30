package com.example.domain.parser

import com.example.domain.model.LyricsProject
import com.example.domain.model.SongPart
import java.util.Locale

object TTMLExporter {

    fun formatTime(timeMs: Long): String {
        val totalSeconds = timeMs / 1000.0
        val minutes = (timeMs / 60000).toInt()
        val remainingSeconds = (timeMs % 60000) / 1000.0
        return if (minutes > 0) {
            String.format(Locale.US, "%d:%06.3f", minutes, remainingSeconds)
        } else {
            String.format(Locale.US, "%.3f", totalSeconds)
        }
    }

    fun export(project: LyricsProject): String {
        val sb = StringBuilder()
        sb.append("""<?xml version="1.0" encoding="UTF-8"?>""").append("\n")
        sb.append("""<tt xmlns="http://www.w3.org/ns/ttml"""").append("\n")
        sb.append("""    xmlns:itunes="http://music.apple.com/lyric-ttml-internal"""").append("\n")
        sb.append("""    xmlns:ttm="http://www.w3.org/ns/ttml#metadata"""").append("\n")
        sb.append("""    itunes:timing="${project.timingMode}" xml:lang="${project.language}">""").append("\n")

        // Head and metadata
        sb.append("  <head>\n")
        sb.append("    <metadata>\n")
        for (agent in project.agents) {
            if (agent.name.isNotBlank()) {
                sb.append("""      <ttm:agent type="${agent.type}" xml:id="${agent.id}">""").append("\n")
                sb.append("""        <ttm:name type="full">${escapeXml(agent.name)}</ttm:name>""").append("\n")
                sb.append("      </ttm:agent>\n")
            } else {
                sb.append("""      <ttm:agent type="${agent.type}" xml:id="${agent.id}"/>""").append("\n")
            }
        }
        sb.append("    </metadata>\n")
        sb.append("  </head>\n")

        // Body
        val defaultAgent = project.agents.firstOrNull()?.id ?: "v1"
        sb.append("""  <body dur="${formatTime(project.durationMs)}" ttm:agent="$defaultAgent">""").append("\n")

        // Group lines by songPart
        val linesByPart = mutableListOf<Pair<SongPart, MutableList<com.example.domain.model.LyricLine>>>()
        var currentPart: SongPart? = null
        var currentList = mutableListOf<com.example.domain.model.LyricLine>()

        for (line in project.lines) {
            if (line.songPart != currentPart) {
                if (currentList.isNotEmpty() && currentPart != null) {
                    linesByPart.add(currentPart to currentList)
                    currentList = mutableListOf()
                }
                currentPart = line.songPart
            }
            currentList.add(line)
        }
        if (currentList.isNotEmpty() && currentPart != null) {
            linesByPart.add(currentPart to currentList)
        }

        if (linesByPart.isEmpty()) {
            // Empty body div
            sb.append("""    <div begin="0.000" end="${formatTime(project.durationMs)}" itunes:songPart="Verse"/>""").append("\n")
        } else {
            for ((part, linesInPart) in linesByPart) {
                val divBegin = linesInPart.minOfOrNull { it.beginMs } ?: 0L
                val divEnd = linesInPart.maxOfOrNull { it.endMs } ?: project.durationMs
                sb.append("""    <div begin="${formatTime(divBegin)}" end="${formatTime(divEnd)}" itunes:songPart="${part.tag}">""").append("\n")

                for ((idx, line) in linesInPart.withIndex()) {
                    val key = if (line.key.isNotBlank()) line.key else "L${idx + 1}"
                    sb.append("""      <p begin="${formatTime(line.beginMs)}" end="${formatTime(line.endMs)}" itunes:key="$key" ttm:agent="${line.agentId}"""")
                    if (!line.translation.isNullOrBlank()) {
                        sb.append(""" ttm:role="x-translation"""")
                    }
                    sb.append(">\n")

                    // Separate normal tokens and background tokens
                    val normalTokens = line.tokens.filter { !it.isBackground }
                    val bgTokens = line.tokens.filter { it.isBackground }

                    for (token in normalTokens) {
                        sb.append("""        <span begin="${formatTime(token.beginMs)}" end="${formatTime(token.endMs)}">${escapeXml(token.text)}</span>""").append("\n")
                    }

                    if (bgTokens.isNotEmpty()) {
                        sb.append("""        <span ttm:role="x-bg">""").append("\n")
                        for (bgToken in bgTokens) {
                            sb.append("""          <span begin="${formatTime(bgToken.beginMs)}" end="${formatTime(bgToken.endMs)}">${escapeXml(bgToken.text)}</span>""").append("\n")
                        }
                        sb.append("""        </span>""").append("\n")
                    }

                    sb.append("      </p>\n")
                }
                sb.append("    </div>\n")
            }
        }

        sb.append("  </body>\n")
        sb.append("</tt>")
        return sb.toString()
    }

    private fun escapeXml(text: String): String {
        return text.replace("&", "&amp;")
            .replace("<", "&lt;")
            .replace(">", "&gt;")
            .replace("\"", "&quot;")
            .replace("'", "&apos;")
    }
}
