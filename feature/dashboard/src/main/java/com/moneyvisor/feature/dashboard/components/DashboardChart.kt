package com.moneyvisor.feature.dashboard.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.moneyvisor.core.designsystem.theme.CrimsonRuby
import com.moneyvisor.core.designsystem.theme.CrimsonRubyDark
import com.moneyvisor.core.designsystem.theme.AppAccent
import com.moneyvisor.core.designsystem.theme.AppAccentDark
import com.moneyvisor.core.designsystem.theme.TotalBlue
import com.moneyvisor.core.designsystem.theme.IncomeGreen
import com.moneyvisor.core.designsystem.theme.IncomeGreenDark
import java.util.*
import java.text.SimpleDateFormat

@Composable
fun DashboardChart(
    modifier: Modifier = Modifier,
    title: String = "Statistics",
    incomeData: List<Float> = emptyList(),
    expenseData: List<Float> = emptyList(),
    totalData: List<Float> = emptyList(),
    isVisible: Boolean = true
) {
    val isDark = MaterialTheme.colorScheme.surface.luminance() < 0.5f
    
    val incomePoints = if (incomeData.isEmpty()) List(7) { 0f } else incomeData
    val expensePoints = if (expenseData.isEmpty()) List(7) { 0f } else expenseData
    val totalPoints = if (totalData.isEmpty()) List(7) { 0f } else totalData
    
    val incomeColor = if (isDark) IncomeGreenDark else IncomeGreen
    val expenseColor = if (isDark) CrimsonRubyDark else CrimsonRuby
    val totalColor = TotalBlue

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp),
        shape = RoundedCornerShape(28.dp),
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 0.dp
    ) {
        Column(
            modifier = Modifier.padding(24.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                
                Row(verticalAlignment = Alignment.CenterVertically) {
                    LegendItem(incomeColor, "In")
                    Spacer(modifier = Modifier.width(10.dp))
                    LegendItem(expenseColor, "Out")
                    Spacer(modifier = Modifier.width(10.dp))
                    LegendItem(totalColor, "Net")
                }
            }

            Spacer(modifier = Modifier.height(28.dp))

            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
            ) {
                val width = size.width
                val height = size.height
                val spaceBetweenPoints = width / (incomePoints.size - 1).coerceAtLeast(1)

                // Draw Area Gradients - Static
                drawAreaGradient(incomePoints, height, spaceBetweenPoints, incomeColor.copy(alpha = 0.15f))
                drawAreaGradient(expensePoints, height, spaceBetweenPoints, expenseColor.copy(alpha = 0.15f))

                // Draw Paths - Static
                drawChartPath(incomePoints, height, spaceBetweenPoints, incomeColor)
                drawChartPath(expensePoints, height, spaceBetweenPoints, expenseColor)
                drawChartPath(totalPoints, height, spaceBetweenPoints, totalColor)
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            val days = remember(incomePoints.size) {
                val dateFormat = SimpleDateFormat("EEE", Locale.getDefault())
                (0 until incomePoints.size).map { i ->
                    val d = Calendar.getInstance()
                    d.add(Calendar.DAY_OF_YEAR, - (incomePoints.size - 1 - i))
                    dateFormat.format(d.time)
                }
            }
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                val step = if (days.size > 10) 5 else 1
                days.forEachIndexed { index, day ->
                    if (index % step == 0) {
                        Text(
                            text = day,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                            fontSize = 9.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun LegendItem(color: Color, label: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(modifier = Modifier.size(8.dp).background(color, RoundedCornerShape(2.dp)))
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            label, 
            fontSize = 10.sp, 
            color = MaterialTheme.colorScheme.onSurfaceVariant, 
            fontWeight = FontWeight.Bold
        )
    }
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawAreaGradient(
    points: List<Float>,
    height: Float,
    spaceBetweenPoints: Float,
    color: Color
) {
    if (points.isEmpty()) return
    
    val areaPath = Path().apply {
        points.forEachIndexed { index, value ->
            val x = index * spaceBetweenPoints
            val y = height / 2 - (value * (height / 2 - 20.dp.toPx())) 
            if (index == 0) moveTo(x, y) else lineTo(x, y)
        }
        lineTo((points.size - 1) * spaceBetweenPoints, height / 2)
        lineTo(0f, height / 2)
        close()
    }

    drawPath(
        path = areaPath,
        brush = Brush.verticalGradient(
            colors = listOf(color, Color.Transparent),
            startY = 0f,
            endY = height
        )
    )
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawChartPath(
    points: List<Float>,
    height: Float,
    spaceBetweenPoints: Float,
    color: Color
) {
    if (points.isEmpty()) return
    
    val path = Path().apply {
        points.forEachIndexed { index, value ->
            val x = index * spaceBetweenPoints
            val y = height / 2 - (value * (height / 2 - 20.dp.toPx())) 
            if (index == 0) moveTo(x, y) else lineTo(x, y)
        }
    }

    drawPath(
        path = path,
        color = color,
        style = Stroke(width = 3.dp.toPx(), cap = androidx.compose.ui.graphics.StrokeCap.Round)
    )
    
    if (points.size <= 7) {
        points.forEachIndexed { index, value ->
            val x = index * spaceBetweenPoints
            val y = height / 2 - (value * (height / 2 - 20.dp.toPx())) 
            drawCircle(color = color, radius = 4.dp.toPx(), center = Offset(x, y))
            drawCircle(color = Color.White, radius = 2.dp.toPx(), center = Offset(x, y))
        }
    }
}
