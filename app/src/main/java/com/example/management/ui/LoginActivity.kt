package com.example.management.ui

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.management.api.RetrofitClient
import com.example.management.data.model.LoginRequest
import com.example.management.databinding.ActivityLoginBinding
import kotlinx.coroutines.launch

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        RetrofitClient.init(this)

        val session = RetrofitClient.getSessionManager(this)

        // Nếu đã đăng nhập trước đó, chuyển thẳng vào MainActivity
        if (session.isLoggedIn()) {
            startActivity(Intent(this, MainActivity::class.java))
            finish()
            return
        }

        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        updateServerText()

        binding.btnLogin.setOnClickListener {
            handleLogin()
        }

        binding.btnChangeServer.setOnClickListener {
            showChangeServerDialog()
        }
    }

    private fun updateServerText() {
        val session = RetrofitClient.getSessionManager(this)
        binding.tvCurrentServer.text = "Máy chủ: ${session.baseUrl}"
    }

    private fun showChangeServerDialog() {
        val session = RetrofitClient.getSessionManager(this)
        val input = EditText(this).apply {
            setText(session.baseUrl)
            setSelection(text.length)
        }

        AlertDialog.Builder(this)
            .setTitle("Cấu hình IP Backend")
            .setMessage("Nhập địa chỉ máy chủ backend:\n- Máy ảo: http://10.0.2.2:8080/\n- Điện thoại thật: http://<IP_MÁY_TÍNH>:8080/")
            .setView(input)
            .setPositiveButton("Lưu") { _, _ ->
                val newUrl = input.text.toString().trim()
                if (newUrl.isNotEmpty()) {
                    session.baseUrl = newUrl
                    RetrofitClient.resetService()
                    updateServerText()
                    Toast.makeText(this, "Đã cập nhật máy chủ!", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Hủy", null)
            .show()
    }

    private fun handleLogin() {
        val username = binding.etUsername.text.toString().trim()
        val password = binding.etPassword.text.toString().trim()

        if (username.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Vui lòng nhập đầy đủ tài khoản và mật khẩu", Toast.LENGTH_SHORT).show()
            return
        }

        binding.progressBar.visibility = View.VISIBLE
        binding.btnLogin.isEnabled = false

        val api = RetrofitClient.getApiService(this)
        val session = RetrofitClient.getSessionManager(this)

        lifecycleScope.launch {
            try {
                val req = LoginRequest(username = username, password = password)
                val response = api.login(req)

                binding.progressBar.visibility = View.GONE
                binding.btnLogin.isEnabled = true

                if (response.isSuccessful && response.body() != null) {
                    val loginRes = response.body()!!
                    if (loginRes.success) {
                        session.saveAuthToken(loginRes.token)
                        session.saveUser(loginRes.user)

                        Toast.makeText(this@LoginActivity, "Đăng nhập thành công!", Toast.LENGTH_SHORT).show()
                        startActivity(Intent(this@LoginActivity, MainActivity::class.java))
                        finish()
                    } else {
                        Toast.makeText(this@LoginActivity, loginRes.message ?: "Đăng nhập thất bại", Toast.LENGTH_LONG).show()
                    }
                } else {
                    Toast.makeText(this@LoginActivity, "Sai tài khoản hoặc mật khẩu (Mã lỗi: ${response.code()})", Toast.LENGTH_LONG).show()
                }
            } catch (e: Exception) {
                binding.progressBar.visibility = View.GONE
                binding.btnLogin.isEnabled = true
                Toast.makeText(this@LoginActivity, "Không thể kết nối máy chủ: ${e.message}\nVui lòng kiểm tra lại cấu hình IP máy chủ.", Toast.LENGTH_LONG).show()
            }
        }
    }
}
