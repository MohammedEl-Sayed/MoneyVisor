package com.moneyvisor.feature.dashboard

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.hilt.navigation.compose.hiltViewModel
import com.moneyvisor.core.designsystem.theme.BrandBlue
import com.moneyvisor.core.designsystem.theme.CrimsonRuby
import com.moneyvisor.feature.dashboard.util.CurrencyInfo
import com.moneyvisor.feature.dashboard.util.CurrencyUtils
import kotlinx.coroutines.launch

@Composable
fun SettingsScreen(
    isVisible: Boolean = true,
    viewModel: DashboardViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val settings = uiState.settings
    
    var showResetDialog by remember { mutableStateOf(false) }
    var showCurrencyDialog by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    val isDark = MaterialTheme.colorScheme.surface.luminance() < 0.5f

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .verticalScroll(rememberScrollState())
                .padding(24.dp)
        ) {
            Text(
                text = "Settings",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
            
            Spacer(modifier = Modifier.height(32.dp))
            
            Text(
                text = "Preferences",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
            
            Spacer(modifier = Modifier.height(16.dp))

            // Theme Selection Card
            ThemeSelectionCard(
                isDark = isDark,
                currentTheme = settings.themeMode,
                onThemeSelected = { viewModel.updateThemeMode(it) }
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Currency Selection Card
            CurrencySelectionCard(
                isDark = isDark,
                currencyCode = settings.currencyCode,
                onClick = { showCurrencyDialog = true }
            )

            Spacer(modifier = Modifier.height(12.dp))
            
            // Data Reset Interval Card
            ChartIntervalCard(
                isDark = isDark,
                currentInterval = settings.chartInterval,
                onIntervalSelected = { viewModel.updateChartInterval(it) }
            )

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "Interface",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )

            Spacer(modifier = Modifier.height(16.dp))

            // FAB Toggle Card
            FabToggleCard(
                isDark = isDark,
                isEnabled = settings.isFabEnabled,
                onToggle = { viewModel.toggleFabEnabled(it) }
            )

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "Security & Privacy",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Biometric Lock Card
            BiometricLockCard(
                isDark = isDark,
                isEnabled = settings.isBiometricEnabled,
                onToggle = { viewModel.toggleBiometric(it) }
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Privacy Mode Card
            PrivacyModeCard(
                isDark = isDark,
                isEnabled = settings.isPrivacyModeEnabled,
                onToggle = { viewModel.togglePrivacyMode(it) }
            )

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "Data Management",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Data Management Section
            DataManagementSection(
                isDark = isDark,
                onExportClick = {
                    viewModel.exportDataToJson { json ->
                        coroutineScope.launch {
                            snackbarHostState.showSnackbar("Export prepared.")
                        }
                    }
                },
                onImportClick = { /* Launch file picker */ },
                onResetClick = { showResetDialog = true }
            )
            
            Spacer(modifier = Modifier.height(64.dp))
        }

        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 16.dp)
        )
    }

    if (showResetDialog) {
        ResetConfirmationDialog(
            onConfirm = {
                viewModel.resetAllData()
                showResetDialog = false
            },
            onDismiss = { showResetDialog = false }
        )
    }

    if (showCurrencyDialog) {
        CurrencySelectionDialog(
            selectedCode = settings.currencyCode,
            onCurrencySelected = {
                viewModel.updateCurrencyCode(it)
                showCurrencyDialog = false
            },
            onDismiss = { showCurrencyDialog = false }
        )
    }
}

@Composable
private fun SettingsCard(
    isDark: Boolean,
    containerColor: Color? = null,
    borderColor: Color? = null,
    onClick: (() -> Unit)? = null,
    content: @Composable () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        shape = RoundedCornerShape(20.dp),
        color = containerColor ?: MaterialTheme.colorScheme.surface,
        border = BorderStroke(
            width = 1.dp, 
            color = borderColor ?: if (isDark) Color.Transparent else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f)
        ),
        shadowElevation = 0.dp,
        onClick = onClick ?: {},
        enabled = onClick != null
    ) {
        Box(modifier = Modifier.padding(20.dp)) {
            content()
        }
    }
}

