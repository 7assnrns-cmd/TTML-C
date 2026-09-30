package com.example.domain.model

enum class SongPart(val displayName: String, val tag: String) {
    INTRO("Intro", "Intro"),
    VERSE("Verse", "Verse"),
    PRE_CHORUS("Pre-Chorus", "PreChorus"),
    CHORUS("Chorus", "Chorus"),
    BRIDGE("Bridge", "Bridge"),
    REFRAIN("Refrain", "Refrain"),
    OUTRO("Outro", "Outro"),
    INSTRUMENTAL("Instrumental", "Instrumental");

    companion object {
        fun fromTag(tag: String?): SongPart {
            if (tag == null) return VERSE
            return entries.firstOrNull { 
                it.tag.equals(tag, ignoreCase = true) || it.displayName.equals(tag, ignoreCase = true)
            } ?: VERSE
        }
    }
}
