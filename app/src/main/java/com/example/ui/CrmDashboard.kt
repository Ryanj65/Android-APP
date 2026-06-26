package com.example.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.TextButton
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.InputChip
import androidx.compose.material3.InputChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.ClientDeal
import com.example.data.CrmTask
import com.example.data.DealStatus
import com.example.data.TaskPriority
import com.example.data.PartExpense
import java.text.NumberFormat
import java.util.Locale

// Navigation tab identifiers
enum class CrmTab(val title: String) {
    DEALS("Заявки"),
    TASKS("Задачи"),
    ANALYTICS("Аналитика")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CrmDashboard(
    viewModel: CrmViewModel,
    modifier: Modifier = Modifier
) {
    var currentTab by remember { mutableStateOf(CrmTab.DEALS) }
    
    // Dialog state controllers
    var showAddDealDialog by remember { mutableStateOf(false) }
    var showAddTaskDialog by remember { mutableStateOf(false) }
    var showDealDetailsDialog by remember { mutableStateOf(false) }
    var showLoginDialog by remember { mutableStateOf(false) }
    var showAccountMenu by remember { mutableStateOf(false) }
    
    // Observers
    val deals by viewModel.filteredDeals.collectAsState()
    val tasks by viewModel.filteredTasks.collectAsState()
    val stats by viewModel.pipelineStats.collectAsState()
    val selectedDeal by viewModel.selectedDeal.collectAsState()
    val isLoggedIn by viewModel.isLoggedIn.collectAsState()
    val isSyncing by viewModel.isSyncing.collectAsState()

    if (!isLoggedIn) {
        var username by remember { mutableStateOf("") }
        var password by remember { mutableStateOf("") }
        val syncError by viewModel.syncError.collectAsState()

        Box(
            modifier = modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 400.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Lock,
                    contentDescription = "Lock Logo",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier
                        .size(64.dp)
                        .background(
                            color = MaterialTheme.colorScheme.primaryContainer,
                            shape = CircleShape
                        )
                        .padding(16.dp)
                )

                Text(
                    text = "Avtodiagnoza",
                    style = MaterialTheme.typography.headlineLarge.copy(
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                )

                Text(
                    text = "Авторизация для доступа к CRM системе",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = username,
                    onValueChange = { username = it },
                    label = { Text("Имя пользователя") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("login_email_input"),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
                    enabled = !isSyncing
                )

                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text("Пароль") },
                    singleLine = true,
                    visualTransformation = androidx.compose.ui.text.input.PasswordVisualTransformation(),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("login_password_input"),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    enabled = !isSyncing
                )

                if (syncError != null) {
                    Text(
                        text = syncError ?: "",
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodyMedium,
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .padding(horizontal = 8.dp)
                            .testTag("login_error_text")
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Button(
                    onClick = {
                        viewModel.login(username, password)
                    },
                    enabled = !isSyncing && username.isNotEmpty() && password.isNotEmpty(),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("login_submit_button")
                ) {
                    if (isSyncing) {
                        androidx.compose.material3.CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            strokeWidth = 2.dp,
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                    } else {
                        Text(
                            text = "Войти",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )
                    }
                }

                Text(
                    text = "Сервер: 194.180.207.24",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                )
            }
        }
    } else {
        Scaffold(
            modifier = modifier.fillMaxSize(),
        topBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
                    .padding(horizontal = 20.dp, vertical = 12.dp)
            ) {
                Spacer(modifier = Modifier.height(32.dp)) // Safe-area spacing
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Avtodiagnoza",
                            style = MaterialTheme.typography.headlineMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface,
                                letterSpacing = (-0.5).sp
                            )
                        )
                    }
                    
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        if (isLoggedIn) {
                            Button(
                                onClick = { showAccountMenu = true },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                                ),
                                contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                                modifier = Modifier.height(34.dp).testTag("account_profile_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = "Синхронизировано",
                                    tint = Color(0xFF4CAF50),
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = viewModel.authManager.name ?: "Админ",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        } else {
                            OutlinedButton(
                                onClick = { showLoginDialog = true },
                                modifier = Modifier.height(34.dp).testTag("header_login_button"),
                                contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Lock,
                                    contentDescription = "Войти",
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Войти", fontSize = 12.sp)
                            }
                        }
                        
                        IconButton(
                            onClick = { 
                                if (isLoggedIn) {
                                    viewModel.fetchJobsFromServer()
                                } else {
                                    viewModel.seedDemoData() 
                                }
                            },
                            modifier = Modifier
                                .background(MaterialTheme.colorScheme.surfaceVariant, CircleShape)
                                .size(34.dp)
                                .testTag("seed_data_button")
                        ) {
                            if (isSyncing) {
                                androidx.compose.material3.CircularProgressIndicator(
                                    modifier = Modifier.size(16.dp),
                                    strokeWidth = 2.dp,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            } else {
                                Icon(
                                    imageVector = Icons.Default.Refresh,
                                    contentDescription = if (isLoggedIn) "Обновить" else "Сбросить демо-данные",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(14.dp))
                
                // M3 Tab Layout
                TabRow(
                    selectedTabIndex = currentTab.ordinal,
                    containerColor = Color.Transparent,
                    contentColor = MaterialTheme.colorScheme.primary,
                    indicator = { tabPositions ->
                        if (currentTab.ordinal < tabPositions.size) {
                            TabRowDefaults.SecondaryIndicator(
                                Modifier.tabIndicatorOffset(tabPositions[currentTab.ordinal]),
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    },
                    divider = {}
                ) {
                    CrmTab.values().forEach { tab ->
                        Tab(
                            selected = currentTab == tab,
                            onClick = { currentTab = tab },
                            text = {
                                Text(
                                    text = tab.title,
                                    fontSize = 14.sp,
                                    fontWeight = if (currentTab == tab) FontWeight.Bold else FontWeight.Medium
                                )
                            },
                            modifier = Modifier.testTag("tab_${tab.name.lowercase()}")
                        )
                    }
                }
            }
        },
        floatingActionButton = {
            if (currentTab != CrmTab.ANALYTICS) {
                FloatingActionButton(
                    onClick = {
                        if (currentTab == CrmTab.DEALS) {
                            showAddDealDialog = true
                        } else {
                            showAddTaskDialog = true
                        }
                    },
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier
                        .padding(bottom = 16.dp)
                        .testTag("add_fab")
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = if (currentTab == CrmTab.DEALS) "Добавить заявку" else "Добавить задачу"
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (currentTab) {
                CrmTab.DEALS -> DealsPanel(
                    deals = deals,
                    viewModel = viewModel,
                    onDealClick = { deal ->
                        viewModel.selectedDeal.value = deal
                        showDealDetailsDialog = true
                    }
                )
                CrmTab.TASKS -> TasksPanel(
                    tasks = tasks,
                    deals = deals,
                    viewModel = viewModel
                )
                CrmTab.ANALYTICS -> AnalyticsPanel(
                    stats = stats,
                    deals = deals,
                    viewModel = viewModel
                )
            }
        }
    }

    // CREATE DEAL DIALOG
    if (showAddDealDialog) {
        AddEditDealDialog(
            viewModel = viewModel,
            onDismiss = { showAddDealDialog = false },
            onConfirm = { newDeal ->
                viewModel.addDeal(newDeal)
                showAddDealDialog = false
            }
        )
    }

    // CREATE TASK DIALOG
    if (showAddTaskDialog) {
        AddEditTaskDialog(
            deals = deals,
            onDismiss = { showAddTaskDialog = false },
            onConfirm = { newTask ->
                viewModel.addTask(newTask)
                showAddTaskDialog = false
            }
        )
    }

    // DEAL DETAILS & EDIT DIALOG (AutoCRM version)
    if (showDealDetailsDialog && selectedDeal != null) {
        DealDetailsDialog(
            deal = selectedDeal!!,
            viewModel = viewModel,
            onDismiss = { showDealDetailsDialog = false }
        )
    }

    // LOGIN DIALOG
    if (showLoginDialog) {
        var username by remember { mutableStateOf("") }
        var password by remember { mutableStateOf("") }
        val syncError by viewModel.syncError.collectAsState()

        AlertDialog(
            onDismissRequest = { if (!isSyncing) showLoginDialog = false },
            title = {
                Text(
                    text = "Вход в систему",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                )
            },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Подключение к серверу 194.180.207.24",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                    
                    OutlinedTextField(
                        value = username,
                        onValueChange = { username = it },
                        label = { Text("Имя пользователя") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth().testTag("login_email_input"),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
                        enabled = !isSyncing
                    )

                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it },
                        label = { Text("Пароль") },
                        singleLine = true,
                        visualTransformation = androidx.compose.ui.text.input.PasswordVisualTransformation(),
                        modifier = Modifier.fillMaxWidth().testTag("login_password_input"),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        enabled = !isSyncing
                    )

                    if (syncError != null) {
                        Text(
                            text = syncError ?: "",
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.testTag("login_error_text")
                        )
                    }

                    if (isSyncing) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            androidx.compose.material3.CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                strokeWidth = 2.dp,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Проверка...",
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.login(username, password, onSuccess = {
                            showLoginDialog = false
                        })
                    },
                    enabled = !isSyncing && username.isNotEmpty() && password.isNotEmpty(),
                    modifier = Modifier.testTag("login_submit_button")
                ) {
                    Text("Войти")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showLoginDialog = false },
                    enabled = !isSyncing,
                    modifier = Modifier.testTag("login_cancel_button")
                ) {
                    Text("Отмена")
                }
            }
        )
    }

