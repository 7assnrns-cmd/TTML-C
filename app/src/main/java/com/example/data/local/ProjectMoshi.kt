package com.example.data.local

import com.example.domain.model.AgentInfo
import com.example.domain.model.KaraokeAlignment
import com.example.domain.model.LyricLine
import com.example.domain.model.LyricToken
import com.example.domain.model.LyricsProject
import com.example.domain.model.SongPart
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory

object ProjectMoshi {
    private val moshi: Moshi = Moshi.Builder()
        .addLast(KotlinJsonAdapterFactory())
        .build()

    private val agentsListType = Types.newParameterizedType(List::class.java, AgentInfo::class.java)
    private val linesListType = Types.newParameterizedType(List::class.java, LyricLine::class.java)

    private val agentsAdapter = moshi.adapter<List<AgentInfo>>(agentsListType)
    private val linesAdapter = moshi.adapter<List<LyricLine>>(linesListType)

    fun agentsToJson(agents: List<AgentInfo>): String = agentsAdapter.toJson(agents)
    fun agentsFromJson(json: String): List<AgentInfo> {
        return try {
            agentsAdapter.fromJson(json) ?: emptyList()
        } catch (_: Exception) {
            emptyList()
        }
    }

    fun linesToJson(lines: List<LyricLine>): String = linesAdapter.toJson(lines)
    fun linesFromJson(json: String): List<LyricLine> {
        return try {
            linesAdapter.fromJson(json) ?: emptyList()
        } catch (_: Exception) {
            emptyList()
        }
    }

    fun toEntity(project: LyricsProject): ProjectEntity {
        return ProjectEntity(
            id = project.id,
            title = project.title,
            artist = project.artist,
            album = project.album,
            durationMs = project.durationMs,
            audioUri = project.audioUri,
            coverUri = project.coverUri,
            language = project.language,
            timingMode = project.timingMode,
            agentsJson = agentsToJson(project.agents),
            linesJson = linesToJson(project.lines),
            updatedAt = project.updatedAt,
            completionPercent = project.completionPercent
        )
    }

    fun toDomain(entity: ProjectEntity): LyricsProject {
        return LyricsProject(
            id = entity.id,
            title = entity.title,
            artist = entity.artist,
            album = entity.album,
            durationMs = entity.durationMs,
            audioUri = entity.audioUri,
            coverUri = entity.coverUri,
            language = entity.language,
            timingMode = entity.timingMode,
            agents = agentsFromJson(entity.agentsJson),
            lines = linesFromJson(entity.linesJson),
            updatedAt = entity.updatedAt
        )
    }
}
