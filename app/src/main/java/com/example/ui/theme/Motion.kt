package com.example.ui.theme

import androidx.compose.animation.core.AnimationSpec
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring

object StudioMotion {
    // Material 3 Expressive Motion Parameters
    const val SpringDefaultSpatialDamping = 0.8f
    const val SpringDefaultSpatialStiffness = 380.0f

    const val SpringDefaultEffectsDamping = 0.8f
    const val SpringDefaultEffectsStiffness = 1600.0f

    fun <T> expressiveSpatialSpec(): AnimationSpec<T> = spring(
        dampingRatio = SpringDefaultSpatialDamping,
        stiffness = SpringDefaultSpatialStiffness
    )

    fun <T> expressiveEffectsSpec(): AnimationSpec<T> = spring(
        dampingRatio = SpringDefaultEffectsDamping,
        stiffness = SpringDefaultEffectsStiffness
    )

    fun <T> fastSpatialSpec(): AnimationSpec<T> = spring(
        dampingRatio = 0.7f,
        stiffness = 600.0f
    )

    fun <T> slowSpatialSpec(): AnimationSpec<T> = spring(
        dampingRatio = 0.9f,
        stiffness = 200.0f
    )
}
