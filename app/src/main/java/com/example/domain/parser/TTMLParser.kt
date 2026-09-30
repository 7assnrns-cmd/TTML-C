package com.example.domain.parser

import com.example.domain.model.AgentInfo
import com.example.domain.model.KaraokeAlignment
import com.example.domain.model.LyricLine
import com.example.domain.model.LyricToken
import com.example.domain.model.LyricsProject
import com.example.domain.model.SongPart
import org.xmlpull.v1.XmlPullParser
import java.io.StringReader

object TTMLParser {

    fun parseTime(timeStr: String?): Long {
        if (timeStr.isNullOrBlank()) return 0L
        val clean = timeStr.trim().removeSuffix("s")
        return try {
            if (clean.contains(":")) {
                val parts = clean.split(":")
                if (parts.size == 2) {
                    val minutes = parts[0].toLong()
                    val seconds = parts[1].toDouble()
                    (minutes * 60000 + seconds * 1000).toLong()
                } else if (parts.size == 3) {
                    val hours = parts[0].toLong()
                    val minutes = parts[1].toLong()
                    val seconds = parts[2].toDouble()
                    (hours * 3600000 + minutes * 60000 + seconds * 1000).toLong()
                } else {
                    0L
                }
            } else {
                (clean.toDouble() * 1000).toLong()
            }
        } catch (_: Exception) {
            0L
        }
    }

