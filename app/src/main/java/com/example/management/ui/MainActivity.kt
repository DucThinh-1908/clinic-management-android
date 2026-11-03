package com.example.management.ui

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.example.management.R
import com.example.management.api.RetrofitClient
import com.example.management.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    private val homeFragment = HomeFragment()
    private val receptionFragment = ReceptionFragment()
    private val appointmentsFragment = AppointmentsFragment()
    private val recordsFragment = MedicalRecordsFragment()
    private val reportsFragment = ReportsFragment()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val session = RetrofitClient.getSessionManager(this)
        val user = session.getUser()

        // Hiển thị vai trò người dùng trên thanh tiêu đề
        val role = user?.roles?.firstOrNull() ?: "Người dùng"
        val roleLabel = when {
            role.contains("DOCTOR", true) -> "Bác sĩ"
            role.contains("NURSE", true) || role.contains("DIEU_DUONG", true) -> "Điều dưỡng"
            role.contains("TECHNICIAN", true) || role.contains("KTV", true) -> "Kỹ thuật viên"
            role.contains("PHARMACIST", true) || role.contains("DUOC_SI", true) -> "Dược sĩ"
            role.contains("ADMIN", true) -> "Quản trị viên"
            role.contains("MANAGER", true) -> "Quản lý"
            role.contains("RECEPTIONIST", true) -> "Lễ tân"
            role.contains("CASHIER", true) || role.contains("THU_NGAN", true) -> "Thu ngân"
            else -> "Bệnh nhân"
        }
        binding.tvUserRoleBadge.text = "$roleLabel (${user?.username ?: ""})"

        // Bấm vào badge vai trò để Đăng xuất
        binding.tvUserRoleBadge.setOnClickListener {
            showLogoutDialog()
        }

        binding.bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> {
                    replaceFragment(homeFragment)
                    true
                }
                R.id.nav_reception -> {
                    replaceFragment(receptionFragment)
                    true
                }
                R.id.nav_appointments -> {
                    replaceFragment(appointmentsFragment)
                    true
                }
                R.id.nav_records -> {
                    replaceFragment(recordsFragment)
                    true
                }
                R.id.nav_reports -> {
                    replaceFragment(reportsFragment)
                    true
                }
                else -> false
            }
        }

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

        // Phân quyền hiển thị các tab trên Bottom Navigation Bar
        val navMenu = binding.bottomNav.menu
        navMenu.findItem(R.id.nav_home)?.isVisible = true
        navMenu.findItem(R.id.nav_reception)?.isVisible = isAdmin || isReceptionist || isNurse
        navMenu.findItem(R.id.nav_appointments)?.isVisible = isAdmin || isReceptionist || isDoctor || isPatient
        navMenu.findItem(R.id.nav_records)?.isVisible = isAdmin || isDoctor || isNurse || isReceptionist || isPatient
        navMenu.findItem(R.id.nav_reports)?.isVisible = isAdmin

        // Mặc định: nếu là lễ tân hoặc điều dưỡng thì vào tab Tiếp đón, các vai trò khác vào Trang chủ
        if (isReceptionist || isNurse) {
            selectNavigationTab(1)
        } else {
            replaceFragment(homeFragment)
        }
    }

    fun selectNavigationTab(index: Int) {
        when (index) {
            0 -> binding.bottomNav.selectedItemId = R.id.nav_home
            1 -> binding.bottomNav.selectedItemId = R.id.nav_reception
            2 -> binding.bottomNav.selectedItemId = R.id.nav_appointments
            3 -> binding.bottomNav.selectedItemId = R.id.nav_records
            4 -> binding.bottomNav.selectedItemId = R.id.nav_reports
        }
    }

    private fun replaceFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragmentContainer, fragment)
            .commit()
    }

    private fun showLogoutDialog() {
        AlertDialog.Builder(this)
            .setTitle("Đăng xuất tài khoản")
            .setMessage("Bạn có chắc chắn muốn đăng xuất khỏi ứng dụng không?")
            .setPositiveButton("Đăng xuất") { _, _ ->
                val session = RetrofitClient.getSessionManager(this)
                session.logout()
                startActivity(Intent(this, LoginActivity::class.java))
                finish()
            }
            .setNegativeButton("Hủy", null)
            .show()
    }
}
