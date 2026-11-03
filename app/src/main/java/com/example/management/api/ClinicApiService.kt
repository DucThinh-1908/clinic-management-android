package com.example.management.api

import com.example.management.data.model.*
import retrofit2.Response
import retrofit2.http.*

interface ClinicApiService {

    // =========================================================================
    // 1. AUTHENTICATION & TÀI KHOẢN
    // =========================================================================
    @POST("api/auth/login")
    suspend fun login(@Body request: LoginRequest): Response<LoginResponse>

    @POST("api/auth/register")
    suspend fun register(@Body request: RegisterRequest): Response<LoginResponse>

    @POST("api/auth/logout")
    suspend fun logout(@Query("userId") userId: Long?): Response<Map<String, Any>>

    @POST("api/auth/change-password")
    suspend fun changePassword(@Body request: ChangePasswordRequest): Response<Map<String, Any>>

    // =========================================================================
    // 2. LỊCH HẸN KHÁM BỆNH (APPOINTMENTS)
    // =========================================================================
    @GET("api/appointments")
    suspend fun getAllAppointments(
        @Query("query") query: String? = null,
        @Query("status") status: String? = null
    ): Response<List<AppointmentItem>>

    @GET("api/appointments/patient/{patientId}")
    suspend fun getAppointmentsByPatient(
        @Path("patientId") patientId: Long
    ): Response<List<AppointmentItem>>

    @POST("api/appointments")
    suspend fun createAppointment(
        @Body request: CreateAppointmentRequest
    ): Response<AppointmentItem>

    @POST("api/appointments/{id}/cancel")
    suspend fun cancelAppointment(
        @Path("id") id: Long,
        @Body request: CancelAppointmentRequest
    ): Response<AppointmentItem>

    // =========================================================================
    // 3. HỒ SƠ BỆNH ÁN & KHÁM BỆNH (MEDICAL RECORDS)
    // =========================================================================
    @GET("api/medical-records")
    suspend fun getAllMedicalRecords(
        @Query("query") query: String? = null
    ): Response<List<MedicalRecordItem>>

    @GET("api/medical-records/patient/{patientId}")
    suspend fun getMedicalRecordsByPatient(
        @Path("patientId") patientId: Long
    ): Response<List<MedicalRecordItem>>

    @GET("api/medical-records/{id}")
    suspend fun getMedicalRecordDetail(
        @Path("id") id: Long
    ): Response<MedicalRecordItem>

    // =========================================================================
    // 4. TOA THUỐC & DƯỢC PHẨM (PHARMACY)
    // =========================================================================
    @GET("api/pharmacy/prescriptions")
    suspend fun getAllPrescriptions(): Response<List<PrescriptionItem>>

    @GET("api/pharmacy/prescriptions/patient/{patientId}")
    suspend fun getPrescriptionsByPatient(
        @Path("patientId") patientId: Long
    ): Response<List<PrescriptionItem>>

    // =========================================================================
    // 5. VIỆN PHÍ & HÓA ĐƠN (BILLING)
    // =========================================================================
    @GET("api/billing/invoices")
    suspend fun getAllInvoices(
        @Query("status") status: String? = null
    ): Response<List<InvoiceItem>>

    // =========================================================================
    // 6. BÁO CÁO & THỐNG KÊ (REPORTS & DASHBOARD)
    // =========================================================================
    @GET("api/reports/realtime-dashboard")
    suspend fun getRealtimeDashboard(): Response<RealtimeDashboard>

    @GET("api/reports/revenue")
    suspend fun getRevenueReport(
        @Query("startDate") startDate: String,
        @Query("endDate") endDate: String,
        @Query("period") period: String = "CUSTOM"
    ): Response<RevenueReport>

    @GET("api/reports/common-diseases")
    suspend fun getCommonDiseases(
        @Query("startDate") startDate: String,
        @Query("endDate") endDate: String
    ): Response<CommonDiseaseReport>
}
