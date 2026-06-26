package com.example.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import androidx.room.Delete
import kotlinx.coroutines.flow.Flow

@Dao
interface CrmDao {
    // === DEALS ===
    @Query("SELECT * FROM deals ORDER BY timestamp DESC")
    fun getAllDeals(): Flow<List<ClientDeal>>

    @Query("SELECT * FROM deals WHERE id = :id LIMIT 1")
    suspend fun getDealById(id: Long): ClientDeal?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDeal(deal: ClientDeal): Long

    @Update
    suspend fun updateDeal(deal: ClientDeal)

    @Delete
    suspend fun deleteDeal(deal: ClientDeal)

    @Query("DELETE FROM deals")
    suspend fun deleteAllDeals()

    // === TASKS ===
    @Query("SELECT * FROM tasks ORDER BY isCompleted ASC, timestamp DESC")
    fun getAllTasks(): Flow<List<CrmTask>>

    @Query("SELECT * FROM tasks WHERE dealId = :dealId ORDER BY isCompleted ASC, timestamp DESC")
    fun getTasksByDealId(dealId: Long): Flow<List<CrmTask>>

    @Query("SELECT * FROM tasks WHERE id = :id LIMIT 1")
    suspend fun getTaskById(id: Long): CrmTask?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTask(task: CrmTask): Long

    @Update
    suspend fun updateTask(task: CrmTask)

    @Delete
    suspend fun deleteTask(task: CrmTask)

    @Query("DELETE FROM tasks WHERE dealId = :dealId")
    suspend fun deleteTasksByDealId(dealId: Long)

    @Query("DELETE FROM tasks")
    suspend fun deleteAllTasks()

    // === EXPENSES ===
    @Query("SELECT * FROM part_expenses WHERE dealId = :dealId ORDER BY timestamp DESC")
    fun getExpensesByDealId(dealId: Long): Flow<List<PartExpense>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExpense(expense: PartExpense): Long

    @Delete
    suspend fun deleteExpense(expense: PartExpense)

    @Query("DELETE FROM part_expenses WHERE dealId = :dealId")
    suspend fun deleteExpensesByDealId(dealId: Long)
}
