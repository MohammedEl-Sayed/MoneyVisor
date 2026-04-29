package com.moneyvisor.core.designsystem.animations

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring

object MoneyVisorAnimations {
    /**
     * Professional spring animation with low bounce.
     * Damping: 0.9f (low bounce)
     * Stiffness: 400f (medium speed)
     */
    val smoothSpring = spring<Float>(
        dampingRatio = 0.9f,
        stiffness = 400f
    )

    val bounceSpring = spring<Float>(
        dampingRatio = Spring.DampingRatioMediumBouncy,
        stiffness = Spring.StiffnessLow
    )
}
