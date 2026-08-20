package com.example.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.AlertNotification
import com.example.data.model.Equipment
import com.example.data.model.MonthlyLog
import kotlinx.coroutines.flow.Flow

@Dao
interface EquipmentDao {

    // Equipment Queries
    @Query("SELECT * FROM equipment ORDER BY monthlyKwh DESC")
    fun getAllEquipment(): Flow<List<Equipment>>

    @Query("SELECT * FROM equipment WHERE id = :id")
    fun getEquipmentById(id: Long): Flow<Equipment?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEquipment(equipment: Equipment): Long

    @Update
    suspend fun updateEquipment(equipment: Equipment)

    @Delete
    suspend fun deleteEquipment(equipment: Equipment)

    @Query("DELETE FROM equipment WHERE id = :id")
    suspend fun deleteEquipmentById(id: Long)

    @Query("DELETE FROM equipment")
    suspend fun deleteAllEquipment()

    // Monthly Logs Queries
    @Query("SELECT * FROM monthly_logs ORDER BY year ASC, monthIndex ASC")
    fun getAllMonthlyLogs(): Flow<List<MonthlyLog>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMonthlyLogs(logs: List<MonthlyLog>)

    @Query("DELETE FROM monthly_logs")
    suspend fun deleteAllMonthlyLogs()

    // Alerts Queries
    @Query("SELECT * FROM alert_notifications ORDER BY timestamp DESC")
    fun getAllAlerts(): Flow<List<AlertNotification>>

    @Query("SELECT COUNT(*) FROM alert_notifications WHERE isRead = 0")
    fun getUnreadAlertCount(): Flow<Int>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAlert(alert: AlertNotification): Long

    @Query("UPDATE alert_notifications SET isRead = 1 WHERE id = :id")
    suspend fun markAlertAsRead(id: Long)

    @Query("UPDATE alert_notifications SET isRead = 1")
    suspend fun markAllAlertsAsRead()

    @Query("DELETE FROM alert_notifications")
    suspend fun deleteAllAlerts()
}
