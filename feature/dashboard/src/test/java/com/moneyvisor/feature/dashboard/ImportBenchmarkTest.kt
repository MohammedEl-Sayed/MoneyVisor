package com.moneyvisor.feature.dashboard

import com.moneyvisor.domain.model.Transaction
import com.moneyvisor.domain.repository.TransactionRepository
import com.moneyvisor.domain.repository.BudgetRepository
import com.moneyvisor.domain.repository.GoalRepository
import com.moneyvisor.data.repository.UserPreferencesRepository
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.json.JSONArray
import org.json.JSONObject
import org.junit.After
import org.junit.Before
import org.junit.Test
import java.util.UUID
import kotlin.system.measureTimeMillis

@OptIn(ExperimentalCoroutinesApi::class)
class ImportBenchmarkTest {

    private val repository: TransactionRepository = mockk()
    private val budgetRepository: BudgetRepository = mockk()
    private val goalRepository: GoalRepository = mockk()
    private val userPrefs: UserPreferencesRepository = mockk()

    private lateinit var viewModel: DashboardViewModel
    private val testDispatcher = UnconfinedTestDispatcher()

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        every { repository.getTransactions() } returns flowOf(emptyList())
        every { budgetRepository.getBudgets() } returns flowOf(emptyList())
        every { goalRepository.getGoals() } returns flowOf(emptyList())

        every { userPrefs.chartInterval } returns flowOf("MONTHLY")
        every { userPrefs.currencyCode } returns flowOf("USD")
        every { userPrefs.themeMode } returns flowOf("SYSTEM")
        every { userPrefs.isBiometricEnabled } returns flowOf(false)
        every { userPrefs.isPrivacyModeEnabled } returns flowOf(false)
        every { userPrefs.isFabEnabled } returns flowOf(true)

        // Mock batch insert
        coEvery { repository.insertTransactions(any()) } returns Unit

        viewModel = DashboardViewModel(repository, budgetRepository, goalRepository, userPrefs)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `benchmark Batch import`() = runTest {
        val numRecords = 1000
        val jsonArray = JSONArray()
        for (i in 0 until numRecords) {
            val obj = JSONObject().apply {
                put("id", UUID.randomUUID().toString())
                put("amount", 10.0)
                put("category", "General")
                put("date", System.currentTimeMillis())
                put("type", "EXPENSE")
                put("tag", "tag")
                put("isDraft", false)
            }
            jsonArray.put(obj)
        }
        val jsonString = jsonArray.toString()

        val time = measureTimeMillis {
            viewModel.importDataFromJson(jsonString) { _ -> }
        }
        println("Batch Import took $time ms for $numRecords records")
    }
}
