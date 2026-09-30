package com.example.domain.model

import java.util.UUID

data class LyricToken(
    val id: String = UUID.randomUUID().toString(),
    val text: String,
    val beginMs: Long = 0L,
    val endMs: Long = 0L,
    val isBackground: Boolean = false,
    val syllableIndex: Int = 0
) {
    val durationMs: Long get() = (endMs - beginMs).coerceAtLeast(0L)

    fun isActiveAt(timeMs: Long): Boolean {
        return timeMs in beginMs..endMs
    }

    fun isPassedAt(timeMs: Long): Boolean {
        return timeMs > endMs
    }

    fun progressAt(timeMs: Long): Float {
        if (durationMs <= 0L) return if (timeMs >= beginMs) 1f else 0f
        if (timeMs < beginMs) return 0f
        if (timeMs > endMs) return 1f
        return (timeMs - beginMs).toFloat() / durationMs.toFloat()
    }
}
