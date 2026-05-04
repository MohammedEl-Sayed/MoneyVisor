package com.moneyvisor.domain.repository

import com.moneyvisor.domain.model.Transaction
import kotlinx.coroutines.flow.Flow

interface TransactionRepository {
    fun getTransactions(): Flow<List<Transaction>>
    suspend fun insertTransaction(transaction: Transaction)
    suspend fun insertTransactions(transactions: List<Transaction>)
    suspend fun deleteTransaction(transaction: Transaction)
    suspend fun getTransactionById(id: String): Transaction?
    fun getTransactionsByDateRange(start: Long, end: Long): Flow<List<Transaction>>
    suspend fun deleteAllTransactions()
}
