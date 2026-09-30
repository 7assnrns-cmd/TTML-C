package com.example.domain.model

enum class KaraokeAlignment {
    START,  // Left align for Singer 1 (v1)
    END,    // Right align for Singer 2 (v2+)
    CENTER  // Center align for Group / Duet
}

data class AgentInfo(
    val id: String = "v1",
    val name: String = "Lead Singer",
    val type: String = "person", // "person" or "group"
    val alignment: KaraokeAlignment = KaraokeAlignment.START,
    val colorHex: String = "#FA2D48" // Apple Music Red-Pink
)
