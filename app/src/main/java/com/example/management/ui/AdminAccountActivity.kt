package com.example.management.ui

import android.app.AlertDialog
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.management.R
import com.example.management.api.RetrofitClient
import com.example.management.data.model.CreateUserAccountRequest
import com.example.management.data.model.ToggleAccountStatusRequest
import com.example.management.data.model.UserAccountDto
import com.example.management.databinding.ActivityAdminAccountBinding
import com.example.management.ui.adapter.AdminAccountAdapter
import kotlinx.coroutines.launch

class AdminAccountActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAdminAccountBinding
    private lateinit var accountAdapter: AdminAccountAdapter

    private var allAccounts: List<UserAccountDto> = emptyList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAdminAccountBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbarAdmin)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        binding.toolbarAdmin.setNavigationOnClickListener { finish() }

        setupRecyclerView()
        setupSearch()

        binding.btnOpenCreateAccount.setOnClickListener {
            showCreateAccountDialog()
        }

        binding.swipeRefreshAdmin.setOnRefreshListener {
            loadAccounts()
        }

        loadAccounts()
    }

    private fun setupRecyclerView() {
        accountAdapter = AdminAccountAdapter { account ->
            showToggleStatusDialog(account)
        }
        binding.rvAdminAccounts.layoutManager = LinearLayoutManager(this)
        binding.rvAdminAccounts.adapter = accountAdapter
    }

    private fun setupSearch() {
        binding.etSearchAccount.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                applySearch(s.toString().trim().lowercase())
            }
            override fun afterTextChanged(s: Editable?) {}
        })
    }

    private fun loadAccounts() {
        val session = RetrofitClient.getSessionManager(this)
        val adminId = session.getUser()?.id ?: 1L
        val api = RetrofitClient.getApiService(this)
        binding.swipeRefreshAdmin.isRefreshing = true

        lifecycleScope.launch {
            try {
                val res = api.getAllAccounts(requesterId = adminId)
                if (res.isSuccessful) {
                    allAccounts = res.body() ?: emptyList()
                    accountAdapter.submitList(allAccounts)
                } else {
                    Toast.makeText(this@AdminAccountActivity, "Không thể tải danh sách tài khoản", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(this@AdminAccountActivity, "Lỗi: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
            } finally {
                binding.swipeRefreshAdmin.isRefreshing = false
            }
        }
    }

    private fun applySearch(query: String) {
        val filtered = allAccounts.filter { acc ->
            if (query.isEmpty()) true else {
                val u = acc.username.lowercase()
                val f = acc.fullName?.lowercase() ?: ""
                val r = acc.roles.joinToString(" ").lowercase()
                val p = acc.phone ?: ""
                u.contains(query) || f.contains(query) || r.contains(query) || p.contains(query)
            }
        }
        accountAdapter.submitList(filtered)
    }

    private fun showCreateAccountDialog() {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_create_account, null)
        val etUsername = dialogView.findViewById<EditText>(R.id.etNewUsername)
        val etPassword = dialogView.findViewById<EditText>(R.id.etNewPassword)
        val etFullName = dialogView.findViewById<EditText>(R.id.etNewFullName)
        val etEmail = dialogView.findViewById<EditText>(R.id.etNewEmail)
        val etPhone = dialogView.findViewById<EditText>(R.id.etNewPhone)
        val spRole = dialogView.findViewById<Spinner>(R.id.spNewRole)
        val btnCancel = dialogView.findViewById<Button>(R.id.btnCancelNewAccount)
        val btnSave = dialogView.findViewById<Button>(R.id.btnSaveNewAccount)

        val rolesList = listOf("DOCTOR", "NURSE", "PHARMACIST", "CASHIER", "TECHNICIAN", "RECEPTIONIST", "ADMIN", "PATIENT")
        val spinnerAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, rolesList)
        spRole.adapter = spinnerAdapter

        val dialog = AlertDialog.Builder(this)
            .setView(dialogView)
            .setCancelable(false)
            .create()

        btnCancel.setOnClickListener { dialog.dismiss() }

        btnSave.setOnClickListener {
            val u = etUsername.text.toString().trim()
            val p = etPassword.text.toString().trim()
            val f = etFullName.text.toString().trim()
            val email = etEmail.text.toString().trim()
            val phone = etPhone.text.toString().trim()
            val selectedRole = spRole.selectedItem.toString()

            if (u.isEmpty() || p.isEmpty() || f.isEmpty()) {
                Toast.makeText(this, "Vui lòng điền đủ Username, Password và Họ tên", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            dialog.dismiss()
            executeCreateAccount(u, p, f, email, phone, selectedRole)
        }

        dialog.show()
    }

    private fun executeCreateAccount(u: String, p: String, f: String, email: String, phone: String, role: String) {
        val session = RetrofitClient.getSessionManager(this)
        val adminId = session.getUser()?.id ?: 1L
        val api = RetrofitClient.getApiService(this)
        binding.swipeRefreshAdmin.isRefreshing = true

        lifecycleScope.launch {
            try {
                val req = CreateUserAccountRequest(
                    username = u,
                    password = p,
                    fullName = f,
                    email = if (email.isNotBlank()) email else null,
                    phone = if (phone.isNotBlank()) phone else null,
                    roles = listOf(role)
                )
                val res = api.createAccount(req, adminId)
                if (res.isSuccessful) {
                    Toast.makeText(this@AdminAccountActivity, "Đã tạo tài khoản @$u thành công!", Toast.LENGTH_LONG).show()
                    loadAccounts()
                } else {
                    Toast.makeText(this@AdminAccountActivity, "Lỗi tạo tài khoản", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(this@AdminAccountActivity, "Lỗi kết nối: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
            } finally {
                binding.swipeRefreshAdmin.isRefreshing = false
            }
        }
    }

    private fun showToggleStatusDialog(account: UserAccountDto) {
        val targetStatus = !account.isActive
        val actionText = if (targetStatus) "Mở khóa / Kích hoạt lại" else "Khóa tài khoản"

        AlertDialog.Builder(this)
            .setTitle(actionText)
            .setMessage("Bạn có chắc chắn muốn $actionText tài khoản @${account.username} (${account.fullName ?: ""})?")
            .setPositiveButton("Xác nhận") { _, _ ->
                executeToggleStatus(account.id, targetStatus)
            }
            .setNegativeButton("Hủy", null)
            .show()
    }

    private fun executeToggleStatus(accountId: Long, newStatus: Boolean) {
        val session = RetrofitClient.getSessionManager(this)
        val adminId = session.getUser()?.id ?: 1L
        val api = RetrofitClient.getApiService(this)
        binding.swipeRefreshAdmin.isRefreshing = true

        lifecycleScope.launch {
            try {
                val req = ToggleAccountStatusRequest(isActive = newStatus, reason = "Thay đổi bởi Quản trị viên")
                val res = api.toggleAccountStatus(accountId, req, adminId)
                if (res.isSuccessful) {
                    val statusText = if (newStatus) "được kích hoạt" else "bị khóa"
                    Toast.makeText(this@AdminAccountActivity, "Tài khoản đã $statusText", Toast.LENGTH_SHORT).show()
                    loadAccounts()
                } else {
                    Toast.makeText(this@AdminAccountActivity, "Không thể cập nhật trạng thái tài khoản", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(this@AdminAccountActivity, "Lỗi kết nối: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
            } finally {
                binding.swipeRefreshAdmin.isRefreshing = false
            }
        }
    }
}
