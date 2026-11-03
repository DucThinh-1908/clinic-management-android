package com.example.management.data.model

data class CheckInAppointmentRequest(
    val appointmentId: Long? = null,
    val appointmentCode: String? = null,
    val roomId: Long? = null,
    val priority: String = "NORMAL", // NORMAL, PRIORITY, EMERGENCY
    val notes: String? = null
)

data class WalkInReceptionRequest(
    val patientId: Long? = null,
    val fullName: String,
    val phone: String,
    val dateOfBirth: String? = null, // yyyy-MM-dd
    val gender: String = "MALE", // MALE, FEMALE, OTHER
    val identityCard: String? = null,
    val healthInsuranceNumber: String? = null,
    val address: String? = null,
    val departmentId: Long? = null,
    val roomId: Long? = null,
    val doctorId: Long? = null,
    val serviceType: String? = "Khám bệnh",
    val priority: String = "NORMAL",
    val notes: String? = null
)

data class TransferRoomRequest(
    val targetRoomId: Long,
    val targetDoctorId: Long? = null,
    val reason: String? = null
)

data class SkipTicketRequest(
    val reason: String? = "Bệnh nhân vắng mặt khi gọi số"
)

data class QueueTicketResponse(
    val id: Long,
    val ticketCode: String?,
    val ticketNumber: Int,
    val patientId: Long?,
    val patientCode: String?,
    val patientName: String?,
    val patientPhone: String?,
    val patientGender: String?,
    val patientDob: String?,
    val patientAge: Int?,
    val identityCard: String?,
    val appointmentId: Long?,
    val appointmentCode: String?,
    val isAppointment: Boolean?,
    val roomId: Long?,
    val roomNumber: String?,
    val roomName: String?,
    val departmentId: Long?,
    val departmentName: String?,
    val doctorId: Long?,
    val doctorName: String?,
    val priority: String?, // NORMAL, PRIORITY, EMERGENCY
    val serviceType: String?,
    val queueDate: String?,
    val status: String?, // WAITING, CALLED, SERVING, COMPLETED, SKIPPED, CANCELLED
    val calledAt: String?,
    val servingAt: String?,
    val completedAt: String?,
    val notes: String?,
    val waitPosition: Int? = 0,
    val createdAt: String?
)

data class RoomQueueOverviewDto(
    val roomId: Long,
    val roomNumber: String?,
    val roomName: String?,
    val departmentId: Long?,
    val departmentName: String?,
    val doctorId: Long?,
    val doctorName: String?,
    val isAvailable: Boolean? = true,
    val currentServing: QueueTicketResponse? = null,
    val currentCalled: QueueTicketResponse? = null,
    val waitingCount: Long = 0,
    val completedCount: Long = 0,
    val totalCount: Long = 0
)

data class QueueStatsDto(
    val date: String?,
    val totalTickets: Long = 0,
    val waiting: Long = 0,
    val called: Long = 0,
    val serving: Long = 0,
    val completed: Long = 0,
    val skipped: Long = 0
)
