package com.example.management.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.management.api.RetrofitClient
import com.example.management.databinding.FragmentReportsBinding
import com.example.management.ui.adapter.HourlyVisitAdapter
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.util.Locale

class ReportsFragment : Fragment() {

    private var _binding: FragmentReportsBinding? = null
    private val binding get() = _binding!!
    private lateinit var hourlyAdapter: HourlyVisitAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentReportsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        hourlyAdapter = HourlyVisitAdapter()
        binding.rvHourlyVisits.layoutManager = LinearLayoutManager(requireContext())
        binding.rvHourlyVisits.adapter = hourlyAdapter

        binding.swipeRefreshReports.setOnRefreshListener {
            loadDashboardData()
        }

        loadDashboardData()
    }

    private fun loadDashboardData() {
        val api = RetrofitClient.getApiService(requireContext())
        binding.swipeRefreshReports.isRefreshing = true

        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val res = api.getRealtimeDashboard()
                binding.swipeRefreshReports.isRefreshing = false

                if (res.isSuccessful && res.body() != null) {
                    val data = res.body()!!
                    binding.tvReportLastUpdated.text = "Cập nhật: ${data.lastUpdated ?: "--:--"}"

                    val formatter = NumberFormat.getCurrencyInstance(Locale("vi", "VN"))
                    binding.tvKpiRevenue.text = formatter.format(data.todayRevenue ?: 0.0)
                    binding.tvKpiPatients.text = "${data.todayPatientsRegistered ?: 0} ca"
                    binding.tvKpiWaiting.text = "${data.waitingConsultationCount ?: 0} người"
                    binding.tvKpiProgress.text = "${data.inConsultationCount ?: 0} / ${data.completedConsultationCount ?: 0}"

                    data.hourlyVisits?.let {
                        hourlyAdapter.submitList(it)
                    }
                } else {
                    Toast.makeText(requireContext(), "Không thể tải báo cáo thời gian thực", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                binding.swipeRefreshReports.isRefreshing = false
                Toast.makeText(requireContext(), "Lỗi mạng: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
