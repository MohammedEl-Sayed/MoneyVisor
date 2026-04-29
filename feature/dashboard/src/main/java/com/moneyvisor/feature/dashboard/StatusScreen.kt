package com.moneyvisor.feature.dashboard

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.moneyvisor.core.designsystem.icons.MoneyVisorIcons
import com.moneyvisor.core.designsystem.theme.*
import com.moneyvisor.feature.dashboard.components.PieChart
import com.moneyvisor.feature.dashboard.components.CategoryPieChart
import com.moneyvisor.feature.dashboard.util.CurrencyUtils
import com.moneyvisor.domain.model.Budget
import com.moneyvisor.domain.model.Goal
import java.text.NumberFormat
import java.util.*

@Composable
fun StatusScreen(
    isVisible: Boolean = true,
    viewModel: DashboardViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val statusState = uiState.status
    val settings = uiState.settings
    
    val isDark = MaterialTheme.colorScheme.surface.luminance() < 0.5f
    val incomeColor = if (isDark) IncomeGreenDark else IncomeGreen
    val expenseColor = if (isDark) CrimsonRubyDark else CrimsonRuby
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
            .padding(24.dp)
    ) {
        Text(
            text = "Status",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )
        
        Spacer(modifier = Modifier.height(32.dp))
        
        PieChart(
            income = statusState.totalIncome,
            expenses = statusState.totalSpent,
            currencyCode = settings.currencyCode,
            isVisible = isVisible,
            modifier = Modifier.padding(vertical = 16.dp)
        )
        
        Spacer(modifier = Modifier.height(48.dp))

        // Monthly Breakdown Header
        Text(
            text = "Expense Breakdown",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )
        
        Spacer(modifier = Modifier.height(16.dp))

        if (statusState.categoryChartData.isNotEmpty()) {
            CategoryPieChart(
                data = statusState.categoryChartData,
                currencyCode = settings.currencyCode,
                isVisible = isVisible
            )
        } else {
            Box(
                modifier = Modifier.fillMaxWidth().height(100.dp).clip(RoundedCornerShape(24.dp)).background(MaterialTheme.colorScheme.surfaceVariant),
                contentAlignment = Alignment.Center
            ) {
                Text("No data for this month", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }

        Spacer(modifier = Modifier.height(48.dp))
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Overview",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
            
            // Trend Indicator
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = if (statusState.monthlyTrend <= 0) MoneyVisorIcons.ArrowDown else MoneyVisorIcons.ArrowUp,
                    contentDescription = null,
                    tint = if (statusState.monthlyTrend <= 0) incomeColor else expenseColor,
                    modifier = Modifier.size(16.dp)
                )
                Text(
                    text = "${if (statusState.monthlyTrend > 0) "+" else ""}${statusState.monthlyTrend.toInt()}% vs last month",
                    style = MaterialTheme.typography.labelSmall,
                    color = if (statusState.monthlyTrend <= 0) incomeColor else expenseColor,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        StatusItem(
            label = "Total Balance",
            value = CurrencyUtils.formatPrivacy(
                CurrencyUtils.formatAmount(statusState.balance, settings.currencyCode),
                settings.isPrivacyModeEnabled
            ),
            color = MaterialTheme.colorScheme.primary,
            isDark = isDark
        )
        
        Spacer(modifier = Modifier.height(12.dp))
        
        StatusItem(
            label = "Total Income",
            value = CurrencyUtils.formatPrivacy(
                CurrencyUtils.formatAmount(statusState.totalIncome, settings.currencyCode),
                settings.isPrivacyModeEnabled
            ),
            color = if (isDark) AppAccentDark else AppAccent,
            isDark = isDark
        )
        
        Spacer(modifier = Modifier.height(12.dp))
        
        StatusItem(
            label = "Total Expenses",
            value = CurrencyUtils.formatPrivacy(
                CurrencyUtils.formatAmount(statusState.totalSpent, settings.currencyCode),
                settings.isPrivacyModeEnabled
            ),
            color = expenseColor,
            isDark = isDark
        )

        if (statusState.budgets.isNotEmpty()) {
            Spacer(modifier = Modifier.height(40.dp))
            Text(
                text = "Budgets",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.padding(bottom = 16.dp)
            )
            statusState.budgets.forEach { budget ->
                BudgetItem(budget, settings.currencyCode, settings.isPrivacyModeEnabled, expenseColor, isDark)
                Spacer(modifier = Modifier.height(12.dp))
            }
        }

        if (statusState.goals.isNotEmpty()) {
            Spacer(modifier = Modifier.height(40.dp))
            Text(
                text = "Savings Goals",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.padding(bottom = 16.dp)
            )
            statusState.goals.forEach { goal ->
                GoalItem(goal, settings.currencyCode, settings.isPrivacyModeEnabled, incomeColor, isDark)
                Spacer(modifier = Modifier.height(12.dp))
            }
        }
        
        Spacer(modifier = Modifier.height(48.dp))
    }
}

@Composable
private fun GoalItem(goal: Goal, currencyCode: String, isPrivacy: Boolean, activeColor: Color, isDark: Boolean) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, if (isDark) Color.Transparent else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f)),
        shadowElevation = 0.dp
    ) {
        Row(
            modifier = Modifier.padding(20.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = goal.name,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "${(goal.progress * 100).toInt()}% of " + CurrencyUtils.formatPrivacy(
                        CurrencyUtils.formatAmount(goal.targetAmount, currencyCode),
                        isPrivacy
                    ),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Box(contentAlignment = Alignment.Center) {
                CircularProgressIndicator(
                    progress = { goal.progress.coerceIn(0f, 1f) },
                    modifier = Modifier.size(48.dp),
                    color = activeColor,
                    trackColor = MaterialTheme.colorScheme.surfaceVariant,
                    strokeWidth = 6.dp
                )
                if (goal.isCompleted) {
                    Icon(
                        imageVector = MoneyVisorIcons.Check,
                        contentDescription = null,
                        tint = activeColor,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun BudgetItem(budget: Budget, currencyCode: String, isPrivacy: Boolean, alertColor: Color, isDark: Boolean) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, if (isDark) Color.Transparent else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f)),
        shadowElevation = 0.dp
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = budget.category,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                val remaining = budget.limitAmount - budget.currentSpent
                Text(
                    text = if (remaining >= 0) {
                        CurrencyUtils.formatPrivacy(CurrencyUtils.formatAmount(remaining, currencyCode), isPrivacy) + " left"
                    } else {
                        "Over by " + CurrencyUtils.formatPrivacy(CurrencyUtils.formatAmount(-remaining, currencyCode), isPrivacy)
                    },
                    style = MaterialTheme.typography.labelSmall,
                    color = if (budget.isOverBudget) alertColor else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
            LinearProgressIndicator(
                progress = { budget.progress.coerceIn(0f, 1f) },
                modifier = Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(4.dp)),
                color = if (budget.isOverBudget) alertColor else MaterialTheme.colorScheme.primary,
                trackColor = MaterialTheme.colorScheme.surfaceVariant
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "${(budget.progress * 100).toInt()}% spent of " + CurrencyUtils.formatPrivacy(
                    CurrencyUtils.formatAmount(budget.limitAmount, currencyCode),
                    isPrivacy
                ),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun StatusItem(
    label: String,
    value: String,
    color: Color,
    isDark: Boolean
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, if (isDark) Color.Transparent else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f)),
        shadowElevation = 0.dp
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 24.dp, vertical = 20.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = value,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Black,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
            
            Box(
                modifier = Modifier
                    .size(12.dp)
                    .clip(CircleShape)
                    .background(color)
            )
        }
    }
}
