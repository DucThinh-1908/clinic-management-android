package com.example.management.data.model

data class RealtimeDashboard(
    val lastUpdated: String?,
    val todayPatientsRegistered: Long?,
    val waitingConsultationCount: Long?,
    val inConsultationCount: Long?,
    val completedConsultationCount: Long?,
    val activeDoctorsCount: Long?,
    val todayRevenue: Double?,
    val todayPrescriptionsCount: Long?,
    val hourlyVisits: List<HourlyVisitItem>?,
    val recentActivities: List<RecentActivityItem>?
)

data class HourlyVisitItem(
    val hourLabel: String?,
    val visitCount: Long?
)

data class RecentActivityItem(
    val time: String?,
    val activityType: String?,
    val description: String?,
    val amount: String?,
    val status: String?
)

data class RevenueReport(
    val totalRevenue: Double?,
    val totalServiceRevenue: Double?,
    val totalMedicineRevenue: Double?,
    val totalInsurancePaid: Double?,
    val netRevenue: Double?,
    val paidInvoiceCount: Long?
)

data class CommonDiseaseReport(
    val totalDiagnoses: Long?,
    val topDiseases: List<DiseaseItem>?
)

data class DiseaseItem(
    val diseaseName: String?,
    val icd10Code: String?,
    val caseCount: Long?,
    val percentage: Double?,
    val primaryDepartment: String?
)
