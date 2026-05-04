package com.moneyvisor.feature.dashboard.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
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
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.moneyvisor.core.designsystem.theme.IncomeGreen
import com.moneyvisor.core.designsystem.theme.IncomeGreenDark
import com.moneyvisor.core.designsystem.theme.CrimsonRuby
import com.moneyvisor.core.designsystem.theme.CrimsonRubyDark
import com.moneyvisor.core.designsystem.theme.AppAccent
import com.moneyvisor.core.designsystem.theme.AppAccentDark
import com.moneyvisor.feature.dashboard.util.CurrencyUtils
import java.util.Locale

@Composable
fun PieChart(
    income: Double,
    expenses: Double,
    modifier: Modifier = Modifier,
    currencyCode: String = "USD",
    isVisible: Boolean = true
) {
    val isDark = MaterialTheme.colorScheme.surface.luminance() < 0.5f
    val incomeColor = if (isDark) IncomeGreenDark else IncomeGreen
    val expenseColor = if (isDark) CrimsonRubyDark else CrimsonRuby
    val accentColor = if (isDark) AppAccentDark else AppAccent
    
    val total = (income + expenses).toFloat()
    val net = income - expenses
    
    val expenseProp = if (total > 0) (expenses.toFloat() / total) else 0.0f
    val incomeProp = if (total > 0) (income.toFloat() / total) else 0.0f

    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier.size(260.dp),
            contentAlignment = Alignment.Center
        ) {
            Canvas(modifier = Modifier.fillMaxSize().padding(20.dp)) {
                val strokeWidth = 35.dp.toPx()
                
                if (total > 0) {
                    // Expenses - Instant render
                    val expenseSweep = expenseProp * 360f
                    drawArc(
                        color = expenseColor,
                        startAngle = -90f,
                        sweepAngle = expenseSweep,
                        useCenter = false,
                        style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                    )
                    
                    // Income - Instant render
                    val incomeStartAngle = -90f + (expenseProp * 360f) + 4f
                    val incomeSweep = incomeProp * 360f - 4f
                    
                    if (incomeSweep > 0.5f) {
                        drawArc(
                            color = accentColor,
                            startAngle = incomeStartAngle,
                            sweepAngle = incomeSweep,
                            useCenter = false,
                            style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                        )
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
                    text = "Net Balance",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = CurrencyUtils.formatAmount(net, currencyCode),
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Black,
                    color = incomeColor
                )
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            LegendItem(accentColor, "Income", CurrencyUtils.formatAmount(income, currencyCode))
            LegendItem(expenseColor, "Expenses", CurrencyUtils.formatAmount(expenses, currencyCode))
        }
    }
}

@Composable
private fun LegendItem(color: Color, label: String, amount: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(10.dp)
                .background(color, CircleShape)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Column {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = amount,
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}
