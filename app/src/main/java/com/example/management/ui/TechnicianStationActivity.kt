package com.example.management.ui

import android.app.AlertDialog
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.management.R
import com.example.management.api.RetrofitClient
import com.example.management.data.model.LabOrderSummaryDto
import com.example.management.data.model.LabResultItemDto
import com.example.management.data.model.UpdateLabResultRequest
import com.example.management.databinding.ActivityTechnicianStationBinding
import com.example.management.ui.adapter.LabOrderAdapter
import kotlinx.coroutines.launch

class TechnicianStationActivity : AppCompatActivity() {

    private lateinit var binding: ActivityTechnicianStationBinding
    private lateinit var adapter: LabOrderAdapter
    private var allOrders: List<LabOrderSummaryDto> = emptyList()
    private var currentFilter: String = "ALL" // ALL, PENDING, COMPLETED

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTechnicianStationBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbarTechnician)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        binding.toolbarTechnician.setNavigationOnClickListener { finish() }

        adapter = LabOrderAdapter(
            onEnterResult = { order, item ->
                showUpdateResultDialog(order, item)
            }
        )

        binding.rvLabOrders.layoutManager = LinearLayoutManager(this)
        binding.rvLabOrders.adapter = adapter

        setupFilterButtons()
        setupSearch()

        binding.swipeRefreshTech.setOnRefreshListener {
            loadLabOrders()
        }

        loadLabOrders()
    }

    private fun setupFilterButtons() {
        binding.btnFilterAll.setOnClickListener {
            currentFilter = "ALL"
            updateFilterUi()
            applyFilterAndSearch()
        }

        binding.btnFilterPending.setOnClickListener {
            currentFilter = "PENDING"
            updateFilterUi()
            applyFilterAndSearch()
        }

        binding.btnFilterCompleted.setOnClickListener {
            currentFilter = "COMPLETED"
            updateFilterUi()
            applyFilterAndSearch()
        }
    }

    private fun updateFilterUi() {
        val selectedBg = ContextCompat.getColor(this, R.color.primary)
        val defaultBg = ContextCompat.getColor(this, R.color.bg_light)
        val selectedText = ContextCompat.getColor(this, R.color.white)
        val defaultText = ContextCompat.getColor(this, R.color.text_primary)

        binding.btnFilterAll.backgroundTintList = android.content.res.ColorStateList.valueOf(if (currentFilter == "ALL") selectedBg else defaultBg)
        binding.btnFilterAll.setTextColor(if (currentFilter == "ALL") selectedText else defaultText)

        binding.btnFilterPending.backgroundTintList = android.content.res.ColorStateList.valueOf(if (currentFilter == "PENDING") selectedBg else defaultBg)
        binding.btnFilterPending.setTextColor(if (currentFilter == "PENDING") selectedText else defaultText)

        binding.btnFilterCompleted.backgroundTintList = android.content.res.ColorStateList.valueOf(if (currentFilter == "COMPLETED") selectedBg else defaultBg)
        binding.btnFilterCompleted.setTextColor(if (currentFilter == "COMPLETED") selectedText else defaultText)
    }

    private fun setupSearch() {
        binding.etSearchLab.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                applyFilterAndSearch()
            }
            override fun afterTextChanged(s: Editable?) {}
        })
    }

    private fun loadLabOrders() {
        val api = RetrofitClient.getApiService(this)
        binding.progressBarTech.visibility = View.VISIBLE

        lifecycleScope.launch {
            try {
                val res = api.getAllLabOrders()
                binding.progressBarTech.visibility = View.GONE
                binding.swipeRefreshTech.isRefreshing = false

                if (res.isSuccessful && res.body() != null) {
                    allOrders = res.body()!!

                    val pendingCount = allOrders.count { it.status != "COMPLETED" }
                    val completedCount = allOrders.count { it.status == "COMPLETED" }
                    val totalCount = allOrders.size

                    binding.tvCountPending.text = pendingCount.toString()
                    binding.tvCountCompleted.text = completedCount.toString()
                    binding.tvCountTotal.text = totalCount.toString()

                    applyFilterAndSearch()
                } else {
                    binding.tvEmptyTech.visibility = View.VISIBLE
                    binding.tvEmptyTech.text = "Không thể tải danh sách chỉ định cận lâm sàng (Mã: ${res.code()})"
                }
            } catch (e: Exception) {
                binding.progressBarTech.visibility = View.GONE
                binding.swipeRefreshTech.isRefreshing = false
                binding.tvEmptyTech.visibility = View.VISIBLE
                binding.tvEmptyTech.text = "Lỗi kết nối: ${e.message}"
            }
        }
    }

    private fun applyFilterAndSearch() {
        val query = binding.etSearchLab.text.toString().trim().lowercase()

        var filtered = when (currentFilter) {
            "PENDING" -> allOrders.filter { it.status != "COMPLETED" }
            "COMPLETED" -> allOrders.filter { it.status == "COMPLETED" }
            else -> allOrders
        }

        if (query.isNotEmpty()) {
            filtered = filtered.filter { order ->
                val codeMatch = order.orderCode?.lowercase()?.contains(query) == true
                val patientMatch = order.patientName?.lowercase()?.contains(query) == true
                val serviceMatch = order.items?.any { it.serviceName?.lowercase()?.contains(query) == true } == true
                codeMatch || patientMatch || serviceMatch
            }
        }

        adapter.submitList(filtered)
        binding.tvEmptyTech.visibility = if (filtered.isEmpty()) View.VISIBLE else View.GONE
        if (filtered.isEmpty()) {
            binding.tvEmptyTech.text = "Không có chỉ định cận lâm sàng nào phù hợp với bộ lọc"
        }
    }

    private fun showUpdateResultDialog(order: LabOrderSummaryDto, item: LabResultItemDto) {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_update_lab_result, null)

        val tvTitle = dialogView.findViewById<TextView>(R.id.tvDialogServiceTitle)
        val etValue = dialogView.findViewById<EditText>(R.id.etTestValue)
        val etUnit = dialogView.findViewById<EditText>(R.id.etUnit)
        val etRefRange = dialogView.findViewById<EditText>(R.id.etRefRange)
        val etConclusion = dialogView.findViewById<EditText>(R.id.etConclusion)
        val etImageUrl = dialogView.findViewById<EditText>(R.id.etImageUrl)
        val btnCancel = dialogView.findViewById<Button>(R.id.btnCancelDialog)
        val btnSave = dialogView.findViewById<Button>(R.id.btnSaveResultDialog)

        tvTitle.text = "${item.serviceName ?: "Xét nghiệm"} | Bệnh nhân: ${order.patientName ?: ""}"

        // Điền dữ liệu cũ nếu đã từng nhập
        if (!item.testValue.isNullOrBlank()) etValue.setText(item.testValue)
        if (!item.unit.isNullOrBlank()) etUnit.setText(item.unit)
        if (!item.referenceRange.isNullOrBlank()) etRefRange.setText(item.referenceRange)
        if (!item.conclusion.isNullOrBlank()) etConclusion.setText(item.conclusion)
        if (!item.imageAttachmentUrl.isNullOrBlank()) etImageUrl.setText(item.imageAttachmentUrl)

        val dialog = AlertDialog.Builder(this)
            .setView(dialogView)
            .setCancelable(false)
            .create()

        btnCancel.setOnClickListener {
            dialog.dismiss()
        }

        btnSave.setOnClickListener {
            val value = etValue.text.toString().trim()
            val unit = etUnit.text.toString().trim().ifEmpty { null }
            val refRange = etRefRange.text.toString().trim().ifEmpty { null }
            val conclusion = etConclusion.text.toString().trim()
            val imgUrl = etImageUrl.text.toString().trim().ifEmpty { null }

            if (value.isEmpty()) {
                etValue.error = "Vui lòng nhập giá trị đo / kết quả xét nghiệm"
                return@setOnClickListener
            }
            if (conclusion.isEmpty()) {
                etConclusion.error = "Vui lòng nhập kết luận lâm sàng (VD: Bình thường)"
                return@setOnClickListener
            }

            val session = RetrofitClient.getSessionManager(this)
            val user = session.getUser()
            val api = RetrofitClient.getApiService(this)

            val req = UpdateLabResultRequest(
                resultId = item.id,
                technicianId = user?.id,
                testValue = value,
                referenceRange = refRange,
                unit = unit,
                conclusion = conclusion,
                imageAttachmentUrl = imgUrl
            )

            btnSave.isEnabled = false
            btnSave.text = "Đang lưu..."

            lifecycleScope.launch {
                try {
                    val res = api.updateLabResult(req)
                    btnSave.isEnabled = true
                    btnSave.text = "Lưu Kết Quả"

                    if (res.isSuccessful) {
                        Toast.makeText(this@TechnicianStationActivity, "Đã lưu kết quả xét nghiệm thành công!", Toast.LENGTH_SHORT).show()
                        dialog.dismiss()
                        loadLabOrders()
                    } else {
                        Toast.makeText(this@TechnicianStationActivity, "Cập nhật thất bại: ${res.code()}", Toast.LENGTH_SHORT).show()
                    }
                } catch (e: Exception) {
                    btnSave.isEnabled = true
                    btnSave.text = "Lưu Kết Quả"
                    Toast.makeText(this@TechnicianStationActivity, "Lỗi: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }

        dialog.show()
    }
}
