package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "lyrics_projects")
data class ProjectEntity(
    @PrimaryKey val id: String,
    val title: String,
    val artist: String,
    val album: String,
    val durationMs: Long,
    val audioUri: String?,
    val coverUri: String?,
    val language: String,
    val timingMode: String,
    val agentsJson: String,
    val linesJson: String,
    val updatedAt: Long,
    val completionPercent: Int
)