    // ACCOUNT DETAILS DIALOG
    if (showAccountMenu) {
        val syncError by viewModel.syncError.collectAsState()

        AlertDialog(
            onDismissRequest = { showAccountMenu = false },
            title = {
                Text(
                    text = "Аккаунт синхронизации",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                )
            },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "Пользователь: ${viewModel.authManager.name ?: "Администратор"}",
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = "Email: ${viewModel.authManager.email ?: ""}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "Сервер: 194.180.207.24",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    if (syncError != null) {
                        Text(
                            text = syncError ?: "",
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }

                    if (isSyncing) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            androidx.compose.material3.CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                strokeWidth = 2.dp,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Синхронизация...", style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.fetchJobsFromServer()
                        showAccountMenu = false
                    },
                    enabled = !isSyncing,
                    modifier = Modifier.testTag("account_sync_button")
                ) {
                    Text("Синхронизировать")
                }
            },
            dismissButton = {
                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(
                        onClick = { showAccountMenu = false },
                        enabled = !isSyncing
                    ) {
                        Text("Закрыть")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    TextButton(
                        onClick = {
                            viewModel.logout()
                            showAccountMenu = false
                        },
                        enabled = !isSyncing,
                        modifier = Modifier.testTag("account_logout_button")
                    ) {
                        Text(
                            text = "Выйти",
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }
        )
    }
}
}

// ==========================================
// SUB-PANELS (DEALS, TASKS, ANALYTICS)
// ==========================================

