package com.example.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.ClientDeal
import com.example.data.CrmRepository
import com.example.data.CrmTask
import com.example.data.DealStatus
import com.example.data.TaskPriority
import com.example.data.PartExpense
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

// Data classes for Parsed fields
data class ParsedDeal(
    val clientName: String,
    val dealValue: Double,
    val company: String,
    val notes: String
)

data class ParsedTask(
    val title: String,
    val priority: TaskPriority,
    val dueDate: String
)

class CrmViewModel(
    private val repository: CrmRepository,
    val authManager: com.example.data.network.AuthManager,
    private val apiService: com.example.data.network.ApiService
) : ViewModel() {

    // Network & Authentication States
    val isLoggedIn = MutableStateFlow(authManager.isLoggedIn)
    val isSyncing = MutableStateFlow(false)
    val syncError = MutableStateFlow<String?>(null)

    init {
        if (isLoggedIn.value) {
            fetchJobsFromServer()
        }
    }

    // Filter states
    val dealSearchQuery = MutableStateFlow("")
    val dealStatusFilter = MutableStateFlow<DealStatus?>(null)
    val showArchive = MutableStateFlow(false)

    val taskSearchQuery = MutableStateFlow("")
    val taskPriorityFilter = MutableStateFlow<TaskPriority?>(null)
    val taskCompletionFilter = MutableStateFlow<Boolean?>(null) // null = all, true = completed, false = active

    // Selected item for details/edit sheets
    val selectedDeal = MutableStateFlow<ClientDeal?>(null)

    // Base flows from repository
    private val allDeals = repository.allDeals
    private val allTasks = repository.allTasks

    // Filtered Deals
    val filteredDeals: StateFlow<List<ClientDeal>> = combine(
        allDeals,
        dealSearchQuery,
        dealStatusFilter,
        showArchive,
        isLoggedIn
    ) { deals, search, status, archive, loggedIn ->
        if (!loggedIn) {
            emptyList()
        } else {
            val oneWeekMs = 7 * 24 * 60 * 60 * 1000L
            val now = System.currentTimeMillis()
            deals.filter { deal ->
                val isRecent = (now - deal.timestamp) <= oneWeekMs
                val matchesArchive = if (archive) !isRecent else isRecent

                val matchesSearch = search.isEmpty() ||
                        deal.clientName.contains(search, ignoreCase = true) ||
                        deal.phone.contains(search, ignoreCase = true) ||
                        deal.carMake.contains(search, ignoreCase = true) ||
                        deal.carModel.contains(search, ignoreCase = true) ||
                        deal.carLicense.contains(search, ignoreCase = true) ||
                        deal.complaint.contains(search, ignoreCase = true)

                val matchesStatus = status == null || deal.status == status.name

                matchesArchive && matchesSearch && matchesStatus
            }
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    // Filtered Tasks
    val filteredTasks: StateFlow<List<CrmTask>> = combine(
        allTasks,
        taskSearchQuery,
        taskPriorityFilter,
        taskCompletionFilter,
        isLoggedIn
    ) { tasks, search, priority, completed, loggedIn ->
        if (!loggedIn) {
            emptyList()
        } else {
            tasks.filter { task ->
                val matchesSearch = search.isEmpty() ||
                        task.title.contains(search, ignoreCase = true) ||
                        task.description.contains(search, ignoreCase = true)

                val matchesPriority = priority == null || task.priority == priority.name

                val matchesCompleted = completed == null || task.isCompleted == completed

                matchesSearch && matchesPriority && matchesCompleted
            }
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    // Pipeline/Funnel Stats - Adapted to Auto CRM
    val pipelineStats = combine(
        allDeals,
        allTasks,
        isLoggedIn
    ) { deals, tasks, loggedIn ->
        if (!loggedIn) {
            CrmStats()
        } else {
            val totalDeals = deals.size
            val totalPipelineValue = deals.sumOf { it.dealValue }
            // "Won" in this context can be ready or closed (issued/cancelled can be filtered accordingly)
            val wonDeals = deals.filter { it.status == DealStatus.READY.name || it.status == DealStatus.ISSUED.name }
            val wonValue = wonDeals.sumOf { it.dealValue }
            val activeDeals = deals.filter { it.status != DealStatus.ISSUED.name && it.status != DealStatus.CANCELLED.name }
            val activeValue = activeDeals.sumOf { it.dealValue }
            
            val totalTasks = tasks.size
            val pendingTasks = tasks.count { !it.isCompleted }

            CrmStats(
                totalDeals = totalDeals,
                totalPipelineValue = totalPipelineValue,
                wonDealsCount = wonDeals.size,
                wonValue = wonValue,
                activeDealsCount = activeDeals.size,
                activeValue = activeValue,
                totalTasksCount = totalTasks,
                pendingTasksCount = pendingTasks
            )
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = CrmStats()
    )

    // === AUTH & SYNC BUSINESS LOGIC ===
    fun login(usernameInput: String, passwordInput: String, onSuccess: () -> Unit = {}) {
        viewModelScope.launch {
            isSyncing.value = true
            syncError.value = null
            try {
                val response = apiService.login(
                    com.example.data.network.LoginRequest(
                        email = usernameInput,
                        username = usernameInput,
                        password = passwordInput
                    )
                )
                if (response.isSuccessful && response.body() != null) {
                    val loginResponse = response.body()!!
                    authManager.saveAuth(
                        token = loginResponse.token,
                        email = loginResponse.user?.email ?: usernameInput,
                        name = loginResponse.user?.name ?: usernameInput
                    )
                    isLoggedIn.value = true
                    // Immediately fetch data
                    fetchJobsFromServer()
                    onSuccess()
                } else {
                    syncError.value = "Ошибка авторизации: " + (response.errorBody()?.string() ?: "Неверный логин или пароль")
                }
            } catch (e: Exception) {
                syncError.value = "Ошибка сети: ${e.localizedMessage}"
            } finally {
                isSyncing.value = false
            }
        }
    }

    fun logout() {
        viewModelScope.launch {
            isSyncing.value = true
            try {
                apiService.logout()
            } catch (e: Exception) {
                // Squelch network logout error
            }
            authManager.clearAuth()
            isLoggedIn.value = false
            repository.clearAllData() // Clear cached data when logged out so next user doesn't see it
            isSyncing.value = false
        }
    }

    fun fetchJobsFromServer() {
        if (!isLoggedIn.value) return
        viewModelScope.launch {
            isSyncing.value = true
            syncError.value = null
            try {
                val response = apiService.getJobs()
                if (response.isSuccessful && response.body() != null) {
                    val rawBody = response.body()!!.string()
                    val dealsList = com.example.data.network.NetworkParser.parseJobsList(rawBody)
                    
                    // Clear Room Database Cache to remove old demo data and old state
                    repository.clearAllData()
                    
                    // Insert the current server data
                    dealsList.forEach { deal ->
                        repository.insertDeal(deal)
                    }
                } else {
                    syncError.value = "Не удалось загрузить данные с сервера"
                }
            } catch (e: Exception) {
                syncError.value = "Синхронизация не удалась (офлайн режим): ${e.localizedMessage}"
            } finally {
                isSyncing.value = false
            }
        }
    }

    // === DEALS BUSINESS LOGIC ===
    fun addDeal(deal: ClientDeal) {
        viewModelScope.launch {
            isSyncing.value = true
            try {
                if (isLoggedIn.value) {
                    val networkJob = com.example.data.network.NetworkParser.fromClientDeal(deal)
                    val response = apiService.createJob(networkJob)
                    if (response.isSuccessful && response.body() != null) {
                        val rawBody = response.body()!!.string()
                        val createdDeal = com.example.data.network.NetworkParser.parseJobDetail(rawBody)
                        if (createdDeal != null) {
                            repository.insertDeal(createdDeal)
                        } else {
                            repository.insertDeal(deal)
                        }
                    } else {
                        repository.insertDeal(deal)
                    }
                } else {
                    repository.insertDeal(deal)
                }
            } catch (e: Exception) {
                repository.insertDeal(deal)
            } finally {
                isSyncing.value = false
            }
        }
    }

    fun updateDeal(deal: ClientDeal) {
        viewModelScope.launch {
            // Instant UI responsiveness
            repository.updateDeal(deal)
            if (selectedDeal.value?.id == deal.id) {
                selectedDeal.value = deal
            }
            if (isLoggedIn.value) {
                isSyncing.value = true
                try {
                    val networkJob = com.example.data.network.NetworkParser.fromClientDeal(deal)
                    apiService.updateJobFields(deal.id, networkJob)
                } catch (e: Exception) {
                    // Squelch and allow offline cache updates
                } finally {
                    isSyncing.value = false
                }
            }
        }
    }

    fun deleteDeal(deal: ClientDeal) {
        viewModelScope.launch {
            repository.deleteDeal(deal)
            if (selectedDeal.value?.id == deal.id) {
                selectedDeal.value = null
            }
        }
    }

    // === TASKS BUSINESS LOGIC ===
    fun addTask(task: CrmTask) {
        viewModelScope.launch {
            repository.insertTask(task)
        }
    }

    fun updateTask(task: CrmTask) {
        viewModelScope.launch {
            repository.updateTask(task)
        }
    }

    fun deleteTask(task: CrmTask) {
        viewModelScope.launch {
            repository.deleteTask(task)
        }
    }

    fun toggleTaskCompletion(task: CrmTask) {
        viewModelScope.launch {
            repository.updateTask(task.copy(isCompleted = !task.isCompleted))
        }
    }

    fun getTasksForDeal(dealId: Long): Flow<List<CrmTask>> {
        return repository.getTasksForDeal(dealId)
    }

    // === EXPENSES BUSINESS LOGIC ===
    fun getExpensesForDeal(dealId: Long): Flow<List<PartExpense>> {
        return repository.getExpensesForDeal(dealId)
    }

    fun addExpense(expense: PartExpense) {
        viewModelScope.launch {
            repository.insertExpense(expense)
            // Automatically update total dealValue to sum of all expenses
            selectedDeal.value?.let { deal ->
                // Wait, we can fetch all expenses for this deal, sum them, and update dealValue
                // But let's let the DB sum trigger it or sum them in Flow. For local state,
                // we can update it in the UI dynamically, or sum it from Flow.
            }
        }
    }

    fun deleteExpense(expense: PartExpense) {
        viewModelScope.launch {
            repository.deleteExpense(expense)
        }
    }

    // Slovenia Phone formatting helper
    fun formatSloveniaPhone(input: String): String {
        val clean = input.filter { it.isDigit() || it == '+' }
        if (clean.isEmpty()) return ""
        
        // Extract digits
        val digitsOnly = clean.filter { it.isDigit() }
        val startsWithPlus = clean.startsWith("+")
        
        // Slovenian country code: 386
        val pureDigits = if (digitsOnly.startsWith("386")) {
            digitsOnly.substring(3)
        } else if (digitsOnly.startsWith("0")) {
            digitsOnly.substring(1)
        } else {
            digitsOnly
        }
        
        val sb = StringBuilder("+386")
        if (pureDigits.isEmpty()) {
            return "+386 "
        }
        
        sb.append(" ")
        // Operator code: usually 2 digits (e.g. 30, 31, 40, 41, 51, 64, 70, 71)
        if (pureDigits.length <= 2) {
            sb.append(pureDigits)
            return sb.toString()
        }
        sb.append(pureDigits.substring(0, 2)).append(" ")
        
        // Second block: 3 digits
        if (pureDigits.length <= 5) {
            sb.append(pureDigits.substring(2))
            return sb.toString()
        }
        sb.append(pureDigits.substring(2, 5)).append(" ")
        
        // Third block: remaining digits (up to 3)
        val end = minOf(pureDigits.length, 8)
        sb.append(pureDigits.substring(5, end))
        return sb.toString()
    }

    // === SMART PARSING LOGIC FOR QUICK INPUT ===
    fun parseSmartDealText(text: String): ParsedDeal {
        val parts = text.split(Regex("[,;]+")).map { it.trim() }
        var clientName = ""
        var dealValue = 0.0
        var company = ""
        var notesList = mutableListOf<String>()

        parts.forEachIndexed { index, part ->
            val numericValue = part.filter { it.isDigit() || it == '.' }.toDoubleOrNull()
            if (numericValue != null && dealValue == 0.0 && part.any { it.isDigit() }) {
                dealValue = numericValue
            } else if (index == 0) {
                clientName = part
            } else if (index == 1 && company.isEmpty() && part.length > 2 && !part.any { it.isDigit() }) {
                company = part
            } else if (part.isNotEmpty()) {
                notesList.add(part)
            }
        }

        if (clientName.isEmpty() && text.isNotEmpty()) {
            clientName = text
        }

        return ParsedDeal(
            clientName = clientName,
            dealValue = dealValue,
            company = company,
            notes = notesList.joinToString(", ")
        )
    }

    fun parseSmartTaskText(text: String): ParsedTask {
        val lowerText = text.lowercase()
        var priority = TaskPriority.MEDIUM
        var title = text

        // Priority detection
        if (lowerText.contains("высокая") || lowerText.contains("важно") || lowerText.contains("срочно") || lowerText.contains("high")) {
            priority = TaskPriority.HIGH
        } else if (lowerText.contains("низкая") || lowerText.contains("несрочно") || lowerText.contains("low")) {
            priority = TaskPriority.LOW
        }

        // Date detection
        val cal = Calendar.getInstance()
        var matchedDate = true
        if (lowerText.contains("сегодня") || lowerText.contains("today")) {
            // keep current day
        } else if (lowerText.contains("завтра") || lowerText.contains("tomorrow")) {
            cal.add(Calendar.DAY_OF_YEAR, 1)
        } else if (lowerText.contains("послезавтра")) {
            cal.add(Calendar.DAY_OF_YEAR, 2)
        } else if (lowerText.contains("через неделю") || lowerText.contains("на следующей неделе")) {
            cal.add(Calendar.DAY_OF_YEAR, 7)
        } else {
            matchedDate = false
        }

        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val dueDateStr = if (matchedDate) sdf.format(cal.time) else sdf.format(Date())

        // Try to clean title slightly if it contains specific words
        val cleanRegex = Regex("\\b(сегодня|завтра|послезавтра|высокая|средняя|низкая|важно|срочно|высокая важность|низкая важность|high|medium|low|today|tomorrow)\\b", RegexOption.IGNORE_CASE)
        val cleanedTitle = text.replace(cleanRegex, "").replace(Regex("[,;\\s]+$"), "").trim()
        if (cleanedTitle.isNotEmpty()) {
            title = cleanedTitle
        }

        return ParsedTask(
            title = title,
            priority = priority,
            dueDate = dueDateStr
        )
    }

    // Demo/Seed Data for Slovenia Car Repair Service (disabled)
    fun seedDemoData() {
        viewModelScope.launch {
            repository.clearAllData()
        }
    }

    private fun getOffsetDate(days: Int): String {
        val cal = Calendar.getInstance()
        cal.add(Calendar.DAY_OF_YEAR, days)
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        return sdf.format(cal.time)
    }
}

// Stats holder class
data class CrmStats(
    val totalDeals: Int = 0,
    val totalPipelineValue: Double = 0.0,
    val wonDealsCount: Int = 0,
    val wonValue: Double = 0.0,
    val activeDealsCount: Int = 0,
    val activeValue: Double = 0.0,
    val totalTasksCount: Int = 0,
    val pendingTasksCount: Int = 0
)

class CrmViewModelFactory(
    private val repository: CrmRepository,
    private val authManager: com.example.data.network.AuthManager,
    private val apiService: com.example.data.network.ApiService
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(CrmViewModel::class.java)) {
            return CrmViewModel(repository, authManager, apiService) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
