package com.example.management.data.model

data class CreatePatientRequest(
    val fullName: String,
    val gender: String = "MALE", // MALE, FEMALE, OTHER
    val dateOfBirth: String? = null, // yyyy-MM-dd
    val phone: String,
    val email: String? = null,
    val identityCard: String? = null,
    val healthInsuranceNumber: String? = null,
    val address: String? = null,
    val emergencyContactName: String? = null,
    val emergencyContactPhone: String? = null,
    val bloodGroup: String? = null,
    val medicalHistory: String? = null,
    val drugAllergies: String? = null,
    val userId: Long? = null
)

data class UpdateInsuranceRequest(
    val healthInsuranceNumber: String
)

data class PatientDetailDto(
    val id: Long,
    val userId: Long?,
    val patientCode: String?,
    val fullName: String?,
    val gender: String?,
    val dateOfBirth: String?,
    val age: Int?,
    val phone: String?,
    val email: String?,
    val identityCard: String?,
    val healthInsuranceNumber: String?,
    val address: String?,
    val emergencyContactName: String?,
    val emergencyContactPhone: String?,
    val bloodGroup: String?,
    val medicalHistory: String?,
    val drugAllergies: String?,
    val isActive: Boolean? = true,
    val totalVisits: Long = 0,
    val createdAt: String?
)
