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
        @Body request: CreateAppointmentRequest,
        @Query("userId") userId: Long? = null
    ): Response<AppointmentItem>

    @PUT("api/appointments/{id}/confirm")
    suspend fun confirmAppointment(
        @Path("id") id: Long,
        @Query("userId") userId: Long? = null
    ): Response<AppointmentItem>

    @PUT("api/appointments/{id}/cancel")
    suspend fun cancelAppointment(
        @Path("id") id: Long,
        @Body request: CancelAppointmentRequest? = null,
        @Query("userId") userId: Long? = null
    ): Response<AppointmentItem>

    @PUT("api/appointments/{id}/no-show")
    suspend fun markNoShow(
        @Path("id") id: Long,
        @Query("userId") userId: Long? = null
    ): Response<AppointmentItem>

    @GET("api/appointments/doctors")
    suspend fun getAvailableDoctors(
        @Query("departmentId") departmentId: Long? = null
    ): Response<List<DoctorOptionDto>>

    // =========================================================================
    // 3. PHÂN HỆ TIẾP ĐÓN & HÀNG CHỜ PHÒNG KHÁM (QUEUE & RECEPTION)
    // =========================================================================
    @POST("api/queue/check-in")
    suspend fun checkInAppointment(
        @Body request: CheckInAppointmentRequest,
        @Query("userId") userId: Long? = null
    ): Response<QueueTicketResponse>

    @POST("api/queue/walk-in")
    suspend fun walkInReception(
        @Body request: WalkInReceptionRequest,
        @Query("userId") userId: Long? = null
    ): Response<QueueTicketResponse>

    @GET("api/queue/overview")
    suspend fun getRoomQueueOverview(
        @Query("date") date: String? = null
    ): Response<List<RoomQueueOverviewDto>>

    @GET("api/queue/stats")
    suspend fun getQueueStats(
        @Query("date") date: String? = null
    ): Response<QueueStatsDto>

    @GET("api/queue/room/{roomId}")
    suspend fun getQueueByRoom(
        @Path("roomId") roomId: Long,
        @Query("status") status: String? = null
    ): Response<List<QueueTicketResponse>>

    @POST("api/queue/room/{roomId}/call-next")
    suspend fun callNextPatientInRoom(
        @Path("roomId") roomId: Long,
        @Query("userId") userId: Long? = null
    ): Response<QueueTicketResponse>

    @PUT("api/queue/{ticketId}/skip")
    suspend fun skipPatient(
        @Path("ticketId") ticketId: Long,
        @Body request: SkipTicketRequest? = null,
        @Query("userId") userId: Long? = null
    ): Response<QueueTicketResponse>

    @PUT("api/queue/{ticketId}/transfer")
    suspend fun transferPatient(
        @Path("ticketId") ticketId: Long,
        @Body request: TransferRoomRequest,
        @Query("userId") userId: Long? = null
    ): Response<QueueTicketResponse>

    // =========================================================================
    // 4. QUẢN LÝ & TRA CỨU HỒ SƠ BỆNH NHÂN (PATIENTS)
    // =========================================================================
    @GET("api/patients/search")
    suspend fun searchPatients(
        @Query("keyword") keyword: String? = null,
        @Query("activeOnly") activeOnly: Boolean? = true
    ): Response<List<PatientDetailDto>>

    @GET("api/patients/{id}")
    suspend fun getPatientById(
        @Path("id") id: Long
    ): Response<PatientDetailDto>

    @POST("api/patients")
    suspend fun createPatient(
        @Body request: CreatePatientRequest,
        @Query("requesterId") requesterId: Long? = null
    ): Response<PatientDetailDto>

    @PUT("api/patients/{id}/insurance")
    suspend fun updatePatientInsurance(
        @Path("id") id: Long,
        @Body request: UpdateInsuranceRequest,
        @Query("requesterId") requesterId: Long? = null
    ): Response<PatientDetailDto>

    // =========================================================================
    // 5. PHÂN HỆ ĐIỀU DƯỠNG: ĐO VÀ THEO DÕI SINH HIỆU (VITAL SIGNS - NURSE)
    // =========================================================================
    @POST("api/medical-examination/vital-signs")
    suspend fun recordVitalSigns(
        @Body request: VitalSignRequest
    ): Response<VitalSignResponse>

    @GET("api/medical-examination/vital-signs/patient/{patientId}")
    suspend fun getLatestVitalSigns(
        @Path("patientId") patientId: Long
    ): Response<VitalSignResponse>

    @GET("api/medical-examination/vital-signs/history/{patientId}")
    suspend fun getVitalSignsHistory(
        @Path("patientId") patientId: Long
    ): Response<List<VitalSignResponse>>

    @POST("api/queue/{ticketId}/start")
    suspend fun startServing(
        @Path("ticketId") ticketId: Long,
        @Query("userId") userId: Long? = null
    ): Response<QueueTicketResponse>

    // =========================================================================
    // 6. PHÂN HỆ KỸ THUẬT VIÊN: CẬN LÂM SÀNG & XÉT NGHIỆM (LAB & TECHNICIAN)
    // =========================================================================
    @GET("api/medical-examination/lab-orders")
    suspend fun getAllLabOrders(): Response<List<LabOrderSummaryDto>>

    @GET("api/medical-examination/lab-orders/patient/{patientId}")
    suspend fun getLabOrdersByPatient(
        @Path("patientId") patientId: Long
    ): Response<List<LabOrderSummaryDto>>

    @GET("api/medical-examination/lab-orders/record/{recordId}")
    suspend fun getLabOrdersByRecord(
        @Path("recordId") recordId: Long
    ): Response<List<LabOrderSummaryDto>>

    @PUT("api/medical-examination/lab-results")
    suspend fun updateLabResult(
        @Body request: UpdateLabResultRequest
    ): Response<LabResultItemDto>

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
    // 4. TOA THUỐC & DƯỢC PHẨM (PHARMACY - PHARMACIST)
    // =========================================================================
    @GET("api/pharmacy/prescriptions")
    suspend fun getPharmacyPrescriptions(
        @Query("status") status: String? = null
    ): Response<List<PrescriptionResponse>>

    @GET("api/pharmacy/prescriptions/{id}")
    suspend fun getPrescriptionById(
        @Path("id") id: Long
    ): Response<PrescriptionResponse>

    @POST("api/pharmacy/dispensations")
    suspend fun dispensePrescription(
        @Body request: DispensePrescriptionRequest
    ): Response<DispensationResponse>

    @GET("api/pharmacy/medicines")
    suspend fun getMedicineCatalog(
        @Query("query") query: String? = null,
        @Query("categoryId") categoryId: Long? = null
    ): Response<List<MedicineDetailDto>>

    @GET("api/pharmacy/alerts")
    suspend fun getStockAlerts(
        @Query("lowStockThreshold") lowStockThreshold: Int? = null,
        @Query("nearExpiryDays") nearExpiryDays: Int? = null
    ): Response<StockAlertOverviewDto>

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