@Composable
fun DealsPanel(
    deals: List<ClientDeal>,
    viewModel: CrmViewModel,
    onDealClick: (ClientDeal) -> Unit
) {
    val searchQuery by viewModel.dealSearchQuery.collectAsState()
    val activeStatusFilter by viewModel.dealStatusFilter.collectAsState()

    Column(modifier = Modifier.fillMaxSize()) {
        // Search & Filter header
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f))
                .padding(16.dp)
        ) {
            // Search Input
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { viewModel.dealSearchQuery.value = it },
                placeholder = { Text("Поиск клиентов, авто, гос.номеров...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Поиск") },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { viewModel.dealSearchQuery.value = "" }) {
                            Icon(Icons.Default.Clear, contentDescription = "Очистить")
                        }
                    }
                },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("deal_search_input"),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
                )
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Horizontal Status Filter Chips
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                item {
                    val isArchiveSelected by viewModel.showArchive.collectAsState()
                    AssistChip(
                        onClick = { viewModel.showArchive.value = !isArchiveSelected },
                        label = { Text("Архив") },
                        colors = AssistChipDefaults.assistChipColors(
                            containerColor = if (isArchiveSelected) MaterialTheme.colorScheme.tertiaryContainer else Color.Transparent,
                            labelColor = if (isArchiveSelected) MaterialTheme.colorScheme.onTertiaryContainer else MaterialTheme.colorScheme.onSurface
                        ),
                        leadingIcon = {
                            Icon(
                                imageVector = if (isArchiveSelected) Icons.Default.Check else Icons.Default.DateRange,
                                contentDescription = "Архив",
                                modifier = Modifier.size(16.dp),
                                tint = if (isArchiveSelected) MaterialTheme.colorScheme.onTertiaryContainer else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    )
                }
                item {
                    AssistChip(
                        onClick = { viewModel.dealStatusFilter.value = null },
                        label = { Text("Все стадии") },
                        colors = AssistChipDefaults.assistChipColors(
                            containerColor = if (activeStatusFilter == null) MaterialTheme.colorScheme.primaryContainer else Color.Transparent,
                            labelColor = if (activeStatusFilter == null) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface
                        )
                    )
                }
                items(DealStatus.values()) { status ->
                    AssistChip(
                        onClick = { viewModel.dealStatusFilter.value = status },
                        label = { Text(status.displayName) },
                        colors = AssistChipDefaults.assistChipColors(
                            containerColor = if (activeStatusFilter == status) {
                                Color(android.graphics.Color.parseColor(status.colorHex)).copy(alpha = 0.25f)
                            } else Color.Transparent,
                            labelColor = if (activeStatusFilter == status) {
                                Color(android.graphics.Color.parseColor(status.colorHex))
                            } else MaterialTheme.colorScheme.onSurface,
                            leadingIconContentColor = Color(android.graphics.Color.parseColor(status.colorHex))
                        ),
                        leadingIcon = {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .background(
                                        Color(android.graphics.Color.parseColor(status.colorHex)),
                                        CircleShape
                                    )
                            )
                        }
                    )
                }
            }
        }

        // Deal list
        if (deals.isEmpty()) {
            EmptyStateView(
                title = "Заявок не найдено",
                subtitle = "Создайте свою первую заявку кнопкой + или сбросьте фильтры!"
            )
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                item { Spacer(modifier = Modifier.height(10.dp)) }
                items(deals, key = { it.id }) { deal ->
                    DealCard(deal = deal, onClick = { onDealClick(deal) })
                }
                item { Spacer(modifier = Modifier.height(80.dp)) } // margin for FAB
            }
        }
    }
}

