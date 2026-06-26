package com.example.data

import kotlinx.coroutines.flow.Flow

class CrmRepository(private val crmDao: CrmDao) {
    val allDeals: Flow<List<ClientDeal>> = crmDao.getAllDeals()
    val allTasks: Flow<List<CrmTask>> = crmDao.getAllTasks()

    fun getTasksForDeal(dealId: Long): Flow<List<CrmTask>> = crmDao.getTasksByDealId(dealId)

    suspend fun getDealById(id: Long): ClientDeal? = crmDao.getDealById(id)

    suspend fun insertDeal(deal: ClientDeal): Long = crmDao.insertDeal(deal)

    suspend fun updateDeal(deal: ClientDeal) = crmDao.updateDeal(deal)

    suspend fun deleteDeal(deal: ClientDeal) {
        // First delete associated tasks and expenses to prevent orphaned records
        crmDao.deleteTasksByDealId(deal.id)
        crmDao.deleteExpensesByDealId(deal.id)
        crmDao.deleteDeal(deal)
    }

    suspend fun getTaskById(id: Long): CrmTask? = crmDao.getTaskById(id)

    suspend fun insertTask(task: CrmTask): Long = crmDao.insertTask(task)

    suspend fun updateTask(task: CrmTask) = crmDao.updateTask(task)

    suspend fun deleteTask(task: CrmTask) = crmDao.deleteTask(task)

    suspend fun deleteTasksByDealId(dealId: Long) = crmDao.deleteTasksByDealId(dealId)

    // === EXPENSES ===
    fun getExpensesForDeal(dealId: Long): Flow<List<PartExpense>> = crmDao.getExpensesByDealId(dealId)

    suspend fun insertExpense(expense: PartExpense): Long = crmDao.insertExpense(expense)

    suspend fun deleteExpense(expense: PartExpense) = crmDao.deleteExpense(expense)

    suspend fun clearAllData() {
        crmDao.deleteAllTasks()
        crmDao.deleteAllDeals()
    }
}
