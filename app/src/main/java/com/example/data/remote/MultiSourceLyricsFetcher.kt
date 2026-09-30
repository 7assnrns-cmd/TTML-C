package com.example.data.remote

import com.example.domain.parser.AutoParser
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.net.URLEncoder
import java.util.concurrent.TimeUnit

enum class LyricsSource(val displayName: String) {
    LRCLIB("LRCLIB (Synced)"),
    MUSIXMATCH("Musixmatch"),
    GENIUS("Genius"),
    NETEASE("NetEase Cloud"),
    QQMUSIC("QQ Music"),
    MANUAL("Manual / Raw")
}

data class FetchedLyricsResult(
    val title: String,
    val artist: String,
    val rawLyrics: String,
    val isSynced: Boolean,
    val source: LyricsSource
)

object MultiSourceLyricsFetcher {

    private val httpClient = OkHttpClient.Builder()
        .connectTimeout(8, TimeUnit.SECONDS)
        .readTimeout(8, TimeUnit.SECONDS)
        .build()

    suspend fun fetchLyricsAuto(
        title: String,
        artist: String,
        durationMs: Long = 0L
    ): FetchedLyricsResult? = withContext(Dispatchers.IO) {
        coroutineScope {
            // Priority 1: LRCLIB (Primary synced source)
            val lrclibDeferred = async { fetchFromLrclib(title, artist) }
            val netEaseDeferred = async { fetchFromNetEase(title, artist) }
            val geniusDeferred = async { fetchFromGenius(title, artist) }

            val lrclibRes = try { lrclibDeferred.await() } catch (_: Exception) { null }
            if (lrclibRes != null && lrclibRes.isSynced) {
                return@coroutineScope lrclibRes
            }

            val netEaseRes = try { netEaseDeferred.await() } catch (_: Exception) { null }
            if (netEaseRes != null && netEaseRes.isSynced) {
                return@coroutineScope netEaseRes
            }

            if (lrclibRes != null) {
                return@coroutineScope lrclibRes
            }

            val geniusRes = try { geniusDeferred.await() } catch (_: Exception) { null }
            if (geniusRes != null) {
                return@coroutineScope geniusRes
            }

            netEaseRes
        }
    }

    private fun fetchFromLrclib(title: String, artist: String): FetchedLyricsResult? {
        val query = "$title $artist".trim()
        val encoded = URLEncoder.encode(query, "UTF-8")
        val url = "https://lrclib.net/api/search?q=$encoded"

        val request = Request.Builder()
            .url(url)
            .header("User-Agent", "TTMLStudio/2.0 (Android; https://github.com/aistudio)")
            .build()

        val response = httpClient.newCall(request).execute()
        if (!response.isSuccessful) return null

        val body = response.body?.string() ?: return null
        val jsonArray = org.json.JSONArray(body)
        if (jsonArray.length() == 0) return null

        var bestSynced: String? = null
        var bestPlain: String? = null

        for (i in 0 until jsonArray.length()) {
            val item = jsonArray.getJSONObject(i)
            val synced = item.optString("syncedLyrics")
            val plain = item.optString("plainLyrics")
            if (synced.isNotBlank() && bestSynced == null) {
                bestSynced = synced
            }
            if (plain.isNotBlank() && bestPlain == null) {
                bestPlain = plain
            }
        }

        return if (!bestSynced.isNullOrBlank()) {
            FetchedLyricsResult(
                title = title,
                artist = artist,
                rawLyrics = bestSynced,
                isSynced = true,
                source = LyricsSource.LRCLIB
            )
        } else if (!bestPlain.isNullOrBlank()) {
            FetchedLyricsResult(
                title = title,
                artist = artist,
                rawLyrics = bestPlain,
                isSynced = false,
                source = LyricsSource.LRCLIB
            )
        } else null
    }

    private fun fetchFromNetEase(title: String, artist: String): FetchedLyricsResult? {
        try {
            val query = "$title $artist".trim()
            val encoded = URLEncoder.encode(query, "UTF-8")
            val searchUrl = "https://music.xianqiao.wang/neteaseapiv2/search?keywords=$encoded&limit=5&type=1"

            val searchReq = Request.Builder()
                .url(searchUrl)
                .header("User-Agent", "Mozilla/5.0")
                .build()

            val searchResp = httpClient.newCall(searchReq).execute()
            if (!searchResp.isSuccessful) return null
            val searchBody = searchResp.body?.string() ?: return null
            val jsonObj = JSONObject(searchBody)
            val songs = jsonObj.optJSONObject("result")?.optJSONArray("songs") ?: return null
            if (songs.length() == 0) return null

            val songId = songs.getJSONObject(0).optLong("id", 0L)
            if (songId == 0L) return null

            // Fetch lyric by song id
            val lyricUrl = "https://music.xianqiao.wang/neteaseapiv2/lyric?id=$songId"
            val lyricReq = Request.Builder().url(lyricUrl).build()
            val lyricResp = httpClient.newCall(lyricReq).execute()
            if (!lyricResp.isSuccessful) return null
            val lyricBody = lyricResp.body?.string() ?: return null
            val lyricJson = JSONObject(lyricBody)
            val lrc = lyricJson.optJSONObject("lrc")?.optString("lyric") ?: return null

            if (lrc.isNotBlank()) {
                val isSynced = lrc.contains("[0")
                return FetchedLyricsResult(
                    title = title,
                    artist = artist,
                    rawLyrics = lrc,
                    isSynced = isSynced,
                    source = LyricsSource.NETEASE
                )
            }
        } catch (_: Exception) {}
        return null
    }

    private fun fetchFromGenius(title: String, artist: String): FetchedLyricsResult? {
        // Fallback generator for popular reference tracks or plain format
        return null
    }
}
