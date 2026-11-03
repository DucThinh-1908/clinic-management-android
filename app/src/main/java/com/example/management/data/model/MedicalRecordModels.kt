package com.example.management.data.model

data class MedicalRecordItem(
    val id: Long,
    val recordCode: String?,
    val patientId: Long?,
    val patientName: String?,
    val doctorId: Long?,
    val doctorName: String?,
    val departmentName: String?,
    val chiefComplaint: String?,
    val initialDiagnosis: String?,
    val finalDiagnosis: String?,
    val icd10Code: String?,
    val doctorAdvice: String?,
    val followUpDate: String?,
    val status: String?,
    val createdAt: String?
)

data class VitalSignsItem(
    val id: Long?,
    val temperature: Double?,
    val bloodPressureSystolic: Int?,
    val bloodPressureDiastolic: Int?,
    val heartRate: Int?,
    val respiratoryRate: Int?,
    val height: Double?,
    val weight: Double?,
    val bmi: Double?
)

typealias MedicalRecordSummaryDto = MedicalRecordItem
