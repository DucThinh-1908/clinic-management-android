package com.example.management.ui

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.management.api.RetrofitClient
import com.example.management.databinding.FragmentHomeBinding
import kotlinx.coroutines.launch

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val session = RetrofitClient.getSessionManager(requireContext())
        val user = session.getUser()

        binding.tvWelcomeUser.text = "Xin chào, ${user?.fullName ?: user?.username ?: "Quý khách"}!"
        val role = user?.roles?.firstOrNull() ?: "Người dùng"
        binding.tvUserSubtitle.text = "Vai trò: $role | Phòng khám Đa khoa CarePlus"

        // Nút đặt lịch nhanh
        binding.btnQuickBook.setOnClickListener {
            startActivity(Intent(requireContext(), BookAppointmentActivity::class.java))
        }

        binding.cardActionBook.setOnClickListener {
            startActivity(Intent(requireContext(), BookAppointmentActivity::class.java))
        }

        val isTechnician = user?.roles?.any {
            it.contains("TECHNICIAN", true) || it.contains("KTV", true)
        } == true

        val isPharmacist = user?.roles?.any {
            it.contains("PHARMACIST", true) || it.contains("DUOC_SI", true)
        } == true

        when {
            isPharmacist -> {
                binding.tvActionPrescriptionTitle.text = "Trạm Dược Sĩ"
                binding.tvActionPrescriptionSubtitle.text = "Cấp phát thuốc & Kho dược"
            }
            isTechnician -> {
                binding.tvActionPrescriptionTitle.text = "Cận Lâm Sàng"
                binding.tvActionPrescriptionSubtitle.text = "Nhập kết quả xét nghiệm"
            }
            else -> {
                binding.tvActionPrescriptionTitle.text = "Bàn Tiếp Đón"
                binding.tvActionPrescriptionSubtitle.text = "Cấp STT & hàng chờ"
            }
        }

        binding.cardActionRecords.setOnClickListener {
            (activity as? MainActivity)?.selectNavigationTab(3) // Chuyển sang tab Bệnh án
        }

        binding.cardActionPrescriptions.setOnClickListener {
            when {
                isPharmacist -> {
                    startActivity(Intent(requireContext(), PharmacyStationActivity::class.java))
                }
                isTechnician -> {
                    startActivity(Intent(requireContext(), TechnicianStationActivity::class.java))
                }
                else -> {
                    (activity as? MainActivity)?.selectNavigationTab(1) // Chuyển sang tab Tiếp đón & Hàng chờ
                }
            }
        }

        binding.cardActionReports.setOnClickListener {
            (activity as? MainActivity)?.selectNavigationTab(4) // Chuyển sang tab Báo cáo
        }

        binding.swipeRefreshHome.setOnRefreshListener {
            loadUpcomingAppointment()
        }

        loadUpcomingAppointment()
    }

    private fun loadUpcomingAppointment() {
        val session = RetrofitClient.getSessionManager(requireContext())
        val user = session.getUser() ?: return
        val api = RetrofitClient.getApiService(requireContext())

        binding.swipeRefreshHome.isRefreshing = true
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val res = api.getAppointmentsByPatient(user.id)
                binding.swipeRefreshHome.isRefreshing = false
                if (res.isSuccessful && !res.body().isNullOrEmpty()) {
                    val upcoming = res.body()!!.firstOrNull { it.status != "CANCELLED" }
                    if (upcoming != null) {
                        binding.tvUpcomingAppointmentCode.text = upcoming.appointmentCode ?: "LH-ID#${upcoming.id}"
                        binding.tvUpcomingDoctor.text = "Bác sĩ: ${upcoming.doctorName ?: "Chưa chỉ định"} - ${upcoming.departmentName ?: "Đa khoa"}"
                        binding.tvUpcomingDateTime.text = "Thời gian: ${upcoming.appointmentTime ?: ""} ngày ${upcoming.appointmentDate ?: ""}"
                    } else {
                        binding.tvUpcomingAppointmentCode.text = "Không có lịch hẹn sắp tới"
                        binding.tvUpcomingDoctor.text = "Bạn chưa có lịch hẹn nào đang chờ"
                        binding.tvUpcomingDateTime.text = "Bấm nút 'Đặt lịch khám ngay' bên trên để hẹn khám"
                    }
                } else {
                    binding.tvUpcomingAppointmentCode.text = "Chưa có lịch hẹn"
                    binding.tvUpcomingDoctor.text = "Hãy đặt lịch để được phục vụ tốt nhất"
                    binding.tvUpcomingDateTime.text = "Khám chữa bệnh chất lượng cao CarePlus"
                }
            } catch (e: Exception) {
                binding.swipeRefreshHome.isRefreshing = false
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
