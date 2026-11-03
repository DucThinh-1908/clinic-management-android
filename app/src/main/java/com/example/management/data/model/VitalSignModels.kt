package com.example.management.data.model

data class VitalSignRequest(
    val patientId: Long,
    val nurseId: Long? = null,
    val ticketId: Long? = null,
    val temperature: Double? = null,
    val bloodPressureSystolic: Int? = null,
    val bloodPressureDiastolic: Int? = null,
    val heartRate: Int? = null,
    val respiratoryRate: Int? = null,
    val spo2: Double? = null,
    val weightKg: Double? = null,
    val heightCm: Double? = null,
    val notes: String? = null
)

data class VitalSignResponse(
    val id: Long,
    val patientId: Long?,
    val patientName: String?,
    val nurseId: Long?,
    val nurseName: String?,
    val temperature: Double?,
    val bloodPressureSystolic: Int?,
    val bloodPressureDiastolic: Int?,
    val heartRate: Int?,
    val respiratoryRate: Int?,
    val spo2: Double?,
    val weightKg: Double?,
    val heightCm: Double?,
    val bmi: Double?,
    val bmiCategory: String?, // Thiếu cân, Bình thường, Thừa cân, Béo phì
    val bloodPressureStatus: String?, // Bình thường, Tăng huyết áp
    val temperatureStatus: String?, // Bình thường, Sốt nhẹ, Sốt cao
    val notes: String?,
    val measuredAt: String?
)
