package com.example.management.data.model

import java.math.BigDecimal

data class LabOrderSummaryDto(
    val id: Long,
    val orderCode: String?,
    val medicalRecordId: Long?,
    val doctorId: Long?,
    val doctorName: String?,
    val patientId: Long?,
    val patientName: String?,
    val diagnosisNote: String?,
    val status: String?, // PENDING, ORDERED, IN_PROGRESS, COMPLETED
    val createdAt: String?,
    val items: List<LabResultItemDto>? = emptyList()
)

data class LabResultItemDto(
    val id: Long,
    val labOrderId: Long?,
    val serviceId: Long?,
    val serviceCode: String?,
    val serviceName: String?,
    val categoryName: String?,
    val price: BigDecimal?,
    val technicianId: Long?,
    val technicianName: String?,
    val roomId: Long?,
    val roomName: String?,
    val testValue: String?,
    val referenceRange: String?,
    val unit: String?,
    val conclusion: String?,
    val imageAttachmentUrl: String?,
    val status: String?, // PENDING, IN_PROGRESS, COMPLETED
    val completedAt: String?
)

data class UpdateLabResultRequest(
    val resultId: Long,
    val technicianId: Long? = null,
    val testValue: String? = null,
    val referenceRange: String? = null,
    val unit: String? = null,
    val conclusion: String? = null,
    val imageAttachmentUrl: String? = null
)
