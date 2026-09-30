package com.example.data.remote

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query
import java.util.concurrent.TimeUnit

@JsonClass(generateAdapter = true)
data class LrclibTrack(
    @Json(name = "id") val id: Long = 0,
    @Json(name = "name") val name: String? = null,
    @Json(name = "trackName") val trackName: String? = null,
    @Json(name = "artistName") val artistName: String? = null,
    @Json(name = "albumName") val albumName: String? = null,
    @Json(name = "duration") val duration: Double? = null,
    @Json(name = "instrumental") val instrumental: Boolean? = false,
    @Json(name = "plainLyrics") val plainLyrics: String? = null,
    @Json(name = "syncedLyrics") val syncedLyrics: String? = null
) {
    val displayTitle: String get() = trackName ?: name ?: "Unknown Title"
    val displayArtist: String get() = artistName ?: "Unknown Artist"
    val displayAlbum: String get() = albumName ?: ""
    val hasSynced: Boolean get() = !syncedLyrics.isNullOrBlank()
}

interface LrclibService {
    @GET("api/search")
    suspend fun search(
        @Query("q") query: String
    ): List<LrclibTrack>

    @GET("api/get")
    suspend fun getLyrics(
        @Query("track_name") trackName: String,
        @Query("artist_name") artistName: String
    ): LrclibTrack

    companion object {
        private const val BASE_URL = "https://lrclib.net/"

        fun create(): LrclibService {
            val logging = HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BASIC
            }
            val client = OkHttpClient.Builder()
                .connectTimeout(15, TimeUnit.SECONDS)
                .readTimeout(15, TimeUnit.SECONDS)
                .addInterceptor(logging)
                .addInterceptor { chain ->
                    val request = chain.request().newBuilder()
                        .header("User-Agent", "TTMLStudioAndroid/1.0 (https://github.com/aistudio)")
                        .build()
                    chain.proceed(request)
                }
                .build()

            return Retrofit.Builder()
                .baseUrl(BASE_URL)
                .client(client)
                .addConverterFactory(MoshiConverterFactory.create())
                .build()
                .create(LrclibService::class.java)
        }
    }
}
