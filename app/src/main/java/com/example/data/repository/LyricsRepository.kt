package com.example.data.repository

import com.example.data.local.ProjectDao
import com.example.data.local.ProjectMoshi
import com.example.data.local.SampleData
import com.example.data.remote.LrclibService
import com.example.data.remote.LrclibTrack
import com.example.domain.model.LyricsProject
import com.example.domain.parser.AutoParser
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class LyricsRepository(
    private val dao: ProjectDao,
    private val lrclibService: LrclibService = LrclibService.create()
) {
    val projects: Flow<List<LyricsProject>> = dao.getAllProjects().map { entities ->
        entities.map { ProjectMoshi.toDomain(it) }
    }

    suspend fun getProjectById(id: String): LyricsProject? {
        val entity = dao.getProjectById(id)
        if (entity != null) {
            return ProjectMoshi.toDomain(entity)
        }
        // Fallback check in samples
        return SampleData.getSampleProjects().firstOrNull { it.id == id }
    }

    suspend fun saveProject(project: LyricsProject) {
        dao.insertProject(ProjectMoshi.toEntity(project.copy(updatedAt = System.currentTimeMillis())))
    }

    suspend fun deleteProject(id: String) {
        dao.deleteProjectById(id)
    }

    suspend fun seedSamplesIfNeeded() {
        val count = dao.countProjects()
        if (count == 0) {
            for (sample in SampleData.getSampleProjects()) {
                dao.insertProject(ProjectMoshi.toEntity(sample))
            }
        }
    }

    suspend fun searchLrclib(query: String): Result<List<LrclibTrack>> {
        return try {
            val results = lrclibService.search(query)
            Result.success(results)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun convertLrclibToProject(track: LrclibTrack): LyricsProject {
        val content = track.syncedLyrics ?: track.plainLyrics ?: ""
        val baseProject = AutoParser.parse(content, track.displayTitle)
        return baseProject.copy(
            artist = track.displayArtist,
            album = track.displayAlbum,
            durationMs = ((track.duration ?: 180.0) * 1000).toLong().coerceAtLeast(60000L)
        )
    }
}
