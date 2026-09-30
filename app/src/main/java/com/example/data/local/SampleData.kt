package com.example.data.local

import com.example.domain.model.AgentInfo
import com.example.domain.model.KaraokeAlignment
import com.example.domain.model.LyricLine
import com.example.domain.model.LyricToken
import com.example.domain.model.LyricsProject
import com.example.domain.model.SongPart

object SampleData {

    fun getSampleProjects(): List<LyricsProject> {
        return listOf(createPumpItProject(), createBlindingLightsProject(), createArabicSongProject())
    }

    private fun createPumpItProject(): LyricsProject {
        val agents = listOf(
            AgentInfo(id = "v1", name = "The Black Eyed Peas", type = "person", alignment = KaraokeAlignment.START, colorHex = "#FA2D48"),
            AgentInfo(id = "v2", name = "Fergie", type = "person", alignment = KaraokeAlignment.END, colorHex = "#00D2FF"),
            AgentInfo(id = "v3", name = "Group Vocals", type = "group", alignment = KaraokeAlignment.CENTER, colorHex = "#A855F7")
        )

        val lines = listOf(
            // Intro
            LyricLine(
                beginMs = 9890L,
                endMs = 10461L,
                agentId = "v3",
                songPart = SongPart.INTRO,
                key = "L1",
                tokens = listOf(
                    LyricToken(text = "Pump", beginMs = 9890L, endMs = 10188L, syllableIndex = 0),
                    LyricToken(text = "it", beginMs = 10188L, endMs = 10461L, syllableIndex = 1)
                )
            ),
            LyricLine(
                beginMs = 15902L,
                endMs = 17660L,
                agentId = "v3",
                songPart = SongPart.INTRO,
                key = "L2",
                tokens = listOf(
                    LyricToken(text = "And", beginMs = 15902L, endMs = 16075L, syllableIndex = 0),
                    LyricToken(text = "pump", beginMs = 16075L, endMs = 16372L, syllableIndex = 1),
                    LyricToken(text = "it", beginMs = 16372L, endMs = 16564L, syllableIndex = 2),
                    LyricToken(text = "(Loud", beginMs = 16732L, endMs = 17196L, isBackground = true, syllableIndex = 3),
                    LyricToken(text = "er)", beginMs = 17196L, endMs = 17660L, isBackground = true, syllableIndex = 4)
                )
            ),
            // Verse 1
            LyricLine(
                beginMs = 21000L,
                endMs = 24500L,
                agentId = "v1",
                songPart = SongPart.VERSE,
                key = "L3",
                tokens = listOf(
                    LyricToken(text = "Ha-ha-ha,", beginMs = 21000L, endMs = 21800L, syllableIndex = 0),
                    LyricToken(text = "turn", beginMs = 21800L, endMs = 22200L, syllableIndex = 1),
                    LyricToken(text = "up", beginMs = 22200L, endMs = 22600L, syllableIndex = 2),
                    LyricToken(text = "the", beginMs = 22600L, endMs = 22900L, syllableIndex = 3),
                    LyricToken(text = "radio!", beginMs = 22900L, endMs = 24500L, syllableIndex = 4)
                )
            ),
            LyricLine(
                beginMs = 25000L,
                endMs = 28200L,
                agentId = "v2",
                songPart = SongPart.VERSE,
                key = "L4",
                tokens = listOf(
                    LyricToken(text = "Blast", beginMs = 25000L, endMs = 25500L, syllableIndex = 0),
                    LyricToken(text = "your", beginMs = 25500L, endMs = 25800L, syllableIndex = 1),
                    LyricToken(text = "stereo", beginMs = 25800L, endMs = 26500L, syllableIndex = 2),
                    LyricToken(text = "right", beginMs = 26500L, endMs = 27000L, syllableIndex = 3),
                    LyricToken(text = "now!", beginMs = 27000L, endMs = 28200L, syllableIndex = 4)
                )
            ),
            // Chorus
            LyricLine(
                beginMs = 29000L,
                endMs = 33000L,
                agentId = "v3",
                songPart = SongPart.CHORUS,
                key = "L5",
                tokens = listOf(
                    LyricToken(text = "Pump", beginMs = 29000L, endMs = 29600L, syllableIndex = 0),
                    LyricToken(text = "it,", beginMs = 29600L, endMs = 30100L, syllableIndex = 1),
                    LyricToken(text = "louder!", beginMs = 30100L, endMs = 31200L, syllableIndex = 2),
                    LyricToken(text = "Pump", beginMs = 31200L, endMs = 31800L, syllableIndex = 3),
                    LyricToken(text = "it!", beginMs = 31800L, endMs = 33000L, syllableIndex = 4)
                )
            )
        )

        return LyricsProject(
            id = "sample_pump_it",
            title = "Pump It",
            artist = "The Black Eyed Peas",
            album = "Monkey Business",
            durationMs = 213067L, // 3:33.067
            language = "en",
            timingMode = "Word",
            agents = agents,
            lines = lines
        )
    }