@Composable
private fun ThemeSelectionCard(
    isDark: Boolean,
    currentTheme: String,
    onThemeSelected: (String) -> Unit
) {
    SettingsCard(isDark = isDark) {
        Column {
            Text(
                text = "App Theme",
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(16.dp))
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    ThemeOption("LIGHT", currentTheme == "LIGHT", { onThemeSelected("LIGHT") }, Modifier.weight(1f))
                    ThemeOption("DARK", currentTheme == "DARK", { onThemeSelected("DARK") }, Modifier.weight(1f))
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    ThemeOption("AMOLED", currentTheme == "AMOLED", { onThemeSelected("AMOLED") }, Modifier.weight(1f))
                    ThemeOption("SYSTEM", currentTheme == "SYSTEM", { onThemeSelected("SYSTEM") }, Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
private fun CurrencySelectionCard(
    isDark: Boolean,
    currencyCode: String,
    onClick: () -> Unit
) {
    SettingsCard(
        isDark = isDark,
        onClick = onClick
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Currency",
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "Selected: ${currencyCode}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                )
            }
            Text(
                text = "Change",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}

@Composable
private fun ChartIntervalCard(
    isDark: Boolean,
    currentInterval: String,
    onIntervalSelected: (String) -> Unit
) {
    SettingsCard(isDark = isDark) {
        Column {
            Text(
                text = "Chart Data Reset",
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = "Choose how often dashboard charts reset.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
            )

            Spacer(modifier = Modifier.height(20.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                IntervalOption(
                    label = "Weekly",
                    isSelected = currentInterval == "WEEKLY",
                    onClick = { onIntervalSelected("WEEKLY") },
                    modifier = Modifier.weight(1f)
                )
                IntervalOption(
                    label = "Monthly",
                    isSelected = currentInterval == "MONTHLY",
                    onClick = { onIntervalSelected("MONTHLY") },
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun FabToggleCard(
    isDark: Boolean,
    isEnabled: Boolean,
    onToggle: (Boolean) -> Unit
) {
    SettingsCard(isDark = isDark) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Quick Add Button",
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "Show floating button to add money.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                )
            }
            CustomSwitch(
                checked = isEnabled,
                onCheckedChange = onToggle
            )
        }
    }
}

@Composable
private fun BiometricLockCard(
    isDark: Boolean,
    isEnabled: Boolean,
    onToggle: (Boolean) -> Unit
) {
    SettingsCard(isDark = isDark) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Biometric Lock",
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "Require fingerprint/face to open app.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                )
            }
            CustomSwitch(
                checked = isEnabled,
                onCheckedChange = onToggle
            )
        }
    }
}

@Composable
private fun PrivacyModeCard(
    isDark: Boolean,
    isEnabled: Boolean,
    onToggle: (Boolean) -> Unit
) {
    SettingsCard(isDark = isDark) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Privacy Mode",
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "Hide balances and transaction amounts.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                )
            }
            CustomSwitch(
                checked = isEnabled,
                onCheckedChange = onToggle
            )
        }
    }
}

@Composable
private fun DataManagementSection(
    isDark: Boolean,
    onExportClick: () -> Unit,
    onImportClick: () -> Unit,
    onResetClick: () -> Unit
) {
    // Import/Export Options
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        DataActionCard(
            title = "Export Data",
            subtitle = "Save to JSON",
            onClick = onExportClick,
            modifier = Modifier.weight(1f),
            isDark = isDark
        )
        DataActionCard(
            title = "Import Data",
            subtitle = "Load JSON",
            onClick = onImportClick,
            modifier = Modifier.weight(1f),
            isDark = isDark
        )
    }

    Spacer(modifier = Modifier.height(12.dp))

    // Reset Data Button Card
    SettingsCard(
        isDark = isDark,
        containerColor = if (isDark) CrimsonRuby.copy(alpha = 0.15f) else Color(0xFFFFF5F5),
        borderColor = if (isDark) CrimsonRuby.copy(alpha = 0.3f) else CrimsonRuby.copy(alpha = 0.1f),
        onClick = onResetClick
    ) {
        Column {
            Text(
                text = "Reset All Data",
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Bold,
                color = CrimsonRuby
            )
            Text(
                text = "Permanently delete all data.",
                style = MaterialTheme.typography.bodySmall,
                color = if (isDark) CrimsonRuby.copy(alpha = 0.7f) else Color(0xFFD32F2F).copy(alpha = 0.6f)
            )
        }
    }
}

@Composable
private fun CustomSwitch(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    val thumbOffset by animateDpAsState(
        targetValue = if (checked) 24.dp else 0.dp,
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
        label = "SwitchAnimation"
    )

    Box(
        modifier = Modifier
            .width(54.dp)
            .height(30.dp)
            .clip(RoundedCornerShape(15.dp))
            .background(if (checked) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.15f))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) { onCheckedChange(!checked) }
            .padding(3.dp),
        contentAlignment = Alignment.CenterStart
    ) {
        Box(
            modifier = Modifier
                .offset(x = thumbOffset)
                .size(24.dp)
                .clip(CircleShape)
                .background(Color.White)
        )
    }
}

