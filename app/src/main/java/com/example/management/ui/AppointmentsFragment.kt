package com.example.management.ui

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.management.R
import com.example.management.api.RetrofitClient
import com.example.management.data.model.AppointmentItem
import com.example.management.data.model.CancelAppointmentRequest
import com.example.management.data.model.CheckInAppointmentRequest
import com.example.management.data.model.QueueTicketResponse
import com.example.management.databinding.FragmentAppointmentsBinding
import com.example.management.ui.adapter.AppointmentAdapter
import kotlinx.coroutines.launch

class AppointmentsFragment : Fragment() {

    private var _binding: FragmentAppointmentsBinding? = null
    private val binding get() = _binding!!
    private lateinit var adapter: AppointmentAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAppointmentsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        adapter = AppointmentAdapter(
            onItemClick = { item ->
                Toast.makeText(requireContext(), "Lịch: ${item.appointmentCode} (${item.status})", Toast.LENGTH_SHORT).show()
            },
            onConfirm = { item -> confirmAppointment(item) },
            onCheckIn = { item -> checkInAppointment(item) },
            onNoShow = { item -> markNoShow(item) },
            onCancel = { item -> cancelAppointment(item) }
        )

        binding.rvAppointments.layoutManager = LinearLayoutManager(requireContext())
        binding.rvAppointments.adapter = adapter

        binding.btnCreateAppointment.setOnClickListener {
            startActivity(Intent(requireContext(), BookAppointmentActivity::class.java))
        }

        binding.swipeRefreshAppointments.setOnRefreshListener {
            loadAppointments()
        }

