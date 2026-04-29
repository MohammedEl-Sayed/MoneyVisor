package com.moneyvisor.data.repository

import com.moneyvisor.core.database.dao.TransactionDao
import com.moneyvisor.core.database.entity.toDomain
import com.moneyvisor.core.database.entity.toEntity
import com.moneyvisor.domain.model.Transaction
import com.moneyvisor.domain.repository.TransactionRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class TransactionRepositoryImpl @Inject constructor(
    private val dao: TransactionDao
) : TransactionRepository {
    override fun getTransactions(): Flow<List<Transaction>> {
        return dao.getTransactions().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun insertTransaction(transaction: Transaction) {
        dao.insertTransaction(transaction.toEntity())
    }

    override suspend fun deleteTransaction(transaction: Transaction) {
        dao.deleteTransaction(transaction.toEntity())
    }

    override suspend fun getTransactionById(id: String): Transaction? {
        return dao.getTransactionById(id)?.toDomain()
    }

    override fun getTransactionsByDateRange(start: Long, end: Long): Flow<List<Transaction>> {
        return dao.getTransactionsByDateRange(start, end).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun deleteAllTransactions() {
        dao.deleteAllTransactions()
    }
}
