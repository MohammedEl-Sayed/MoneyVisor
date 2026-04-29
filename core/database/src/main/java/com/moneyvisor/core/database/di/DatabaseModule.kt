package com.moneyvisor.core.database.di

import android.content.Context
import androidx.room.Room
import com.moneyvisor.core.database.MoneyVisorDatabase
import com.moneyvisor.core.database.dao.TransactionDao
import com.moneyvisor.core.database.dao.BudgetDao
import com.moneyvisor.core.database.dao.GoalDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): MoneyVisorDatabase {
        return Room.databaseBuilder(
            context,
            MoneyVisorDatabase::class.java,
            "moneyvisor.db"
        ).fallbackToDestructiveMigration().build()
    }

    @Provides
    @Singleton
    fun provideTransactionDao(db: MoneyVisorDatabase): TransactionDao {
        return db.transactionDao
    }

    @Provides
    @Singleton
    fun provideBudgetDao(db: MoneyVisorDatabase): BudgetDao {
        return db.budgetDao
    }

    @Provides
    @Singleton
    fun provideGoalDao(db: MoneyVisorDatabase): GoalDao {
        return db.goalDao
    }
}