        loadAppointments()
    }

    override fun onResume() {
        super.onResume()
        loadAppointments()
    }

    private fun loadAppointments() {
        val session = RetrofitClient.getSessionManager(requireContext())
        val user = session.getUser() ?: return
        val api = RetrofitClient.getApiService(requireContext())

        binding.progressBarAppointments.visibility = View.VISIBLE
        binding.tvEmptyAppointments.visibility = View.GONE

        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val isStaff = user.roles?.any {
                    it.contains("DOCTOR", true) ||
                    it.contains("ADMIN", true) ||
                    it.contains("MANAGER", true) ||
                    it.contains("RECEPTIONIST", true) ||
                    it.contains("LE_TAN", true)
                } == true

                val response = if (isStaff) {
                    api.getAllAppointments()
                } else {
                    api.getAppointmentsByPatient(user.id)
                }

                binding.progressBarAppointments.visibility = View.GONE
                binding.swipeRefreshAppointments.isRefreshing = false

                if (response.isSuccessful && response.body() != null) {
                    val list = response.body()!!
                    adapter.submitList(list)
                    binding.tvEmptyAppointments.visibility = if (list.isEmpty()) View.VISIBLE else View.GONE
                } else {
                    binding.tvEmptyAppointments.visibility = View.VISIBLE
                    Toast.makeText(requireContext(), "Không thể tải lịch hẹn", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                binding.progressBarAppointments.visibility = View.GONE
                binding.swipeRefreshAppointments.isRefreshing = false
                binding.tvEmptyAppointments.visibility = View.VISIBLE
                Toast.makeText(requireContext(), "Lỗi mạng: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // Nghiệp vụ Lễ tân: Xác nhận lịch hẹn
    private fun confirmAppointment(item: AppointmentItem) {
        val session = RetrofitClient.getSessionManager(requireContext())
        val user = session.getUser()
        val api = RetrofitClient.getApiService(requireContext())

        AlertDialog.Builder(requireContext())
            .setTitle("Xác nhận lịch hẹn")
            .setMessage("Xác nhận cho bệnh nhân ${item.patientName ?: "Khách"} đến khám vào ${item.appointmentTime} ngày ${item.appointmentDate}?")
            .setPositiveButton("Xác nhận") { _, _ ->
                viewLifecycleOwner.lifecycleScope.launch {
                    try {
                        val res = api.confirmAppointment(item.id, user?.id)
                        if (res.isSuccessful) {
                            Toast.makeText(requireContext(), "Đã xác nhận lịch hẹn thành công!", Toast.LENGTH_SHORT).show()
                            loadAppointments()
                        } else {
                            Toast.makeText(requireContext(), "Xác nhận thất bại: ${res.code()}", Toast.LENGTH_SHORT).show()
                        }
                    } catch (e: Exception) {
                        Toast.makeText(requireContext(), "Lỗi: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
                }
            }
            .setNegativeButton("Hủy", null)
            .show()
    }

    // Nghiệp vụ Lễ tân: Check-in bệnh nhân và cấp số thứ tự vào phòng khám
    private fun checkInAppointment(item: AppointmentItem) {
        val session = RetrofitClient.getSessionManager(requireContext())
        val user = session.getUser()
        val api = RetrofitClient.getApiService(requireContext())

        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val req = CheckInAppointmentRequest(
                    appointmentId = item.id,
                    appointmentCode = item.appointmentCode,
                    roomId = null,
                    priority = "NORMAL",
                    notes = "Tiếp nhận tại quầy lễ tân ứng dụng"
                )
                val res = api.checkInAppointment(req, user?.id)
                if (res.isSuccessful && res.body() != null) {
                    showTicketSuccessDialog(res.body()!!)
                    loadAppointments()
                } else {
                    Toast.makeText(requireContext(), "Tiếp nhận check-in thất bại: ${res.code()}", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(requireContext(), "Lỗi check-in: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // Nghiệp vụ Lễ tân: Báo vắng mặt (No-show)
    private fun markNoShow(item: AppointmentItem) {
        val session = RetrofitClient.getSessionManager(requireContext())
        val user = session.getUser()
        val api = RetrofitClient.getApiService(requireContext())

        AlertDialog.Builder(requireContext())
            .setTitle("Đánh dấu vắng mặt")
            .setMessage("Bệnh nhân ${item.patientName} không đến đúng hẹn?")
            .setPositiveButton("Xác nhận vắng mặt") { _, _ ->
                viewLifecycleOwner.lifecycleScope.launch {
                    try {
                        val res = api.markNoShow(item.id, user?.id)
                        if (res.isSuccessful) {
                            Toast.makeText(requireContext(), "Đã đánh dấu bệnh nhân vắng mặt", Toast.LENGTH_SHORT).show()
                            loadAppointments()
                        } else {
                            Toast.makeText(requireContext(), "Thao tác thất bại", Toast.LENGTH_SHORT).show()
                        }
                    } catch (e: Exception) {
                        Toast.makeText(requireContext(), "Lỗi: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
                }
            }
            .setNegativeButton("Quay lại", null)
            .show()
    }

    // Nghiệp vụ Lễ tân: Hủy lịch hẹn
    private fun cancelAppointment(item: AppointmentItem) {
        val session = RetrofitClient.getSessionManager(requireContext())
        val user = session.getUser()
        val api = RetrofitClient.getApiService(requireContext())

        val input = EditText(requireContext())
        input.hint = "Nhập lý do hủy lịch hẹn..."

        AlertDialog.Builder(requireContext())
            .setTitle("Hủy lịch hẹn")
            .setView(input)
            .setPositiveButton("Xác nhận hủy") { _, _ ->
                val reason = input.text.toString().trim().ifEmpty { "Lễ tân hủy theo yêu cầu" }
                viewLifecycleOwner.lifecycleScope.launch {
                    try {
                        val res = api.cancelAppointment(item.id, CancelAppointmentRequest(reason), user?.id)
                        if (res.isSuccessful) {
                            Toast.makeText(requireContext(), "Đã hủy lịch hẹn", Toast.LENGTH_SHORT).show()
                            loadAppointments()
                        } else {
                            Toast.makeText(requireContext(), "Hủy thất bại", Toast.LENGTH_SHORT).show()
                        }
                    } catch (e: Exception) {
                        Toast.makeText(requireContext(), "Lỗi: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
                }
            }
            .setNegativeButton("Đóng", null)
            .show()
    }

    // Hiển thị phiếu số thứ tự tiếp đón phòng khám
    private fun showTicketSuccessDialog(ticket: QueueTicketResponse) {
        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_ticket_success, null)
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

        val dialog = AlertDialog.Builder(requireContext())
            .setView(dialogView)
            .setCancelable(false)
            .create()

        btnClose.setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