    private fun createBlindingLightsProject(): LyricsProject {
        val agents = listOf(
            AgentInfo(id = "v1", name = "The Weeknd", type = "person", alignment = KaraokeAlignment.START, colorHex = "#FA2D48"),
            AgentInfo(id = "v2", name = "Backing Echo", type = "person", alignment = KaraokeAlignment.END, colorHex = "#10B981")
        )

        val lines = listOf(
            LyricLine(
                beginMs = 26000L,
                endMs = 31500L,
                agentId = "v1",
                songPart = SongPart.VERSE,
                tokens = listOf(
                    LyricToken(text = "I've", beginMs = 26000L, endMs = 26400L),
                    LyricToken(text = "been", beginMs = 26400L, endMs = 26800L),
                    LyricToken(text = "on", beginMs = 26800L, endMs = 27200L),
                    LyricToken(text = "my", beginMs = 27200L, endMs = 27600L),
                    LyricToken(text = "own", beginMs = 27600L, endMs = 28200L),
                    LyricToken(text = "for", beginMs = 28200L, endMs = 28600L),
                    LyricToken(text = "long", beginMs = 28600L, endMs = 29200L),
                    LyricToken(text = "enough", beginMs = 29200L, endMs = 31500L)
                )
            ),
            LyricLine(
                beginMs = 32000L,
                endMs = 37000L,
                agentId = "v1",
                songPart = SongPart.VERSE,
                tokens = listOf(
                    LyricToken(text = "Maybe", beginMs = 32000L, endMs = 32600L),
                    LyricToken(text = "you", beginMs = 32600L, endMs = 33000L),
                    LyricToken(text = "can", beginMs = 33000L, endMs = 33400L),
                    LyricToken(text = "show", beginMs = 33400L, endMs = 34000L),
                    LyricToken(text = "me", beginMs = 34000L, endMs = 34500L),
                    LyricToken(text = "how", beginMs = 34500L, endMs = 35000L),
                    LyricToken(text = "to", beginMs = 35000L, endMs = 35400L),
                    LyricToken(text = "love,", beginMs = 35400L, endMs = 37000L)
                )
            ),
            LyricLine(
                beginMs = 37500L,
                endMs = 43000L,
                agentId = "v1",
                songPart = SongPart.CHORUS,
                tokens = listOf(
                    LyricToken(text = "I", beginMs = 37500L, endMs = 37900L),
                    LyricToken(text = "said,", beginMs = 37900L, endMs = 38400L),
                    LyricToken(text = "ooh,", beginMs = 38400L, endMs = 39800L),
                    LyricToken(text = "I'm", beginMs = 39800L, endMs = 40300L),
                    LyricToken(text = "blinded", beginMs = 40300L, endMs = 41500L),
                    LyricToken(text = "by", beginMs = 41500L, endMs = 41900L),
                    LyricToken(text = "the", beginMs = 41900L, endMs = 42200L),
                    LyricToken(text = "lights", beginMs = 42200L, endMs = 43000L)
                )
            )
        )

        return LyricsProject(
            id = "sample_blinding_lights",
            title = "Blinding Lights",
            artist = "The Weeknd",
            album = "After Hours",
            durationMs = 200000L,
            language = "en",
            timingMode = "Word",
            agents = agents,
            lines = lines
        )
    }

    private fun createArabicSongProject(): LyricsProject {
        val agents = listOf(
            AgentInfo(id = "v1", name = "ماجدة الرومي", type = "person", alignment = KaraokeAlignment.START, colorHex = "#F59E0B"),
            AgentInfo(id = "v2", name = "الكورال", type = "group", alignment = KaraokeAlignment.CENTER, colorHex = "#00D2FF")
        )

        val lines = listOf(
            LyricLine(
                beginMs = 12000L,
                endMs = 17500L,
                agentId = "v1",
                songPart = SongPart.VERSE,
                translation = "Words... changing all of my life",
                tokens = listOf(
                    LyricToken(text = "يسمعني", beginMs = 12000L, endMs = 13200L),
                    LyricToken(text = "حين", beginMs = 13200L, endMs = 14000L),
                    LyricToken(text = "يرقصني", beginMs = 14000L, endMs = 15800L),
                    LyricToken(text = "كلمات", beginMs = 15800L, endMs = 17500L)
                )
            ),
            LyricLine(
                beginMs = 18000L,
                endMs = 23000L,
                agentId = "v1",
                songPart = SongPart.VERSE,
                tokens = listOf(
                    LyricToken(text = "ليست", beginMs = 18000L, endMs = 19000L),
                    LyricToken(text = "كالكلمات", beginMs = 19000L, endMs = 21500L),
                    LyricToken(text = "(كلمات)", beginMs = 21600L, endMs = 23000L, isBackground = true)
                )
            ),
            LyricLine(
                beginMs = 24000L,
                endMs = 30000L,
                agentId = "v2",
                songPart = SongPart.CHORUS,
                tokens = listOf(
                    LyricToken(text = "ياخذني", beginMs = 24000L, endMs = 25500L),
                    LyricToken(text = "من", beginMs = 25500L, endMs = 26200L),
                    LyricToken(text = "تحت", beginMs = 26200L, endMs = 27000L),
                    LyricToken(text = "ذراعي", beginMs = 27000L, endMs = 28500L),
                    LyricToken(text = "كالطفلة", beginMs = 28500L, endMs = 30000L)
                )
            )
        )

        return LyricsProject(
            id = "sample_arabic_kalimat",
            title = "كلمات",
            artist = "ماجدة الرومي",
            album = "كلمات",
            durationMs = 240000L,
            language = "ar",
            timingMode = "Word",
            agents = agents,
            lines = lines
        )
    }
}
