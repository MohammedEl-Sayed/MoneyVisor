package com.moneyvisor.feature.dashboard

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.hilt.navigation.compose.hiltViewModel
import com.moneyvisor.core.designsystem.icons.MoneyVisorIcons
import com.moneyvisor.core.designsystem.theme.BrandBlue
import com.moneyvisor.feature.dashboard.components.TransactionItem
import com.moneyvisor.feature.dashboard.components.TransactionDetailsPopup
import com.moneyvisor.feature.dashboard.util.CurrencyUtils
import com.moneyvisor.domain.model.Transaction
import java.util.*

// Moved outside to fix scope and type inference
data class DateItem(
    val timeInMillis: Long,
    val day: Int,
    val dayName: String,
    val month: Int,
    val year: Int
)

@Composable
fun TimelineScreen(
    isVisible: Boolean = true,
    viewModel: DashboardViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val timelineState = uiState.timeline
    val settings = uiState.settings
    
    var startDate by remember { mutableStateOf<Calendar?>(Calendar.getInstance()) }
    var endDate by remember { mutableStateOf<Calendar?>(null) }
    var isRangeSelectionActive by remember { mutableStateOf(false) }
    
    var showFullCalendarPopup by remember { mutableStateOf(false) }
    var isMonthYearPickerVisible by remember { mutableStateOf(false) }
    var selectedTransactionForDetails by remember { mutableStateOf<Transaction?>(null) }
    var searchQuery by remember { mutableStateOf("") }

    val listState = rememberLazyListState()
    
    val onDateSelected: (Calendar) -> Unit = { date ->
        startDate = date
        endDate = null
        isRangeSelectionActive = false
    }

    val onDateLongSelected: (Calendar) -> Unit = { date ->
        if (!isRangeSelectionActive || endDate != null) {
            startDate = date
            endDate = null
            isRangeSelectionActive = true
        } else {
            if (!isDateSameDay(date, startDate)) {
                if (date.before(startDate)) {
                    endDate = startDate
                    startDate = date
                } else {
                    endDate = date
                }
                isRangeSelectionActive = false
            } else {
                isRangeSelectionActive = false
            }
        }
    }

    val filteredTransactions = remember(startDate, endDate, timelineState.transactions, searchQuery) {
        val start = startDate?.clone() as? Calendar ?: Calendar.getInstance()
        val startOfDay = start.apply {
            set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0); set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0)
        }.timeInMillis
        val end = (endDate ?: startDate)?.clone() as? Calendar ?: Calendar.getInstance()
        val endOfDay = end.apply {
            set(Calendar.HOUR_OF_DAY, 23); set(Calendar.MINUTE, 59); set(Calendar.SECOND, 59); set(Calendar.MILLISECOND, 999)
        }.timeInMillis
        
        timelineState.transactions.filter { 
            val matchesDate = it.date in startOfDay..endOfDay
            val matchesSearch = if (searchQuery.isEmpty()) true 
                               else it.category.contains(searchQuery, ignoreCase = true) || 
                                    it.tag.contains(searchQuery, ignoreCase = true)
            matchesDate && matchesSearch
        }
    }

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
            item {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = MaterialTheme.colorScheme.surface,
                    shape = RoundedCornerShape(bottomStart = 32.dp, bottomEnd = 32.dp),
                    shadowElevation = 0.dp
                ) {
                    Column(modifier = Modifier.padding(24.dp)) {
                        Text(
                            text = "Timeline",
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        
                        Spacer(modifier = Modifier.height(24.dp))
                        
                        OutlinedTextField(
                            value = searchQuery,
                            onValueChange = { searchQuery = it },
                            placeholder = { Text("Search transactions, tags...") },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            singleLine = true,
                            leadingIcon = { Icon(MoneyVisorIcons.Search, null, tint = MaterialTheme.colorScheme.onSurfaceVariant) },
                            trailingIcon = if (searchQuery.isNotEmpty()) {
                                { 
                                    IconButton(onClick = { searchQuery = "" }) { 
                                        Icon(MoneyVisorIcons.Close, null, tint = MaterialTheme.colorScheme.onSurfaceVariant) 
                                    } 
                                }
                            } else null,
                            colors = TextFieldDefaults.colors(
                                focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                                unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                                focusedIndicatorColor = Color.Transparent,
                                unfocusedIndicatorColor = Color.Transparent,
                                focusedTextColor = MaterialTheme.colorScheme.onBackground,
                                unfocusedTextColor = MaterialTheme.colorScheme.onBackground
                            )
                        )

                        Spacer(modifier = Modifier.height(24.dp))
                        
                        val headerDate = startDate ?: Calendar.getInstance()
                        val monthYearFormat = remember { java.text.SimpleDateFormat("MMMM yyyy", Locale.getDefault()) }
                        
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(64.dp)
                                    .clip(RoundedCornerShape(20.dp))
                                    .background(MaterialTheme.colorScheme.surfaceVariant)
                                    .clickable { isMonthYearPickerVisible = true },
                                contentAlignment = Alignment.CenterStart
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 20.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(MoneyVisorIcons.Calendar, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(24.dp))
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Text(
                                        text = monthYearFormat.format(headerDate.time),
                                        style = MaterialTheme.typography.titleLarge,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                            
                            Spacer(modifier = Modifier.width(12.dp))
                            
                            Box(
                                modifier = Modifier
                                    .size(64.dp)
                                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f), RoundedCornerShape(20.dp))
                                    .clickable { showFullCalendarPopup = true },
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = MoneyVisorIcons.ArrowDown,
                                    contentDescription = "Expand Calendar",
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(32.dp)
                                )
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(24.dp))
                        
                        ScrollableWeeklyPicker(
                            startDate = startDate,
                            endDate = endDate,
                            onDateSelected = onDateSelected,
                            onDateLongSelected = onDateLongSelected
                        )
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(24.dp))
                TagsFilterSection(
                    availableTags = timelineState.availableTags,
                    selectedTags = timelineState.selectedTags,
                    onTagToggle = viewModel::toggleTagFilter
                )
            }

            item {
                Spacer(modifier = Modifier.height(16.dp))
                if (filteredTransactions.isNotEmpty()) {
                    Text(
                        text = "Transactions",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground,
                        modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp)
                    )
                }
            }

            if (filteredTransactions.isEmpty()) {
                item {
                    Box(modifier = Modifier.fillMaxWidth().padding(48.dp), contentAlignment = Alignment.Center) {
                        Text("No transactions found", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            } else {
                items(filteredTransactions, key = { it.id }) { transaction ->
                    TransactionItem(
                        transaction = transaction,
                        currencyCode = settings.currencyCode,
                        isPrivacyMode = settings.isPrivacyModeEnabled,
                        modifier = Modifier.clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) {
                            if (!transaction.isDraft) selectedTransactionForDetails = transaction
                        }
                    )
                }
            }
        }
    }

    if (isMonthYearPickerVisible) {
        val headerDate = startDate ?: Calendar.getInstance()
        Dialog(onDismissRequest = { isMonthYearPickerVisible = false }) {
            Surface(
                shape = RoundedCornerShape(32.dp),
                color = MaterialTheme.colorScheme.surface,
                modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp)
            ) {
                MonthYearPicker(
                    initialDate = headerDate,
                    onConfirm = { month, year ->
                        startDate = (headerDate.clone() as Calendar).apply {
                            set(Calendar.MONTH, month); set(Calendar.YEAR, year); set(Calendar.DAY_OF_MONTH, 1)
                        }
                        endDate = null
                        isMonthYearPickerVisible = false
                    },
                    onDismiss = { isMonthYearPickerVisible = false }
                )
            }
        }
    }

    if (showFullCalendarPopup) {
        Dialog(
            onDismissRequest = { showFullCalendarPopup = false },
            properties = DialogProperties(usePlatformDefaultWidth = false)
        ) {
            Surface(
                shape = RoundedCornerShape(32.dp),
                color = MaterialTheme.colorScheme.surface,
                modifier = Modifier
                    .fillMaxWidth(0.95f)
                    .padding(vertical = 24.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = if (isRangeSelectionActive) "Selecting End Date..." else "Select Date Range",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Black,
                        color = if (isRangeSelectionActive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.padding(start = 12.dp)
                    )
                    Text(
                        text = "Long press two dates for range, tap for single day",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                        modifier = Modifier.padding(start = 12.dp)
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                    
                    FullMonthGrid(
                        startDate = startDate,
                        endDate = endDate,
                        onDateSelected = onDateSelected,
                        onDateLongSelected = onDateLongSelected
                    )
                    
                    Spacer(modifier = Modifier.height(32.dp))
                    
                    Button(
                        onClick = { showFullCalendarPopup = false },
                        modifier = Modifier.fillMaxWidth().height(64.dp),
                        shape = RoundedCornerShape(20.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = Color.White
                        )
                    ) {
                        Text("Apply & Close", fontWeight = FontWeight.Black, fontSize = 18.sp)
                    }
                }
            }
        }
    }

    selectedTransactionForDetails?.let { transaction ->
        TransactionDetailsPopup(
            transaction = transaction, 
            currencyCode = settings.currencyCode,
            onDismiss = { selectedTransactionForDetails = null }
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ScrollableWeeklyPicker(
    startDate: Calendar?,
    endDate: Calendar?,
    onDateSelected: (Calendar) -> Unit,
    onDateLongSelected: (Calendar) -> Unit
) {
    val rangeCount = 2000
    val initialIndex = rangeCount / 2
    val listState = rememberLazyListState(initialFirstVisibleItemIndex = initialIndex - 3)
    
    val dateItems = remember {
        val base = Calendar.getInstance()
        val dayFormat = java.text.SimpleDateFormat("EEE", Locale.getDefault())
        List(rangeCount) { i ->
            val date = (base.clone() as Calendar).apply { add(Calendar.DAY_OF_MONTH, i - initialIndex) }
            DateItem(
                timeInMillis = date.timeInMillis,
                day = date.get(Calendar.DAY_OF_MONTH),
                dayName = dayFormat.format(date.time),
                month = date.get(Calendar.MONTH),
                year = date.get(Calendar.YEAR)
            )
        }
    }

    LazyRow(
        state = listState,
        modifier = Modifier.fillMaxWidth(),
        contentPadding = PaddingValues(horizontal = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(dateItems, key = { it.timeInMillis }) { item ->
            val isSelected = remember(item.timeInMillis, startDate, endDate) {
                isDateSameDay(item.timeInMillis, startDate) || isDateSameDay(item.timeInMillis, endDate)
            }
            val isInRange = remember(item.timeInMillis, startDate, endDate) {
                isDateInRange(item.timeInMillis, startDate, endDate)
            }
            
            Column(
                modifier = Modifier
                    .width(56.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(when { 
                        isSelected -> MaterialTheme.colorScheme.primary 
                        isInRange -> MaterialTheme.colorScheme.primary.copy(alpha = 0.25f)
                        else -> MaterialTheme.colorScheme.surfaceVariant
                    })
                    .combinedClickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = { 
                            val cal = Calendar.getInstance().apply { timeInMillis = item.timeInMillis }
                            onDateSelected(cal) 
                        },
                        onLongClick = { 
                            val cal = Calendar.getInstance().apply { timeInMillis = item.timeInMillis }
                            onDateLongSelected(cal) 
                        }
                    )
                    .padding(vertical = 12.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = item.day.toString(), 
                    fontWeight = FontWeight.Bold, 
                    color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = item.dayName, 
                    fontSize = 10.sp, 
                    color = if (isSelected) Color.White.copy(alpha = 0.7f) else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                )
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun FullMonthGrid(
    startDate: Calendar?,
    endDate: Calendar?,
    onDateSelected: (Calendar) -> Unit,
    onDateLongSelected: (Calendar) -> Unit
) {
    val baseDate = startDate ?: Calendar.getInstance()
    val calendar = (baseDate.clone() as Calendar).apply { set(Calendar.DAY_OF_MONTH, 1) }
    val firstDayOfWeek = calendar.get(Calendar.DAY_OF_WEEK) - 1
    val daysInMonth = calendar.getActualMaximum(Calendar.DAY_OF_MONTH)
    val days = mutableListOf<Calendar?>()
    for (i in 0 until firstDayOfWeek) days.add(null)
    for (i in 1..daysInMonth) days.add((calendar.clone() as Calendar).apply { set(Calendar.DAY_OF_MONTH, i) })

    Column {
        Row(modifier = Modifier.fillMaxWidth()) {
            listOf("S", "M", "T", "W", "T", "F", "S").forEach {
                Text(
                    text = it, 
                    modifier = Modifier.weight(1f), 
                    textAlign = TextAlign.Center, 
                    style = MaterialTheme.typography.labelLarge, 
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                    fontWeight = FontWeight.ExtraBold
                )
            }
        }
        Spacer(modifier = Modifier.height(20.dp))
        
        days.chunked(7).forEach { week ->
            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                week.forEach { date ->
                    if (date != null) {
                        val isSelected = isDateSameDay(date, startDate) || isDateSameDay(date, endDate)
                        val isInRange = isDateInRange(date, startDate, endDate)
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .aspectRatio(1f)
                                .padding(2.dp)
                                .clip(CircleShape)
                                .background(when { 
                                    isSelected -> MaterialTheme.colorScheme.primary 
                                    isInRange -> MaterialTheme.colorScheme.primary.copy(alpha = 0.25f)
                                    else -> Color.Transparent 
                                })
                                .combinedClickable(
                                    interactionSource = remember { MutableInteractionSource() },
                                    indication = null,
                                    onClick = { onDateSelected(date) },
                                    onLongClick = { onDateLongSelected(date) }
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = date.get(Calendar.DAY_OF_MONTH).toString(), 
                                fontWeight = if (isSelected) FontWeight.Black else FontWeight.Bold, 
                                color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface,
                                fontSize = 18.sp
                            )
                        }
                    } else Spacer(modifier = Modifier.weight(1f).aspectRatio(1f))
                }
                if (week.size < 7) repeat(7 - week.size) { Spacer(modifier = Modifier.weight(1f).aspectRatio(1f)) }
            }
        }
    }
}

@Composable
fun MonthYearPicker(
    initialDate: Calendar,
    onConfirm: (Int, Int) -> Unit,
    onDismiss: () -> Unit
) {
    var selectedMonth by remember { mutableIntStateOf(initialDate.get(Calendar.MONTH)) }
    var selectedYear by remember { mutableIntStateOf(initialDate.get(Calendar.YEAR)) }
    var selectionMode by remember { mutableStateOf("MONTH") }
    val months = listOf("Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec")
    val years = (2020..2050).toList()

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(16.dp))
                    .background(if (selectionMode == "YEAR") MaterialTheme.colorScheme.primary.copy(alpha = 0.1f) else Color.Transparent)
                    .clickable { selectionMode = if (selectionMode == "MONTH") "YEAR" else "MONTH" }
                    .padding(horizontal = 24.dp, vertical = 8.dp)
            ) {
                Text(text = selectedYear.toString(), style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Black, color = if (selectionMode == "YEAR") MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onBackground)
            }
        }
        Spacer(modifier = Modifier.height(24.dp))
        if (selectionMode == "MONTH") {
            androidx.compose.foundation.lazy.grid.LazyVerticalGrid(
                columns = androidx.compose.foundation.lazy.grid.GridCells.Fixed(3),
                modifier = Modifier.height(240.dp),
                contentPadding = PaddingValues(8.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(months.size) { index ->
                    val isSelected = index == selectedMonth
                    Box(
                        modifier = Modifier
                            .height(60.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant)
                            .clickable { selectedMonth = index },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = months[index], fontWeight = FontWeight.ExtraBold, color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        } else {
            val pickerListState = rememberLazyListState(initialFirstVisibleItemIndex = (years.indexOf(selectedYear) - 1).coerceAtLeast(0))
            LazyColumn(state = pickerListState, modifier = Modifier.height(240.dp), contentPadding = PaddingValues(vertical = 80.dp), verticalArrangement = Arrangement.spacedBy(8.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                items(years.size) { index ->
                    val year = years[index]
                    val isSelected = year == selectedYear
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(0.7f)
                            .height(64.dp)
                            .clip(RoundedCornerShape(20.dp))
                            .background(if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent)
                            .clickable { selectedYear = year; selectionMode = "MONTH" },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = year.toString(), style = if (isSelected) MaterialTheme.typography.headlineSmall else MaterialTheme.typography.titleMedium, fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.Bold, color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                        )
                    }
                }
            }
        }
        Spacer(modifier = Modifier.height(32.dp))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(20.dp))
                    .clickable { onDismiss() }, 
                contentAlignment = Alignment.Center
            ) {
                Icon(imageVector = MoneyVisorIcons.Close, contentDescription = "Cancel", tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(32.dp))
            }
            Button(
                onClick = { onConfirm(selectedMonth, selectedYear) }, 
                modifier = Modifier.weight(1f).height(64.dp), 
                shape = RoundedCornerShape(20.dp), 
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(MoneyVisorIcons.Check, null, tint = Color.White, modifier = Modifier.size(24.dp))
                    Text("Confirm", fontWeight = FontWeight.Black, fontSize = 18.sp, color = Color.White)
                }
            }
        }
    }
}

@Composable
fun TagsFilterSection(
    availableTags: List<String>,
    selectedTags: Set<String>,
    onTagToggle: (String) -> Unit
) {
    if (availableTags.isEmpty()) return
    val isDark = MaterialTheme.colorScheme.surface.luminance() < 0.5f
    Column(modifier = Modifier.padding(horizontal = 24.dp)) {
        Text(text = "Filter by Tags", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground)
        Spacer(modifier = Modifier.height(12.dp))
        androidx.compose.foundation.lazy.LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            items(availableTags) { tag ->
                val isSelected = tag in selectedTags
                val containerColor = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface
                val borderColor = if (isSelected) Color.Transparent else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f)
                val interactionSource = remember { MutableInteractionSource() }
                
                Box(
                    modifier = Modifier
                        .height(44.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(containerColor)
                        .border(BorderStroke(1.dp, borderColor), RoundedCornerShape(16.dp))
                        .clickable(interactionSource = interactionSource, indication = null) { onTagToggle(tag) }
                        .padding(horizontal = 12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
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
                            style = MaterialTheme.typography.labelMedium, 
                            fontWeight = FontWeight.Bold, 
                            color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
        }
    }
}

private fun isDateSameDay(c1: Calendar?, c2: Calendar?): Boolean {
    if (c1 == null || c2 == null) return false
    return c1.get(Calendar.YEAR) == c2.get(Calendar.YEAR) && c1.get(Calendar.DAY_OF_YEAR) == c2.get(Calendar.DAY_OF_YEAR)
}

private fun isDateSameDay(time: Long, c2: Calendar?): Boolean {
    if (c2 == null) return false
    val c1 = Calendar.getInstance().apply { timeInMillis = time }
    return isDateSameDay(c1, c2)
}

private fun isDateInRange(time: Long, start: Calendar?, end: Calendar?): Boolean {
    if (start == null || end == null) return false
    val sTime = (if (start.before(end)) start else end).clone() as Calendar 
    sTime.apply { set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0); set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0) }
    val eTime = (if (start.after(end)) start else end).clone() as Calendar
    eTime.apply { set(Calendar.HOUR_OF_DAY, 23); set(Calendar.MINUTE, 59); set(Calendar.SECOND, 59); set(Calendar.MILLISECOND, 999) }
    
    return time in sTime.timeInMillis..eTime.timeInMillis
}

private fun isDateInRange(date: Calendar, start: Calendar?, end: Calendar?): Boolean {
    return isDateInRange(date.timeInMillis, start, end)
}
