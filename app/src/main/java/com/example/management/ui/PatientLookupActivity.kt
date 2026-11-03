package com.example.management.ui

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.Spinner
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.management.R
import com.example.management.api.RetrofitClient
import com.example.management.data.model.CreatePatientRequest
import com.example.management.data.model.PatientDetailDto
import com.example.management.data.model.UpdateInsuranceRequest
import com.example.management.databinding.ActivityPatientLookupBinding
import com.example.management.ui.adapter.PatientAdapter
import kotlinx.coroutines.launch

class PatientLookupActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPatientLookupBinding
    private lateinit var adapter: PatientAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPatientLookupBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbarPatientLookup)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        binding.toolbarPatientLookup.setNavigationOnClickListener { finish() }

        adapter = PatientAdapter(
            onQuickReception = { patient ->
                // Mở màn hình tiếp nhận vãng lai với dữ liệu có sẵn
                val intent = Intent(this, WalkInReceptionActivity::class.java).apply {
                    putExtra("EXTRA_PATIENT_ID", patient.id)
                    putExtra("EXTRA_FULL_NAME", patient.fullName)
                    putExtra("EXTRA_PHONE", patient.phone)
                    putExtra("EXTRA_DOB", patient.dateOfBirth)
                    putExtra("EXTRA_CCCD", patient.identityCard)
                    putExtra("EXTRA_BHYT", patient.healthInsuranceNumber)
                    putExtra("EXTRA_ADDRESS", patient.address)
                }
                startActivity(intent)
            },
            onPatientClick = { patient ->
                showPatientDetailDialog(patient)
            }
        )

        binding.rvPatients.layoutManager = LinearLayoutManager(this)
        binding.rvPatients.adapter = adapter

        binding.btnSearch.setOnClickListener {
            performSearch()
        }

        binding.btnCreateNewPatient.setOnClickListener {
            showCreatePatientDialog()
        }

        // Tải danh sách mặc định
        performSearch()
    }

    private fun performSearch() {
        val keyword = binding.etSearchKeyword.text.toString().trim()
        val api = RetrofitClient.getApiService(this)

        binding.progressBarPatients.visibility = View.VISIBLE
        binding.tvEmptyPatients.visibility = View.GONE

        lifecycleScope.launch {
            try {
                val res = api.searchPatients(keyword.ifEmpty { null })
                binding.progressBarPatients.visibility = View.GONE
                if (res.isSuccessful && res.body() != null) {
                    val list = res.body()!!
                    adapter.submitList(list)
                    binding.tvEmptyPatients.visibility = if (list.isEmpty()) View.VISIBLE else View.GONE
                    if (list.isEmpty()) {
                        binding.tvEmptyPatients.text = "Không tìm thấy hồ sơ bệnh nhân nào phù hợp với '$keyword'"
                    }
                } else {
                    binding.tvEmptyPatients.visibility = View.VISIBLE
                    binding.tvEmptyPatients.text = "Không thể tải dữ liệu bệnh nhân"
                }
            } catch (e: Exception) {
                binding.progressBarPatients.visibility = View.GONE
                binding.tvEmptyPatients.visibility = View.VISIBLE
                binding.tvEmptyPatients.text = "Lỗi kết nối: ${e.message}"
            }
        }
    }

    private fun showCreatePatientDialog() {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.activity_walk_in_reception, null)
        // Dùng form tạo nhanh qua AlertDialog
        val etName = EditText(this).apply { hint = "Họ và tên bệnh nhân *" }
        val etPhone = EditText(this).apply { hint = "Số điện thoại liên hệ *" }
        val etCccd = EditText(this).apply { hint = "CCCD / CMND" }
        val etInsurance = EditText(this).apply { hint = "Mã số thẻ BHYT" }

        val layout = android.widget.LinearLayout(this).apply {
            orientation = android.widget.LinearLayout.VERTICAL
            setPadding(50, 40, 50, 20)
            addView(etName)
            addView(etPhone)
            addView(etCccd)
            addView(etInsurance)
        }

        AlertDialog.Builder(this)
            .setTitle("Đăng ký hồ sơ bệnh nhân mới")
            .setView(layout)
            .setPositiveButton("Lưu hồ sơ") { _, _ ->
                val name = etName.text.toString().trim()
                val phone = etPhone.text.toString().trim()
                val cccd = etCccd.text.toString().trim().ifEmpty { null }
                val bhyt = etInsurance.text.toString().trim().ifEmpty { null }

                if (name.isEmpty() || phone.isEmpty()) {
                    Toast.makeText(this, "Họ tên và Số điện thoại là bắt buộc", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }

                val req = CreatePatientRequest(
                    fullName = name,
                    phone = phone,
                    identityCard = cccd,
                    healthInsuranceNumber = bhyt
                )

                val session = RetrofitClient.getSessionManager(this)
                val user = session.getUser()
                val api = RetrofitClient.getApiService(this)

                lifecycleScope.launch {
                    try {
                        val res = api.createPatient(req, user?.id)
                        if (res.isSuccessful && res.body() != null) {
                            Toast.makeText(this@PatientLookupActivity, "Đăng ký hồ sơ bệnh nhân thành công!", Toast.LENGTH_SHORT).show()
                            binding.etSearchKeyword.setText(phone)
                            performSearch()
                        } else {
                            Toast.makeText(this@PatientLookupActivity, "Đăng ký thất bại: ${res.code()}", Toast.LENGTH_SHORT).show()
                        }
                    } catch (e: Exception) {
                        Toast.makeText(this@PatientLookupActivity, "Lỗi: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
                }
            }
            .setNegativeButton("Hủy", null)
            .show()
    }

    private fun showPatientDetailDialog(patient: PatientDetailDto) {
        val msg = """
            Mã bệnh nhân: ${patient.patientCode ?: "N/A"}
            Họ và tên: ${patient.fullName ?: "N/A"}
            Giới tính: ${if (patient.gender == "MALE") "Nam" else "Nữ"}
            Ngày sinh: ${patient.dateOfBirth ?: "N/A"}
            Số điện thoại: ${patient.phone ?: "N/A"}
            Email: ${patient.email ?: "Chưa có"}
            CCCD: ${patient.identityCard ?: "Chưa có"}
            Mã thẻ BHYT: ${patient.healthInsuranceNumber ?: "Chưa có"}
            Địa chỉ: ${patient.address ?: "Chưa có"}
            Người liên hệ khẩn cấp: ${patient.emergencyContactName ?: "N/A"} (${patient.emergencyContactPhone ?: "N/A"})
            Nhóm máu: ${patient.bloodGroup ?: "Chưa xác định"}
            Dị ứng thuốc: ${patient.drugAllergies ?: "Không"}
            Tổng số lần khám: ${patient.totalVisits} lần
        """.trimIndent()

        AlertDialog.Builder(this)
            .setTitle("Chi tiết hồ sơ bệnh nhân")
            .setMessage(msg)
            .setPositiveButton("Tiếp nhận khám") { _, _ ->
                val intent = Intent(this, WalkInReceptionActivity::class.java).apply {
                    putExtra("EXTRA_PATIENT_ID", patient.id)
                    putExtra("EXTRA_FULL_NAME", patient.fullName)
                    putExtra("EXTRA_PHONE", patient.phone)
                    putExtra("EXTRA_DOB", patient.dateOfBirth)
                    putExtra("EXTRA_CCCD", patient.identityCard)
                    putExtra("EXTRA_BHYT", patient.healthInsuranceNumber)
                    putExtra("EXTRA_ADDRESS", patient.address)
                }
                startActivity(intent)
            }
            .setNeutralButton("Cập nhật BHYT") { _, _ ->
                showUpdateInsuranceDialog(patient)
            }
            .setNegativeButton("Đóng", null)
            .show()
    }

    private fun showUpdateInsuranceDialog(patient: PatientDetailDto) {
        val etInput = EditText(this).apply {
            hint = "Nhập mã số thẻ BHYT mới"
            setText(patient.healthInsuranceNumber ?: "")
        }

        AlertDialog.Builder(this)
            .setTitle("Cập nhật thông tin thẻ BHYT")
            .setView(etInput)
            .setPositiveButton("Lưu BHYT") { _, _ ->
                val code = etInput.text.toString().trim()
                if (code.isEmpty()) return@setPositiveButton

                val session = RetrofitClient.getSessionManager(this)
                val user = session.getUser()
                val api = RetrofitClient.getApiService(this)

                lifecycleScope.launch {
                    try {
                        val res = api.updatePatientInsurance(patient.id, UpdateInsuranceRequest(code), user?.id)
                        if (res.isSuccessful) {
                            Toast.makeText(this@PatientLookupActivity, "Đã cập nhật BHYT thành công", Toast.LENGTH_SHORT).show()
                            performSearch()
                        } else {
                            Toast.makeText(this@PatientLookupActivity, "Cập nhật thất bại", Toast.LENGTH_SHORT).show()
                        }
                    } catch (e: Exception) {
                        Toast.makeText(this@PatientLookupActivity, "Lỗi: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
                }
            }
            .setNegativeButton("Hủy", null)
            .show()
    }
}
