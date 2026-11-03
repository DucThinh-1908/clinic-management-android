package com.example.management.ui

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.management.api.RetrofitClient
import com.example.management.data.model.CreateAppointmentRequest
import com.example.management.databinding.ActivityBookAppointmentBinding
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class BookAppointmentActivity : AppCompatActivity() {

    private lateinit var binding: ActivityBookAppointmentBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityBookAppointmentBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnBack.setOnClickListener {
            finish()
        }

        // Điền trước ngày hôm nay hoặc ngày mai
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val tomorrow = Date(System.currentTimeMillis() + 24 * 60 * 60 * 1000)
        binding.etDate.setText(sdf.format(tomorrow))
        binding.etTime.setText("09:00:00")

        binding.btnSubmitBook.setOnClickListener {
            submitBooking()
        }
    }

    private fun submitBooking() {
        val date = binding.etDate.text.toString().trim()
        val time = binding.etTime.text.toString().trim()
        val symptoms = binding.etSymptoms.text.toString().trim()

        if (date.isEmpty() || time.isEmpty()) {
            Toast.makeText(this, "Vui lòng nhập ngày và giờ khám", Toast.LENGTH_SHORT).show()
            return
        }

        val session = RetrofitClient.getSessionManager(this)
        val user = session.getUser()
        val api = RetrofitClient.getApiService(this)

        binding.pbBooking.visibility = View.VISIBLE
        binding.btnSubmitBook.isEnabled = false

        lifecycleScope.launch {
            try {
                val req = CreateAppointmentRequest(
                    doctorId = 2L, // Mặc định bác sĩ 2 hoặc null để phòng khám phân công
                    patientId = user?.id ?: 1L,
                    departmentId = 1L,
                    appointmentDate = date,
                    appointmentTime = time,
                    symptomsSummary = if (symptoms.isNotEmpty()) symptoms else "Đặt lịch khám qua ứng dụng di động",
                    bookingChannel = "MOBILE_APP"
                )

                val res = api.createAppointment(req)
                binding.pbBooking.visibility = View.GONE
                binding.btnSubmitBook.isEnabled = true

                if (res.isSuccessful && res.body() != null) {
                    val booked = res.body()!!
                    Toast.makeText(
                        this@BookAppointmentActivity,
                        "Đặt lịch thành công! Mã: ${booked.appointmentCode}",
                        Toast.LENGTH_LONG
                    ).show()
                    finish()
                } else {
                    Toast.makeText(
                        this@BookAppointmentActivity,
                        "Đặt lịch thất bại. Vui lòng kiểm tra lại",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            } catch (e: Exception) {
                binding.pbBooking.visibility = View.GONE
                binding.btnSubmitBook.isEnabled = true
                Toast.makeText(this@BookAppointmentActivity, "Lỗi kết nối: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
