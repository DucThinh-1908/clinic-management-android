package com.example.management.ui

import android.app.AlertDialog
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.management.R
import com.example.management.api.RetrofitClient
import com.example.management.data.model.VitalSignRequest
import com.example.management.data.model.VitalSignResponse
import com.example.management.databinding.ActivityRecordVitalSignsBinding
import com.example.management.ui.adapter.VitalSignHistoryAdapter
import kotlinx.coroutines.launch

class RecordVitalSignsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRecordVitalSignsBinding
    private var patientId: Long = -1L
    private var ticketId: Long? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRecordVitalSignsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbarVitalSigns)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        binding.toolbarVitalSigns.setNavigationOnClickListener { finish() }

        patientId = intent.getLongExtra("EXTRA_PATIENT_ID", -1L)
        val patientName = intent.getStringExtra("EXTRA_PATIENT_NAME") ?: "Bệnh nhân"
        val patientCode = intent.getStringExtra("EXTRA_PATIENT_CODE") ?: ""
        val age = intent.getIntExtra("EXTRA_PATIENT_AGE", 0)
        val gender = intent.getStringExtra("EXTRA_PATIENT_GENDER") ?: ""
        val ticketIdExtra = intent.getLongExtra("EXTRA_TICKET_ID", -1L)
        if (ticketIdExtra > 0) ticketId = ticketIdExtra

        binding.tvPatientName.text = "Bệnh nhân: $patientName"
        val genderDisplay = if (gender == "MALE") "Nam" else if (gender == "FEMALE") "Nữ" else ""
        val ageDisplay = if (age > 0) "$age tuổi" else ""
        val codeDisplay = if (patientCode.isNotBlank()) "Mã BN: $patientCode" else ""
        binding.tvPatientSub.text = listOf(genderDisplay, ageDisplay, codeDisplay).filter { it.isNotBlank() }.joinToString(" | ")

        setupLiveBmiCalculator()

        binding.btnViewHistory.setOnClickListener {
            loadVitalSignsHistory()
        }

        binding.btnSaveVitalSigns.setOnClickListener {
            saveVitalSigns()
        }
    }

    private fun setupLiveBmiCalculator() {
        val watcher = object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                calculateLiveBmi()
            }
            override fun afterTextChanged(s: Editable?) {}
        }

        binding.etHeight.addTextChangedListener(watcher)
        binding.etWeight.addTextChangedListener(watcher)
    }

    private fun calculateLiveBmi() {
        val heightCm = binding.etHeight.text.toString().toDoubleOrNull()
        val weightKg = binding.etWeight.text.toString().toDoubleOrNull()

        if (heightCm != null && weightKg != null && heightCm > 50 && weightKg > 5) {
            val heightM = heightCm / 100.0
            val bmi = weightKg / (heightM * heightM)
            val category = when {
                bmi < 18.5 -> "Thiếu cân"
                bmi < 24.9 -> "Bình thường (Chuẩn)"
                bmi < 29.9 -> "Thừa cân (Tiền béo phì)"
                else -> "Béo phì"
            }
            binding.tvBmiPreview.text = String.format("%.1f - %s", bmi, category)
        } else {
            binding.tvBmiPreview.text = "Chưa tính (cần nhập chiều cao, cân nặng)"
        }
    }

    private fun saveVitalSigns() {
        if (patientId <= 0) {
            Toast.makeText(this, "Không tìm thấy mã bệnh nhân hợp lệ", Toast.LENGTH_SHORT).show()
            return
        }

        val systolic = binding.etBpSystolic.text.toString().toIntOrNull()
        val diastolic = binding.etBpDiastolic.text.toString().toIntOrNull()
        val temperature = binding.etTemperature.text.toString().toDoubleOrNull()
        val heartRate = binding.etHeartRate.text.toString().toIntOrNull()
        val respiratoryRate = binding.etRespiratoryRate.text.toString().toIntOrNull()
        val spo2 = binding.etSpo2.text.toString().toDoubleOrNull()
        val height = binding.etHeight.text.toString().toDoubleOrNull()
        val weight = binding.etWeight.text.toString().toDoubleOrNull()
        val notes = binding.etNurseNotes.text.toString().trim().ifEmpty { null }

        if (systolic == null && temperature == null && heartRate == null && spo2 == null) {
            Toast.makeText(this, "Vui lòng nhập ít nhất một chỉ số sinh hiệu (Huyết áp, Thân nhiệt, hoặc Mạch)", Toast.LENGTH_LONG).show()
            return
        }

        val session = RetrofitClient.getSessionManager(this)
        val user = session.getUser()
        val api = RetrofitClient.getApiService(this)

        val req = VitalSignRequest(
            patientId = patientId,
            nurseId = user?.id,
            ticketId = ticketId,
            temperature = temperature,
            bloodPressureSystolic = systolic,
            bloodPressureDiastolic = diastolic,
            heartRate = heartRate,
            respiratoryRate = respiratoryRate,
            spo2 = spo2,
            weightKg = weight,
            heightCm = height,
            notes = notes
        )

        binding.btnSaveVitalSigns.isEnabled = false
        binding.btnSaveVitalSigns.text = "Đang lưu sinh hiệu..."

        lifecycleScope.launch {
            try {
                val res = api.recordVitalSigns(req)
                binding.btnSaveVitalSigns.isEnabled = true
                binding.btnSaveVitalSigns.text = "Lưu Kết Quả Đo Sinh Hiệu"

                if (res.isSuccessful && res.body() != null) {
                    showSuccessSummaryDialog(res.body()!!)
                } else {
                    Toast.makeText(this@RecordVitalSignsActivity, "Lưu sinh hiệu thất bại: ${res.code()}", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                binding.btnSaveVitalSigns.isEnabled = true
                binding.btnSaveVitalSigns.text = "Lưu Kết Quả Đo Sinh Hiệu"
                Toast.makeText(this@RecordVitalSignsActivity, "Lỗi mạng: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun showSuccessSummaryDialog(result: VitalSignResponse) {
        val summary = StringBuilder()
        summary.append("Đã ghi nhận thành công chỉ số sinh hiệu:\n\n")

        if (result.bloodPressureSystolic != null && result.bloodPressureDiastolic != null) {
            val bpStatus = result.bloodPressureStatus?.let { " ($it)" } ?: ""
            summary.append("• Huyết áp: ${result.bloodPressureSystolic}/${result.bloodPressureDiastolic} mmHg$bpStatus\n")
        }
        if (result.temperature != null) {
            val tempStatus = result.temperatureStatus?.let { " ($it)" } ?: ""
            summary.append("• Thân nhiệt: ${result.temperature}°C$tempStatus\n")
        }
        if (result.heartRate != null) {
            summary.append("• Nhịp tim: ${result.heartRate} bpm\n")
        }
        if (result.spo2 != null) {
            summary.append("• SpO2: ${result.spo2}%\n")
        }
        if (result.bmi != null) {
            val cat = result.bmiCategory?.let { " ($it)" } ?: ""
            summary.append(String.format("• BMI: %.1f%s\n", result.bmi, cat))
        }

        AlertDialog.Builder(this)
            .setTitle("Ghi nhận sinh hiệu thành công")
            .setMessage(summary.toString())
            .setPositiveButton("Hoàn tất") { _, _ ->
                finish()
            }
            .setCancelable(false)
            .show()
    }

    private fun loadVitalSignsHistory() {
        val api = RetrofitClient.getApiService(this)
        lifecycleScope.launch {
            try {
                val res = api.getVitalSignsHistory(patientId)
                if (res.isSuccessful && res.body() != null) {
                    val history = res.body()!!
                    if (history.isEmpty()) {
                        Toast.makeText(this@RecordVitalSignsActivity, "Bệnh nhân chưa có lịch sử đo sinh hiệu nào trước đó", Toast.LENGTH_SHORT).show()
                        return@launch
                    }
                    showHistoryDialog(history)
                } else {
                    Toast.makeText(this@RecordVitalSignsActivity, "Không thể tải lịch sử sinh hiệu", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(this@RecordVitalSignsActivity, "Lỗi: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun showHistoryDialog(history: List<VitalSignResponse>) {
        val rv = RecyclerView(this).apply {
            layoutManager = LinearLayoutManager(this@RecordVitalSignsActivity)
            setPadding(24, 20, 24, 20)
        }
        val historyAdapter = VitalSignHistoryAdapter(history)
        rv.adapter = historyAdapter

        AlertDialog.Builder(this)
            .setTitle("Lịch sử sinh hiệu (${history.size} lần đo)")
            .setView(rv)
            .setPositiveButton("Đóng", null)
            .show()
    }
}
