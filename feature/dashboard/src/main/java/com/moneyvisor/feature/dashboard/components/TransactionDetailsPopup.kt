package com.moneyvisor.feature.dashboard.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.moneyvisor.core.designsystem.icons.MoneyVisorIcons
import com.moneyvisor.core.designsystem.theme.*
import com.moneyvisor.domain.model.Transaction
import com.moneyvisor.domain.model.TransactionType
import com.moneyvisor.feature.dashboard.util.CurrencyUtils
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun TransactionDetailsPopup(
    transaction: Transaction,
    onDismiss: () -> Unit,
    currencyCode: String = "USD"
) {
    val isDark = MaterialTheme.colorScheme.surface.luminance() < 0.5f
    val incomeColor = if (isDark) IncomeGreenDark else IncomeGreen
    val expenseColor = if (isDark) CrimsonRubyDark else CrimsonRuby
    
    val dateFormatter = SimpleDateFormat("MMMM dd, yyyy 'at' hh:mm a", Locale.US)
    val isIncome = transaction.type == TransactionType.INCOME

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(32.dp),
            color = MaterialTheme.colorScheme.surface,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .clip(CircleShape)
                        .background((if (isIncome) incomeColor else expenseColor).copy(alpha = 0.1f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = MoneyVisorIcons.Savings,
                        contentDescription = null,
                        tint = if (isIncome) incomeColor else expenseColor,
                        modifier = Modifier.size(40.dp)
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    text = if (isIncome) "Income Received" else "Payment Successful",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Black,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = CurrencyUtils.formatAmount(transaction.amount, currencyCode),
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.Black,
                    color = if (isIncome) incomeColor else MaterialTheme.colorScheme.onSurface
                )

                Spacer(modifier = Modifier.height(32.dp))

                // Details List
                DetailItem("Category", transaction.category)
                DetailItem("Date", dateFormatter.format(Date(transaction.date)))
                if (transaction.tag.isNotEmpty()) {
                    DetailItem("Tag", transaction.tag)
                }
                DetailItem("Status", "Confirmed")

                Spacer(modifier = Modifier.height(40.dp))

                Button(
                    onClick = onDismiss,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(60.dp),
                    shape = RoundedCornerShape(20.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant, 
                        contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                ) {
                    Text("Close", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
            }
        }
    }
}

@Composable
private fun DetailItem(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}
