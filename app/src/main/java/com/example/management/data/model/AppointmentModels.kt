package com.example.management.data.model

data class AppointmentItem(
    val id: Long,
    val appointmentCode: String?,
    val patientId: Long?,
    val patientName: String?,
    val patientPhone: String?,
    val doctorId: Long?,
    val doctorName: String?,
    val departmentId: Long?,
    val departmentName: String?,
    val appointmentDate: String?,
    val appointmentTime: String?,
    val symptomsSummary: String?,
    val status: String?, // PENDING, CONFIRMED, COMPLETED, CANCELLED, NO_SHOW
    val bookingChannel: String?,
    val notes: String?
)

data class CreateAppointmentRequest(
    val doctorId: Long?,
    val patientId: Long?,
    val departmentId: Long?,
    val appointmentDate: String, // yyyy-MM-dd
    val appointmentTime: String, // HH:mm:ss
    val symptomsSummary: String?,
    val bookingChannel: String = "MOBILE_APP"
)

data class CancelAppointmentRequest(
    val reason: String
)

data class DoctorOptionDto(
    val id: Long,
    val fullName: String?,
    val departmentId: Long?,
    val departmentName: String?,
    val roomNumber: String?
)
