package com.moneyvisor.core.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.moneyvisor.core.database.dao.TransactionDao
import com.moneyvisor.core.database.dao.BudgetDao
import com.moneyvisor.core.database.dao.GoalDao
import com.moneyvisor.core.database.entity.TransactionEntity
import com.moneyvisor.core.database.entity.BudgetEntity
import com.moneyvisor.core.database.entity.GoalEntity

@Database(
    entities = [
        TransactionEntity::class,
        BudgetEntity::class,
        GoalEntity::class
    ], 
    version = 6,
 
    exportSchema = false
)
abstract class MoneyVisorDatabase : RoomDatabase() {
    abstract val transactionDao: TransactionDao
    abstract val budgetDao: BudgetDao
    abstract val goalDao: GoalDao
}
