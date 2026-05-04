package com.moneyvisor.feature.transaction

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.moneyvisor.core.designsystem.icons.MoneyVisorIcons

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransactionScreen(
    transactionId: String? = null,
    onNavigateBack: () -> Unit,
    viewModel: TransactionViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val sheetState = rememberModalBottomSheetState()
    var showAddTagSheet by remember { mutableStateOf(false) }
    var newTagText by remember { mutableStateOf("") }

    LaunchedEffect(transactionId) {
        if (transactionId != null) {
            viewModel.loadTransaction(transactionId)
        }
    }

    LaunchedEffect(uiState.isSaved) {
        if (uiState.isSaved) {
            onNavigateBack()
        }
    }

    if (showAddTagSheet) {
        ModalBottomSheet(
            onDismissRequest = { showAddTagSheet = false },
            sheetState = sheetState,
            containerColor = MaterialTheme.colorScheme.surface,
            dragHandle = { BottomSheetDefaults.DragHandle(color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)) }
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp)
                    .padding(bottom = 40.dp)
            ) {
                Text(
                    "Create New Tag",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                
                Text(
                    "Organize your transactions better with custom labels",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 8.dp)
                )
                
                Spacer(modifier = Modifier.height(32.dp))
                
                TextField(
                    value = newTagText,
                    onValueChange = { newTagText = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(64.dp),
                    placeholder = { Text("Tag Name (e.g. Vacation, Office)") },
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                        unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                        focusedIndicatorColor = MaterialTheme.colorScheme.primary,
                        unfocusedIndicatorColor = Color.Transparent,
                        focusedTextColor = MaterialTheme.colorScheme.onSurface,
                        unfocusedTextColor = MaterialTheme.colorScheme.onSurface
                    ),
                    shape = RoundedCornerShape(16.dp),
                    singleLine = true
                )
                
                Spacer(modifier = Modifier.height(32.dp))
                
                Button(
                    onClick = {
                        if (newTagText.isNotBlank()) {
                            viewModel.addCustomTag(newTagText)
                            newTagText = ""
                            showAddTagSheet = false
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(60.dp),
                    shape = RoundedCornerShape(20.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp)
                ) {
                    Text(
                        "Add Tag", 
                        fontWeight = FontWeight.Bold, 
                        color = Color.White,
                        fontSize = 16.sp
                    )
                }
            }
        }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
        ) {
            // Header with Gradient
            TransactionHeader(
                transactionId = transactionId,
                onNavigateBack = onNavigateBack,
                onDelete = viewModel::deleteTransaction
            )

            // Form Section
            Column(
                modifier = Modifier
                    .offset(y = (-20).dp)
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp))
                    .background(MaterialTheme.colorScheme.surface)
                    .padding(24.dp)
            ) {
                // Type Selector
                TransactionTypeSelector(
                    selectedType = uiState.type,
                    onTypeChange = viewModel::onTypeChange
                )

                Spacer(modifier = Modifier.height(32.dp))

                // Amount Section
                TransactionAmountInput(
                    amount = uiState.amount,
                    onAmountChange = viewModel::onAmountChange
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Category Section
                TransactionCategoryInput(
                    category = uiState.category,
                    onCategoryChange = viewModel::onCategoryChange
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Tags Section
                TransactionTagsSection(
                    availableTags = uiState.availableTags,
                    selectedTag = uiState.tag,
                    onTagChange = viewModel::onTagChange,
                    onAddTagClick = { showAddTagSheet = true }
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Repetition Section
                TransactionRepetitionSection(
                    isRepeated = uiState.isRepeated,
                    repeatInterval = uiState.repeatInterval,
                    repeatValue = uiState.repeatValue,
                    onRepeatedChange = viewModel::onRepeatedChange,
                    onRepeatIntervalChange = viewModel::onRepeatIntervalChange,
                    onRepeatValueChange = viewModel::onRepeatValueChange
                )

                Spacer(modifier = Modifier.height(48.dp))

                Button(
                    onClick = viewModel::saveTransaction,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(64.dp),
                    shape = RoundedCornerShape(20.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = MoneyVisorIcons.Check,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(24.dp)
                        )
                        Text(
                            text = if (uiState.isDraft) "Confirm Transaction" else "Save Transaction",
                            color = Color.White,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Black
                        )
                    }
                }
                
                if (transactionId == null || uiState.isDraft) {
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    OutlinedButton(
                        onClick = viewModel::saveTransactionAsDraft,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(64.dp),
                        shape = RoundedCornerShape(20.dp),
                        border = androidx.compose.foundation.BorderStroke(2.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.primary)
                    ) {
                        Text(
                            text = "Save as Draft",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Black
                        )
                    }
                }
                
                if (uiState.error != null) {
                    Text(
                        text = uiState.error!!,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.padding(top = 16.dp),
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
                
                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }
}
