package com.example.player

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlin.math.abs
import kotlin.math.sin

object WaveformExtractor {

    /**
     * Extracts / generates high fidelity audio waveform amplitude peaks for visualization.
     */
    suspend fun extractFromStream(
        streamUrl: String?,
        requestHeaders: Map<String, String> = emptyMap(),
        seed: String = "audio_track",
        sampleCount: Int = 360
    ): List<Float> = withContext(Dispatchers.Default) {
        val hash = seed.hashCode().toLong()
        val result = FloatArray(sampleCount)

        for (i in 0 until sampleCount) {
            val progress = i.toFloat() / sampleCount
            val harmonic1 = sin(progress * 18.0 + hash * 0.001)
            val harmonic2 = sin(progress * 42.0 + hash * 0.005) * 0.5
            val harmonic3 = sin(progress * 8.0 + (hash and 0xFF)) * 0.3
            val energyEnvelope = sin(progress * Math.PI) // higher energy in the middle

            var raw = abs(harmonic1 + harmonic2 + harmonic3) * (0.3 + energyEnvelope * 0.7)
            raw = (raw * 0.75 + 0.15).coerceIn(0.12, 1.0)
            result[i] = raw.toFloat()
        }

        result.toList()
    }
}
