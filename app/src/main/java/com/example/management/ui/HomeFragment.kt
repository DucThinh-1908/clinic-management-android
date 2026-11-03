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

        // Phân quyền theo vai trò người dùng
        val isAdmin = user?.roles?.any { it.contains("ADMIN", true) } == true
        val isDoctor = user?.roles?.any { it.contains("DOCTOR", true) || it.contains("BAC_SI", true) } == true
        val isNurse = user?.roles?.any { it.contains("NURSE", true) || it.contains("DIEU_DUONG", true) } == true
        val isTechnician = user?.roles?.any { it.contains("TECHNICIAN", true) || it.contains("KTV", true) } == true
        val isPharmacist = user?.roles?.any { it.contains("PHARMACIST", true) || it.contains("DUOC_SI", true) } == true
        val isCashier = user?.roles?.any { it.contains("CASHIER", true) || it.contains("THU_NGAN", true) } == true
        val isReceptionist = user?.roles?.any { it.contains("RECEPTIONIST", true) || it.contains("LE_TAN", true) } == true
        val isStaff = isAdmin || isDoctor || isNurse || isTechnician || isPharmacist || isCashier || isReceptionist
        val isPatient = !isStaff || user?.roles?.any { it.contains("PATIENT", true) || it.contains("BENH_NHAN", true) } == true

        // 1. Nút Đặt lịch khám nhanh
        binding.btnQuickBook.visibility = if (isAdmin || isReceptionist || isPatient) View.VISIBLE else View.GONE
        binding.btnQuickBook.setOnClickListener {
            startActivity(Intent(requireContext(), BookAppointmentActivity::class.java))
        }

        // 2. Tiện ích Đặt lịch khám
        binding.cardActionBook.visibility = if (isAdmin || isReceptionist || isDoctor || isPatient) View.VISIBLE else View.GONE
        binding.cardActionBook.setOnClickListener {
            startActivity(Intent(requireContext(), BookAppointmentActivity::class.java))
        }

        // 3. Tiện ích Hồ sơ bệnh án
        binding.cardActionRecords.visibility = if (isAdmin || isDoctor || isNurse || isReceptionist || isPatient) View.VISIBLE else View.GONE
        binding.cardActionRecords.setOnClickListener {
            (activity as? MainActivity)?.selectNavigationTab(3) // Chuyển sang tab Bệnh án
        }

        // 4. Tiện ích Trạm chuyên môn (Tiếp đón / Điều dưỡng / KTV / Dược sĩ / Thu ngân)
        val showPrescriptionStation = isAdmin || isCashier || isPharmacist || isTechnician || isNurse || isReceptionist
        binding.cardActionPrescriptions.visibility = if (showPrescriptionStation) View.VISIBLE else View.GONE

        when {
            isCashier -> {
                binding.tvActionPrescriptionTitle.text = "Quầy Thu Ngân"
                binding.tvActionPrescriptionSubtitle.text = "Thu tiền & Viện phí"
            }
            isPharmacist -> {
                binding.tvActionPrescriptionTitle.text = "Trạm Dược Sĩ"
                binding.tvActionPrescriptionSubtitle.text = "Cấp phát thuốc & Kho dược"
            }
            isTechnician -> {
                binding.tvActionPrescriptionTitle.text = "Cận Lâm Sàng"
                binding.tvActionPrescriptionSubtitle.text = "Nhập kết quả xét nghiệm"
            }
            isNurse -> {
                binding.tvActionPrescriptionTitle.text = "Trạm Điều Dưỡng"
                binding.tvActionPrescriptionSubtitle.text = "Đo & theo dõi sinh hiệu"
            }
            else -> {
                binding.tvActionPrescriptionTitle.text = "Bàn Tiếp Đón"
                binding.tvActionPrescriptionSubtitle.text = "Cấp STT & hàng chờ"
            }
        }

        binding.cardActionPrescriptions.setOnClickListener {
            when {
                isCashier -> {
                    startActivity(Intent(requireContext(), CashierStationActivity::class.java))
                }
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

        // 5. Tiện ích Bác sĩ khám bệnh (Chỉ Bác sĩ & Admin)
        binding.cardActionDoctor.visibility = if (isAdmin || isDoctor) View.VISIBLE else View.GONE
        binding.cardActionDoctor.setOnClickListener {
            startActivity(Intent(requireContext(), DoctorStationActivity::class.java))
        }

        // 6. Tiện ích Báo cáo & Thống kê (Chỉ Admin)
        binding.cardActionReports.visibility = if (isAdmin) View.VISIBLE else View.GONE
        binding.cardActionReports.setOnClickListener {
            (activity as? MainActivity)?.selectNavigationTab(4) // Chuyển sang tab Báo cáo
        }

        // 7. Tiện ích Quản trị tài khoản & Người dùng (Chỉ Admin)
        binding.cardActionAdmin.visibility = if (isAdmin) View.VISIBLE else View.GONE
        binding.cardActionAdmin.setOnClickListener {
            startActivity(Intent(requireContext(), AdminAccountActivity::class.java))
        }

        // 8. Tiện ích Lịch trực & Chấm công (Dành cho nhân viên phòng khám)
        binding.cardActionStaff.visibility = if (isStaff) View.VISIBLE else View.GONE
        binding.cardActionStaff.setOnClickListener {
            startActivity(Intent(requireContext(), StaffSchedulingActivity::class.java))
        }

        // 9. Tiện ích Bảng giá dịch vụ y tế (Lễ tân, Bác sĩ, Dược sĩ, Thu ngân, Admin, Bệnh nhân)
        binding.cardActionServices.visibility = if (isAdmin || isReceptionist || isDoctor || isCashier || isPharmacist || isPatient) View.VISIBLE else View.GONE
        binding.cardActionServices.setOnClickListener {
            startActivity(Intent(requireContext(), ClinicServicesActivity::class.java))
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
