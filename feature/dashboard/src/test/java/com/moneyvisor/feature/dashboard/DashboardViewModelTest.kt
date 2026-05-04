package com.moneyvisor.feature.dashboard

import app.cash.turbine.test
import com.moneyvisor.domain.model.Transaction
import com.moneyvisor.domain.model.TransactionType
import com.moneyvisor.domain.repository.TransactionRepository
import com.moneyvisor.domain.repository.BudgetRepository
import com.moneyvisor.domain.repository.GoalRepository
import com.moneyvisor.data.repository.UserPreferencesRepository
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import java.util.Calendar

@OptIn(ExperimentalCoroutinesApi::class)
class DashboardViewModelTest {

    private val repository: TransactionRepository = mockk()
    private val budgetRepository: BudgetRepository = mockk()
    private val goalRepository: GoalRepository = mockk()
    private val userPrefs: UserPreferencesRepository = mockk()
    private lateinit var viewModel: DashboardViewModel
    private val testDispatcher = UnconfinedTestDispatcher()

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        every { budgetRepository.getBudgets() } returns flowOf(emptyList())
        every { goalRepository.getGoals() } returns flowOf(emptyList())
        every { userPrefs.chartInterval } returns flowOf("WEEKLY")
        every { userPrefs.currencyCode } returns flowOf("USD")
        every { userPrefs.themeMode } returns flowOf("SYSTEM")
        every { userPrefs.isBiometricEnabled } returns flowOf(false)
        every { userPrefs.isPrivacyModeEnabled } returns flowOf(false)
        every { userPrefs.isFabEnabled } returns flowOf(true)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `uiState aggregates totals correctly`() = runTest {
        val transactions = listOf(
            Transaction(id = "1", amount = 1000.0, type = TransactionType.INCOME, category = "Salary", date = System.currentTimeMillis(), tag = "Salary"),
            Transaction(id = "2", amount = 200.0, type = TransactionType.EXPENSE, category = "Food", date = System.currentTimeMillis(), tag = "Food"),
            Transaction(id = "3", amount = 300.0, type = TransactionType.EXPENSE, category = "Rent", date = System.currentTimeMillis(), tag = "Rent")
        )
        every { repository.getTransactions() } returns flowOf(transactions)

        viewModel = DashboardViewModel(repository, budgetRepository, goalRepository, userPrefs)

        viewModel.uiState.test {
            val state = awaitItem()
            assertEquals(1000.0, state.dashboard.totalIncome, 0.001)
            assertEquals(500.0, state.dashboard.totalSpent, 0.001)
            assertEquals(500.0, state.dashboard.balance, 0.001)
            assertEquals(3, state.dashboard.transactions.size)
        }
    }

    @Test
    fun `uiState calculates chart data correctly`() = runTest {
        val cal = Calendar.getInstance()
        val today = cal.timeInMillis
        
        cal.add(Calendar.DAY_OF_YEAR, -1)
        val yesterday = cal.timeInMillis

        val transactions = listOf(
            Transaction(id = "1", amount = 100.0, type = TransactionType.EXPENSE, category = "Food", date = today, tag = "Food"),
            Transaction(id = "2", amount = 50.0, type = TransactionType.EXPENSE, category = "Transport", date = yesterday, tag = "Transport")
        )
        every { repository.getTransactions() } returns flowOf(transactions)

        viewModel = DashboardViewModel(repository, budgetRepository, goalRepository, userPrefs)

        viewModel.uiState.test {
            val state = awaitItem()
            // Last item in chartData is today, second to last is yesterday
            assertEquals(1f, state.dashboard.expenseChartData.last()) // Max is 100, so 100/100 = 1.0
            assertEquals(0.5f, state.dashboard.expenseChartData[state.dashboard.expenseChartData.size - 2]) // 50/100 = 0.5
        }
    }
}