    fun parse(xmlContent: String, defaultTitle: String = "Imported TTML"): LyricsProject {
        val factory = org.xmlpull.v1.XmlPullParserFactory.newInstance()
        factory.isNamespaceAware = true
        val parser = factory.newPullParser()
        parser.setInput(StringReader(xmlContent))

        val agents = mutableListOf<AgentInfo>()
        val lines = mutableListOf<LyricLine>()

        var currentAgentId: String? = null
        var currentAgentType = "person"
        var currentAgentName = ""
        var insideAgent = false
        var insideAgentName = false

        var currentSongPart = SongPart.VERSE
        var currentLineBegin = 0L
        var currentLineEnd = 0L
        var currentLineAgent = "v1"
        var currentLineKey = ""
        var currentLineTokens = mutableListOf<LyricToken>()
        var insideParagraph = false

        var insideBgSpan = false
        var currentSpanBegin = 0L
        var currentSpanEnd = 0L
        var currentSpanText = StringBuilder()
        var insideWordSpan = false

        var language = "en"
        var timingMode = "Word"
        var durationMs = 180000L

        var eventType = parser.eventType
        while (eventType != XmlPullParser.END_DOCUMENT) {
            when (eventType) {
                XmlPullParser.START_TAG -> {
                    val tagName = parser.name
                    when {
                        tagName.equals("tt", ignoreCase = true) -> {
                            val langAttr = getAttribute(parser, "xml:lang") ?: getAttribute(parser, "lang")
                            if (!langAttr.isNullOrBlank()) language = langAttr
                            val timingAttr = getAttribute(parser, "itunes:timing") ?: getAttribute(parser, "timing")
                            if (!timingAttr.isNullOrBlank()) timingMode = timingAttr
                        }
                        tagName.equals("body", ignoreCase = true) -> {
                            val durAttr = getAttribute(parser, "dur")
                            if (!durAttr.isNullOrBlank()) {
                                durationMs = parseTime(durAttr)
                            }
                        }
                        tagName.equals("agent", ignoreCase = true) -> {
                            insideAgent = true
                            currentAgentId = getAttribute(parser, "xml:id") ?: getAttribute(parser, "id") ?: "v${agents.size + 1}"
                            currentAgentType = getAttribute(parser, "type") ?: "person"
                            currentAgentName = ""
                        }
                        tagName.equals("name", ignoreCase = true) && insideAgent -> {
                            insideAgentName = true
                        }
                        tagName.equals("div", ignoreCase = true) -> {
                            val partAttr = getAttribute(parser, "itunes:songPart") ?: getAttribute(parser, "songPart")
                            currentSongPart = SongPart.fromTag(partAttr)
                        }
                        tagName.equals("p", ignoreCase = true) -> {
                            insideParagraph = true
                            currentLineBegin = parseTime(getAttribute(parser, "begin"))
                            currentLineEnd = parseTime(getAttribute(parser, "end"))
                            currentLineAgent = getAttribute(parser, "ttm:agent") ?: getAttribute(parser, "agent") ?: (agents.firstOrNull()?.id ?: "v1")
                            currentLineKey = getAttribute(parser, "itunes:key") ?: getAttribute(parser, "key") ?: ""
                            currentLineTokens = mutableListOf()
                        }
                        tagName.equals("span", ignoreCase = true) -> {
                            val role = getAttribute(parser, "ttm:role") ?: getAttribute(parser, "role")
                            if (role == "x-bg") {
                                insideBgSpan = true
                            } else {
                                insideWordSpan = true
                                currentSpanBegin = parseTime(getAttribute(parser, "begin"))
                                currentSpanEnd = parseTime(getAttribute(parser, "end"))
                                currentSpanText = StringBuilder()
                            }
                        }
                    }
                }
                XmlPullParser.TEXT -> {
                    val text = parser.text
                    if (insideAgentName) {
                        currentAgentName += text.trim()
                    } else if (insideWordSpan) {
                        currentSpanText.append(text)
                    } else if (insideParagraph && !insideWordSpan && text.isNotBlank()) {
                        // Plain text directly under <p>
                        val raw = text.trim()
                        if (raw.isNotEmpty()) {
                            currentLineTokens.add(
                                LyricToken(
                                    text = raw,
                                    beginMs = currentLineBegin,
                                    endMs = currentLineEnd,
                                    isBackground = insideBgSpan
                                )
                            )
                        }
                    }
                }
                XmlPullParser.END_TAG -> {
                    val tagName = parser.name
                    when {
                        tagName.equals("name", ignoreCase = true) -> {
                            insideAgentName = false
                        }
                        tagName.equals("agent", ignoreCase = true) -> {
                            insideAgent = false
                            val id = currentAgentId ?: "v${agents.size + 1}"
                            val alignment = when {
                                agents.isEmpty() -> KaraokeAlignment.START
                                currentAgentType.equals("group", ignoreCase = true) -> KaraokeAlignment.CENTER
                                else -> KaraokeAlignment.END
                            }
                            val colors = listOf("#FA2D48", "#00D2FF", "#A855F7", "#10B981", "#F59E0B")
                            val color = colors[agents.size % colors.size]
                            agents.add(
                                AgentInfo(
                                    id = id,
                                    name = if (currentAgentName.isNotBlank()) currentAgentName else "Singer ${agents.size + 1}",
                                    type = currentAgentType,
                                    alignment = alignment,
                                    colorHex = color
                                )
                            )
                        }
                        tagName.equals("span", ignoreCase = true) -> {
                            if (insideWordSpan) {
                                insideWordSpan = false
                                val wordText = currentSpanText.toString().trim()
                                if (wordText.isNotEmpty()) {
                                    currentLineTokens.add(
                                        LyricToken(
                                            text = wordText,
                                            beginMs = currentSpanBegin,
                                            endMs = currentSpanEnd,
                                            isBackground = insideBgSpan,
                                            syllableIndex = currentLineTokens.size
                                        )
                                    )
                                }
                            } else if (insideBgSpan) {
                                insideBgSpan = false
                            }
                        }
                        tagName.equals("p", ignoreCase = true) -> {
                            insideParagraph = false
                            if (currentLineTokens.isNotEmpty()) {
                                val minToken = currentLineTokens.minOfOrNull { it.beginMs } ?: currentLineBegin
                                val maxToken = currentLineTokens.maxOfOrNull { it.endMs } ?: currentLineEnd
                                lines.add(
                                    LyricLine(
                                        beginMs = if (currentLineBegin > 0L) currentLineBegin else minToken,
                                        endMs = if (currentLineEnd > 0L) currentLineEnd else maxToken,
                                        agentId = currentLineAgent,
                                        songPart = currentSongPart,
                                        tokens = currentLineTokens.toList(),
                                        key = currentLineKey
                                    )
                                )
                            }
                        }
                    }
                }
            }
            eventType = parser.next()
        }

        // Ensure default agents if none were parsed
        val finalAgents = if (agents.isEmpty()) {
            listOf(
                AgentInfo(id = "v1", name = "Lead Vocal", type = "person", alignment = KaraokeAlignment.START, colorHex = "#FA2D48"),
                AgentInfo(id = "v2", name = "Secondary Vocal", type = "person", alignment = KaraokeAlignment.END, colorHex = "#00D2FF")
            )
        } else {
            agents
        }

        val maxEndMs = lines.maxOfOrNull { it.endMs } ?: durationMs
        return LyricsProject(
            title = defaultTitle,
            durationMs = if (maxEndMs > durationMs) maxEndMs + 5000L else durationMs,
            language = language,
            timingMode = timingMode,
            agents = finalAgents,
            lines = lines
        )
    }

    private fun getAttribute(parser: XmlPullParser, attributeName: String): String? {
        // Search direct or without namespace prefix
        for (i in 0 until parser.attributeCount) {
            val name = parser.getAttributeName(i)
            val prefix = parser.getAttributePrefix(i)
            val fullName = if (prefix != null) "$prefix:$name" else name
            if (fullName.equals(attributeName, ignoreCase = true) || name.equals(attributeName, ignoreCase = true)) {
                return parser.getAttributeValue(i)
            }
        }
        return null
    }
}
