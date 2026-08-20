package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.AppDatabase
import com.example.data.model.AlertNotification
import com.example.data.model.EnergyAuditSummary
import com.example.data.model.Equipment
import com.example.data.model.EquipmentCategory
import com.example.data.model.MonthlyLog
import com.example.data.remote.GeminiAiService
import com.example.data.repository.UserSettings
import com.example.data.repository.VoltSenseRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class EquipmentSortOption(val displayName: String) {
    HIGHEST_CONSUMPTION("Highest Consumption (kWh)"),
    HIGHEST_COST("Highest Cost ($/mo)"),
    LOWEST_EFFICIENCY("Lowest Efficiency (Inefficient first)"),
    HIGHEST_EFFICIENCY("Highest Efficiency (Eco first)"),
    NAME("Name (A-Z)"),
    OLDEST("Oldest First")
}

data class ChatMessage(
    val isUser: Boolean,
    val text: String,
    val timestamp: Long = System.currentTimeMillis()
)

class VoltSenseViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: VoltSenseRepository
    private val geminiService = GeminiAiService()

    val settings: StateFlow<UserSettings>
    val allEquipment: StateFlow<List<Equipment>>
    val allMonthlyLogs: StateFlow<List<MonthlyLog>>
    val allAlerts: StateFlow<List<AlertNotification>>
    val unreadAlertCount: StateFlow<Int>
    val auditSummary: StateFlow<EnergyAuditSummary>

    // UI Filter & Search State
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _selectedCategory = MutableStateFlow<EquipmentCategory?>(null)
    val selectedCategory: StateFlow<EquipmentCategory?> = _selectedCategory.asStateFlow()

    private val _sortOption = MutableStateFlow(EquipmentSortOption.HIGHEST_CONSUMPTION)
    val sortOption: StateFlow<EquipmentSortOption> = _sortOption.asStateFlow()

    // Filtered equipment list
    val filteredEquipment: StateFlow<List<Equipment>>

    // Dialog & Sheet State
    private val _selectedEquipmentForDetail = MutableStateFlow<Equipment?>(null)
    val selectedEquipmentForDetail: StateFlow<Equipment?> = _selectedEquipmentForDetail.asStateFlow()

    private val _showAddEditModal = MutableStateFlow(false)
    val showAddEditModal: StateFlow<Boolean> = _showAddEditModal.asStateFlow()

    private val _equipmentToEdit = MutableStateFlow<Equipment?>(null)
    val equipmentToEdit: StateFlow<Equipment?> = _equipmentToEdit.asStateFlow()

    private val _showNotificationSheet = MutableStateFlow(false)
    val showNotificationSheet: StateFlow<Boolean> = _showNotificationSheet.asStateFlow()

    // Comparison State
    private val _compareDeviceA = MutableStateFlow<Equipment?>(null)
    val compareDeviceA: StateFlow<Equipment?> = _compareDeviceA.asStateFlow()

    private val _compareDeviceB = MutableStateFlow<Equipment?>(null)
    val compareDeviceB: StateFlow<Equipment?> = _compareDeviceB.asStateFlow()

    // AI Assistant State
    private val _chatMessages = MutableStateFlow<List<ChatMessage>>(listOf(
        ChatMessage(
            isUser = false,
            text = "👋 Hello! I am your VoltSense AI Energy Consultant. Ask me anything about your appliances' energy consumption, efficiency rating, electricity cost reduction, or whether it's worth replacing an old device!"
        )
    ))
    val chatMessages: StateFlow<List<ChatMessage>> = _chatMessages.asStateFlow()

    private val _isAiThinking = MutableStateFlow(false)
    val isAiThinking: StateFlow<Boolean> = _isAiThinking.asStateFlow()

    init {
        val db = AppDatabase.getDatabase(application)
        repository = VoltSenseRepository(db.equipmentDao(), application)
        settings = repository.settings

        allEquipment = repository.allEquipment.stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            emptyList()
        )

        allMonthlyLogs = repository.allMonthlyLogs.stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            emptyList()
        )

        allAlerts = repository.allAlerts.stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            emptyList()
        )

        unreadAlertCount = repository.unreadAlertCount.stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            0
        )

        auditSummary = repository.auditSummary.stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            EnergyAuditSummary(0, 0.0, 0.0, 0.0, 0, 0.0, 0, 0)
        )

        filteredEquipment = combine(
            allEquipment,
            _searchQuery,
            _selectedCategory,
            _sortOption
        ) { list, query, category, sort ->
            var result = list

            if (query.isNotBlank()) {
                result = result.filter {
                    it.name.contains(query, ignoreCase = true) ||
                            it.category.displayName.contains(query, ignoreCase = true) ||
                            it.roomLocation.contains(query, ignoreCase = true) ||
                            it.brandModel.contains(query, ignoreCase = true)
                }
            }

            if (category != null) {
                result = result.filter { it.category == category }
            }

            when (sort) {
                EquipmentSortOption.HIGHEST_CONSUMPTION -> result.sortedByDescending { it.monthlyKwh }
                EquipmentSortOption.HIGHEST_COST -> result.sortedByDescending { it.monthlyCost }
                EquipmentSortOption.LOWEST_EFFICIENCY -> result.sortedBy { it.efficiencyScore }
                EquipmentSortOption.HIGHEST_EFFICIENCY -> result.sortedByDescending { it.efficiencyScore }
                EquipmentSortOption.NAME -> result.sortedBy { it.name.lowercase() }
                EquipmentSortOption.OLDEST -> result.sortedByDescending { it.ageYears }
            }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

        // Initial Seed
        viewModelScope.launch {
            repository.seedInitialDataIfEmpty()
        }
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun setSelectedCategory(category: EquipmentCategory?) {
        _selectedCategory.value = category
    }

    fun setSortOption(option: EquipmentSortOption) {
        _sortOption.value = option
    }

    fun openAddEquipmentDialog() {
        _equipmentToEdit.value = null
        _showAddEditModal.value = true
    }

    fun openEditEquipmentDialog(equipment: Equipment) {
        _equipmentToEdit.value = equipment
        _showAddEditModal.value = true
    }

    fun closeAddEditDialog() {
        _showAddEditModal.value = false
        _equipmentToEdit.value = null
    }

    fun selectEquipmentForDetail(equipment: Equipment?) {
        _selectedEquipmentForDetail.value = equipment
    }

    fun setShowNotificationSheet(show: Boolean) {
        _showNotificationSheet.value = show
        if (show) {
            viewModelScope.launch {
                repository.markAllAlertsAsRead()
            }
        }
    }

    fun markAlertAsRead(alertId: Long) {
        viewModelScope.launch {
            repository.markAlertAsRead(alertId)
        }
    }

    fun setCompareDeviceA(equipment: Equipment?) {
        _compareDeviceA.value = equipment
    }

    fun setCompareDeviceB(equipment: Equipment?) {
        _compareDeviceB.value = equipment
    }

    fun saveEquipment(
        name: String,
        category: EquipmentCategory,
        roomLocation: String,
        powerWatts: Double,
        hoursPerDay: Double,
        ageYears: Double,
        brandModel: String,
        starRating: Int,
        isInverterOrEco: Boolean,
        replacementCostEstimate: Double?,
        notes: String,
        id: Long = 0
    ) {
        viewModelScope.launch {
            repository.saveEquipment(
                name = name,
                category = category,
                roomLocation = roomLocation,
                powerWatts = powerWatts,
                hoursPerDay = hoursPerDay,
                ageYears = ageYears,
                brandModel = brandModel,
                starRating = starRating,
                isInverterOrEco = isInverterOrEco,
                replacementCostEstimate = replacementCostEstimate,
                notes = notes,
                id = id
            )
            closeAddEditDialog()
            _selectedEquipmentForDetail.value = null
        }
    }

    fun deleteEquipment(equipment: Equipment) {
        viewModelScope.launch {
            repository.deleteEquipment(equipment)
            if (_selectedEquipmentForDetail.value?.id == equipment.id) {
                _selectedEquipmentForDetail.value = null
            }
            if (_compareDeviceA.value?.id == equipment.id) _compareDeviceA.value = null
            if (_compareDeviceB.value?.id == equipment.id) _compareDeviceB.value = null
        }
    }

    fun updateSettings(costPerKwh: Double, currencySymbol: String, highThreshold: Double, notifsEnabled: Boolean) {
        val newSettings = UserSettings(
            costPerKwh = costPerKwh,
            currencySymbol = currencySymbol,
            highConsumptionThresholdKwh = highThreshold,
            notificationsEnabled = notifsEnabled
        )
        repository.updateSettings(newSettings, viewModelScope)
    }

    fun triggerAnomalySimulation() {
        viewModelScope.launch {
            repository.triggerSampleHighConsumptionAnomaly()
        }
    }

    fun resetData() {
        viewModelScope.launch {
            repository.resetAllData()
            _selectedEquipmentForDetail.value = null
            _compareDeviceA.value = null
            _compareDeviceB.value = null
        }
    }

    fun sendAiMessage(prompt: String) {
        if (prompt.isBlank()) return
        val userMsg = ChatMessage(isUser = true, text = prompt)
        _chatMessages.value = _chatMessages.value + userMsg
        _isAiThinking.value = true

        viewModelScope.launch {
            val responseText = geminiService.askEnergyAdvisor(
                userQuery = prompt,
                equipmentList = allEquipment.value,
                auditSummary = auditSummary.value,
                currencySymbol = settings.value.currencySymbol,
                costPerKwh = settings.value.costPerKwh
            )
            _chatMessages.value = _chatMessages.value + ChatMessage(isUser = false, text = responseText)
            _isAiThinking.value = false
        }
    }
}
