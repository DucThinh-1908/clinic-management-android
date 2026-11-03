package com.example.management.data.model

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
