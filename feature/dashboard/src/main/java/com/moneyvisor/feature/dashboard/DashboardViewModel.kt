package com.moneyvisor.feature.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.moneyvisor.domain.model.Transaction
import com.moneyvisor.domain.model.TransactionType
import com.moneyvisor.domain.model.Budget
import com.moneyvisor.domain.model.Goal
import com.moneyvisor.domain.repository.TransactionRepository
import com.moneyvisor.domain.repository.BudgetRepository
import com.moneyvisor.domain.repository.GoalRepository
import com.moneyvisor.data.repository.UserPreferencesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject
import java.util.Calendar
import java.util.UUID
import javax.inject.Inject

enum class SortType {
    DATE_DESC, DATE_ASC, AMOUNT_DESC, AMOUNT_ASC, CATEGORY_ASC
}

data class CategoryData(
    val category: String,
    val amount: Double,
    val percentage: Float
)

// Granular states for performance
data class DashboardState(
    val transactions: List<Transaction> = emptyList(),
    val totalIncome: Double = 0.0,
    val totalSpent: Double = 0.0,
    val balance: Double = 0.0,
    val incomeChartData: List<Float> = emptyList(),
    val expenseChartData: List<Float> = emptyList(),
    val totalChartData: List<Float> = emptyList(),
    val availableTags: List<String> = emptyList(),
    val selectedTags: Set<String> = emptySet(),
    val selectedSort: SortType = SortType.DATE_DESC,
    val chartInterval: String = "WEEKLY"
)

data class TimelineState(
    val transactions: List<Transaction> = emptyList(),
    val availableTags: List<String> = emptyList(),
    val selectedTags: Set<String> = emptySet()
)

data class StatusState(
    val budgets: List<Budget> = emptyList(),
    val goals: List<Goal> = emptyList(),
    val categoryChartData: List<CategoryData> = emptyList(),
    val monthlyTrend: Float = 0f,
    val totalIncome: Double = 0.0,
    val totalSpent: Double = 0.0,
    val balance: Double = 0.0
)

data class SettingsUiState(
    val currencyCode: String = "USD",
    val isBiometricEnabled: Boolean = false,
    val isPrivacyModeEnabled: Boolean = false,
    val themeMode: String = "SYSTEM",
    val chartInterval: String = "WEEKLY",
    val isFabEnabled: Boolean = true
)

