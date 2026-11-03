package com.example.management.ui

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.ArrayAdapter
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.management.R
import com.example.management.api.RetrofitClient
import com.example.management.data.model.QueueTicketResponse
import com.example.management.data.model.RoomQueueOverviewDto
import com.example.management.data.model.WalkInReceptionRequest
import com.example.management.databinding.ActivityWalkInReceptionBinding
import kotlinx.coroutines.launch

class WalkInReceptionActivity : AppCompatActivity() {

    private lateinit var binding: ActivityWalkInReceptionBinding
    private var roomList: List<RoomQueueOverviewDto> = emptyList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityWalkInReceptionBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbarWalkIn)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        binding.toolbarWalkIn.setNavigationOnClickListener { finish() }

        setupSpinners()
        loadRooms()

        // Check xem có truyền thông tin bệnh nhân từ màn hình tra cứu sang không
        val patientId = intent.getLongExtra("EXTRA_PATIENT_ID", -1L).takeIf { it > 0 }
        val fullName = intent.getStringExtra("EXTRA_FULL_NAME")
        val phone = intent.getStringExtra("EXTRA_PHONE")
        val dob = intent.getStringExtra("EXTRA_DOB")
        val cccd = intent.getStringExtra("EXTRA_CCCD")
        val bhyt = intent.getStringExtra("EXTRA_BHYT")
        val address = intent.getStringExtra("EXTRA_ADDRESS")

        if (!fullName.isNullOrBlank()) binding.etFullName.setText(fullName)
        if (!phone.isNullOrBlank()) binding.etPhone.setText(phone)
        if (!dob.isNullOrBlank()) binding.etDob.setText(dob)
        if (!cccd.isNullOrBlank()) binding.etIdentityCard.setText(cccd)
        if (!bhyt.isNullOrBlank()) binding.etHealthInsurance.setText(bhyt)
        if (!address.isNullOrBlank()) binding.etAddress.setText(address)

        binding.btnSubmitWalkIn.setOnClickListener {
            submitWalkIn(patientId)
        }
    }

    private fun setupSpinners() {
        val genders = listOf("Nam", "Nữ", "Khác")
        val genderAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, genders)
        binding.spinnerGender.adapter = genderAdapter

        val priorities = listOf("Bình thường (NORMAL)", "Ưu tiên (Người già / Trẻ nhỏ)", "Cấp cứu (EMERGENCY)")
        val priorityAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, priorities)
        binding.spinnerPriority.adapter = priorityAdapter
    }

    private fun loadRooms() {
        val api = RetrofitClient.getApiService(this)
        lifecycleScope.launch {
            try {
                val res = api.getRoomQueueOverview()
                if (res.isSuccessful && res.body() != null) {
                    roomList = res.body()!!
                    val roomNames = roomList.map {
                        val name = it.roomName ?: it.roomNumber ?: "Phòng ${it.roomId}"
                        val doc = it.doctorName?.let { d -> " ($d)" } ?: ""
                        "$name$doc"
                    }
                    val roomAdapter = ArrayAdapter(this@WalkInReceptionActivity, android.R.layout.simple_spinner_dropdown_item, roomNames)
                    binding.spinnerRoom.adapter = roomAdapter
                }
            } catch (ignored: Exception) {}
        }
    }

    private fun submitWalkIn(existingPatientId: Long?) {
        val fullName = binding.etFullName.text.toString().trim()
        val phone = binding.etPhone.text.toString().trim()
        val dob = binding.etDob.text.toString().trim().ifEmpty { null }
        val cccd = binding.etIdentityCard.text.toString().trim().ifEmpty { null }
        val bhyt = binding.etHealthInsurance.text.toString().trim().ifEmpty { null }
        val address = binding.etAddress.text.toString().trim().ifEmpty { null }
        val symptoms = binding.etSymptoms.text.toString().trim().ifEmpty { "Khám tổng quát vãng lai" }

        if (fullName.isEmpty()) {
            binding.etFullName.error = "Vui lòng nhập họ và tên bệnh nhân"
            return
        }
        if (phone.isEmpty()) {
            binding.etPhone.error = "Vui lòng nhập số điện thoại liên hệ"
            return
        }

        val genderCode = when (binding.spinnerGender.selectedItemPosition) {
            1 -> "FEMALE"
            2 -> "OTHER"
            else -> "MALE"
        }

        val priorityCode = when (binding.spinnerPriority.selectedItemPosition) {
            1 -> "PRIORITY"
            2 -> "EMERGENCY"
            else -> "NORMAL"
        }

        val selectedRoomIndex = binding.spinnerRoom.selectedItemPosition
        val selectedRoom = if (selectedRoomIndex in roomList.indices) roomList[selectedRoomIndex] else null

        val req = WalkInReceptionRequest(
            patientId = existingPatientId,
            fullName = fullName,
            phone = phone,
            dateOfBirth = dob,
            gender = genderCode,
            identityCard = cccd,
            healthInsuranceNumber = bhyt,
            address = address,
            departmentId = selectedRoom?.departmentId,
            roomId = selectedRoom?.roomId,
            doctorId = selectedRoom?.doctorId,
            serviceType = "Khám bệnh",
            priority = priorityCode,
            notes = symptoms
        )

        val session = RetrofitClient.getSessionManager(this)
        val user = session.getUser()
        val api = RetrofitClient.getApiService(this)

        binding.btnSubmitWalkIn.isEnabled = false
        binding.btnSubmitWalkIn.text = "Đang xử lý cấp số..."

        lifecycleScope.launch {
            try {
                val res = api.walkInReception(req, user?.id)
                binding.btnSubmitWalkIn.isEnabled = true
                binding.btnSubmitWalkIn.text = "Cấp Số Thứ Tự & Tiếp Nhận Khám"

                if (res.isSuccessful && res.body() != null) {
                    showTicketSuccessDialog(res.body()!!)
                } else {
                    Toast.makeText(this@WalkInReceptionActivity, "Lỗi tiếp nhận: ${res.code()}", Toast.LENGTH_LONG).show()
                }
            } catch (e: Exception) {
                binding.btnSubmitWalkIn.isEnabled = true
                binding.btnSubmitWalkIn.text = "Cấp Số Thứ Tự & Tiếp Nhận Khám"
                Toast.makeText(this@WalkInReceptionActivity, "Lỗi mạng: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun showTicketSuccessDialog(ticket: QueueTicketResponse) {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_ticket_success, null)
        val tvNumber = dialogView.findViewById<TextView>(R.id.tvDialogTicketNumber)
        val tvCode = dialogView.findViewById<TextView>(R.id.tvDialogTicketCode)
        val tvPatient = dialogView.findViewById<TextView>(R.id.tvDialogPatientName)
        val tvRoom = dialogView.findViewById<TextView>(R.id.tvDialogRoom)
        val tvDoctor = dialogView.findViewById<TextView>(R.id.tvDialogDoctor)
        val tvWaitAhead = dialogView.findViewById<TextView>(R.id.tvDialogWaitAhead)
        val btnClose = dialogView.findViewById<View>(R.id.btnDialogClose)

        tvNumber.text = String.format("%02d", ticket.ticketNumber)
        tvCode.text = "Mã phiếu: ${ticket.ticketCode ?: "PK"}"
        tvPatient.text = "Bệnh nhân: ${ticket.patientName ?: "Khách hàng"}"
        tvRoom.text = "Phòng khám: ${ticket.roomName ?: ticket.roomNumber ?: "Phòng khám chung"}"
        tvDoctor.text = "Bác sĩ: ${ticket.doctorName ?: "Chưa chỉ định"}"
        val waitPos = ticket.waitPosition ?: 0
        tvWaitAhead.text = "Số người đang chờ trước bạn: $waitPos người"

        val dialog = AlertDialog.Builder(this)
            .setView(dialogView)
            .setCancelable(false)
            .create()

        btnClose.setOnClickListener {
            dialog.dismiss()
            finish()
        }

        dialog.show()
    }
}