@Composable
private fun ThemeOption(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .height(44.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun CurrencySelectionDialog(
    selectedCode: String,
    onCurrencySelected: (String) -> Unit,
    onDismiss: () -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    val currencies = remember { CurrencyUtils.getAllCurrencies() }
    val filteredCurrencies = remember(searchQuery) {
        if (searchQuery.isEmpty()) currencies
        else currencies.filter { 
            it.code.contains(searchQuery, ignoreCase = true) || 
            it.name.contains(searchQuery, ignoreCase = true) ||
            it.country.contains(searchQuery, ignoreCase = true)
        }
    }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(28.dp),
            color = MaterialTheme.colorScheme.surface,
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.85f)
                .padding(vertical = 16.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp)
            ) {
                Text(
                    text = "Select Currency",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Black,
                    color = MaterialTheme.colorScheme.onSurface
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Search currency...") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    singleLine = true,
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                        unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent,
                        focusedTextColor = MaterialTheme.colorScheme.onSurface,
                        unfocusedTextColor = MaterialTheme.colorScheme.onSurface
                    )
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(filteredCurrencies) { currency ->
                        val isSelected = currency.code == selectedCode
                        Surface(
                            onClick = { onCurrencySelected(currency.code) },
                            shape = RoundedCornerShape(16.dp),
                            color = if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.1f) else Color.Transparent,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "${currency.code} - ${currency.name}",
                                        style = MaterialTheme.typography.bodyLarge,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = currency.country,
                                        style = MaterialTheme.typography.labelMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                                    )
                                }
                                if (isSelected) {
                                    Icon(
                                        imageVector = Icons.Rounded.Check,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Button(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant,
                        contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                ) {
                    Text("Close", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
private fun DataActionCard(
    title: String,
    subtitle: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    isDark: Boolean
) {
    Surface(
        modifier = modifier.padding(vertical = 4.dp),
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, if (isDark) Color.Transparent else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f)),
        shadowElevation = 0.dp,
        onClick = onClick
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
            )
        }
    }
}

@Composable
private fun IntervalOption(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .height(50.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            fontWeight = FontWeight.Bold,
            color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun ResetConfirmationDialog(
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(28.dp),
            color = MaterialTheme.colorScheme.surface,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Are you sure?",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Black,
                    color = MaterialTheme.colorScheme.onSurface
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Text(
                    text = "This will permanently delete all your transactions. This action cannot be undone.",
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                Spacer(modifier = Modifier.height(32.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Button(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f).height(56.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant,
                            contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    ) {
                        Text("No", fontWeight = FontWeight.Bold)
                    }
                    
                    Button(
                        onClick = onConfirm,
                        modifier = Modifier.weight(1f).height(56.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = CrimsonRuby,
                            contentColor = Color.White
                        )
                    ) {
                        Text("Yes, Reset", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
