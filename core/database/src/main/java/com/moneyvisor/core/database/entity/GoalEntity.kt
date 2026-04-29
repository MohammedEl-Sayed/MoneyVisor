package com.moneyvisor.core.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.moneyvisor.domain.model.Goal

@Entity(tableName = "goals")
data class GoalEntity(
    @PrimaryKey
    val id: String,
    val name: String,
    val targetAmount: Double,
    val currentSaved: Double = 0.0
)

fun GoalEntity.toDomain() = Goal(
    id = id,
    name = name,
    targetAmount = targetAmount,
    currentSaved = currentSaved
)

fun Goal.toEntity() = GoalEntity(
    id = id,
    name = name,
    targetAmount = targetAmount,
    currentSaved = currentSaved
)
