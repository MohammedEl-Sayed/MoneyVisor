package com.moneyvisor.data.di

import com.moneyvisor.data.repository.TransactionRepositoryImpl
import com.moneyvisor.data.repository.BudgetRepositoryImpl
import com.moneyvisor.data.repository.GoalRepositoryImpl
import com.moneyvisor.domain.repository.TransactionRepository
import com.moneyvisor.domain.repository.BudgetRepository
import com.moneyvisor.domain.repository.GoalRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class DataModule {

    @Binds
    @Singleton
    abstract fun bindTransactionRepository(
        transactionRepositoryImpl: TransactionRepositoryImpl
    ): TransactionRepository

    @Binds
    @Singleton
    abstract fun bindBudgetRepository(
        budgetRepositoryImpl: BudgetRepositoryImpl
    ): BudgetRepository

    @Binds
    @Singleton
    abstract fun bindGoalRepository(
        goalRepositoryImpl: GoalRepositoryImpl
    ): GoalRepository
}