@Composable
fun TasksPanel(
    tasks: List<CrmTask>,
    deals: List<ClientDeal>,
    viewModel: CrmViewModel
) {
    val searchQuery by viewModel.taskSearchQuery.collectAsState()
    val priorityFilter by viewModel.taskPriorityFilter.collectAsState()
    val completionFilter by viewModel.taskCompletionFilter.collectAsState()

    Column(modifier = Modifier.fillMaxSize()) {
        // Search & Priority Filters Header
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f))
                .padding(16.dp)
        ) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { viewModel.taskSearchQuery.value = it },
                placeholder = { Text("Поиск задач...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Поиск") },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { viewModel.taskSearchQuery.value = "" }) {
                            Icon(Icons.Default.Clear, contentDescription = "Очистить")
                        }
                    }
                },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("task_search_input"),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
                )
            )

            Spacer(modifier = Modifier.height(10.dp))

            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Priority row
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    item {
                        AssistChip(
                            onClick = { viewModel.taskPriorityFilter.value = null },
                            label = { Text("Все") },
                            colors = AssistChipDefaults.assistChipColors(
                                containerColor = if (priorityFilter == null) MaterialTheme.colorScheme.primaryContainer else Color.Transparent,
                                labelColor = if (priorityFilter == null) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface
                            )
                        )
                    }
                    items(TaskPriority.values()) { priority ->
                        AssistChip(
                            onClick = { viewModel.taskPriorityFilter.value = priority },
                            label = { Text(priority.displayName) },
                            colors = AssistChipDefaults.assistChipColors(
                                containerColor = if (priorityFilter == priority) {
                                    Color(android.graphics.Color.parseColor(priority.colorHex)).copy(alpha = 0.2f)
                                } else Color.Transparent,
                                labelColor = if (priorityFilter == priority) {
                                    Color(android.graphics.Color.parseColor(priority.colorHex))
                                } else MaterialTheme.colorScheme.onSurface
                            )
                        )
                    }
                }

                // Custom completion status switch
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .padding(2.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    val activeBg = MaterialTheme.colorScheme.surface
                    val inactiveBg = Color.Transparent
                    
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(6.dp))
                            .background(if (completionFilter == false) activeBg else inactiveBg)
                            .clickable { viewModel.taskCompletionFilter.value = false }
                            .padding(vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("Активные", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(6.dp))
                            .background(if (completionFilter == true) activeBg else inactiveBg)
                            .clickable { viewModel.taskCompletionFilter.value = true }
                            .padding(vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("Выполненные", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(6.dp))
                            .background(if (completionFilter == null) activeBg else inactiveBg)
                            .clickable { viewModel.taskCompletionFilter.value = null }
                            .padding(vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("Все", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // Task List
        if (tasks.isEmpty()) {
            EmptyStateView(
                title = "Задач не найдено",
                subtitle = "Отличная работа! Нет задач в этой категории."
            )
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                item { Spacer(modifier = Modifier.height(10.dp)) }
                items(tasks, key = { it.id }) { task ->
                    val associatedDeal = deals.find { it.id == task.dealId }
                    TaskCard(
                        task = task,
                        associatedDeal = associatedDeal,
                        onToggle = { viewModel.toggleTaskCompletion(task) },
                        onDelete = { viewModel.deleteTask(task) }
                    )
                }
                item { Spacer(modifier = Modifier.height(80.dp)) } // space for FAB
            }
        }
    }
}

@Composable
fun AnalyticsPanel(
    stats: CrmStats,
    deals: List<ClientDeal>,
    viewModel: CrmViewModel
) {
    var smartDealInput by remember { mutableStateOf("") }
    var smartTaskInput by remember { mutableStateOf("") }
    
    val parsedDeal = viewModel.parseSmartDealText(smartDealInput)
    val parsedTask = viewModel.parseSmartTaskText(smartTaskInput)

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // --- STATS HIGHLIGHT ---
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        "Финансовые Показатели",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text("Выполнено (READY/CLOSED)", fontSize = 11.sp, color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f))
                            Text(
                                text = formatCurrency(stats.wonValue),
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text("Активный пайплайн", fontSize = 11.sp, color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f))
                            Text(
                                text = formatCurrency(stats.activeValue),
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    HorizontalDivider(color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.15f))
                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Активных ремонтов: ${stats.activeDealsCount} / Всего: ${stats.totalDeals}",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                        )
                        val conversion = if (stats.totalDeals > 0) {
                            (stats.wonDealsCount.toFloat() / stats.totalDeals.toFloat() * 100).toInt()
                        } else 0
                        Text(
                            text = "Выполнено: $conversion%",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
            }
        }

        // --- PIPELINE VISUAL FUNNEL ---
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(2.dp),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        "Воронка Заявок (По стадиям ремонта)",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        "Распределение ремонтов по статусам",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    // Funnel drawing canvas
                    PipelineFunnelChart(deals = deals)
                }
            }
        }

        // --- CONVENIENT SMART DATA ENTRY ---
        item {
            Text(
                text = "Удобный умный ввод (Smart Entry)",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(top = 8.dp)
            )
        }

        // QUICK DEAL SMART BOX
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)),
                shape = RoundedCornerShape(14.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        "⚡️ Быстрое добавление клиента/ремонта",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        "Введите: Имя, сумма в €, авто, жалоба",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    TextField(
                        value = smartDealInput,
                        onValueChange = { smartDealInput = it },
                        placeholder = { Text("Пример: Семен Семенов, 150 €, Golf 7, троит двигатель") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("smart_deal_input"),
                        textStyle = TextStyle(fontSize = 13.sp),
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = MaterialTheme.colorScheme.surface,
                            unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                            focusedIndicatorColor = MaterialTheme.colorScheme.primary
                        ),
                        shape = RoundedCornerShape(8.dp)
                    )

                    if (smartDealInput.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(10.dp))
                        Text("Распознанные поля:", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            ParsingTag(label = "Имя: ${parsedDeal.clientName.ifEmpty { "—" }}", color = MaterialTheme.colorScheme.primary)
                            ParsingTag(label = "Сумма: ${parsedDeal.dealValue} €", color = MaterialTheme.colorScheme.secondary)
                            ParsingTag(label = "Авто: ${parsedDeal.company.ifEmpty { "—" }}", color = MaterialTheme.colorScheme.tertiary)
                        }

                        Spacer(modifier = Modifier.height(8.dp))
                        Button(
                            onClick = {
                                val autoParts = parsedDeal.company.split(" ")
                                val carMake = autoParts.getOrNull(0) ?: ""
                                val carModel = autoParts.drop(1).joinToString(" ")
                                viewModel.addDeal(
                                    ClientDeal(
                                        clientName = parsedDeal.clientName.ifEmpty { "Новый клиент" },
                                        dealValue = parsedDeal.dealValue,
                                        carMake = carMake,
                                        carModel = carModel,
                                        complaint = parsedDeal.notes,
                                        status = "NEW",
                                        detailedStatus = "Новая"
                                    )
                                )
                                smartDealInput = ""
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(36.dp),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = androidx.compose.foundation.layout.PaddingValues(0.dp)
                        ) {
                            Text("Создать заявку", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ParsingTag(label: String, color: Color) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(color.copy(alpha = 0.12f))
            .border(1.dp, color.copy(alpha = 0.3f), RoundedCornerShape(6.dp))
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Text(text = label, color = color, fontSize = 10.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun PipelineFunnelChart(deals: List<ClientDeal>) {
    // Count deals by stages
    val counts = DealStatus.values().associateWith { status ->
        deals.count { it.status == status.name }
    }
    val maxCount = counts.values.maxOrNull() ?: 1

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        DealStatus.values().forEach { status ->
            val count = counts[status] ?: 0
            val fraction = if (maxCount > 0) count.toFloat() / maxCount.toFloat() else 0f
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = status.displayName,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.width(90.dp)
                )

                Row(
                    modifier = Modifier
                        .weight(1f)
                        .height(24.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxHeight()
                            .fillMaxWidth(fraction = 0.05f + (fraction * 0.85f))
                            .clip(RoundedCornerShape(6.dp))
                            .background(Color(android.graphics.Color.parseColor(status.colorHex)))
                            .padding(horizontal = 8.dp),
                        contentAlignment = Alignment.CenterStart
                    ) {
                        if (count > 0) {
                            Text(
                                text = "$count",
                                color = Color.White,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                    
                    if (count == 0) {
                        Text(
                            text = "0",
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                            fontSize = 11.sp,
                            modifier = Modifier.padding(start = 6.dp)
                        )
                    }
                }
            }
        }
    }
}

// ==========================================
// CARDS & VIEWS DESIGN
// ==========================================

@Composable
fun DealCard(deal: ClientDeal, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .testTag("deal_card_${deal.id}"),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        elevation = CardDefaults.cardElevation(0.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Status bar indicator
            Box(
                modifier = Modifier
                    .width(4.dp)
                    .height(54.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(Color(android.graphics.Color.parseColor(deal.statusEnum.colorHex)))
            )

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = deal.clientName,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(2.dp))

                // Car details
                val carInfo = if (deal.carMake.isNotEmpty()) "${deal.carMake} ${deal.carModel}" else "Без автомобиля"
                Text(
                    text = carInfo,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                // Complaint snippet
                if (deal.complaint.isNotEmpty()) {
                    Text(
                        text = deal.complaint,
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.padding(top = 2.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            // Fixed-width status capsule & price column on the right
            Column(
                modifier = Modifier.width(110.dp),
                horizontalAlignment = Alignment.End
            ) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(100.dp))
                        .background(Color(android.graphics.Color.parseColor(deal.statusEnum.colorHex)).copy(alpha = 0.12f))
                        .padding(horizontal = 8.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = deal.statusEnum.displayName,
                        color = Color(android.graphics.Color.parseColor(deal.statusEnum.colorHex)),
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                
                Spacer(modifier = Modifier.height(6.dp))
                
                Text(
                    text = formatCurrency(deal.dealValue),
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.primary,
                    textAlign = TextAlign.End
                )
            }
        }
    }
}

@Composable
fun TaskCard(
    task: CrmTask,
    associatedDeal: ClientDeal?,
    onToggle: () -> Unit,
    onDelete: () -> Unit
) {
    val cardBg by animateColorAsState(
        targetValue = if (task.isCompleted) {
            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.25f)
        } else {
            MaterialTheme.colorScheme.surface
        },
        animationSpec = tween(200),
        label = "cardBg"
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("task_card_${task.id}"),
        colors = CardDefaults.cardColors(containerColor = cardBg),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.8f)),
        elevation = CardDefaults.cardElevation(0.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(
                checked = task.isCompleted,
                onCheckedChange = { onToggle() },
                colors = CheckboxDefaults.colors(
                    checkedColor = MaterialTheme.colorScheme.primary,
                    checkmarkColor = MaterialTheme.colorScheme.onPrimary
                ),
                modifier = Modifier.testTag("task_checkbox_${task.id}")
            )

            Spacer(modifier = Modifier.width(10.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = task.title,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = if (task.isCompleted) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                    
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(Color(android.graphics.Color.parseColor(task.priorityEnum.colorHex)).copy(alpha = 0.15f))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = task.priorityEnum.displayName,
                            color = Color(android.graphics.Color.parseColor(task.priorityEnum.colorHex)),
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                if (task.description.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = task.description,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Associated Deal tag
                    if (associatedDeal != null) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(6.dp)
                                    .background(
                                        Color(android.graphics.Color.parseColor(associatedDeal.statusEnum.colorHex)),
                                        CircleShape
                                    )
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = associatedDeal.clientName,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    } else {
                        Text("Общая задача", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }

                    // Due Date
                    if (task.dueDate.isNotEmpty()) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.DateRange,
                                contentDescription = "Дата выполнения",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                                modifier = Modifier.size(12.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = task.dueDate,
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            IconButton(
                onClick = onDelete,
                modifier = Modifier.testTag("delete_task_button_${task.id}")
            ) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Удалить задачу",
                    tint = MaterialTheme.colorScheme.error.copy(alpha = 0.8f),
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

@Composable
fun EmptyStateView(title: String, subtitle: String) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.Info,
            contentDescription = "Инфо",
            tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.4f),
            modifier = Modifier.size(64.dp)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = subtitle,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
    }
}

// ==========================================
// DIALOGS & FORMS IMPLEMENTATION
// ==========================================

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditDealDialog(
    viewModel: CrmViewModel,
    onDismiss: () -> Unit,
    onConfirm: (ClientDeal) -> Unit
) {
    var namePhoneInput by remember { mutableStateOf("") }
    var whatNeeded by remember { mutableStateOf("") }
    var visitDate by remember { mutableStateOf("") }
    
    var errorInput by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Column {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(100.dp))
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f))
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(
                        "БЫСТРАЯ ЗАЯВКА",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text("Создание заявки", fontWeight = FontWeight.Bold, fontSize = 20.sp)
                Text(
                    "Три поля для старта, детали заполним в карточке.",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedTextField(
                    value = namePhoneInput,
                    onValueChange = { namePhoneInput = it; errorInput = false },
                    label = { Text("ИМЯ / ТЕЛЕФОН *") },
                    placeholder = { Text("Иван или +386...") },
                    isError = errorInput,
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                if (errorInput) {
                    Text("Имя или телефон обязательны", color = MaterialTheme.colorScheme.error, fontSize = 11.sp)
                }

                OutlinedTextField(
                    value = whatNeeded,
                    onValueChange = { whatNeeded = it },
                    label = { Text("ЧТО НУЖНО? *") },
                    placeholder = { Text("Не заводится / ошибка / диагностика") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = visitDate,
                        onValueChange = { visitDate = it },
                        label = { Text("ДАТА ВИЗИТА") },
                        placeholder = { Text("Можно не указывать") },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                    IconButton(
                        onClick = {
                            // Quick placeholder timestamp
                            val sdf = java.text.SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
                            visitDate = sdf.format(java.util.Date())
                        },
                        modifier = Modifier
                            .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(8.dp))
                            .size(54.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.DateRange,
                            contentDescription = "Сегодня",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                Text("Поля со звездочкой обязательны.", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (namePhoneInput.trim().isEmpty()) {
                        errorInput = true
                    } else {
                        // Smart extraction
                        val cleaned = namePhoneInput.trim()
                        val numPart = cleaned.filter { it.isDigit() || it == '+' }
                        
                        var clientName = cleaned
                        var phone = ""
                        
                        if (numPart.length >= 6) {
                            phone = viewModel.formatSloveniaPhone(numPart)
                            clientName = cleaned.replace(numPart, "").replace(Regex("[,;]+"), "").trim()
                            if (clientName.isEmpty()) {
                                clientName = "Новый клиент"
                            }
                        }
                        
                        onConfirm(
                            ClientDeal(
                                clientName = clientName,
                                phone = phone,
                                complaint = whatNeeded.trim(),
                                visitDate = visitDate.trim(),
                                status = "NEW",
                                detailedStatus = "Новая",
                                dealValue = 0.0
                            )
                        )
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .testTag("submit_deal_button")
            ) {
                Text("Создать заявку", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {}
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditTaskDialog(
    deals: List<ClientDeal>,
    onDismiss: () -> Unit,
    onConfirm: (CrmTask) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var date by remember { mutableStateOf("") }
    var priority by remember { mutableStateOf(TaskPriority.MEDIUM) }
    var selectedDealId by remember { mutableStateOf<Long?>(null) }
    
    var errorTitle by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Новая задача", fontWeight = FontWeight.Bold) },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it; errorTitle = false },
                    label = { Text("Название задачи *") },
                    isError = errorTitle,
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                if (errorTitle) {
                    Text("Название обязательно", color = MaterialTheme.colorScheme.error, fontSize = 11.sp)
                }

                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Описание") },
                    maxLines = 2,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = date,
                    onValueChange = { date = it },
                    label = { Text("Срок выполнения (yyyy-mm-dd)") },
                    placeholder = { Text("Пример: 2026-06-30") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                // Priority
                var expandedPriority by remember { mutableStateOf(false) }
                ExposedDropdownMenuBox(
                    expanded = expandedPriority,
                    onExpandedChange = { expandedPriority = !expandedPriority }
                ) {
                    OutlinedTextField(
                        readOnly = true,
                        value = priority.displayName,
                        onValueChange = {},
                        label = { Text("Приоритет") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedPriority) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(MenuAnchorType.PrimaryNotEditable, enabled = true)
                    )
                    ExposedDropdownMenu(
                        expanded = expandedPriority,
                        onDismissRequest = { expandedPriority = false }
                    ) {
                        TaskPriority.values().forEach { prio ->
                            DropdownMenuItem(
                                text = { Text(prio.displayName) },
                                onClick = {
                                    priority = prio
                                    expandedPriority = false
                                }
                            )
                        }
                    }
                }

                // Link to Deal
                var expandedDeals by remember { mutableStateOf(false) }
                val selectedDealName = deals.find { it.id == selectedDealId }?.clientName ?: "Не связывать"
                ExposedDropdownMenuBox(
                    expanded = expandedDeals,
                    onExpandedChange = { expandedDeals = !expandedDeals }
                ) {
                    OutlinedTextField(
                        readOnly = true,
                        value = selectedDealName,
                        onValueChange = {},
                        label = { Text("Связать с ремонтом") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedDeals) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(MenuAnchorType.PrimaryNotEditable, enabled = true)
                    )
                    ExposedDropdownMenu(
                        expanded = expandedDeals,
                        onDismissRequest = { expandedDeals = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Не связывать") },
                            onClick = {
                                selectedDealId = null
                                expandedDeals = false
                            }
                        )
                        deals.forEach { d ->
                            DropdownMenuItem(
                                text = { Text("${d.clientName} (${d.carMake})") },
                                onClick = {
                                    selectedDealId = d.id
                                    expandedDeals = false
                                }
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (title.trim().isEmpty()) {
                        errorTitle = true
                    } else {
                        onConfirm(
                            CrmTask(
                                title = title.trim(),
                                description = description.trim(),
                                dueDate = date.trim().ifEmpty {
                                    val sdf = java.text.SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                                    sdf.format(java.util.Date())
                                },
                                priority = priority.name,
                                dealId = selectedDealId,
                                isCompleted = false
                            )
                        )
                    }
                },
                modifier = Modifier.testTag("submit_task_button")
            ) {
                Text("Добавить")
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) {
                Text("Отмена")
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DealDetailsDialog(
    deal: ClientDeal,
    viewModel: CrmViewModel,
    onDismiss: () -> Unit
) {
    // --- EDIT STATES ---
    var isEditingClient by remember { mutableStateOf(false) }
    var isAddingCar by remember { mutableStateOf(false) }

    // Customer fields
    var clientName by remember { mutableStateOf(deal.clientName) }
    var phone by remember { mutableStateOf(deal.phone) }
    var email by remember { mutableStateOf(deal.email) }

    // Trouble description fields
    var complaint by remember { mutableStateOf(deal.complaint) }
    var diagnostics by remember { mutableStateOf(deal.diagnostics) }
    var solution by remember { mutableStateOf(deal.solution) }

    // Operations fields
    var detailedStatus by remember { mutableStateOf(deal.detailedStatus) }
    var visitDate by remember { mutableStateOf(deal.visitDate) }
    var priority by remember { mutableStateOf(deal.statusEnum) } // Using DealStatus as stage
    var laborMinutes by remember { mutableStateOf(deal.laborMinutes.toString()) }
    var remindDate by remember { mutableStateOf(deal.remindDate) }
    
    // Revenue share
    var myShare by remember { mutableStateOf(deal.myShare) }
    var alexShare by remember { mutableStateOf(deal.alexShare) }

    // Car fields
    var carMake by remember { mutableStateOf(deal.carMake) }
    var carModel by remember { mutableStateOf(deal.carModel) }
    var carLicense by remember { mutableStateOf(deal.carLicense) }
    var carVin by remember { mutableStateOf(deal.carVin) }
    var carYear by remember { mutableStateOf(deal.carYear) }
    var carEngine by remember { mutableStateOf(deal.carEngine) }

    // Parts expenses fields
    var newPartName by remember { mutableStateOf("") }
    var newPartCost by remember { mutableStateOf("") }

    // Observers
    val expenses by viewModel.getExpensesForDeal(deal.id).collectAsState(initial = emptyList())
    val linkedTasks by viewModel.getTasksForDeal(deal.id).collectAsState(initial = emptyList())

    // Auto-save changes helper for the textareas & operational inputs
    val saveChanges = {
        val totalPartsCost = expenses.sumOf { it.cost }
        viewModel.updateDeal(
            deal.copy(
                clientName = clientName.trim(),
                phone = phone.trim(),
                email = email.trim(),
                complaint = complaint.trim(),
                diagnostics = diagnostics.trim(),
                solution = solution.trim(),
                detailedStatus = detailedStatus.trim(),
                visitDate = visitDate.trim(),
                laborMinutes = laborMinutes.toIntOrNull() ?: 0,
                remindDate = remindDate.trim(),
                myShare = myShare,
                alexShare = alexShare,
                carMake = carMake.trim(),
                carModel = carModel.trim(),
                carLicense = carLicense.trim(),
                carVin = carVin.trim(),
                carYear = carYear.trim(),
                carEngine = carEngine.trim(),
                dealValue = totalPartsCost // Auto CRM update: value is total of parts expenses
            )
        )
    }

    // Recalculate value whenever expenses list changes
    LaunchedEffect(expenses) {
        val totalPartsCost = expenses.sumOf { it.cost }
        if (deal.dealValue != totalPartsCost) {
            viewModel.updateDeal(deal.copy(dealValue = totalPartsCost))
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        modifier = Modifier.fillMaxWidth(0.96f),
        properties = androidx.compose.ui.window.DialogProperties(usePlatformDefaultWidth = false),
        title = {
            Column(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Заявка #${deal.id} • ${detailedStatus.ifEmpty { deal.statusEnum.displayName }}",
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Создана: 29.03.2026 16:42 • Дата визита: ${visitDate.ifEmpty { "—" }}",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Row {
                        IconButton(
                            onClick = {
                                viewModel.deleteDeal(deal)
                                onDismiss()
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "Удалить заявку",
                                tint = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                }
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(480.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // STATUS SELECTOR
                var expandedStatus by remember { mutableStateOf(false) }
                val currentStatus = deal.statusEnum

                ExposedDropdownMenuBox(
                    expanded = expandedStatus,
                    onExpandedChange = { expandedStatus = !expandedStatus }
                ) {
                    OutlinedTextField(
                        value = currentStatus.displayName,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Статус заявки") },
                        leadingIcon = {
                            Box(
                                modifier = Modifier
                                    .size(14.dp)
                                    .background(
                                        Color(android.graphics.Color.parseColor(currentStatus.colorHex)),
                                        CircleShape
                                    )
                            )
                        },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedStatus) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(android.graphics.Color.parseColor(currentStatus.colorHex)),
                            unfocusedBorderColor = Color(android.graphics.Color.parseColor(currentStatus.colorHex)).copy(alpha = 0.6f)
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                    )
                    ExposedDropdownMenu(
                        expanded = expandedStatus,
                        onDismissRequest = { expandedStatus = false }
                    ) {
                        DealStatus.values().forEach { s ->
                            DropdownMenuItem(
                                text = {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Box(
                                            modifier = Modifier
                                                .size(10.dp)
                                                .background(Color(android.graphics.Color.parseColor(s.colorHex)), CircleShape)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(s.displayName, fontWeight = FontWeight.Bold)
                                    }
                                },
                                onClick = {
                                    viewModel.updateDeal(deal.copy(status = s.name))
                                    expandedStatus = false
                                }
                            )
                        }
                    }
                }

                // PANEL 1: ЖАЛОБА, ДИАГНОСТИКА, РЕШЕНИЕ
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.25f)),
                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                ) {
                    Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Жалоба, диагностика, решение", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = MaterialTheme.colorScheme.primary)
                            Text("✓ Сохранено", fontSize = 11.sp, color = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f))
                        }

                        OutlinedTextField(
                            value = complaint,
                            onValueChange = { complaint = it; saveChanges() },
                            label = { Text("ЖАЛОБА") },
                            modifier = Modifier.fillMaxWidth(),
                            maxLines = 3,
                            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = MaterialTheme.colorScheme.primary)
                        )

                        OutlinedTextField(
                            value = diagnostics,
                            onValueChange = { diagnostics = it; saveChanges() },
                            label = { Text("ДИАГНОСТИКА") },
                            modifier = Modifier.fillMaxWidth(),
                            maxLines = 3,
                            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = MaterialTheme.colorScheme.primary)
                        )

                        OutlinedTextField(
                            value = solution,
                            onValueChange = { solution = it; saveChanges() },
                            label = { Text("РЕШЕНИЕ") },
                            modifier = Modifier.fillMaxWidth(),
                            maxLines = 3,
                            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = MaterialTheme.colorScheme.primary)
                        )
                    }
                }

                // PANEL 2: ОПЕРАТИВНЫЕ ДЕЙСТВИЯ
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.25f)),
                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                ) {
                    Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Оперативные действия", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = MaterialTheme.colorScheme.primary)
                            Text("✓ Сохранено", fontSize = 11.sp, color = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f))
                        }

                        OutlinedTextField(
                            value = detailedStatus,
                            onValueChange = { detailedStatus = it; saveChanges() },
                            label = { Text("СТАТУС") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )

                        OutlinedTextField(
                            value = visitDate,
                            onValueChange = { visitDate = it; saveChanges() },
                            label = { Text("ДАТА ВИЗИТА") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )

                        OutlinedTextField(
                            value = laborMinutes,
                            onValueChange = { laborMinutes = it; saveChanges() },
                            label = { Text("ТРУДОЗАТРАТЫ (МИН)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )

                        OutlinedTextField(
                            value = remindDate,
                            onValueChange = { remindDate = it; saveChanges() },
                            label = { Text("НАПОМНИТЬ") },
                            placeholder = { Text("дд/мм/гггг чч:мм") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )

                        Text("Доли по заявке", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            OutlinedTextField(
                                value = myShare.toString(),
                                onValueChange = {
                                    val newVal = it.toIntOrNull() ?: 0
                                    myShare = newVal
                                    alexShare = (100 - newVal).coerceAtLeast(0)
                                    saveChanges()
                                },
                                label = { Text("МОЯ ДОЛЯ") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                modifier = Modifier.weight(1f)
                            )
                            OutlinedTextField(
                                value = alexShare.toString(),
                                onValueChange = {
                                    val newVal = it.toIntOrNull() ?: 0
                                    alexShare = newVal
                                    myShare = (100 - newVal).coerceAtLeast(0)
                                    saveChanges()
                                },
                                label = { Text("ALEX") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                modifier = Modifier.weight(1f)
                            )
                        }
                        Text("Остаток распределяется автоматически.", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }

                // PANEL 3: КЛИЕНТ
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.25f)),
                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                ) {
                    Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Клиент", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = MaterialTheme.colorScheme.primary)
                            OutlinedButton(
                                onClick = { isEditingClient = !isEditingClient },
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                                modifier = Modifier.height(28.dp)
                            ) {
                                Text(if (isEditingClient) "Готово" else "Редактировать клиента", fontSize = 11.sp)
                            }
                        }

                        if (!isEditingClient) {
                            Text(text = clientName, fontWeight = FontWeight.ExtraBold, fontSize = 18.sp)
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Phone, contentDescription = "Телефон", modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.primary)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(text = phone.ifEmpty { "Нет телефона" }, fontSize = 13.sp)
                            }
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Email, contentDescription = "Email", modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.secondary)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(text = email.ifEmpty { "Email не указан" }, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        } else {
                            OutlinedTextField(
                                value = clientName,
                                onValueChange = { clientName = it; saveChanges() },
                                label = { Text("Имя") },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth()
                            )
                            OutlinedTextField(
                                value = phone,
                                onValueChange = { phone = viewModel.formatSloveniaPhone(it); saveChanges() },
                                label = { Text("Телефон") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth()
                            )
                            OutlinedTextField(
                                value = email,
                                onValueChange = { email = it; saveChanges() },
                                label = { Text("Email") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }

                // PANEL 4: АВТОМОБИЛЬ
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.25f)),
                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                ) {
                    Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Автомобиль", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = MaterialTheme.colorScheme.primary)
                            if (carMake.isEmpty() && !isAddingCar) {
                                Button(
                                    onClick = { isAddingCar = true },
                                    shape = RoundedCornerShape(8.dp),
                                    contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                                    modifier = Modifier.height(28.dp)
                                ) {
                                    Text("Добавить автомобиль", fontSize = 11.sp)
                                }
                            } else {
                                OutlinedButton(
                                    onClick = { isAddingCar = !isAddingCar },
                                    shape = RoundedCornerShape(8.dp),
                                    contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                                    modifier = Modifier.height(28.dp)
                                ) {
                                    Text(if (isAddingCar) "Свернуть" else "Редактировать авто", fontSize = 11.sp)
                                }
                            }
                        }

                        if (carMake.isNotEmpty() && !isAddingCar) {
                            Text("Данные автомобиля добавлены.", fontSize = 11.sp, color = MaterialTheme.colorScheme.primary)
                            Text(text = "$carMake $carModel ($carYear)", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                            Text(text = "Гос. Номер: ${carLicense.ifEmpty { "—" }}", fontSize = 12.sp)
                            Text(text = "VIN: ${carVin.ifEmpty { "—" }}", fontSize = 12.sp)
                            Text(text = "Двигатель: ${carEngine.ifEmpty { "—" }}", fontSize = 12.sp)
                        } else if (isAddingCar) {
                            OutlinedTextField(
                                value = carMake,
                                onValueChange = { carMake = it; saveChanges() },
                                label = { Text("МАРКА") },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth()
                            )
                            OutlinedTextField(
                                value = carModel,
                                onValueChange = { carModel = it; saveChanges() },
                                label = { Text("МОДЕЛЬ") },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth()
                            )
                            OutlinedTextField(
                                value = carLicense,
                                onValueChange = { carLicense = it; saveChanges() },
                                label = { Text("ГОС. НОМЕР") },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth()
                            )
                            OutlinedTextField(
                                value = carVin,
                                onValueChange = { carVin = it; saveChanges() },
                                label = { Text("VIN") },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth()
                            )
                            OutlinedTextField(
                                value = carYear,
                                onValueChange = { carYear = it; saveChanges() },
                                label = { Text("ГОД") },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth()
                            )
                            OutlinedTextField(
                                value = carEngine,
                                onValueChange = { carEngine = it; saveChanges() },
                                label = { Text("ДВИГАТЕЛЬ") },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth()
                            )
                        } else {
                            Text("Данные автомобиля отсутствуют.", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }

                // PANEL 5: ВЛОЖЕНИЯ
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.25f)),
                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                ) {
                    Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text("Вложения", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = MaterialTheme.colorScheme.primary)
                        Text("Фото до ремонта • Фото после • Документы", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("Нет файлов", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f))
                        
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            OutlinedButton(onClick = {}, shape = RoundedCornerShape(8.dp)) {
                                Text("Choose Files", fontSize = 11.sp)
                            }
                            Text("No file chosen", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }

                // PANEL 6: РАСХОДЫ НА ЗАПЧАСТИ
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.25f)),
                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                ) {
                    Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Расходы на запчасти", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = MaterialTheme.colorScheme.primary)
                            Text(
                                "Итого: ${formatCurrency(expenses.sumOf { it.cost })}",
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 14.sp,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }

                        // Table list of expenses
                        if (expenses.isEmpty()) {
                            Text("Расходы отсутствуют", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f))
                        } else {
                            Column(
                                modifier = Modifier.fillMaxWidth(),
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("РАСХОД", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.weight(1.5f))
                                    Text("СУММА", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.weight(1f))
                                    Text("ДЕЙСТВИЯ", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.weight(1.2f), textAlign = TextAlign.End)
                                }
                                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

                                expenses.forEach { exp ->
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(exp.name, fontSize = 12.sp, modifier = Modifier.weight(1.5f), maxLines = 1, overflow = TextOverflow.Ellipsis)
                                        Text(formatCurrency(exp.cost), fontSize = 12.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.error, modifier = Modifier.weight(1f))
                                        
                                        Row(
                                            modifier = Modifier.weight(1.2f),
                                            horizontalArrangement = Arrangement.End
                                        ) {
                                            IconButton(
                                                onClick = { viewModel.deleteExpense(exp) },
                                                modifier = Modifier.size(24.dp)
                                            ) {
                                                Icon(Icons.Default.Delete, contentDescription = "Удалить", tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(16.dp))
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

                        // Form to add new expense
                        Text("Добавить деталь/расход:", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        OutlinedTextField(
                            value = newPartName,
                            onValueChange = { newPartName = it },
                            label = { Text("НАИМЕНОВАНИЕ") },
                            placeholder = { Text("Например: Тормозные диски") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedTextField(
                                value = newPartCost,
                                onValueChange = { newPartCost = it },
                                label = { Text("СТОИМОСТЬ, €") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                singleLine = true,
                                modifier = Modifier.weight(1.3f)
                            )

                            Button(
                                onClick = {
                                    val cost = newPartCost.toDoubleOrNull()
                                    if (newPartName.trim().isNotEmpty() && cost != null) {
                                        viewModel.addExpense(
                                            PartExpense(
                                                dealId = deal.id,
                                                name = newPartName.trim(),
                                                cost = cost
                                            )
                                        )
                                        newPartName = ""
                                        newPartCost = ""
                                    }
                                },
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("+ Добавить", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                modifier = Modifier.fillMaxWidth().height(44.dp)
            ) {
                Text("Закрыть")
            }
        }
    )
}

@Composable
fun DetailRow(label: String, value: String, isPhone: Boolean = false, isEmail: Boolean = false) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.Top
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(text = label, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(text = value, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Medium)
        }
        
        if (isPhone) {
            Icon(
                imageVector = Icons.Default.Phone,
                contentDescription = "Позвонить",
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(20.dp).align(Alignment.CenterVertically)
            )
        }
        if (isEmail) {
            Icon(
                imageVector = Icons.Default.Email,
                contentDescription = "Отправить email",
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(20.dp).align(Alignment.CenterVertically)
            )
        }
    }
}

// Simple local currency formatter helper (Euros)
fun formatCurrency(amount: Double): String {
    val formatter = NumberFormat.getCurrencyInstance(Locale.GERMANY) // standard EU/German layout: XX.XX €
    return try {
        formatter.format(amount)
    } catch (e: Exception) {
        String.format(Locale.US, "%.2f €", amount)
    }
}
