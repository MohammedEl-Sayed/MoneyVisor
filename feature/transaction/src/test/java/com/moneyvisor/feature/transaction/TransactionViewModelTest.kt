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

@OptIn(ExperimentalCoroutinesApi::class)
class TransactionViewModelTest {

    private val repository: TransactionRepository = mockk(relaxed = true)
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
    fun `loadTransaction updates uiState correctly`() = runTest {
        val transaction = Transaction(
            id = "test-id",
            amount = 150.0,
            type = TransactionType.EXPENSE,
            category = "Food",
            date = 123456789L,
            tag = "Lunch",
            isDraft = true,
            isRepeated = true,
            repeatInterval = RepeatInterval.DAYS,
            repeatValue = 2
        )
        coEvery { repository.getTransactionById("test-id") } returns transaction

        viewModel.loadTransaction("test-id")

        viewModel.uiState.test {
            val state = awaitItem()
            assertEquals("test-id", state.id)
            assertEquals("150.0", state.amount)
            assertEquals(TransactionType.EXPENSE, state.type)
            assertEquals("Food", state.category)
            assertEquals("Lunch", state.tag)
            assertTrue(state.isDraft)
            assertTrue(state.isRepeated)
            assertEquals(RepeatInterval.DAYS, state.repeatInterval)
            assertEquals("2", state.repeatValue)
        }
    }

    @Test
    fun `onAmountChange updates amount`() = runTest {
        viewModel.onAmountChange("200.5")

        viewModel.uiState.test {
            val state = awaitItem()
            assertEquals("200.5", state.amount)
        }
    }

    @Test
    fun `onTypeChange updates type`() = runTest {
        viewModel.onTypeChange(TransactionType.INCOME)

        viewModel.uiState.test {
            val state = awaitItem()
            assertEquals(TransactionType.INCOME, state.type)
        }
    }

    @Test
    fun `addCustomTag adds new tag and selects it`() = runTest {
        viewModel.addCustomTag("NewTag")

        viewModel.uiState.test {
            val state = awaitItem()
            assertTrue(state.availableTags.contains("NewTag"))
            assertEquals("NewTag", state.tag)
        }
    }

    @Test
    fun `addCustomTag ignores blank tag`() = runTest {
        val initialTagsSize = viewModel.uiState.value.availableTags.size
        viewModel.addCustomTag("  ")

        viewModel.uiState.test {
            val state = awaitItem()
            assertEquals(initialTagsSize, state.availableTags.size)
        }
    }

    @Test
    fun `saveTransaction saves correctly with valid data`() = runTest {
        viewModel.onAmountChange("100.0")
        viewModel.onCategoryChange("Food")

        viewModel.saveTransaction()

        viewModel.uiState.test {
            val state = awaitItem()
            assertTrue(state.isSaved)
            assertEquals(null, state.error)
        }
        coVerify(exactly = 1) { repository.insertTransaction(any()) }
    }

    @Test
    fun `saveTransaction sets error with invalid amount`() = runTest {
        viewModel.onAmountChange("invalid")
        viewModel.onCategoryChange("Food")

        viewModel.saveTransaction()

        viewModel.uiState.test {
            val state = awaitItem()
            assertEquals("Please enter a valid amount", state.error)
            assertFalse(state.isSaved)
        }
        coVerify(exactly = 0) { repository.insertTransaction(any()) }
    }

    @Test
    fun `saveTransaction sets error with empty category`() = runTest {
        viewModel.onAmountChange("100.0")
        viewModel.onCategoryChange("")

        viewModel.saveTransaction()

        viewModel.uiState.test {
            val state = awaitItem()
            assertEquals("Please enter a category", state.error)
            assertFalse(state.isSaved)
        }
        coVerify(exactly = 0) { repository.insertTransaction(any()) }
    }

    @Test
    fun `deleteTransaction deletes when id is not null`() = runTest {
        val transaction = Transaction(
            id = "test-id",
            amount = 150.0,
            type = TransactionType.EXPENSE,
            category = "Food",
            date = 123456789L
        )
        coEvery { repository.getTransactionById("test-id") } returns transaction

        viewModel.loadTransaction("test-id")
        viewModel.deleteTransaction()

        viewModel.uiState.test {
            val state = awaitItem()
            assertTrue(state.isSaved)
        }
        coVerify(exactly = 1) { repository.deleteTransaction(transaction) }
    }
}
