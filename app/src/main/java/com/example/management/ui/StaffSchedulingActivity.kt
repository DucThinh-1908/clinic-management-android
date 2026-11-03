package com.example.management.ui

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.management.R
import com.example.management.api.RetrofitClient
import com.example.management.data.model.AttendanceResponse
import com.example.management.data.model.ScheduleResponse
import com.example.management.data.model.StaffResponse
import com.example.management.databinding.ActivityStaffSchedulingBinding
import com.example.management.ui.adapter.AttendanceAdapter
import com.example.management.ui.adapter.StaffMemberAdapter
import com.example.management.ui.adapter.StaffScheduleAdapter
import kotlinx.coroutines.launch

class StaffSchedulingActivity : AppCompatActivity() {

    private lateinit var binding: ActivityStaffSchedulingBinding

    private lateinit var scheduleAdapter: StaffScheduleAdapter
    private lateinit var staffAdapter: StaffMemberAdapter
    private lateinit var attendanceAdapter: AttendanceAdapter

    private var allSchedules: List<ScheduleResponse> = emptyList()
    private var allStaff: List<StaffResponse> = emptyList()
    private var allAttendance: List<AttendanceResponse> = emptyList()

    private var currentTab: String = "SCHEDULES" // SCHEDULES, STAFF, ATTENDANCE

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityStaffSchedulingBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbarStaff)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        binding.toolbarStaff.setNavigationOnClickListener { finish() }

        setupRecyclerViews()
        setupTabButtons()
        setupSearch()
        setupAttendanceButtons()

        binding.swipeRefreshStaff.setOnRefreshListener {
            loadStaffData()
        }

        loadStaffData()
    }

    private fun setupRecyclerViews() {
        scheduleAdapter = StaffScheduleAdapter()
        binding.rvStaffSchedules.layoutManager = LinearLayoutManager(this)
        binding.rvStaffSchedules.adapter = scheduleAdapter

        staffAdapter = StaffMemberAdapter()
        binding.rvStaffMembers.layoutManager = LinearLayoutManager(this)
        binding.rvStaffMembers.adapter = staffAdapter

        attendanceAdapter = AttendanceAdapter()
        binding.rvMyAttendance.layoutManager = LinearLayoutManager(this)
        binding.rvMyAttendance.adapter = attendanceAdapter
    }

    private fun setupTabButtons() {
        binding.btnTabSchedules.setOnClickListener {
            currentTab = "SCHEDULES"
            updateTabUi()
            applyFilterAndSearch()
        }

        binding.btnTabStaffList.setOnClickListener {
            currentTab = "STAFF"
            updateTabUi()
            applyFilterAndSearch()
        }

        binding.btnTabMyAttendance.setOnClickListener {
            currentTab = "ATTENDANCE"
            updateTabUi()
            applyFilterAndSearch()
        }
    }

    private fun updateTabUi() {
        val selectedBg = ContextCompat.getColor(this, R.color.primary)
        val defaultBg = ContextCompat.getColor(this, R.color.bg_light)
        val selectedText = ContextCompat.getColor(this, R.color.white)
        val defaultText = ContextCompat.getColor(this, R.color.text_primary)

        binding.btnTabSchedules.backgroundTintList =
            android.content.res.ColorStateList.valueOf(if (currentTab == "SCHEDULES") selectedBg else defaultBg)
        binding.btnTabSchedules.setTextColor(if (currentTab == "SCHEDULES") selectedText else defaultText)

        binding.btnTabStaffList.backgroundTintList =
            android.content.res.ColorStateList.valueOf(if (currentTab == "STAFF") selectedBg else defaultBg)
        binding.btnTabStaffList.setTextColor(if (currentTab == "STAFF") selectedText else defaultText)

        binding.btnTabMyAttendance.backgroundTintList =
            android.content.res.ColorStateList.valueOf(if (currentTab == "ATTENDANCE") selectedBg else defaultBg)
        binding.btnTabMyAttendance.setTextColor(if (currentTab == "ATTENDANCE") selectedText else defaultText)

        when (currentTab) {
            "SCHEDULES" -> {
                binding.tvStaffSectionTitle.text = "LỊCH TRỰC PHÒNG KHÁM"
                binding.rvStaffSchedules.visibility = View.VISIBLE
                binding.rvStaffMembers.visibility = View.GONE
                binding.rvMyAttendance.visibility = View.GONE
            }
            "STAFF" -> {
                binding.tvStaffSectionTitle.text = "ĐỘI NGŨ NHÂN SỰ & Y BÁC SĨ"
                binding.rvStaffSchedules.visibility = View.GONE
                binding.rvStaffMembers.visibility = View.VISIBLE
                binding.rvMyAttendance.visibility = View.GONE
            }
            "ATTENDANCE" -> {
                binding.tvStaffSectionTitle.text = "LỊCH SỬ CHẤM CÔNG CỦA TÔI"
                binding.rvStaffSchedules.visibility = View.GONE
                binding.rvStaffMembers.visibility = View.GONE
                binding.rvMyAttendance.visibility = View.VISIBLE
            }
        }
    }

    private fun setupSearch() {
        binding.etSearchStaff.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                applyFilterAndSearch()
            }
            override fun afterTextChanged(s: Editable?) {}
        })
    }

    private fun setupAttendanceButtons() {
        val session = RetrofitClient.getSessionManager(this)
        val user = session.getUser()
        val userId = user?.id ?: 1L

        binding.btnStaffCheckIn.setOnClickListener {
            val api = RetrofitClient.getApiService(this)
            lifecycleScope.launch {
                try {
                    val res = api.staffCheckIn(actorUserId = userId)
                    if (res.isSuccessful) {
                        Toast.makeText(this@StaffSchedulingActivity, "Điểm danh vào ca trực thành công!", Toast.LENGTH_SHORT).show()
                        binding.tvAttendanceStatusInfo.text = "Đã vào ca trực lúc ${java.time.LocalTime.now().toString().take(5)}"
                        loadStaffData()
                    } else {
                        Toast.makeText(this@StaffSchedulingActivity, "Không thể điểm danh vào ca", Toast.LENGTH_SHORT).show()
                    }
                } catch (e: Exception) {
                    Toast.makeText(this@StaffSchedulingActivity, "Lỗi: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
                }
            }
        }

        binding.btnStaffCheckOut.setOnClickListener {
            val api = RetrofitClient.getApiService(this)
            lifecycleScope.launch {
                try {
                    val res = api.staffCheckOut(actorUserId = userId)
                    if (res.isSuccessful) {
                        Toast.makeText(this@StaffSchedulingActivity, "Đã hoàn thành và kết thúc ca trực!", Toast.LENGTH_SHORT).show()
                        binding.tvAttendanceStatusInfo.text = "Đã kết thúc ca trực"
                        loadStaffData()
                    } else {
                        Toast.makeText(this@StaffSchedulingActivity, "Không thể kết thúc ca trực", Toast.LENGTH_SHORT).show()
                    }
                } catch (e: Exception) {
                    Toast.makeText(this@StaffSchedulingActivity, "Lỗi: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun loadStaffData() {
        val session = RetrofitClient.getSessionManager(this)
        val userId = session.getUser()?.id ?: 1L
        val api = RetrofitClient.getApiService(this)
        binding.swipeRefreshStaff.isRefreshing = true

        lifecycleScope.launch {
            try {
                // 1. Tải lịch trực
                val schRes = api.getStaffSchedules()
                if (schRes.isSuccessful) {
                    allSchedules = schRes.body() ?: emptyList()
                }

                // 2. Tải danh sách nhân sự
                val staffRes = api.getAllStaff()
                if (staffRes.isSuccessful) {
                    allStaff = staffRes.body() ?: emptyList()
                }

                // 3. Tải lịch sử chấm công
                val attRes = api.getMyAttendance(actorUserId = userId)
                if (attRes.isSuccessful) {
                    allAttendance = attRes.body() ?: emptyList()
                }

                applyFilterAndSearch()
            } catch (e: Exception) {
                Toast.makeText(this@StaffSchedulingActivity, "Lỗi tải dữ liệu: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
            } finally {
                binding.swipeRefreshStaff.isRefreshing = false
            }
        }
    }

    private fun applyFilterAndSearch() {
        val query = binding.etSearchStaff.text.toString().trim().lowercase()

        when (currentTab) {
            "SCHEDULES" -> {
                val filtered = allSchedules.filter { sch ->
                    if (query.isEmpty()) true else {
                        val sName = sch.staffName?.lowercase() ?: ""
                        val sShift = sch.shiftName?.lowercase() ?: ""
                        val sDept = sch.departmentName?.lowercase() ?: ""
                        sName.contains(query) || sShift.contains(query) || sDept.contains(query)
                    }
                }
                scheduleAdapter.submitList(filtered)
            }
            "STAFF" -> {
                val filtered = allStaff.filter { st ->
                    if (query.isEmpty()) true else {
                        val name = st.fullName?.lowercase() ?: ""
                        val code = st.staffCode?.lowercase() ?: ""
                        val pos = st.position?.lowercase() ?: ""
                        val dept = st.departmentName?.lowercase() ?: ""
                        name.contains(query) || code.contains(query) || pos.contains(query) || dept.contains(query)
                    }
                }
                staffAdapter.submitList(filtered)
            }
            "ATTENDANCE" -> {
                val filtered = allAttendance.filter { att ->
                    if (query.isEmpty()) true else {
                        val date = att.workDate?.lowercase() ?: ""
                        val status = att.status?.lowercase() ?: ""
                        date.contains(query) || status.contains(query)
                    }
                }
                attendanceAdapter.submitList(filtered)
            }
        }
    }
}
