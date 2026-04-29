package com.moneyvisor.feature.transaction

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.moneyvisor.domain.model.RepeatInterval
import com.moneyvisor.domain.model.Transaction
import com.moneyvisor.domain.model.TransactionType
import com.moneyvisor.domain.repository.TransactionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

data class TransactionUiState(
    val id: String? = null,
    val amount: String = "",
    val type: TransactionType = TransactionType.EXPENSE,
    val category: String = "",
    val tag: String = "",
    val availableTags: List<String> = listOf("Food", "Salary", "Transport", "Fun"),
    val isSaved: Boolean = false,
    val isDraft: Boolean = false,
    val error: String? = null,
    val isRepeated: Boolean = false,
    val repeatInterval: RepeatInterval = RepeatInterval.NONE,
    val repeatValue: String = "1"
)

@HiltViewModel
class TransactionViewModel @Inject constructor(
    private val repository: TransactionRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(TransactionUiState())
    val uiState = _uiState.asStateFlow()

    fun loadTransaction(id: String) {
        viewModelScope.launch {
            repository.getTransactionById(id)?.let { t ->
                _uiState.update { it.copy(
                    id = t.id,
                    amount = t.amount.toString(),
                    type = t.type,
                    category = t.category,
                    tag = t.tag,
                    isDraft = t.isDraft,
                    isRepeated = t.isRepeated,
                    repeatInterval = t.repeatInterval,
                    repeatValue = t.repeatValue.toString()
                ) }
            }
        }
    }

    fun onAmountChange(amount: String) {
        _uiState.update { it.copy(amount = amount) }
    }

    fun onTypeChange(type: TransactionType) {
        _uiState.update { it.copy(type = type) }
    }

    fun onCategoryChange(category: String) {
        _uiState.update { it.copy(category = category) }
    }

    fun onTagChange(tag: String) {
        _uiState.update { it.copy(tag = tag) }
    }

    fun onRepeatedChange(isRepeated: Boolean) {
        _uiState.update { 
            it.copy(
                isRepeated = isRepeated,
                repeatInterval = if (isRepeated && it.repeatInterval == RepeatInterval.NONE) RepeatInterval.DAYS else it.repeatInterval
            ) 
        }
    }

    fun onRepeatIntervalChange(interval: RepeatInterval) {
        _uiState.update { it.copy(repeatInterval = interval) }
    }

    fun onRepeatValueChange(value: String) {
        _uiState.update { it.copy(repeatValue = value) }
    }

    fun addCustomTag(tag: String) {
        if (tag.isNotBlank() && !_uiState.value.availableTags.contains(tag)) {
            _uiState.update { 
                it.copy(
                    availableTags = it.availableTags + tag,
                    tag = tag
                )
            }
        }
    }

    fun saveTransaction() {
        saveInternal(isDraft = false)
    }

    fun saveTransactionAsDraft() {
        saveInternal(isDraft = true)
    }

    private fun saveInternal(isDraft: Boolean) {
        val amountValue = _uiState.value.amount.toDoubleOrNull()
        if (amountValue == null || amountValue <= 0) {
            _uiState.update { it.copy(error = "Please enter a valid amount") }
            return
        }
        if (_uiState.value.category.isBlank()) {
            _uiState.update { it.copy(error = "Please enter a category") }
            return
        }

        val repeatValueInt = _uiState.value.repeatValue.toIntOrNull() ?: 1

        viewModelScope.launch {
            val transaction = Transaction(
                id = _uiState.value.id ?: UUID.randomUUID().toString(),
                amount = amountValue,
                type = _uiState.value.type,
                category = _uiState.value.category,
                date = System.currentTimeMillis(),
                tag = _uiState.value.tag,
                isDraft = isDraft,
                isRepeated = _uiState.value.isRepeated,
                repeatInterval = if (_uiState.value.isRepeated) _uiState.value.repeatInterval else RepeatInterval.NONE,
                repeatValue = repeatValueInt
            )
            repository.insertTransaction(transaction)
            _uiState.update { it.copy(isSaved = true) }
        }
    }

    fun deleteTransaction() {
        _uiState.value.id?.let { id ->
            viewModelScope.launch {
                val t = repository.getTransactionById(id)
                if (t != null) {
                    repository.deleteTransaction(t)
                }
                _uiState.update { it.copy(isSaved = true) }
            }
        }
    }
}
