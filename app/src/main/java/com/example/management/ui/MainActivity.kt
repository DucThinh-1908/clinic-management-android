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

        // Mặc định: nếu là lễ tân, điều dưỡng hoặc quản trị viên thì vào tab Tiếp đón, ngược lại vào Home
        val isStaffWithQueueAccess = user?.roles?.any {
            it.contains("RECEPTIONIST", true) || it.contains("LE_TAN", true) ||
            it.contains("NURSE", true) || it.contains("DIEU_DUONG", true) ||
            it.contains("ADMIN", true)
        } == true

        if (isStaffWithQueueAccess) {
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
