package com.example.management.data.model

data class InvoiceDetailDto(
    val id: Long,
    val invoiceCode: String?,
    val patientId: Long?,
    val patientName: String?,
    val patientCode: String?,
    val patientPhone: String?,
    val healthInsuranceNumber: String?,
    val cashierId: Long?,
    val cashierName: String?,
    val appointmentId: Long?,
    val medicalRecordId: Long?,
    val recordCode: String?,
    val doctorName: String?,
    val diagnosis: String?,
    val totalServiceAmount: Double = 0.0,
    val totalMedicineAmount: Double = 0.0,
    val discountAmount: Double = 0.0,
    val insurancePaidAmount: Double = 0.0,
    val finalAmount: Double = 0.0,
    val status: String?, // UNPAID, PAID, REFUNDED, CANCELLED
    val statusDesc: String?,
    val note: String?,
    val createdAt: String?,
    val paidAt: String?,
    val items: List<InvoiceItemDetailDto> = emptyList(),
    val payments: List<PaymentItemDto> = emptyList()
)

data class InvoiceItemDetailDto(
    val id: Long?,
    val invoiceId: Long?,
    val itemType: String?, // CONSULTATION_FEE, LAB_SERVICE, MEDICINE, OTHER
    val itemTypeDesc: String?,
    val referenceId: Long?,
    val itemName: String?,
    val unitPrice: Double = 0.0,
    val quantity: Int = 1,
    val subtotal: Double = 0.0
)

data class PaymentItemDto(
    val id: Long?,
    val paymentCode: String?,
    val invoiceId: Long?,
    val cashierId: Long?,
    val cashierName: String?,
    val amount: Double = 0.0,
    val paymentMethod: String?, // CASH, CREDIT_CARD, BANK_TRANSFER, VNPAY_QR, MOMO
    val paymentMethodDesc: String?,
    val transactionReference: String?,
    val status: String?,
    val paymentTime: String?,
    val note: String?
)

data class MedicalCandidateDto(
    val medicalRecordId: Long,
    val recordCode: String?,
    val patientId: Long?,
    val patientCode: String?,
    val patientName: String?,
    val patientPhone: String?,
    val doctorName: String?,
    val diagnosis: String?,
    val completedAt: String?,
    val invoiceCreated: Boolean = false,
    val invoiceId: Long? = null,
    val invoiceCode: String? = null
)

data class CreateInvoiceRequest(
    val medicalRecordId: Long,
    val cashierId: Long? = null,
    val discountAmount: Double? = null,
    val insuranceRate: Double? = null,
    val insurancePaidAmount: Double? = null,
    val note: String? = null
)

data class ApplyInsuranceDiscountRequest(
    val insuranceRate: Double? = null,
    val insurancePaidAmount: Double? = null,
    val discountAmount: Double? = null,
    val discountReason: String? = null
)

data class ProcessPaymentRequest(
    val invoiceId: Long,
    val cashierId: Long? = null,
    val amount: Double,
    val paymentMethod: String, // CASH, CREDIT_CARD, BANK_TRANSFER, VNPAY_QR, MOMO
    val transactionReference: String? = null,
    val customerGivenAmount: Double? = null,
    val note: String? = null
)

data class PaymentResponseDto(
    val success: Boolean,
    val message: String?,
    val paymentCode: String?,
    val invoiceCode: String?,
    val amountPaid: Double?,
    val changeAmount: Double?, // Tiền thừa trả lại cho khách
    val paymentMethod: String?,
    val paymentTime: String?
)

data class BillingStatsDto(
    val totalRevenue: Double = 0.0,
    val totalCash: Double = 0.0,
    val totalBankTransfer: Double = 0.0,
    val totalCard: Double = 0.0,
    val totalEWallet: Double = 0.0,
    val totalRefunded: Double = 0.0,
    val paidInvoiceCount: Long = 0,
    val unpaidInvoiceCount: Long = 0,
    val refundedInvoiceCount: Long = 0
)

data class RefundInvoiceRequest(
    val invoiceId: Long,
    val actorId: Long? = null,
    val reason: String
)
