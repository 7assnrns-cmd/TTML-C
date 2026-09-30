package com.example.domain.model

import java.util.UUID

data class LyricLine(
    val id: String = UUID.randomUUID().toString(),
    val beginMs: Long = 0L,
    val endMs: Long = 0L,
    val agentId: String = "v1",
    val songPart: SongPart = SongPart.VERSE,
    val tokens: List<LyricToken> = emptyList(),
    val key: String = "",
    val translation: String? = null,
    val romanization: String? = null
) {
    val durationMs: Long get() = (endMs - beginMs).coerceAtLeast(0L)

    val fullText: String
        get() = tokens.joinToString(" ") { it.text }

    fun isActiveAt(timeMs: Long): Boolean {
        return timeMs in beginMs..endMs
    }

    fun isPassedAt(timeMs: Long): Boolean {
        return timeMs > endMs
    }

    fun activeTokenAt(timeMs: Long): LyricToken? {
        return tokens.firstOrNull { it.isActiveAt(timeMs) }
    }
}
