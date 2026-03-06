package com.twig.gameplan

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface PlanDao {
    @Query("SELECT * FROM plans")
    fun getAllPlans(): Flow<List<Plan>>

    @Query("SELECT * FROM plans WHERE id = :planId")
    fun getPlanById(planId: Long): Flow<Plan?>

    @Query("SELECT * FROM plans WHERE groupId = :groupId")
    fun getPlansByGroup(groupId: Long): Flow<List<Plan>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPlan(plan: Plan)

    @Update
    suspend fun updatePlan(plan: Plan)

    @Delete
    suspend fun deletePlan(plan: Plan)

    @Query("DELETE FROM plans")
    suspend fun deleteAllPlans()
}