data class DashboardUiState(
    val dashboard: DashboardState = DashboardState(),
    val timeline: TimelineState = TimelineState(),
    val status: StatusState = StatusState(),
    val settings: SettingsUiState = SettingsUiState()
)

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val repository: TransactionRepository,
    private val budgetRepository: BudgetRepository,
    private val goalRepository: GoalRepository,
    private val userPrefs: UserPreferencesRepository
) : ViewModel() {

    private val _selectedTags = MutableStateFlow<Set<String>>(emptySet())
    private val _selectedSort = MutableStateFlow(SortType.DATE_DESC)
    private val _refreshTrigger = MutableStateFlow(System.currentTimeMillis())

    init {
        viewModelScope.launch {
            while(true) {
                kotlinx.coroutines.delay(60000)
                _refreshTrigger.value = System.currentTimeMillis()
            }
        }
    }

    // Split settingsFlow into multiple combines to stay under 5-parameter limit
    private val primarySettings = combine(
        userPrefs.chartInterval,
        userPrefs.currencyCode,
        userPrefs.themeMode
    ) { interval, currency, theme -> Triple(interval, currency, theme) }

    private val toggleSettings = combine(
        userPrefs.isBiometricEnabled,
        userPrefs.isPrivacyModeEnabled,
        userPrefs.isFabEnabled
    ) { biometric, privacy, fab -> Triple(biometric, privacy, fab) }

    val settingsState: StateFlow<SettingsUiState> = combine(
        primarySettings,
        toggleSettings
    ) { primary, toggles ->
        SettingsUiState(
            currencyCode = primary.second,
            isBiometricEnabled = toggles.first,
            isPrivacyModeEnabled = toggles.second,
            themeMode = primary.third,
            chartInterval = primary.first,
            isFabEnabled = toggles.third
        )
    }.flowOn(Dispatchers.Default).stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), SettingsUiState())

    private val transactionsFlow = repository.getTransactions().flowOn(Dispatchers.IO)
    private val budgetsFlow = budgetRepository.getBudgets().flowOn(Dispatchers.IO)
    private val goalsFlow = goalRepository.getGoals().flowOn(Dispatchers.IO)

    private val domainDataFlow = combine(
        transactionsFlow,
        budgetsFlow,
        goalsFlow
    ) { t, b, g -> Triple(t, b, g) }

    val uiState: StateFlow<DashboardUiState> = combine(
        domainDataFlow,
        _selectedTags,
        _selectedSort,
        settingsState,
        _refreshTrigger
    ) { domainData, tags, sort, settings, _ ->
        val transactions = domainData.first
        val budgets = domainData.second
        val goals = domainData.third
        
        val nonDraft = transactions.filter { !it.isDraft }
        val income = nonDraft.filter { it.type == TransactionType.INCOME }.sumOf { it.amount }
        val spent = nonDraft.filter { it.type == TransactionType.EXPENSE }.sumOf { it.amount }
        val allTags = nonDraft.map { it.tag }.filter { it.isNotEmpty() }.distinct().sorted()

        // 1. Dashboard calculations
        val filtered = if (tags.isEmpty()) transactions else transactions.filter { it.tag in tags }
        val sorted = when (sort) {
            SortType.DATE_DESC -> filtered.sortedByDescending { it.date }
            SortType.DATE_ASC -> filtered.sortedBy { it.date }
            SortType.AMOUNT_DESC -> filtered.sortedByDescending { it.amount }
            SortType.AMOUNT_ASC -> filtered.sortedBy { it.amount }
            SortType.CATEGORY_ASC -> filtered.sortedBy { it.category }
        }

        val daysCount = if (settings.chartInterval == "MONTHLY") 30 else 7
        val now = Calendar.getInstance()
        val lastNIncome = mutableListOf<Float>()
        val lastNSpent = mutableListOf<Float>()
        val lastNTotal = mutableListOf<Float>()
        
        for (i in (daysCount - 1) downTo 0) {
            val c = now.clone() as Calendar
            c.add(Calendar.DAY_OF_YEAR, -i)
            val sD = c.apply { set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0); set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0) }.timeInMillis
            val eD = c.apply { set(Calendar.HOUR_OF_DAY, 23); set(Calendar.MINUTE, 59); set(Calendar.SECOND, 59); set(Calendar.MILLISECOND, 999) }.timeInMillis
            
            val dI = nonDraft.filter { it.type == TransactionType.INCOME && it.date in sD..eD }.sumOf { it.amount }
            val dS = nonDraft.filter { it.type == TransactionType.EXPENSE && it.date in sD..eD }.sumOf { it.amount }
            lastNIncome.add(dI.toFloat())
            lastNSpent.add(dS.toFloat())
            lastNTotal.add((dI - dS).toFloat())
        }
        val maxVal = (lastNIncome + lastNSpent + lastNTotal.map { Math.abs(it) }).maxOrNull() ?: 1f

        // 2. Status calculations
        val budgetsWithSpending = budgets.map { budget ->
            val s = nonDraft.filter { it.type == TransactionType.EXPENSE && it.category == budget.category }.sumOf { it.amount }
            budget.copy(currentSpent = s)
        }
        
        val startOfCurrentMonth = Calendar.getInstance().apply {
            set(Calendar.DAY_OF_MONTH, 1); set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0); set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0)
        }.timeInMillis
        val currentMonthExpenses = nonDraft.filter { it.type == TransactionType.EXPENSE && it.date >= startOfCurrentMonth }
        val totalMonthSpent = currentMonthExpenses.sumOf { it.amount }
        val breakdown = currentMonthExpenses.groupBy { it.category }.map { (cat, list) ->
            val amt = list.sumOf { it.amount }
            CategoryData(cat, amt, if (totalMonthSpent > 0) (amt / totalMonthSpent).toFloat() else 0f)
        }.sortedByDescending { it.amount }

        val lastMonthCal = Calendar.getInstance().apply { 
            add(Calendar.MONTH, -1); set(Calendar.DAY_OF_MONTH, 1)
            set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0); set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0)
        }
        val lastMonthSpent = nonDraft.filter { it.type == TransactionType.EXPENSE && it.date >= lastMonthCal.timeInMillis && it.date < startOfCurrentMonth }.sumOf { it.amount }
        val trend = if (lastMonthSpent > 0) ((totalMonthSpent - lastMonthSpent) / lastMonthSpent * 100.0).toFloat() else 0f

        DashboardUiState(
            dashboard = DashboardState(
                transactions = sorted.take(10),
                totalIncome = income, totalSpent = spent, balance = income - spent,
                incomeChartData = lastNIncome.map { it / maxVal },
                expenseChartData = lastNSpent.map { it / maxVal },
                totalChartData = lastNTotal.map { it / maxVal },
                availableTags = allTags, selectedTags = tags, selectedSort = sort,
                chartInterval = settings.chartInterval
            ),
            timeline = TimelineState(
                transactions = sorted,
                availableTags = allTags, selectedTags = tags
            ),
            status = StatusState(
                budgets = budgetsWithSpending, goals = goals,
                categoryChartData = breakdown, monthlyTrend = trend,
                totalIncome = income, totalSpent = spent, balance = income - spent
            ),
            settings = settings
        )
    }.flowOn(Dispatchers.Default).stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), DashboardUiState())

    fun toggleTagFilter(tag: String) {
        val current = _selectedTags.value
        _selectedTags.value = if (tag in current) current - tag else current + tag
    }

    fun updateSort(sortType: SortType) {
        _selectedSort.value = sortType
    }

    fun updateChartInterval(interval: String) = viewModelScope.launch { userPrefs.setChartInterval(interval) }
    fun updateCurrencyCode(code: String) = viewModelScope.launch { userPrefs.setCurrencyCode(code) }
    fun toggleBiometric(enabled: Boolean) = viewModelScope.launch { userPrefs.setBiometricEnabled(enabled) }
    fun togglePrivacyMode(enabled: Boolean) = viewModelScope.launch { userPrefs.setPrivacyModeEnabled(enabled) }
    fun updateThemeMode(mode: String) = viewModelScope.launch { userPrefs.setThemeMode(mode) }
    fun toggleFabEnabled(enabled: Boolean) = viewModelScope.launch { userPrefs.setFabEnabled(enabled) }
    fun resetAllData() = viewModelScope.launch { repository.deleteAllTransactions() }

    fun exportDataToJson(onResult: (String) -> Unit) {
        viewModelScope.launch {
            val transactions = repository.getTransactions().first()
            val jsonArray = JSONArray()
            transactions.forEach { t ->
                val obj = JSONObject().apply {
                    put("id", t.id); put("amount", t.amount); put("category", t.category)
                    put("date", t.date); put("type", t.type.name); put("tag", t.tag); put("isDraft", t.isDraft)
                }
                jsonArray.put(obj)
            }
            onResult(jsonArray.toString(4))
        }
    }

    fun importDataFromJson(json: String, onComplete: (Boolean) -> Unit) {
        viewModelScope.launch {
            try {
                val regex = Regex("\\[[\\s\\S]*\\]")
                val cleanedJson = regex.find(json)?.value ?: json.trim()
                if (!cleanedJson.startsWith("[") || !cleanedJson.endsWith("]")) { onComplete(false); return@launch }
                val jsonArray = JSONArray(cleanedJson)
                val transactionsToInsert = mutableListOf<Transaction>()
                for (i in 0 until jsonArray.length()) {
                    val obj = jsonArray.getJSONObject(i)
                    transactionsToInsert.add(Transaction(
                        id = obj.optString("id", UUID.randomUUID().toString()),
                        amount = obj.optDouble("amount", 0.0),
                        category = obj.optString("category", "General"),
                        date = obj.optLong("date", System.currentTimeMillis()),
                        type = try { TransactionType.valueOf(obj.optString("type", "EXPENSE")) } catch(e: Exception) { TransactionType.EXPENSE },
                        tag = obj.optString("tag", ""), isDraft = obj.optBoolean("isDraft", false)
                    ))
                }
                repository.insertTransactions(transactionsToInsert)
                onComplete(true)
            } catch (e: Exception) { onComplete(false) }
        }
    }

    fun deleteTransaction(transaction: Transaction) = viewModelScope.launch { repository.deleteTransaction(transaction) }
    fun addBudget(category: String, limit: Double) = viewModelScope.launch { budgetRepository.insertBudget(Budget(UUID.randomUUID().toString(), category, limit)) }
    fun addGoal(name: String, target: Double) = viewModelScope.launch { goalRepository.insertGoal(Goal(UUID.randomUUID().toString(), name, target)) }
}
