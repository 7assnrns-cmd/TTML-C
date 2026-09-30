package com.example.domain.parser

import com.example.domain.model.AgentInfo
import com.example.domain.model.KaraokeAlignment
import com.example.domain.model.LyricLine
import com.example.domain.model.LyricToken
import com.example.domain.model.LyricsProject
import com.example.domain.model.SongPart
import java.util.Locale
import java.util.regex.Pattern

object LrcParser {

    private val LRC_TIMESTAMP_REGEX = Pattern.compile("\\[(\\d{1,2}):(\\d{2})(?:\\.(\\d{1,3}))?]")
    private val SYLLABLE_REGEX = Pattern.compile("<(\\d{1,2}):(\\d{2})(?:\\.(\\d{1,3}))?>([^<]+)")

    fun parse(content: String, title: String = "LRC Song"): LyricsProject {
        val lines = mutableListOf<LyricLine>()
        var artist = ""
        var album = ""
        var detectedTitle = title

        val rawLines = content.lines()
        for (rawLine in rawLines) {
            val trimmed = rawLine.trim()
            if (trimmed.startsWith("[ti:", ignoreCase = true)) {
                detectedTitle = trimmed.removePrefix("[ti:").removeSuffix("]").trim()
                continue
            }
            if (trimmed.startsWith("[ar:", ignoreCase = true)) {
                artist = trimmed.removePrefix("[ar:").removeSuffix("]").trim()
                continue
            }
            if (trimmed.startsWith("[al:", ignoreCase = true)) {
                album = trimmed.removePrefix("[al:").removeSuffix("]").trim()
                continue
            }

            val matcher = LRC_TIMESTAMP_REGEX.matcher(trimmed)
            if (matcher.find()) {
                val min = matcher.group(1)?.toLong() ?: 0L
                val sec = matcher.group(2)?.toLong() ?: 0L
                val msStr = matcher.group(3) ?: "0"
                val ms = when (msStr.length) {
                    1 -> msStr.toLong() * 100
                    2 -> msStr.toLong() * 10
                    else -> msStr.take(3).toLong()
                }
                val lineBegin = min * 60000 + sec * 1000 + ms
                val textAfter = trimmed.substring(matcher.end()).trim()

                // Check for enhanced syllable tags: <00:01.00>word <00:01.50>next
                val sylMatcher = SYLLABLE_REGEX.matcher(textAfter)
                val tokens = mutableListOf<LyricToken>()

                if (sylMatcher.find()) {
                    sylMatcher.reset()
                    val sylList = mutableListOf<Pair<Long, String>>()
                    while (sylMatcher.find()) {
                        val sMin = sylMatcher.group(1)?.toLong() ?: 0L
                        val sSec = sylMatcher.group(2)?.toLong() ?: 0L
                        val sMsStr = sylMatcher.group(3) ?: "0"
                        val sMs = when (sMsStr.length) {
                            1 -> sMsStr.toLong() * 100
                            2 -> sMsStr.toLong() * 10
                            else -> sMsStr.take(3).toLong()
                        }
                        val sylBegin = sMin * 60000 + sSec * 1000 + sMs
                        val sylText = sylMatcher.group(4)?.trim() ?: ""
                        sylList.add(sylBegin to sylText)
                    }

                    for (i in 0 until sylList.size) {
                        val curr = sylList[i]
                        val nextBegin = if (i + 1 < sylList.size) sylList[i + 1].first else curr.first + 600L
                        tokens.add(
                            LyricToken(
                                text = curr.second,
                                beginMs = curr.first,
                                endMs = nextBegin,
                                isBackground = curr.second.startsWith("(") && curr.second.endsWith(")"),
                                syllableIndex = i
                            )
                        )
                    }
                } else {
                    // Standard LRC line without per-word timestamps
                    // Do NOT invent word timings! Keep beginMs = 0L and endMs = 0L for individual words
                    val words = textAfter.split(Regex("\\s+")).filter { it.isNotBlank() }
                    for (i in words.indices) {
                        tokens.add(
                            LyricToken(
                                text = words[i],
                                beginMs = 0L,
                                endMs = 0L,
                                isBackground = words[i].startsWith("(") && words[i].endsWith(")"),
                                syllableIndex = i
                            )
                        )
                    }
                }

                val lineEnd = if (tokens.any { it.endMs > 0L }) tokens.maxOf { it.endMs } else lineBegin + 3000L
                lines.add(
                    LyricLine(
                        beginMs = lineBegin,
                        endMs = lineEnd,
                        tokens = tokens,
                        songPart = SongPart.VERSE
                    )
                )
            }
        }

        // Adjust line end times to match next line start without guessing per-word timings
        for (i in 0 until lines.size) {
            val current = lines[i]
            val nextBegin = if (i + 1 < lines.size) lines[i + 1].beginMs else current.beginMs + 3500L
            val correctedEnd = if (current.endMs > current.beginMs) current.endMs else nextBegin.coerceAtLeast(current.beginMs + 500L)

            lines[i] = current.copy(
                endMs = correctedEnd
            )
        }

        val totalDuration = (lines.maxOfOrNull { it.endMs } ?: 180000L) + 5000L
        return LyricsProject(
            title = detectedTitle,
            artist = artist,
            album = album,
            durationMs = totalDuration,
            lines = lines
        )
    }
}

