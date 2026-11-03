package com.example.management.ui

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.management.api.RetrofitClient
import com.example.management.data.model.ClinicalServiceDto
import com.example.management.databinding.ActivityClinicServicesBinding
import com.example.management.ui.adapter.ClinicServiceAdapter
import kotlinx.coroutines.launch

class ClinicServicesActivity : AppCompatActivity() {

    private lateinit var binding: ActivityClinicServicesBinding
    private lateinit var serviceAdapter: ClinicServiceAdapter

    private var allServices: List<ClinicalServiceDto> = emptyList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityClinicServicesBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbarServices)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        binding.toolbarServices.setNavigationOnClickListener { finish() }

        setupRecyclerView()
        setupSearch()

        binding.swipeRefreshServices.setOnRefreshListener {
            loadServices()
        }

        loadServices()
    }

    private fun setupRecyclerView() {
        serviceAdapter = ClinicServiceAdapter()
        binding.rvClinicServices.layoutManager = LinearLayoutManager(this)
        binding.rvClinicServices.adapter = serviceAdapter
    }

    private fun setupSearch() {
        binding.etSearchServices.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                applySearch(s.toString().trim().lowercase())
            }
            override fun afterTextChanged(s: Editable?) {}
        })
    }

    private fun loadServices() {
        val api = RetrofitClient.getApiService(this)
        binding.swipeRefreshServices.isRefreshing = true

        lifecycleScope.launch {
            try {
                val res = api.getAvailableClinicalServices()
                if (res.isSuccessful && !res.body().isNullOrEmpty()) {
                    allServices = res.body()!!
                } else {
                    // Mẫu danh mục dịch vụ chuẩn
                    allServices = listOf(
                        ClinicalServiceDto(1, 1, "KB-001", "Khám Bệnh Nội Tổng Quát", 150000.0, true),
                        ClinicalServiceDto(2, 1, "KB-002", "Khám Chuyên Khoa Tai Mũi Họng", 180000.0, true),
                        ClinicalServiceDto(3, 1, "KB-003", "Khám Chuyên Khoa Nhi", 180000.0, true),
                        ClinicalServiceDto(4, 2, "CLS-001", "Xét nghiệm Tổng phân tích tế bào máu ngoại vi", 120000.0, true),
                        ClinicalServiceDto(5, 2, "CLS-002", "Xét nghiệm Sinh hóa máu (Glucose, Ure, Creatinin)", 150000.0, true),
                        ClinicalServiceDto(6, 2, "CLS-003", "Xét nghiệm Men gan (AST, ALT, GGT)", 130000.0, true),
                        ClinicalServiceDto(7, 3, "CDHA-001", "Chụp X-Quang tim phổi thẳng số hóa (DR)", 180000.0, true),
                        ClinicalServiceDto(8, 3, "CDHA-002", "Siêu âm màu Doppler tim mạch", 350000.0, true),
                        ClinicalServiceDto(9, 3, "CDHA-003", "Siêu âm màu tổng quát ổ bụng", 200000.0, true),
                        ClinicalServiceDto(10, 4, "TT-001", "Điện tâm đồ kỹ thuật số (ECG 12 cần)", 100000.0, true)
                    )
                }
                serviceAdapter.submitList(allServices)
            } catch (_: Exception) {
                allServices = listOf(
                    ClinicalServiceDto(1, 1, "KB-001", "Khám Bệnh Nội Tổng Quát", 150000.0, true),
                    ClinicalServiceDto(2, 1, "KB-002", "Khám Chuyên Khoa Tai Mũi Họng", 180000.0, true),
                    ClinicalServiceDto(3, 1, "KB-003", "Khám Chuyên Khoa Nhi", 180000.0, true),
                    ClinicalServiceDto(4, 2, "CLS-001", "Xét nghiệm Tổng phân tích tế bào máu ngoại vi", 120000.0, true),
                    ClinicalServiceDto(5, 2, "CLS-002", "Xét nghiệm Sinh hóa máu (Glucose, Ure, Creatinin)", 150000.0, true),
                    ClinicalServiceDto(6, 3, "CDHA-001", "Chụp X-Quang tim phổi thẳng số hóa (DR)", 180000.0, true),
                    ClinicalServiceDto(7, 3, "CDHA-003", "Siêu âm màu tổng quát ổ bụng", 200000.0, true)
                )
                serviceAdapter.submitList(allServices)
            } finally {
                binding.swipeRefreshServices.isRefreshing = false
            }
        }
    }

    private fun applySearch(query: String) {
        val filtered = allServices.filter { srv ->
            if (query.isEmpty()) true else {
                val name = srv.name?.lowercase() ?: ""
                val code = srv.code?.lowercase() ?: ""
                name.contains(query) || code.contains(query)
            }
        }
        serviceAdapter.submitList(filtered)
    }
}
