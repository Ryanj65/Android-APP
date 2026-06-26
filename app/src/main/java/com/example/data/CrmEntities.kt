package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class DealStatus(val displayName: String, val colorHex: String) {
    NEW("Новая", "#2196F3"),
    DIAGNOSTICS("Диагностика", "#03A9F4"),
    IN_PROGRESS("В работе", "#9C27B0"),
    AWAITING_DECISION("Ожидает решения", "#E91E63"),
    AWAITING_PARTS("Ожидает запчасти", "#FF9800"),
    READY("Готово", "#4CAF50"),
    ISSUED("Выдано", "#009688"),
    CANCELLED("Отменено", "#74777F")
}

enum class TaskPriority(val displayName: String, val colorHex: String) {
    LOW("Низкий", "#8BC34A"),
    MEDIUM("Средний", "#FFC107"),
    HIGH("Высокий", "#FF5722")
}

@Entity(tableName = "deals")
data class ClientDeal(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val clientName: String,
    val phone: String = "",
    val email: String = "",
    val company: String = "",
    val dealValue: Double = 0.0,
    val status: String = "NEW", // NEW, IN_PROGRESS, READY, INVOICE, CLOSED
    val notes: String = "",
    
    // Auto Service fields
    val complaint: String = "",
    val diagnostics: String = "",
    val solution: String = "",
    val visitDate: String = "",
    val remindDate: String = "",
    val detailedStatus: String = "Ожидает запчасти",
    val laborMinutes: Int = 0,
    val myShare: Int = 50,
    val alexShare: Int = 50,
    
    // Car fields
    val carMake: String = "",
    val carModel: String = "",
    val carLicense: String = "",
    val carVin: String = "",
    val carYear: String = "",
    val carEngine: String = "",
    
    val timestamp: Long = System.currentTimeMillis()
) {
    val statusEnum: DealStatus
        get() = try { DealStatus.valueOf(status) } catch (e: Exception) { DealStatus.NEW }
}

@Entity(tableName = "tasks")
data class CrmTask(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val description: String = "",
    val dueDate: String = "", // E.g., "2026-06-27"
    val priority: String = "MEDIUM", // Matches TaskPriority enum name
    val isCompleted: Boolean = false,
    val dealId: Long? = null, // Linked deal
    val timestamp: Long = System.currentTimeMillis()
) {
    val priorityEnum: TaskPriority
        get() = try { TaskPriority.valueOf(priority) } catch (e: Exception) { TaskPriority.MEDIUM }
}

@Entity(tableName = "part_expenses")
data class PartExpense(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val dealId: Long,
    val name: String,
    val cost: Double,
    val timestamp: Long = System.currentTimeMillis()
)
