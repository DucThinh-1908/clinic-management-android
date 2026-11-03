package com.example.management.data.model

data class SaveDiagnosisRequest(
    val medicalRecordId: Long,
    val symptoms: String?,
    val initialDiagnosis: String?,
    val finalDiagnosis: String?,
    val icd10Code: String?,
    val treatmentPlan: String?,
    val doctorAdvice: String?
)

data class OrderLabServicesRequest(
    val medicalRecordId: Long,
    val patientId: Long,
    val doctorId: Long,
    val serviceIds: List<Long>,
    val clinicalNotes: String? = null
)

data class ScheduleFollowUpRequest(
    val medicalRecordId: Long,
    val followUpDate: String, // YYYY-MM-DD
    val notes: String? = null
)

data class CompleteEncounterRequest(
    val medicalRecordId: Long,
    val doctorId: Long,
    val summaryNotes: String? = null
)

data class ClinicalServiceDto(
    val id: Long,
    val categoryId: Long?,
    val code: String?,
    val name: String?,
    val price: Double = 0.0,
    val insuranceCovered: Boolean = false,
    val description: String? = null
)

data class MedicalSummaryPrintDto(
    val clinicName: String?,
    val clinicAddress: String?,
    val patientName: String?,
    val patientCode: String?,
    val gender: String?,
    val age: Int?,
    val diagnosis: String?,
    val icd10Code: String?,
    val doctorAdvice: String?,
    val followUpDate: String?,
    val doctorName: String?
)
