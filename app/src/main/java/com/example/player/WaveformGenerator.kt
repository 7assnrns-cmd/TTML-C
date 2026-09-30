package com.example.player

import kotlin.math.abs
import kotlin.math.sin

object WaveformGenerator {

    /**
     * Generates a deterministic, realistic audio waveform envelope given a duration and seed text.
     * Peak values are normalized between 0.05f and 1.0f.
     */
    fun generateWaveform(sampleCount: Int = 300, seed: String = "TTML"): List<Float> {
        val hash = seed.hashCode().toLong()
        val peaks = FloatArray(sampleCount)

        for (i in 0 until sampleCount) {
            val progress = i.toDouble() / sampleCount
            // Layered harmonics to mimic musical energy (drums, vocals, synths)
            val bass = abs(sin(progress * 18.0 + (hash % 10))) * 0.45
            val mid = abs(sin(progress * 42.0 + (hash % 7))) * 0.35
            val high = abs(sin(progress * 120.0 + (hash % 13))) * 0.20
            
            // Musical envelope (quieter intro and outro, louder choruses)
            val envelope = when {
                progress < 0.1 -> 0.2 + (progress / 0.1) * 0.6
                progress > 0.9 -> 0.2 + ((1.0 - progress) / 0.1) * 0.6
                else -> 0.65 + 0.35 * abs(sin(progress * 6.28 * 3.0))
            }

            val raw = ((bass + mid + high) * envelope).coerceIn(0.08, 1.0)
            peaks[i] = raw.toFloat()
        }

        return peaks.toList()
    }
}
