package com.moneyvisor.feature.dashboard.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import com.moneyvisor.core.designsystem.animations.MoneyVisorAnimations
import java.text.NumberFormat
import java.util.Locale

@Composable
fun AnimatedAmount(
    amount: Double,
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.onSurface
) {
    val animatedAmount by animateFloatAsState(
        targetValue = amount.toFloat(),
        animationSpec = MoneyVisorAnimations.smoothSpring,
        label = "AmountAnimation"
    )

    val formatter = NumberFormat.getCurrencyInstance(Locale.US)
    
    Text(
        text = formatter.format(animatedAmount),
        style = MaterialTheme.typography.headlineMedium,
        fontWeight = FontWeight.Bold,
        color = color,
        modifier = modifier
    )
}
