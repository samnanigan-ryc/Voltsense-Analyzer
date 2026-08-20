package com.example.data.local

import androidx.room.TypeConverter
import com.example.data.model.EquipmentCategory
import com.example.data.model.ReplacementVerdict

class Converters {
    @TypeConverter
    fun fromEquipmentCategory(value: EquipmentCategory): String = value.name

    @TypeConverter
    fun toEquipmentCategory(value: String): EquipmentCategory = try {
        EquipmentCategory.valueOf(value)
    } catch (e: Exception) {
        EquipmentCategory.OTHER
    }

    @TypeConverter
    fun fromReplacementVerdict(value: ReplacementVerdict): String = value.name

    @TypeConverter
    fun toReplacementVerdict(value: String): ReplacementVerdict = try {
        ReplacementVerdict.valueOf(value)
    } catch (e: Exception) {
        ReplacementVerdict.EFFICIENT_KEEP
    }
}
