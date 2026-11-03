package com.example.management.data.model

data class PrescriptionResponse(
    val id: Long,
    val prescriptionCode: String?,
    val medicalRecordId: Long?,
    val doctorId: Long?,
    val doctorName: String?,
    val patientId: Long?,
    val patientName: String?,
    val patientCode: String?,
    val patientPhone: String?,
    val diagnosis: String?,
    val usageNote: String?,
    val status: String?,
    val createdAt: String?,
    val totalAmount: Double?,
    val items: List<PrescriptionItemDetail>?
)

data class PrescriptionItemDetail(
    val id: Long?,
    val medicineId: Long?,
    val medicineCode: String?,
    val brandName: String?,
    val activeIngredient: String?,
    val dosageForm: String?,
    val unit: String?,
    val quantity: Int?,
    val morningDose: String?,
    val noonDose: String?,
    val afternoonDose: String?,
    val nightDose: String?,
    val instructions: String?,
    val unitPrice: Double?,
    val totalAmount: Double?
)

data class DispensePrescriptionRequest(
    val prescriptionId: Long,
    val pharmacistId: Long? = null,
    val note: String? = null
)

data class DispensationResponse(
    val id: Long,
    val dispensationCode: String?,
    val prescriptionId: Long?,
    val prescriptionCode: String?,
    val pharmacistId: Long?,
    val pharmacistName: String?,
    val patientName: String?,
    val dispensedAt: String?,
    val note: String?
)

data class MedicineDetailDto(
    val id: Long,
    val categoryId: Long?,
    val categoryName: String?,
    val code: String?,
    val brandName: String?,
    val activeIngredient: String?,
    val dosageForm: String?,
    val unit: String?,
    val usageInstructions: String?,
    val sellingPrice: Double?,
    val isPrescriptionRequired: Boolean?,
    val contraindications: String?,
    val isActive: Boolean?,
    val totalAvailableQuantity: Int?
)

data class StockAlertOverviewDto(
    val lowStockCount: Int = 0,
    val nearExpiryCount: Int = 0,
    val expiredCount: Int = 0,
    val alerts: List<StockAlertItemDto> = emptyList()
)

data class StockAlertItemDto(
    val alertType: String?, // LOW_STOCK, NEAR_EXPIRY, EXPIRED
    val medicineId: Long?,
    val medicineCode: String?,
    val brandName: String?,
    val activeIngredient: String?,
    val unit: String?,
    val batchId: Long?,
    val batchNumber: String?,
    val quantityAvailable: Int?,
    val threshold: Int?,
    val expiryDate: String?,
    val daysRemaining: Long?,
    val severity: String?, // HIGH, WARNING, CRITICAL
    val message: String?
)

// Legacy compatibility
data class PrescriptionItem(
    val id: Long,
    val prescriptionCode: String?,
    val medicalRecordId: Long?,
    val patientName: String?,
    val doctorName: String?,
    val totalAmount: Double?,
    val status: String?,
    val items: List<PrescriptionMedicineItem>?
)

data class PrescriptionMedicineItem(
    val medicineId: Long?,
    val medicineName: String?,
    val quantity: Int?,
    val unit: String?,
    val instructions: String?,
    val morningDose: String?,
    val noonDose: String?,
    val afternoonDose: String?,
    val nightDose: String?
)

data class InvoiceItem(
    val id: Long,
    val invoiceCode: String?,
    val patientName: String?,
    val totalServiceAmount: Double?,
    val totalMedicineAmount: Double?,
    val discountAmount: Double?,
    val insurancePaidAmount: Double?,
    val finalAmount: Double?,
    val status: String?,
    val createdAt: String?
)