object LrcExporter {
    fun export(project: LyricsProject): String {
        val sb = StringBuilder()
        sb.append("[ti:${project.title}]\n")
        if (project.artist.isNotBlank()) sb.append("[ar:${project.artist}]\n")
        if (project.album.isNotBlank()) sb.append("[al:${project.album}]\n")

        for (line in project.lines) {
            val min = (line.beginMs / 60000).toInt()
            val sec = ((line.beginMs % 60000) / 1000).toInt()
            val hundredths = ((line.beginMs % 1000) / 10).toInt()
            sb.append(String.format(Locale.US, "[%02d:%02d.%02d]", min, sec, hundredths))
            sb.append(line.fullText)
            sb.append("\n")
        }
        return sb.toString()
    }

    fun exportEnhanced(project: LyricsProject): String {
        val sb = StringBuilder()
        sb.append("[ti:${project.title}]\n")
        if (project.artist.isNotBlank()) sb.append("[ar:${project.artist}]\n")

        for (line in project.lines) {
            val min = (line.beginMs / 60000).toInt()
            val sec = ((line.beginMs % 60000) / 1000).toInt()
            val hundredths = ((line.beginMs % 1000) / 10).toInt()
            sb.append(String.format(Locale.US, "[%02d:%02d.%02d]", min, sec, hundredths))
            for (token in line.tokens) {
                val tMin = (token.beginMs / 60000).toInt()
                val tSec = ((token.beginMs % 60000) / 1000).toInt()
                val tHund = ((token.beginMs % 1000) / 10).toInt()
                sb.append(String.format(Locale.US, "<%02d:%02d.%02d>%s ", tMin, tSec, tHund, token.text))
            }
            sb.append("\n")
        }
        return sb.toString()
    }
}

object AutoParser {
    fun parse(content: String, defaultTitle: String = "Untitled Song"): LyricsProject {
        val trimmed = content.trim()
        return when {
            trimmed.startsWith("<") && (trimmed.contains("<tt") || trimmed.contains("<body")) -> {
                TTMLParser.parse(trimmed, defaultTitle)
            }
            trimmed.contains("[0") || trimmed.contains("[ti:") -> {
                LrcParser.parse(trimmed, defaultTitle)
            }
            else -> {
                // Plain text lyrics: split each line into tokens
                val lines = trimmed.lines().filter { it.isNotBlank() }
                val lyricLines = lines.mapIndexed { index, text ->
                    val words = text.trim().split(Regex("\\s+")).filter { it.isNotBlank() }
                    LyricLine(
                        beginMs = 0L,
                        endMs = 0L,
                        agentId = "v1",
                        songPart = SongPart.VERSE,
                        tokens = words.mapIndexed { wIdx, word ->
                            LyricToken(
                                text = word,
                                beginMs = 0L,
                                endMs = 0L,
                                isBackground = word.startsWith("(") && word.endsWith(")"),
                                syllableIndex = wIdx
                            )
                        }
                    )
                }
                LyricsProject(
                    title = defaultTitle,
                    lines = lyricLines
                )
            }
        }
    }
}
