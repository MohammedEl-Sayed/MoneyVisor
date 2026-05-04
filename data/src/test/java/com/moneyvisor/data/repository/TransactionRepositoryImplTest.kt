package com.moneyvisor.data.repository

import com.moneyvisor.core.database.dao.TransactionDao
import com.moneyvisor.core.database.entity.TransactionEntity
import com.moneyvisor.domain.model.Transaction
import com.moneyvisor.domain.model.TransactionType
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class TransactionRepositoryImplTest {

    private val dao: TransactionDao = mockk()
    private val repository = TransactionRepositoryImpl(dao)

    @Test
    fun `getTransactions returns mapped domain models`() = runTest {
        val entities = listOf(
            TransactionEntity(id = "1", amount = 100.0, type = "INCOME", category = "Salary", date = 123L, tag = "Salary"),
            TransactionEntity(id = "2", amount = 50.0, type = "EXPENSE", category = "Food", date = 456L, tag = "Food")
        )
        every { dao.getTransactions() } returns flowOf(entities)

        repository.getTransactions().collect { transactions ->
            assertEquals(2, transactions.size)
            assertEquals("Salary", transactions[0].category)
            assertEquals(TransactionType.INCOME, transactions[0].type)
            assertEquals("Food", transactions[1].category)
            assertEquals(TransactionType.EXPENSE, transactions[1].type)
        }
    }

    @Test
    fun `insertTransaction calls dao insert`() = runTest {
        val transaction = Transaction(id = "1", amount = 10.0, type = TransactionType.INCOME, category = "Test", date = 123L, tag = "Tag")
        coEvery { dao.insertTransaction(any()) } returns Unit

        repository.insertTransaction(transaction)

        coVerify { dao.insertTransaction(match { it.id == "1" && it.category == "Test" }) }
    }

    @Test
    fun `deleteTransaction calls dao delete`() = runTest {
        val transaction = Transaction(id = "1", amount = 10.0, type = TransactionType.INCOME, category = "Test", date = 123L, tag = "Tag")
        coEvery { dao.deleteTransaction(any()) } returns Unit

        repository.deleteTransaction(transaction)

        coVerify { dao.deleteTransaction(match { it.id == "1" }) }
    }

    @Test
    fun `getTransactionById returns mapped domain model`() = runTest {
        val entity = TransactionEntity(id = "1", amount = 10.0, type = "INCOME", category = "Test", date = 123L, tag = "Tag")
        coEvery { dao.getTransactionById("1") } returns entity

        val result = repository.getTransactionById("1")

        assertEquals("Test", result?.category)
        assertEquals(TransactionType.INCOME, result?.type)
    }

    @Test
    fun `getTransactionsByDateRange returns mapped domain models`() = runTest {
        val start = 100L
        val end = 500L
        val entities = listOf(
            TransactionEntity(id = "1", amount = 100.0, type = "INCOME", category = "Salary", date = 123L, tag = "Salary"),
            TransactionEntity(id = "2", amount = 50.0, type = "EXPENSE", category = "Food", date = 456L, tag = "Food")
        )
        every { dao.getTransactionsByDateRange(start, end) } returns flowOf(entities)

        repository.getTransactionsByDateRange(start, end).collect { transactions ->
            assertEquals(2, transactions.size)
            assertEquals("Salary", transactions[0].category)
            assertEquals(TransactionType.INCOME, transactions[0].type)
            assertEquals("Food", transactions[1].category)
            assertEquals(TransactionType.EXPENSE, transactions[1].type)
        }
    }

    @Test
    fun `getTransactionsByDateRange returns empty list when no entities match`() = runTest {
        every { dao.getTransactionsByDateRange(any(), any()) } returns flowOf(emptyList())
        repository.getTransactionsByDateRange(100L, 500L).collect { transactions ->
            assertEquals(0, transactions.size)
        }
    }
}
