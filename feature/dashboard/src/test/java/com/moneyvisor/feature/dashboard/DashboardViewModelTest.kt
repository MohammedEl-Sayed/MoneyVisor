package com.moneyvisor.feature.dashboard

import app.cash.turbine.test
import com.moneyvisor.domain.model.Transaction
import com.moneyvisor.domain.model.TransactionType
import com.moneyvisor.domain.repository.TransactionRepository
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
    private val budgetRepository: com.moneyvisor.domain.repository.BudgetRepository = mockk()
    private val goalRepository: com.moneyvisor.domain.repository.GoalRepository = mockk()
    private val userPrefs: com.moneyvisor.data.repository.UserPreferencesRepository = mockk()
    private lateinit var viewModel: DashboardViewModel
    private val testDispatcher = UnconfinedTestDispatcher()

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        io.mockk.every { budgetRepository.getBudgets() } returns kotlinx.coroutines.flow.flowOf(emptyList())
        io.mockk.every { goalRepository.getGoals() } returns kotlinx.coroutines.flow.flowOf(emptyList())
        io.mockk.every { userPrefs.chartInterval } returns kotlinx.coroutines.flow.flowOf("WEEKLY")
        io.mockk.every { userPrefs.currencyCode } returns kotlinx.coroutines.flow.flowOf("USD")
        io.mockk.every { userPrefs.themeMode } returns kotlinx.coroutines.flow.flowOf("SYSTEM")
        io.mockk.every { userPrefs.isBiometricEnabled } returns kotlinx.coroutines.flow.flowOf(false)
        io.mockk.every { userPrefs.isPrivacyModeEnabled } returns kotlinx.coroutines.flow.flowOf(false)
        io.mockk.every { userPrefs.isFabEnabled } returns kotlinx.coroutines.flow.flowOf(true)
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
