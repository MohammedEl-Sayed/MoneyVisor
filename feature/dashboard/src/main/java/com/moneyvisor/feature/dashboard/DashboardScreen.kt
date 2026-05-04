package com.moneyvisor.feature.dashboard

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.moneyvisor.core.designsystem.icons.MoneyVisorIcons
import com.moneyvisor.core.designsystem.theme.BrandBlue
import com.moneyvisor.feature.dashboard.components.*
import com.moneyvisor.feature.dashboard.util.CurrencyUtils
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import com.moneyvisor.domain.model.Transaction
import java.util.*

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun DashboardScreen(
    isVisible: Boolean = true,
    onNavigateToAddTransaction: () -> Unit,
    onNavigateToEditTransaction: (String) -> Unit,
    onNavigateToTimeline: () -> Unit,
    viewModel: DashboardViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val dashboardState = uiState.dashboard
    val settings = uiState.settings
    
    var showSortPopup by remember { mutableStateOf(false) }
    var selectedTransactionForDetails by remember { mutableStateOf<Transaction?>(null) }
    var transactionForOptions by remember { mutableStateOf<Transaction?>(null) }
    val listState = rememberLazyListState()

    val isDark = MaterialTheme.colorScheme.surface.luminance() < 0.5f

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        LazyColumn(
            state = listState,
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 24.dp)
        ) {
            item { ProfileHeader(userName = "Oliver Bennet") }

            item {
                BalanceCard(
                    balance = CurrencyUtils.formatPrivacy(
                        CurrencyUtils.formatAmount(dashboardState.balance, settings.currencyCode),
                        settings.isPrivacyModeEnabled
                    ),
                    onAddMoneyClick = onNavigateToAddTransaction,
                    isPrivacyMode = settings.isPrivacyModeEnabled,
                    onPrivacyToggle = { viewModel.togglePrivacyMode(it) }
                )
            }

            item {
                Spacer(modifier = Modifier.height(24.dp))
                DashboardChart(
                    title = if (dashboardState.chartInterval == "MONTHLY") "Monthly Stats" else "Weekly Stats",
                    incomeData = dashboardState.incomeChartData,
                    expenseData = dashboardState.expenseChartData,
                    totalData = dashboardState.totalChartData,
                    isVisible = isVisible
                )
            }

            // Tag Filter Section
            if (dashboardState.availableTags.isNotEmpty()) {
                item {
                    Column(modifier = Modifier.padding(top = 24.dp)) {
                        Text(
                            text = "Filter by Tags",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground,
                            modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp)
                        )
                        androidx.compose.foundation.lazy.LazyRow(
                            contentPadding = PaddingValues(horizontal = 24.dp),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            items(dashboardState.availableTags) { tag ->
                                val isSelected = tag in dashboardState.selectedTags
                                val interactionSource = remember { MutableInteractionSource() }
                                val containerColor = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface
                                val borderColor = if (isSelected) Color.Transparent else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f)
                                
                                Box(
                                    modifier = Modifier
                                        .height(44.dp)
                                        .clip(RoundedCornerShape(16.dp))
                                        .background(containerColor)
                                        .border(BorderStroke(1.dp, borderColor), RoundedCornerShape(16.dp))
                                        .clickable(
                                            interactionSource = interactionSource,
                                            indication = null
                                        ) { viewModel.toggleTagFilter(tag) }
                                        .padding(horizontal = 16.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Checkbox(
                                            checked = isSelected,
                                            onCheckedChange = null,
                                            colors = CheckboxDefaults.colors(
                                                checkedColor = if (isDark) Color.White else Color.White,
                                                uncheckedColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f),
                                                checkmarkColor = MaterialTheme.colorScheme.primary
                                            ),
                                            modifier = Modifier.size(18.dp)
                                        )
                                        Text(
                                            text = tag,
                                            style = MaterialTheme.typography.labelLarge,
                                            fontWeight = FontWeight.ExtraBold,
                                            color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            item {
                Column {
                    Spacer(modifier = Modifier.height(32.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 24.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Recent Transactions",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        
                        Surface(
                            onClick = { showSortPopup = true },
                            shape = RoundedCornerShape(14.dp),
                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                            modifier = Modifier.height(44.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 16.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Icon(MoneyVisorIcons.Filter, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Sort", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.primary)
                            }
                        }
                    }
                }
            }

            items(dashboardState.transactions, key = { it.id }) { transaction ->
                TransactionItem(
                    transaction = transaction,
                    currencyCode = settings.currencyCode,
                    isPrivacyMode = settings.isPrivacyModeEnabled,
                    modifier = Modifier
                        .animateItem()
                        .combinedClickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                            onClick = { 
                                if (transaction.isDraft) onNavigateToEditTransaction(transaction.id)
                                else selectedTransactionForDetails = transaction
                            },
                            onLongClick = { transactionForOptions = transaction }
                        )
                )
            }
            
            // Add See All Button at the end of transactions list
            if (dashboardState.transactions.isNotEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 16.dp, bottom = 32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Surface(
                            onClick = onNavigateToTimeline,
                            shape = RoundedCornerShape(20.dp),
                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.08f),
                            modifier = Modifier
                                .fillMaxWidth(0.6f)
                                .height(56.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(
                                    text = "See All History",
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontWeight = FontWeight.Black,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    if (showSortPopup) {
        SortPopup(
            selectedSort = dashboardState.selectedSort,
            onSortSelected = { viewModel.updateSort(it); showSortPopup = false },
            onDismiss = { showSortPopup = false }
        )
    }

    selectedTransactionForDetails?.let { transaction ->
        TransactionDetailsPopup(
            transaction = transaction, currencyCode = settings.currencyCode,
            onDismiss = { selectedTransactionForDetails = null }
        )
    }

    transactionForOptions?.let { transaction ->
        TransactionOptionsPopup(
            transaction = transaction,
            onDelete = { viewModel.deleteTransaction(transaction); transactionForOptions = null },
            onDismiss = { transactionForOptions = null }
        )
    }
}
