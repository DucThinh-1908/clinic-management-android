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
import com.example.management.data.model.DispensePrescriptionRequest
import com.example.management.data.model.MedicineDetailDto
import com.example.management.data.model.PrescriptionResponse
import com.example.management.data.model.StockAlertItemDto
import com.example.management.data.model.StockAlertOverviewDto
import com.example.management.databinding.ActivityPharmacyStationBinding
import com.example.management.ui.adapter.MedicineStockAdapter
import com.example.management.ui.adapter.PrescriptionDispenseAdapter
import com.example.management.ui.adapter.StockAlertAdapter
import kotlinx.coroutines.launch

class PharmacyStationActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPharmacyStationBinding

    private lateinit var prescriptionAdapter: PrescriptionDispenseAdapter
    private lateinit var medicineStockAdapter: MedicineStockAdapter
    private lateinit var stockAlertAdapter: StockAlertAdapter

    private var allPrescriptions: List<PrescriptionResponse> = emptyList()
    private var allMedicines: List<MedicineDetailDto> = emptyList()
    private var allAlerts: List<StockAlertItemDto> = emptyList()

    // Tab hiện tại: PENDING, DISPENSED, STOCK, ALERTS
    private var currentTab: String = "PENDING"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPharmacyStationBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbarPharmacy)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        binding.toolbarPharmacy.setNavigationOnClickListener { finish() }

        setupRecyclerViews()
        setupTabButtons()
        setupSearch()

        binding.swipeRefreshPharmacy.setOnRefreshListener {
            loadAllPharmacyData()
        }

        loadAllPharmacyData()
    }

    private fun setupRecyclerViews() {
        prescriptionAdapter = PrescriptionDispenseAdapter(
            onDispenseClick = { prescription ->
                showConfirmDispenseDialog(prescription)
            }
        )
        binding.rvPrescriptions.layoutManager = LinearLayoutManager(this)
        binding.rvPrescriptions.adapter = prescriptionAdapter

        medicineStockAdapter = MedicineStockAdapter { medicine ->
            Toast.makeText(
                this,
                "Thuốc: ${medicine.brandName} - Tồn kho: ${medicine.totalAvailableQuantity ?: 0} ${medicine.unit ?: ""}",
                Toast.LENGTH_SHORT
            ).show()
        }
        binding.rvMedicineStock.layoutManager = LinearLayoutManager(this)
        binding.rvMedicineStock.adapter = medicineStockAdapter

        stockAlertAdapter = StockAlertAdapter()
        binding.rvStockAlerts.layoutManager = LinearLayoutManager(this)
        binding.rvStockAlerts.adapter = stockAlertAdapter
    }

    private fun setupTabButtons() {
        binding.btnFilterPending.setOnClickListener {
            currentTab = "PENDING"
            updateTabUi()
            applyFilterAndSearch()
        }

        binding.btnFilterDispensed.setOnClickListener {
            currentTab = "DISPENSED"
            updateTabUi()
            applyFilterAndSearch()
        }

        binding.btnFilterMedicineStock.setOnClickListener {
            currentTab = "STOCK"
            updateTabUi()
            applyFilterAndSearch()
        }

        binding.btnFilterStockAlerts.setOnClickListener {
            currentTab = "ALERTS"
            updateTabUi()
            applyFilterAndSearch()
        }
    }

    private fun updateTabUi() {
        val selectedBg = ContextCompat.getColor(this, R.color.primary)
        val defaultBg = ContextCompat.getColor(this, R.color.bg_light)
        val selectedText = ContextCompat.getColor(this, R.color.white)
        val defaultText = ContextCompat.getColor(this, R.color.text_primary)

        binding.btnFilterPending.backgroundTintList =
            android.content.res.ColorStateList.valueOf(if (currentTab == "PENDING") selectedBg else defaultBg)
        binding.btnFilterPending.setTextColor(if (currentTab == "PENDING") selectedText else defaultText)

        binding.btnFilterDispensed.backgroundTintList =
            android.content.res.ColorStateList.valueOf(if (currentTab == "DISPENSED") selectedBg else defaultBg)
        binding.btnFilterDispensed.setTextColor(if (currentTab == "DISPENSED") selectedText else defaultText)

        binding.btnFilterMedicineStock.backgroundTintList =
            android.content.res.ColorStateList.valueOf(if (currentTab == "STOCK") selectedBg else defaultBg)
        binding.btnFilterMedicineStock.setTextColor(if (currentTab == "STOCK") selectedText else defaultText)

        binding.btnFilterStockAlerts.backgroundTintList =
            android.content.res.ColorStateList.valueOf(if (currentTab == "ALERTS") selectedBg else defaultBg)
        binding.btnFilterStockAlerts.setTextColor(if (currentTab == "ALERTS") selectedText else defaultText)

        when (currentTab) {
            "PENDING" -> {
                binding.tvSectionTitle.text = "DANH SÁCH ĐƠN THUỐC CHỜ CẤP PHÁT"
                binding.rvPrescriptions.visibility = View.VISIBLE
                binding.rvMedicineStock.visibility = View.GONE
                binding.rvStockAlerts.visibility = View.GONE
            }
            "DISPENSED" -> {
                binding.tvSectionTitle.text = "DANH SÁCH ĐƠN THUỐC ĐÃ CẤP PHÁT"
                binding.rvPrescriptions.visibility = View.VISIBLE
                binding.rvMedicineStock.visibility = View.GONE
                binding.rvStockAlerts.visibility = View.GONE
            }
            "STOCK" -> {
                binding.tvSectionTitle.text = "DANH MỤC THUỐC & TỒN KHO KHẢ DỤNG"
                binding.rvPrescriptions.visibility = View.GONE
                binding.rvMedicineStock.visibility = View.VISIBLE
                binding.rvStockAlerts.visibility = View.GONE
            }
            "ALERTS" -> {
                binding.tvSectionTitle.text = "CẢNH BÁO TỒN KHO THẤP & CẬN HẠN DÙNG"
                binding.rvPrescriptions.visibility = View.GONE
                binding.rvMedicineStock.visibility = View.GONE
                binding.rvStockAlerts.visibility = View.VISIBLE
            }
        }
    }

    private fun setupSearch() {
        binding.etSearchPharmacy.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                applyFilterAndSearch()
            }
            override fun afterTextChanged(s: Editable?) {}
        })
    }

    private fun loadAllPharmacyData() {
        val api = RetrofitClient.getApiService(this)
        binding.swipeRefreshPharmacy.isRefreshing = true

        lifecycleScope.launch {
            try {
                // 1. Tải danh sách đơn thuốc
                val presRes = api.getPharmacyPrescriptions()
                if (presRes.isSuccessful) {
                    allPrescriptions = presRes.body() ?: emptyList()
                }

                // 2. Tải danh mục thuốc và tồn kho
                val medRes = api.getMedicineCatalog()
                if (medRes.isSuccessful) {
                    allMedicines = medRes.body() ?: emptyList()
                }

                // 3. Tải cảnh báo tồn kho
                val alertRes = api.getStockAlerts()
                if (alertRes.isSuccessful) {
                    val alertDto = alertRes.body() ?: StockAlertOverviewDto()
                    allAlerts = alertDto.alerts
                    val totalAlerts = alertDto.lowStockCount + alertDto.nearExpiryCount + alertDto.expiredCount
                    binding.tvCountStockAlerts.text = totalAlerts.toString()
                }

                // Cập nhật thống kê KPI đơn thuốc
                val pendingCount = allPrescriptions.count { it.status == "PENDING" }
                val dispensedCount = allPrescriptions.count { it.status == "DISPENSED" }
                binding.tvCountPrescriptionPending.text = pendingCount.toString()
                binding.tvCountPrescriptionDispensed.text = dispensedCount.toString()

                applyFilterAndSearch()
            } catch (e: Exception) {
                Toast.makeText(
                    this@PharmacyStationActivity,
                    "Không thể tải dữ liệu dược phẩm: ${e.localizedMessage}",
                    Toast.LENGTH_LONG
                ).show()
            } finally {
                binding.swipeRefreshPharmacy.isRefreshing = false
            }
        }
    }

    private fun applyFilterAndSearch() {
        val query = binding.etSearchPharmacy.text.toString().trim().lowercase()

        when (currentTab) {
            "PENDING" -> {
                val filtered = allPrescriptions.filter { it.status == "PENDING" }.filter { pres ->
                    matchesPrescriptionQuery(pres, query)
                }
                prescriptionAdapter.submitList(filtered)
                toggleEmptyState(filtered.isEmpty(), "Không có đơn thuốc chờ cấp phát", "Tất cả đơn thuốc mới đã được cấp phát")
            }
            "DISPENSED" -> {
                val filtered = allPrescriptions.filter { it.status == "DISPENSED" }.filter { pres ->
                    matchesPrescriptionQuery(pres, query)
                }
                prescriptionAdapter.submitList(filtered)
                toggleEmptyState(filtered.isEmpty(), "Chưa có đơn thuốc nào được cấp phát", "Các đơn sau khi hoàn tất cấp phát sẽ hiển thị tại đây")
            }
            "STOCK" -> {
                val filtered = allMedicines.filter { med ->
                    val brand = med.brandName?.lowercase() ?: ""
                    val code = med.code?.lowercase() ?: ""
                    val active = med.activeIngredient?.lowercase() ?: ""
                    val cat = med.categoryName?.lowercase() ?: ""
                    query.isEmpty() || brand.contains(query) || code.contains(query) || active.contains(query) || cat.contains(query)
                }
                medicineStockAdapter.submitList(filtered)
                toggleEmptyState(filtered.isEmpty(), "Không tìm thấy thuốc phù hợp", "Vui lòng thử tìm kiếm theo từ khóa hoặc mã thuốc khác")
            }
            "ALERTS" -> {
                val filtered = allAlerts.filter { alert ->
                    val name = alert.brandName?.lowercase() ?: ""
                    val code = alert.medicineCode?.lowercase() ?: ""
                    val batch = alert.batchNumber?.lowercase() ?: ""
                    query.isEmpty() || name.contains(query) || code.contains(query) || batch.contains(query)
                }
                stockAlertAdapter.submitList(filtered)
                toggleEmptyState(filtered.isEmpty(), "Kho dược an toàn", "Hiện tại không có thuốc nào chạm ngưỡng tồn thấp hoặc cận hạn dùng")
            }
        }
    }

    private fun matchesPrescriptionQuery(pres: PrescriptionResponse, query: String): Boolean {
        if (query.isEmpty()) return true
        val code = pres.prescriptionCode?.lowercase() ?: ""
        val patientName = pres.patientName?.lowercase() ?: ""
        val patientCode = pres.patientCode?.lowercase() ?: ""
        val phone = pres.patientPhone?.lowercase() ?: ""
        val doctor = pres.doctorName?.lowercase() ?: ""
        val diagnosis = pres.diagnosis?.lowercase() ?: ""
        val hasMatchingMedicine = pres.items?.any {
            (it.brandName?.lowercase() ?: "").contains(query) ||
            (it.medicineCode?.lowercase() ?: "").contains(query)
        } == true

        return code.contains(query) || patientName.contains(query) ||
               patientCode.contains(query) || phone.contains(query) ||
               doctor.contains(query) || diagnosis.contains(query) || hasMatchingMedicine
    }

    private fun toggleEmptyState(isEmpty: Boolean, title: String, sub: String) {
        if (isEmpty) {
            binding.llEmptyPharmacy.visibility = View.VISIBLE
            binding.tvEmptyTitle.text = title
            binding.tvEmptySub.text = sub
        } else {
            binding.llEmptyPharmacy.visibility = View.GONE
        }
    }

    private fun showConfirmDispenseDialog(prescription: PrescriptionResponse) {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_dispense_prescription, null)
        val tvSubtitle = dialogView.findViewById<TextView>(R.id.tvDialogSubtitle)
        val etNote = dialogView.findViewById<EditText>(R.id.etDispenseNote)
        val btnCancel = dialogView.findViewById<Button>(R.id.btnCancelDispense)
        val btnConfirm = dialogView.findViewById<Button>(R.id.btnConfirmDispense)

        val pCode = prescription.prescriptionCode ?: "DT#${prescription.id}"
        val pName = prescription.patientName ?: "Bệnh nhân"
        tvSubtitle.text = "Mã đơn: $pCode | Bệnh nhân: $pName"

        val dialog = AlertDialog.Builder(this)
            .setView(dialogView)
            .setCancelable(false)
            .create()

        btnCancel.setOnClickListener {
            dialog.dismiss()
        }

        btnConfirm.setOnClickListener {
            val note = etNote.text.toString().trim()
            dialog.dismiss()
            executeDispensation(prescription.id, pCode, note)
        }

        dialog.show()
    }

    private fun executeDispensation(prescriptionId: Long, prescriptionCode: String, note: String) {
        val session = RetrofitClient.getSessionManager(this)
        val user = session.getUser()
        val pharmacistId = user?.id

        val api = RetrofitClient.getApiService(this)
        binding.swipeRefreshPharmacy.isRefreshing = true

        lifecycleScope.launch {
            try {
                val req = DispensePrescriptionRequest(
                    prescriptionId = prescriptionId,
                    pharmacistId = pharmacistId,
                    note = if (note.isNotBlank()) note else null
                )

                val res = api.dispensePrescription(req)
                if (res.isSuccessful && res.body() != null) {
                    val disp = res.body()!!
                    Toast.makeText(
                        this@PharmacyStationActivity,
                        "Cấp phát thành công đơn thuốc $prescriptionCode! Mã phiếu: ${disp.dispensationCode ?: ""}",
                        Toast.LENGTH_LONG
                    ).show()
                    loadAllPharmacyData()
                } else {
                    val err = res.errorBody()?.string() ?: "Lỗi cấp phát đơn thuốc"
                    Toast.makeText(
                        this@PharmacyStationActivity,
                        "Cấp phát thất bại: $err",
                        Toast.LENGTH_LONG
                    ).show()
                }
            } catch (e: Exception) {
                Toast.makeText(
                    this@PharmacyStationActivity,
                    "Lỗi kết nối cấp phát thuốc: ${e.localizedMessage}",
                    Toast.LENGTH_LONG
                ).show()
            } finally {
                binding.swipeRefreshPharmacy.isRefreshing = false
            }
        }
    }
}
