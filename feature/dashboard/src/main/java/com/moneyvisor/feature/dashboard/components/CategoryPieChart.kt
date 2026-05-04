package com.moneyvisor.feature.dashboard.components

import androidx.compose.foundation.background
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.moneyvisor.feature.dashboard.CategoryData
import com.moneyvisor.feature.dashboard.util.CurrencyUtils

private val CategoryColors = listOf(
    Color(0xFF4A64F6), // Brand Blue
    Color(0xFFFF7043), // Orange
    Color(0xFF26A69A), // Teal
    Color(0xFFAB47BC), // Purple
    Color(0xFFFBC02D), // Yellow
    Color(0xFF5C6BC0)  // Indigo
)

@Composable
fun CategoryPieChart(
    data: List<CategoryData>,
    currencyCode: String,
    isVisible: Boolean = true,
    modifier: Modifier = Modifier
) {
    // Pre-calculate final start angles
    val finalStartAngles = remember(data) {
        val size = minOf(data.size, 6)
        val angles = FloatArray(size)
        var current = -90f
        for (i in 0 until size) {
            angles[i] = current
            current += data[i].percentage * 360f
        }
        angles
    }

    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier.size(220.dp),
            contentAlignment = Alignment.Center
        ) {
            Canvas(modifier = Modifier.fillMaxSize().padding(20.dp)) {
                val strokeWidth = 30.dp.toPx()
                
                if (data.isNotEmpty()) {
                    data.take(6).forEachIndexed { index, item ->
                        val sweepAngle = item.percentage * 360f
                        if (sweepAngle > 0.5f) {
                            drawArc(
                                color = CategoryColors.getOrElse(index) { Color.Gray },
                                startAngle = if (index < finalStartAngles.size) finalStartAngles[index] else -90f,
                                sweepAngle = sweepAngle,
                                useCenter = false,
                                style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                            )
                        }
                    }
                } else {
                    drawArc(
                        color = Color.LightGray.copy(alpha = 0.2f),
                        startAngle = 0f,
                        sweepAngle = 360f,
                        useCenter = false,
                        style = Stroke(width = strokeWidth)
                    )
                }
            }
            
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "Top Spending",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = if (data.isNotEmpty()) data[0].category else "N/A",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Black,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Legend
        Column(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            data.take(4).forEachIndexed { index, item ->
                CategoryLegendItem(
                    color = CategoryColors.getOrElse(index) { Color.Gray },
                    label = item.category,
                    amount = CurrencyUtils.formatAmount(item.amount, currencyCode),
                    percentage = (item.percentage * 100).toInt()
                )
            }
        }
    }
}

@Composable
private fun CategoryLegendItem(color: Color, label: String, amount: String, percentage: Int) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
            Box(modifier = Modifier.size(8.dp).background(color, CircleShape))
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "$percentage%",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Text(
            text = amount,
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}
