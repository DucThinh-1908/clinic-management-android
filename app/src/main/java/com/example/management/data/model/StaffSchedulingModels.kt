package com.example.management.data.model

data class StaffResponse(
    val id: Long,
    val userId: Long?,
    val staffCode: String?,
    val fullName: String?,
    val email: String?,
    val phone: String?,
    val position: String?, // Bác sĩ, Điều dưỡng, Dược sĩ, Kỹ thuật viên...
    val departmentId: Long?,
    val departmentName: String?,
    val status: String?, // ACTIVE, ON_LEAVE, RESIGNED
    val dateOfBirth: String?,
    val gender: String?,
    val address: String?
)

data class ShiftDto(
    val id: Long,
    val name: String?, // Ca sáng, Ca chiều, Ca đêm
    val startTime: String?,
    val endTime: String?,
    val colorCode: String?
)

data class ScheduleResponse(
    val id: Long,
    val staffId: Long?,
    val staffName: String?,
    val staffCode: String?,
    val departmentName: String?,
    val roomId: Long?,
    val roomNumber: String?,
    val workDate: String?,
    val shiftId: Long?,
    val shiftName: String?,
    val shiftTime: String?,
    val status: String? // SCHEDULED, COMPLETED, ABSENT
)

data class AttendanceCheckInRequest(
    val scheduleId: Long? = null,
    val notes: String? = null
)

data class AttendanceCheckOutRequest(
    val attendanceId: Long? = null,
    val notes: String? = null
)

data class AttendanceResponse(
    val id: Long,
    val staffId: Long?,
    val staffName: String?,
    val workDate: String?,
    val checkInTime: String?,
    val checkOutTime: String?,
    val status: String?, // ON_TIME, LATE, EARLY_LEAVE
    val notes: String?
)

data class LeaveSwapRequest(
    val staffId: Long,
    val type: String, // LEAVE (Nghỉ phép) hoặc SWAP (Đổi ca)
    val fromDate: String,
    val toDate: String,
    val targetStaffId: Long? = null,
    val reason: String
)

data class LeaveSwapResponse(
    val id: Long,
    val staffId: Long?,
    val staffName: String?,
    val type: String?,
    val fromDate: String?,
    val toDate: String?,
    val reason: String?,
    val status: String?, // PENDING, APPROVED, REJECTED
    val reviewedBy: String?,
    val reviewNotes: String?
)
