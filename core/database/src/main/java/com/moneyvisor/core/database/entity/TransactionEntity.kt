package com.moneyvisor.core.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.moneyvisor.domain.model.RepeatInterval
import com.moneyvisor.domain.model.Transaction
import com.moneyvisor.domain.model.TransactionType

@Entity(tableName = "transactions")
data class TransactionEntity(
    @PrimaryKey
    val id: String,
    val amount: Double,
    val type: String,
    val category: String,
    val date: Long,
    val tag: String = "",
    val isDraft: Boolean = false,
    val receiptUri: String? = null,
    val isRepeated: Boolean = false,
    val repeatInterval: String = RepeatInterval.NONE.name,
    val repeatValue: Int = 1
)

fun TransactionEntity.toDomain() = Transaction(
    id = id,
    amount = amount,
    type = TransactionType.valueOf(type),
    category = category,
    date = date,
    tag = tag,
    isDraft = isDraft,
    receiptUri = receiptUri,
    isRepeated = isRepeated,
    repeatInterval = RepeatInterval.valueOf(repeatInterval),
    repeatValue = repeatValue
)

fun Transaction.toEntity() = TransactionEntity(
    id = id,
    amount = amount,
    type = type.name,
    category = category,
    date = date,
    tag = tag,
    isDraft = isDraft,
    receiptUri = receiptUri,
    isRepeated = isRepeated,
    repeatInterval = repeatInterval.name,
    repeatValue = repeatValue
)
