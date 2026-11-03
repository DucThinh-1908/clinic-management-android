package com.example.management.ui

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.management.api.RetrofitClient
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

        adapter = AppointmentAdapter { item ->
            Toast.makeText(requireContext(), "Mã lịch: ${item.appointmentCode} (${item.status})", Toast.LENGTH_SHORT).show()
        }

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
                // Nếu là bác sĩ/quản lý: load all, nếu là bệnh nhân: load theo patientId
                val isStaff = user.roles?.any { it.contains("DOCTOR", true) || it.contains("ADMIN", true) || it.contains("MANAGER", true) } == true
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

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
