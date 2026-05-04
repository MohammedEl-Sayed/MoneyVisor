package com.moneyvisor.data.repository

import com.moneyvisor.core.database.dao.BudgetDao
import com.moneyvisor.core.database.entity.BudgetEntity
import com.moneyvisor.domain.model.Budget
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class BudgetRepositoryImplTest {

    private val dao: BudgetDao = mockk()
    private val repository = BudgetRepositoryImpl(dao)

    @Test
    fun `getBudgets returns mapped domain models`() = runTest {
        val entities = listOf(
            BudgetEntity(id = "1", category = "Groceries", limitAmount = 500.0),
            BudgetEntity(id = "2", category = "Entertainment", limitAmount = 200.0)
        )
        every { dao.getBudgets() } returns flowOf(entities)

        repository.getBudgets().collect { budgets ->
            assertEquals(2, budgets.size)
            assertEquals("Groceries", budgets[0].category)
            assertEquals(500.0, budgets[0].limitAmount, 0.0)
            assertEquals(0.0, budgets[0].currentSpent, 0.0) // default value when mapping from entity
            assertEquals("Entertainment", budgets[1].category)
            assertEquals(200.0, budgets[1].limitAmount, 0.0)
        }
    }

    @Test
    fun `insertBudget calls dao insert`() = runTest {
        val budget = Budget(id = "1", category = "Dining", limitAmount = 150.0, currentSpent = 50.0)
        coEvery { dao.insertBudget(any()) } returns Unit

        repository.insertBudget(budget)

        coVerify { dao.insertBudget(match { it.id == "1" && it.category == "Dining" && it.limitAmount == 150.0 }) }
    }

    @Test
    fun `deleteBudget calls dao delete`() = runTest {
        val budget = Budget(id = "1", category = "Dining", limitAmount = 150.0)
        coEvery { dao.deleteBudget(any()) } returns Unit

        repository.deleteBudget(budget)

        coVerify { dao.deleteBudget(match { it.id == "1" && it.category == "Dining" }) }
    }

    @Test
    fun `getBudgetByCategory returns mapped domain model when found`() = runTest {
        val entity = BudgetEntity(id = "1", category = "Utilities", limitAmount = 300.0)
        coEvery { dao.getBudgetByCategory("Utilities") } returns entity

        val result = repository.getBudgetByCategory("Utilities")

        assertEquals("Utilities", result?.category)
        assertEquals(300.0, result?.limitAmount?.toDouble() ?: 0.0, 0.0)
        assertEquals(0.0, result?.currentSpent?.toDouble() ?: -1.0, 0.0) // default mapped
    }

    @Test
    fun `getBudgetByCategory returns null when not found`() = runTest {
        coEvery { dao.getBudgetByCategory("Unknown") } returns null

        val result = repository.getBudgetByCategory("Unknown")

        assertNull(result)
    }
}
