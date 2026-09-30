package com.example.domain.model

import java.util.UUID

data class LyricsProject(
    val id: String = UUID.randomUUID().toString(),
    val title: String,
    val artist: String = "",
    val album: String = "",
    val durationMs: Long = 180000L, // 3 mins default
    val audioUri: String? = null,
    val coverUri: String? = null,
    val language: String = "en",
    val timingMode: String = "Word",
    val agents: List<AgentInfo> = listOf(
        AgentInfo(id = "v1", name = "Lead Vocal", type = "person", alignment = KaraokeAlignment.START, colorHex = "#FA2D48"),
        AgentInfo(id = "v2", name = "Secondary Vocal", type = "person", alignment = KaraokeAlignment.END, colorHex = "#00D2FF"),
        AgentInfo(id = "v3", name = "Group / Chorus", type = "group", alignment = KaraokeAlignment.CENTER, colorHex = "#A855F7")
    ),
    val lines: List<LyricLine> = emptyList(),
    val updatedAt: Long = System.currentTimeMillis()
) {
    val completionPercent: Int
        get() {
            if (lines.isEmpty()) return 0
            val timedLines = lines.count { line ->
                line.durationMs > 0 && line.tokens.any { it.durationMs > 0 }
            }
            return ((timedLines.toFloat() / lines.size) * 100).toInt().coerceIn(0, 100)
        }

    fun getAgent(agentId: String): AgentInfo {
        return agents.firstOrNull { it.id == agentId }
            ?: AgentInfo(id = agentId, name = agentId)
    }
}
