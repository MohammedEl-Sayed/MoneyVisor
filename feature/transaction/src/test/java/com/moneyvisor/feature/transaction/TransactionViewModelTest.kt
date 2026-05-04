package com.moneyvisor.feature.transaction

import app.cash.turbine.test
import com.moneyvisor.domain.model.RepeatInterval
import com.moneyvisor.domain.model.Transaction
import com.moneyvisor.domain.model.TransactionType
import com.moneyvisor.domain.repository.TransactionRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.util.UUID

@OptIn(ExperimentalCoroutinesApi::class)
class TransactionViewModelTest {

    private val repository: TransactionRepository = mockk()
    private lateinit var viewModel: TransactionViewModel
    private val testDispatcher = UnconfinedTestDispatcher()

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        viewModel = TransactionViewModel(repository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `saveTransaction with empty amount sets error state`() = runTest {
        viewModel.onAmountChange("")
        viewModel.saveTransaction()

        viewModel.uiState.test {
            val state = awaitItem()
            assertEquals("Please enter a valid amount", state.error)
        }
    }

    @Test
    fun `saveTransaction with negative amount sets error state`() = runTest {
        viewModel.onAmountChange("-10.0")
        viewModel.saveTransaction()

        viewModel.uiState.test {
            val state = awaitItem()
            assertEquals("Please enter a valid amount", state.error)
        }
    }

    @Test
    fun `saveTransaction with zero amount sets error state`() = runTest {
        viewModel.onAmountChange("0")
        viewModel.saveTransaction()

        viewModel.uiState.test {
            val state = awaitItem()
            assertEquals("Please enter a valid amount", state.error)
        }
    }

    @Test
    fun `saveTransaction with empty category sets error state`() = runTest {
        viewModel.onAmountChange("100")
        viewModel.onCategoryChange("")
        viewModel.saveTransaction()

        viewModel.uiState.test {
            val state = awaitItem()
            assertEquals("Please enter a category", state.error)
        }
    }

    @Test
    fun `saveTransaction successfully saves transaction`() = runTest {
        coEvery { repository.insertTransaction(any()) } returns Unit

        viewModel.onAmountChange("100")
        viewModel.onCategoryChange("Food")
        viewModel.onTypeChange(TransactionType.EXPENSE)
        viewModel.saveTransaction()

        viewModel.uiState.test {
            val state = awaitItem()
            assertTrue(state.isSaved)
            assertEquals(null, state.error)
        }

        coVerify {
            repository.insertTransaction(match {
                it.amount == 100.0 &&
                it.category == "Food" &&
                it.type == TransactionType.EXPENSE &&
                !it.isDraft
            })
        }
    }
}
