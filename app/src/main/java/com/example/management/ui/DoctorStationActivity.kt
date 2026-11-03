package com.example.management.ui

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.management.R
import com.example.management.api.RetrofitClient
import com.example.management.data.model.*
import com.example.management.databinding.ActivityDoctorStationBinding
import com.example.management.ui.adapter.DoctorQueueAdapter
import kotlinx.coroutines.launch

class DoctorStationActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDoctorStationBinding
    private lateinit var queueAdapter: DoctorQueueAdapter

    private var queueTickets: List<QueueTicketResponse> = emptyList()
    private var activeTicket: QueueTicketResponse? = null
    private var activeRecordId: Long? = null
    private var availableServices: List<ClinicalServiceDto> = emptyList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDoctorStationBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbarDoctor)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        binding.toolbarDoctor.setNavigationOnClickListener { finish() }

        setupRecyclerView()
        setupActions()

        binding.swipeRefreshDoctor.setOnRefreshListener {
            loadDoctorQueue()
            loadAvailableServices()
        }

        loadDoctorQueue()
        loadAvailableServices()
    }

    private fun setupRecyclerView() {
        queueAdapter = DoctorQueueAdapter { ticket ->
            callPatientIntoRoom(ticket)
        }
        binding.rvDoctorQueue.layoutManager = LinearLayoutManager(this)
        binding.rvDoctorQueue.adapter = queueAdapter
    }

    private fun setupActions() {
        binding.btnDoctorDiagnosis.setOnClickListener {
            val recId = activeRecordId
            val ticket = activeTicket
            if (recId != null && ticket != null) {
                showDiagnosisDialog(recId, ticket)
            } else {
                Toast.makeText(this, "Chưa có bệnh nhân nào trong phòng khám", Toast.LENGTH_SHORT).show()
            }
        }

        binding.btnDoctorOrderLab.setOnClickListener {
            val recId = activeRecordId
            val ticket = activeTicket
            if (recId != null && ticket != null) {
                showOrderLabDialog(recId, ticket)
            } else {
                Toast.makeText(this, "Chưa có bệnh nhân nào trong phòng khám", Toast.LENGTH_SHORT).show()
            }
        }

        binding.btnDoctorComplete.setOnClickListener {
            val recId = activeRecordId
            val ticket = activeTicket
            if (recId != null && ticket != null) {
                showCompleteEncounterDialog(recId, ticket)
            } else {
                Toast.makeText(this, "Chưa có bệnh nhân nào trong phòng khám", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun loadDoctorQueue() {
        val api = RetrofitClient.getApiService(this)
        binding.swipeRefreshDoctor.isRefreshing = true

        lifecycleScope.launch {
            try {
                // Lấy danh sách hàng chờ phòng khám (phòng số 1 mặc định hoặc theo bác sĩ)
                val res = api.getQueueByRoom(1L, "WAITING")
                if (res.isSuccessful) {
                    val tickets = res.body() ?: emptyList()
                    queueTickets = tickets.filter { it.status == "WAITING" || it.status == "CALLED" }
                    queueAdapter.submitList(queueTickets)

                    if (queueTickets.isEmpty()) {
                        binding.llEmptyDoctorQueue.visibility = View.VISIBLE
                    } else {
                        binding.llEmptyDoctorQueue.visibility = View.GONE
                    }
                }
            } catch (e: Exception) {
                Toast.makeText(this@DoctorStationActivity, "Lỗi tải hàng chờ: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
            } finally {
                binding.swipeRefreshDoctor.isRefreshing = false
            }
        }
    }

    private fun loadAvailableServices() {
        val api = RetrofitClient.getApiService(this)
        lifecycleScope.launch {
            try {
                val res = api.getAvailableClinicalServices()
                if (res.isSuccessful) {
                    availableServices = res.body() ?: emptyList()
                }
            } catch (_: Exception) {}
        }
    }

    private fun callPatientIntoRoom(ticket: QueueTicketResponse) {
        val session = RetrofitClient.getSessionManager(this)
        val doctorId = session.getUser()?.id

        val api = RetrofitClient.getApiService(this)
        binding.swipeRefreshDoctor.isRefreshing = true

        lifecycleScope.launch {
            try {
                val res = api.startServing(ticket.id, doctorId)
                if (res.isSuccessful) {
                    activeTicket = ticket
                    activeRecordId = ticket.id // Sử dụng ticket id làm tham chiếu phiên khám
                    updateActivePatientUi(ticket)
                    loadPatientVitals(ticket.patientId)
                    loadDoctorQueue()
                    Toast.makeText(this@DoctorStationActivity, "Đã tiếp nhận bệnh nhân ${ticket.patientName ?: ""}", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this@DoctorStationActivity, "Không thể tiếp nhận bệnh nhân", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(this@DoctorStationActivity, "Lỗi kết nối: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
            } finally {
                binding.swipeRefreshDoctor.isRefreshing = false
            }
        }
    }

    private fun updateActivePatientUi(ticket: QueueTicketResponse) {
        binding.tvActivePatientName.text = ticket.patientName ?: "Bệnh nhân"
        binding.tvActivePatientSub.text = "Mã BN: ${ticket.patientCode ?: "BN-${ticket.patientId ?: ""}"} | Triệu chứng ban đầu: ${ticket.serviceType ?: "Khám bệnh"}"
        val num = String.format("%02d", ticket.ticketNumber ?: 0)
        binding.tvActiveTicketNumber.text = "STT: $num"
        binding.tvActiveTicketNumber.setTextColor(ContextCompat.getColor(this, R.color.accent_green))
        binding.tvActiveTicketNumber.setBackgroundResource(R.drawable.bg_badge_completed)
        binding.llDoctorActions.visibility = View.VISIBLE
    }

    private fun loadPatientVitals(patientId: Long?) {
        if (patientId == null) return
        val api = RetrofitClient.getApiService(this)

        lifecycleScope.launch {
            try {
                val res = api.getLatestVitalSigns(patientId)
                if (res.isSuccessful && res.body() != null) {
                    val v = res.body()!!
                    binding.llActivePatientVitals.visibility = View.VISIBLE
                    binding.tvActiveVitalsText.text = "Sinh hiệu: HA ${v.bloodPressureSystolic ?: "--"}/${v.bloodPressureDiastolic ?: "--"} mmHg | Mạch: ${v.heartRate ?: "--"} bpm | Thân nhiệt: ${v.temperature ?: "--"}°C | SpO2: ${v.spo2 ?: "--"}% | BMI: ${v.bmi ?: "--"}"
                } else {
                    binding.llActivePatientVitals.visibility = View.GONE
                }
            } catch (_: Exception) {
                binding.llActivePatientVitals.visibility = View.GONE
            }
        }
    }

    // =========================================================================
    // 1. NHẬP CHẨN ĐOÁN & ICD-10
    // =========================================================================
    private fun showDiagnosisDialog(recordId: Long, ticket: QueueTicketResponse) {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_doctor_diagnosis, null)
        val tvSub = dialogView.findViewById<TextView>(R.id.tvDiagPatientSubtitle)
        val etSymptoms = dialogView.findViewById<EditText>(R.id.etSymptoms)
        val etFinal = dialogView.findViewById<EditText>(R.id.etFinalDiagnosis)
        val etIcd10 = dialogView.findViewById<EditText>(R.id.etIcd10)
        val etAdvice = dialogView.findViewById<EditText>(R.id.etDoctorAdvice)
        val etFollowUp = dialogView.findViewById<EditText>(R.id.etFollowUpDate)
        val btnCancel = dialogView.findViewById<Button>(R.id.btnCancelDiag)
        val btnSave = dialogView.findViewById<Button>(R.id.btnSaveDiag)

        tvSub.text = "Bệnh nhân: ${ticket.patientName ?: ""} | Mã BN: ${ticket.patientCode ?: ""}"

        val dialog = AlertDialog.Builder(this)
            .setView(dialogView)
            .setCancelable(false)
            .create()

        btnCancel.setOnClickListener { dialog.dismiss() }

        btnSave.setOnClickListener {
            val symptoms = etSymptoms.text.toString().trim()
            val diag = etFinal.text.toString().trim()
            val icd = etIcd10.text.toString().trim()
            val advice = etAdvice.text.toString().trim()
            val followUp = etFollowUp.text.toString().trim()

            if (diag.isEmpty()) {
                Toast.makeText(this, "Vui lòng nhập chẩn đoán xác định", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            dialog.dismiss()
            executeSaveDiagnosis(recordId, symptoms, diag, icd, advice, followUp)
        }

        dialog.show()
    }

    private fun executeSaveDiagnosis(
        recordId: Long,
        symptoms: String,
        diagnosis: String,
        icd10: String,
        advice: String,
        followUpDate: String
    ) {
        val api = RetrofitClient.getApiService(this)
        binding.swipeRefreshDoctor.isRefreshing = true

        lifecycleScope.launch {
            try {
                val req = SaveDiagnosisRequest(
                    medicalRecordId = recordId,
                    symptoms = if (symptoms.isNotBlank()) symptoms else null,
                    initialDiagnosis = diagnosis,
                    finalDiagnosis = diagnosis,
                    icd10Code = if (icd10.isNotBlank()) icd10 else null,
                    treatmentPlan = null,
                    doctorAdvice = if (advice.isNotBlank()) advice else null
                )
                val res = api.saveDiagnosis(req)
                if (res.isSuccessful) {
                    Toast.makeText(this@DoctorStationActivity, "Đã lưu chẩn đoán $diagnosis ($icd10)", Toast.LENGTH_SHORT).show()

                    // Nếu có ngày tái khám
                    if (followUpDate.isNotBlank()) {
                        api.scheduleFollowUp(ScheduleFollowUpRequest(medicalRecordId = recordId, followUpDate = followUpDate))
                    }
                } else {
                    Toast.makeText(this@DoctorStationActivity, "Lỗi lưu chẩn đoán", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(this@DoctorStationActivity, "Lỗi kết nối: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
            } finally {
                binding.swipeRefreshDoctor.isRefreshing = false
            }
        }
    }

    // =========================================================================
    // 2. CHỈ ĐỊNH CẬN LÂM SÀNG
    // =========================================================================
    private fun showOrderLabDialog(recordId: Long, ticket: QueueTicketResponse) {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_doctor_order_lab, null)
        val tvSub = dialogView.findViewById<TextView>(R.id.tvOrderLabSubtitle)
        val llServices = dialogView.findViewById<LinearLayout>(R.id.llClinicalServicesList)
        val etNotes = dialogView.findViewById<EditText>(R.id.etLabNotes)
        val btnCancel = dialogView.findViewById<Button>(R.id.btnCancelOrderLab)
        val btnConfirm = dialogView.findViewById<Button>(R.id.btnConfirmOrderLab)

        tvSub.text = "Chỉ định dịch vụ cho ${ticket.patientName ?: ""}"

        val checkedServiceIds = mutableListOf<Long>()

        val servicesToShow = if (availableServices.isNotEmpty()) availableServices else listOf(
            ClinicalServiceDto(1, 1, "CLS-001", "Xét nghiệm Tổng phân tích tế bào máu ngoại vi", 120000.0, true),
            ClinicalServiceDto(2, 1, "CLS-002", "Xét nghiệm Sinh hóa máu (Glucose, Ure, Creatinin)", 150000.0, true),
            ClinicalServiceDto(3, 2, "CLS-003", "Chụp X-Quang ngực thẳng kỹ thuật số (CR/DR)", 180000.0, true),
            ClinicalServiceDto(4, 2, "CLS-004", "Siêu âm màu tổng quát ổ bụng", 200000.0, true)
        )

        llServices.removeAllViews()
        for (srv in servicesToShow) {
            val cb = CheckBox(this).apply {
                text = "${srv.name} (${srv.code}) - ${srv.price.toInt()} đ"
                textSize = 12f
                setTextColor(ContextCompat.getColor(this@DoctorStationActivity, R.color.text_primary))
                setOnCheckedChangeListener { _, isChecked ->
                    if (isChecked) checkedServiceIds.add(srv.id) else checkedServiceIds.remove(srv.id)
                }
            }
            llServices.addView(cb)
        }

        val dialog = AlertDialog.Builder(this)
            .setView(dialogView)
            .setCancelable(false)
            .create()

        btnCancel.setOnClickListener { dialog.dismiss() }

        btnConfirm.setOnClickListener {
            if (checkedServiceIds.isEmpty()) {
                Toast.makeText(this, "Vui lòng chọn ít nhất 1 dịch vụ cận lâm sàng", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            val notes = etNotes.text.toString().trim()
            dialog.dismiss()
            executeOrderLab(recordId, ticket.patientId ?: 1L, checkedServiceIds, notes)
        }

        dialog.show()
    }

    private fun executeOrderLab(recordId: Long, patientId: Long, serviceIds: List<Long>, notes: String) {
        val session = RetrofitClient.getSessionManager(this)
        val doctorId = session.getUser()?.id ?: 1L

        val api = RetrofitClient.getApiService(this)
        binding.swipeRefreshDoctor.isRefreshing = true

        lifecycleScope.launch {
            try {
                val req = OrderLabServicesRequest(
                    medicalRecordId = recordId,
                    patientId = patientId,
                    doctorId = doctorId,
                    serviceIds = serviceIds,
                    clinicalNotes = if (notes.isNotBlank()) notes else null
                )
                val res = api.orderClinicalServices(req)
                if (res.isSuccessful) {
                    Toast.makeText(this@DoctorStationActivity, "Đã gửi chỉ định ${serviceIds.size} dịch vụ tới phòng xét nghiệm", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this@DoctorStationActivity, "Lỗi tạo phiếu chỉ định", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(this@DoctorStationActivity, "Lỗi kết nối: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
            } finally {
                binding.swipeRefreshDoctor.isRefreshing = false
            }
        }
    }

    // =========================================================================
    // 3. KẾT THÚC PHIÊN KHÁM
    // =========================================================================
    private fun showCompleteEncounterDialog(recordId: Long, ticket: QueueTicketResponse) {
        val pName = ticket.patientName ?: "Bệnh nhân"

        AlertDialog.Builder(this)
            .setTitle("Kết thúc phiên khám")
            .setMessage("Xác nhận hoàn tất phiên khám cho bệnh nhân $pName? Hồ sơ sẽ được chuyển tới quầy thu ngân để thanh toán viện phí.")
            .setPositiveButton("Hoàn tất") { _, _ ->
                executeCompleteEncounter(recordId)
            }
            .setNegativeButton("Hủy", null)
            .show()
    }

    private fun executeCompleteEncounter(recordId: Long) {
        val session = RetrofitClient.getSessionManager(this)
        val doctorId = session.getUser()?.id ?: 1L

        val api = RetrofitClient.getApiService(this)
        binding.swipeRefreshDoctor.isRefreshing = true

        lifecycleScope.launch {
            try {
                val req = CompleteEncounterRequest(
                    medicalRecordId = recordId,
                    doctorId = doctorId,
                    summaryNotes = "Đã hoàn thành khám và kê toa"
                )
                val res = api.completeEncounter(req)
                if (res.isSuccessful) {
                    Toast.makeText(this@DoctorStationActivity, "Đã hoàn tất ca khám và chuyển tới quầy thu ngân", Toast.LENGTH_LONG).show()

                    activeTicket = null
                    activeRecordId = null
                    binding.tvActivePatientName.text = "Chưa có bệnh nhân nào"
                    binding.tvActivePatientSub.text = "Chọn một bệnh nhân từ hàng chờ bên dưới để tiếp nhận"
                    binding.tvActiveTicketNumber.text = "STT: --"
                    binding.tvActiveTicketNumber.setTextColor(ContextCompat.getColor(this@DoctorStationActivity, R.color.text_muted))
                    binding.llDoctorActions.visibility = View.GONE
                    binding.llActivePatientVitals.visibility = View.GONE

                    loadDoctorQueue()
                } else {
                    Toast.makeText(this@DoctorStationActivity, "Lỗi kết thúc ca khám", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(this@DoctorStationActivity, "Lỗi kết nối: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
            } finally {
                binding.swipeRefreshDoctor.isRefreshing = false
            }
        }
    }
}
