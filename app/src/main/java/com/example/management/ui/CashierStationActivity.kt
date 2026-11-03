package com.example.management.ui

import android.app.AlertDialog
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.management.R
import com.example.management.api.RetrofitClient
import com.example.management.data.model.*
import com.example.management.databinding.ActivityCashierStationBinding
import com.example.management.ui.adapter.InvoiceAdapter
import com.example.management.ui.adapter.MedicalCandidateAdapter
import kotlinx.coroutines.launch
import java.text.DecimalFormat

class CashierStationActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCashierStationBinding

    private lateinit var invoiceAdapter: InvoiceAdapter
    private lateinit var candidateAdapter: MedicalCandidateAdapter

    private var allInvoices: List<InvoiceDetailDto> = emptyList()
    private var allCandidates: List<MedicalCandidateDto> = emptyList()
    private val currencyFormat = DecimalFormat("#,###")

    // Tab hiện tại: UNPAID, CANDIDATES, PAID, ALL
    private var currentTab: String = "UNPAID"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCashierStationBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbarCashier)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        binding.toolbarCashier.setNavigationOnClickListener { finish() }

        setupRecyclerViews()
        setupTabButtons()
        setupSearch()

        binding.swipeRefreshCashier.setOnRefreshListener {
            loadAllBillingData()
        }

        loadAllBillingData()
    }

    private fun setupRecyclerViews() {
        invoiceAdapter = InvoiceAdapter(
            onProcessPayment = { invoice ->
                showProcessPaymentDialog(invoice)
            },
            onApplyDiscount = { invoice ->
                showApplyDiscountDialog(invoice)
            },
            onRefundInvoice = { invoice ->
                showRefundInvoiceDialog(invoice)
            }
        )
        binding.rvCashierInvoices.layoutManager = LinearLayoutManager(this)
        binding.rvCashierInvoices.adapter = invoiceAdapter

        candidateAdapter = MedicalCandidateAdapter { candidate ->
            showConfirmCreateInvoiceDialog(candidate)
        }
        binding.rvMedicalCandidates.layoutManager = LinearLayoutManager(this)
        binding.rvMedicalCandidates.adapter = candidateAdapter
    }

    private fun setupTabButtons() {
        binding.btnTabUnpaid.setOnClickListener {
            currentTab = "UNPAID"
            updateTabUi()
            applyFilterAndSearch()
        }

        binding.btnTabCandidates.setOnClickListener {
            currentTab = "CANDIDATES"
            updateTabUi()
            applyFilterAndSearch()
        }

        binding.btnTabPaid.setOnClickListener {
            currentTab = "PAID"
            updateTabUi()
            applyFilterAndSearch()
        }

        binding.btnTabAll.setOnClickListener {
            currentTab = "ALL"
            updateTabUi()
            applyFilterAndSearch()
        }
    }

    private fun updateTabUi() {
        val selectedBg = ContextCompat.getColor(this, R.color.primary)
        val defaultBg = ContextCompat.getColor(this, R.color.bg_light)
        val selectedText = ContextCompat.getColor(this, R.color.white)
        val defaultText = ContextCompat.getColor(this, R.color.text_primary)

        binding.btnTabUnpaid.backgroundTintList =
            android.content.res.ColorStateList.valueOf(if (currentTab == "UNPAID") selectedBg else defaultBg)
        binding.btnTabUnpaid.setTextColor(if (currentTab == "UNPAID") selectedText else defaultText)

        binding.btnTabCandidates.backgroundTintList =
            android.content.res.ColorStateList.valueOf(if (currentTab == "CANDIDATES") selectedBg else defaultBg)
        binding.btnTabCandidates.setTextColor(if (currentTab == "CANDIDATES") selectedText else defaultText)

        binding.btnTabPaid.backgroundTintList =
            android.content.res.ColorStateList.valueOf(if (currentTab == "PAID") selectedBg else defaultBg)
        binding.btnTabPaid.setTextColor(if (currentTab == "PAID") selectedText else defaultText)

        binding.btnTabAll.backgroundTintList =
            android.content.res.ColorStateList.valueOf(if (currentTab == "ALL") selectedBg else defaultBg)
        binding.btnTabAll.setTextColor(if (currentTab == "ALL") selectedText else defaultText)

        when (currentTab) {
            "UNPAID" -> {
                binding.tvCashierSectionTitle.text = "DANH SÁCH HÓA ĐƠN CHỜ THU TIỀN"
                binding.rvCashierInvoices.visibility = View.VISIBLE
                binding.rvMedicalCandidates.visibility = View.GONE
            }
            "CANDIDATES" -> {
                binding.tvCashierSectionTitle.text = "DANH SÁCH CA KHÁM CHỜ LẬP HÓA ĐƠN"
                binding.rvCashierInvoices.visibility = View.GONE
                binding.rvMedicalCandidates.visibility = View.VISIBLE
            }
            "PAID" -> {
                binding.tvCashierSectionTitle.text = "DANH SÁCH HÓA ĐƠN ĐÃ THANH TOÁN"
                binding.rvCashierInvoices.visibility = View.VISIBLE
                binding.rvMedicalCandidates.visibility = View.GONE
            }
            "ALL" -> {
                binding.tvCashierSectionTitle.text = "TẤT CẢ LỊCH SỬ VIỆN PHÍ"
                binding.rvCashierInvoices.visibility = View.VISIBLE
                binding.rvMedicalCandidates.visibility = View.GONE
            }
        }
    }

    private fun setupSearch() {
        binding.etSearchCashier.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                applyFilterAndSearch()
            }
            override fun afterTextChanged(s: Editable?) {}
        })
    }

    private fun loadAllBillingData() {
        val api = RetrofitClient.getApiService(this)
        binding.swipeRefreshCashier.isRefreshing = true

        lifecycleScope.launch {
            try {
                // 1. Tải danh sách tất cả hóa đơn
                val invRes = api.getInvoices()
                if (invRes.isSuccessful) {
                    allInvoices = invRes.body() ?: emptyList()
                }

                // 2. Tải danh sách ca khám chờ lập hóa đơn
                val candRes = api.getBillingCandidates()
                if (candRes.isSuccessful) {
                    allCandidates = candRes.body() ?: emptyList()
                }

                // 3. Tải thống kê viện phí
                val statsRes = api.getBillingStats()
                if (statsRes.isSuccessful && statsRes.body() != null) {
                    val stats = statsRes.body()!!
                    binding.tvRevenueToday.text = "${currencyFormat.format(stats.totalRevenue)} đ"
                    binding.tvCountInvoiceUnpaid.text = stats.unpaidInvoiceCount.toString()
                    binding.tvCountInvoicePaid.text = stats.paidInvoiceCount.toString()
                } else {
                    val unpaid = allInvoices.count { it.status == "UNPAID" }
                    val paid = allInvoices.count { it.status == "PAID" }
                    val revenue = allInvoices.filter { it.status == "PAID" }.sumOf { it.finalAmount }
                    binding.tvRevenueToday.text = "${currencyFormat.format(revenue)} đ"
                    binding.tvCountInvoiceUnpaid.text = unpaid.toString()
                    binding.tvCountInvoicePaid.text = paid.toString()
                }

                applyFilterAndSearch()
            } catch (e: Exception) {
                Toast.makeText(
                    this@CashierStationActivity,
                    "Lỗi tải dữ liệu viện phí: ${e.localizedMessage}",
                    Toast.LENGTH_LONG
                ).show()
            } finally {
                binding.swipeRefreshCashier.isRefreshing = false
            }
        }
    }

    private fun applyFilterAndSearch() {
        val query = binding.etSearchCashier.text.toString().trim().lowercase()

        when (currentTab) {
            "UNPAID" -> {
                val filtered = allInvoices.filter { it.status == "UNPAID" }.filter { matchesInvoiceQuery(it, query) }
                invoiceAdapter.submitList(filtered)
                toggleEmptyState(filtered.isEmpty(), "Không có hóa đơn chờ thu tiền", "Tất cả các khoản phí hiện tại đã được thanh toán")
            }
            "CANDIDATES" -> {
                val filtered = allCandidates.filter { cand ->
                    if (query.isEmpty()) true else {
                        val name = cand.patientName?.lowercase() ?: ""
                        val pCode = cand.patientCode?.lowercase() ?: ""
                        val rCode = cand.recordCode?.lowercase() ?: ""
                        val phone = cand.patientPhone?.lowercase() ?: ""
                        val doc = cand.doctorName?.lowercase() ?: ""
                        name.contains(query) || pCode.contains(query) || rCode.contains(query) || phone.contains(query) || doc.contains(query)
                    }
                }
                candidateAdapter.submitList(filtered)
                toggleEmptyState(filtered.isEmpty(), "Không có ca khám chờ lập hóa đơn", "Tất cả hồ sơ khám đã hoàn tất việc tạo hóa đơn viện phí")
            }
            "PAID" -> {
                val filtered = allInvoices.filter { it.status == "PAID" }.filter { matchesInvoiceQuery(it, query) }
                invoiceAdapter.submitList(filtered)
                toggleEmptyState(filtered.isEmpty(), "Chưa có hóa đơn đã thanh toán", "Các giao dịch đã thu tiền sẽ hiển thị tại mục này")
            }
            "ALL" -> {
                val filtered = allInvoices.filter { matchesInvoiceQuery(it, query) }
                invoiceAdapter.submitList(filtered)
                toggleEmptyState(filtered.isEmpty(), "Không có dữ liệu viện phí", "Không tìm thấy hóa đơn nào phù hợp với điều kiện tìm kiếm")
            }
        }
    }

    private fun matchesInvoiceQuery(inv: InvoiceDetailDto, query: String): Boolean {
        if (query.isEmpty()) return true
        val code = inv.invoiceCode?.lowercase() ?: ""
        val patientName = inv.patientName?.lowercase() ?: ""
        val patientCode = inv.patientCode?.lowercase() ?: ""
        val phone = inv.patientPhone?.lowercase() ?: ""
        val rCode = inv.recordCode?.lowercase() ?: ""
        val doctor = inv.doctorName?.lowercase() ?: ""
        val insurance = inv.healthInsuranceNumber?.lowercase() ?: ""

        return code.contains(query) || patientName.contains(query) ||
                patientCode.contains(query) || phone.contains(query) ||
                rCode.contains(query) || doctor.contains(query) || insurance.contains(query)
    }

    private fun toggleEmptyState(isEmpty: Boolean, title: String, sub: String) {
        if (isEmpty) {
            binding.llEmptyCashier.visibility = View.VISIBLE
            binding.tvEmptyCashierTitle.text = title
            binding.tvEmptyCashierSub.text = sub
        } else {
            binding.llEmptyCashier.visibility = View.GONE
        }
    }

    // =========================================================================
    // 1. LẬP HÓA ĐƠN TỪ CA KHÁM
    // =========================================================================
    private fun showConfirmCreateInvoiceDialog(candidate: MedicalCandidateDto) {
        val pName = candidate.patientName ?: "Bệnh nhân"
        val rCode = candidate.recordCode ?: "BA#${candidate.medicalRecordId}"

        AlertDialog.Builder(this)
            .setTitle("Lập hóa đơn viện phí")
            .setMessage("Xác nhận tạo hóa đơn viện phí cho bệnh nhân $pName (Hồ sơ khám: $rCode)? Hệ thống sẽ tự động tổng hợp tiền công khám, cận lâm sàng và tiền thuốc.")
            .setPositiveButton("Lập hóa đơn") { _, _ ->
                executeCreateInvoice(candidate.medicalRecordId)
            }
            .setNegativeButton("Hủy", null)
            .show()
    }

    private fun executeCreateInvoice(medicalRecordId: Long) {
        val session = RetrofitClient.getSessionManager(this)
        val user = session.getUser()
        val cashierId = user?.id

        val api = RetrofitClient.getApiService(this)
        binding.swipeRefreshCashier.isRefreshing = true

        lifecycleScope.launch {
            try {
                val req = CreateInvoiceRequest(
                    medicalRecordId = medicalRecordId,
                    cashierId = cashierId
                )
                val res = api.createInvoiceFromRecord(req)
                if (res.isSuccessful && res.body() != null) {
                    val invoice = res.body()!!
                    Toast.makeText(
                        this@CashierStationActivity,
                        "Lập hóa đơn thành công! Mã: ${invoice.invoiceCode ?: ""}",
                        Toast.LENGTH_LONG
                    ).show()
                    currentTab = "UNPAID"
                    updateTabUi()
                    loadAllBillingData()
                } else {
                    val err = res.errorBody()?.string() ?: "Lỗi tạo hóa đơn"
                    Toast.makeText(this@CashierStationActivity, "Thất bại: $err", Toast.LENGTH_LONG).show()
                }
            } catch (e: Exception) {
                Toast.makeText(this@CashierStationActivity, "Lỗi kết nối: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
            } finally {
                binding.swipeRefreshCashier.isRefreshing = false
            }
        }
    }

    // =========================================================================
    // 2. THU TIỀN VIỆN PHÍ (PAYMENT)
    // =========================================================================
    private fun showProcessPaymentDialog(invoice: InvoiceDetailDto) {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_process_payment, null)
        val tvSubtitle = dialogView.findViewById<TextView>(R.id.tvPaymentDialogSubtitle)
        val tvAmount = dialogView.findViewById<TextView>(R.id.tvDialogPayAmount)
        val rgMethods = dialogView.findViewById<RadioGroup>(R.id.rgPaymentMethods)
        val llCash = dialogView.findViewById<LinearLayout>(R.id.llCashSection)
        val llRef = dialogView.findViewById<LinearLayout>(R.id.llRefSection)
        val etGiven = dialogView.findViewById<EditText>(R.id.etCustomerGivenAmount)
        val tvChange = dialogView.findViewById<TextView>(R.id.tvChangeAmount)
        val etRef = dialogView.findViewById<EditText>(R.id.etTransactionRef)
        val etNote = dialogView.findViewById<EditText>(R.id.etPaymentNote)
        val btnCancel = dialogView.findViewById<Button>(R.id.btnCancelPayment)
        val btnConfirm = dialogView.findViewById<Button>(R.id.btnConfirmPayment)

        val code = invoice.invoiceCode ?: "HD#${invoice.id}"
        val name = invoice.patientName ?: "Bệnh nhân"
        tvSubtitle.text = "Hóa đơn: $code | Bệnh nhân: $name"
        tvAmount.text = "${currencyFormat.format(invoice.finalAmount)} đ"

        var selectedMethod = "CASH"

        rgMethods.setOnCheckedChangeListener { _, checkedId ->
            when (checkedId) {
                R.id.rbCash -> {
                    selectedMethod = "CASH"
                    llCash.visibility = View.VISIBLE
                    llRef.visibility = View.GONE
                }
                R.id.rbBankTransfer -> {
                    selectedMethod = "BANK_TRANSFER"
                    llCash.visibility = View.GONE
                    llRef.visibility = View.VISIBLE
                }
                R.id.rbCreditCard -> {
                    selectedMethod = "CREDIT_CARD"
                    llCash.visibility = View.GONE
                    llRef.visibility = View.VISIBLE
                }
                R.id.rbQrCode -> {
                    selectedMethod = "VNPAY_QR"
                    llCash.visibility = View.GONE
                    llRef.visibility = View.VISIBLE
                }
            }
        }

        // Tự động tính tiền thừa trả khách khi gõ tiền khách đưa
        etGiven.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val givenVal = s.toString().toDoubleOrNull() ?: 0.0
                val change = givenVal - invoice.finalAmount
                if (change >= 0) {
                    tvChange.text = "${currencyFormat.format(change)} đ"
                    tvChange.setTextColor(ContextCompat.getColor(this@CashierStationActivity, R.color.accent_green))
                } else {
                    tvChange.text = "Khách còn thiếu: ${currencyFormat.format(-change)} đ"
                    tvChange.setTextColor(ContextCompat.getColor(this@CashierStationActivity, R.color.accent_coral))
                }
            }
            override fun afterTextChanged(s: Editable?) {}
        })

        val dialog = AlertDialog.Builder(this)
            .setView(dialogView)
            .setCancelable(false)
            .create()

        btnCancel.setOnClickListener { dialog.dismiss() }

        btnConfirm.setOnClickListener {
            val given = etGiven.text.toString().toDoubleOrNull()
            val ref = etRef.text.toString().trim()
            val note = etNote.text.toString().trim()

            if (selectedMethod == "CASH" && (given == null || given < invoice.finalAmount)) {
                Toast.makeText(this, "Số tiền khách đưa chưa đủ để thanh toán", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            dialog.dismiss()
            executePayment(invoice.id, code, invoice.finalAmount, selectedMethod, given, ref, note)
        }

        dialog.show()
    }

    private fun executePayment(
        invoiceId: Long,
        invoiceCode: String,
        amount: Double,
        method: String,
        customerGivenAmount: Double?,
        reference: String?,
        note: String?
    ) {
        val session = RetrofitClient.getSessionManager(this)
        val user = session.getUser()
        val cashierId = user?.id

        val api = RetrofitClient.getApiService(this)
        binding.swipeRefreshCashier.isRefreshing = true

        lifecycleScope.launch {
            try {
                val req = ProcessPaymentRequest(
                    invoiceId = invoiceId,
                    cashierId = cashierId,
                    amount = amount,
                    paymentMethod = method,
                    transactionReference = if (!reference.isNullOrBlank()) reference else null,
                    customerGivenAmount = customerGivenAmount,
                    note = if (!note.isNullOrBlank()) note else null
                )

                val res = api.processPayment(req)
                if (res.isSuccessful && res.body() != null) {
                    val pResp = res.body()!!
                    val changeMsg = if (pResp.changeAmount != null && pResp.changeAmount > 0) {
                        "\nTiền thừa gửi lại khách: ${currencyFormat.format(pResp.changeAmount)} đ"
                    } else ""

                    Toast.makeText(
                        this@CashierStationActivity,
                        "Thu tiền thành công cho hóa đơn $invoiceCode!$changeMsg",
                        Toast.LENGTH_LONG
                    ).show()

                    loadAllBillingData()
                } else {
                    val err = res.errorBody()?.string() ?: "Lỗi xử lý thanh toán"
                    Toast.makeText(this@CashierStationActivity, "Thất bại: $err", Toast.LENGTH_LONG).show()
                }
            } catch (e: Exception) {
                Toast.makeText(this@CashierStationActivity, "Lỗi kết nối: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
            } finally {
                binding.swipeRefreshCashier.isRefreshing = false
            }
        }
    }

    // =========================================================================
    // 3. ÁP DỤNG BẢO HIỂM Y TẾ & GIẢM GIÁ
    // =========================================================================
    private fun showApplyDiscountDialog(invoice: InvoiceDetailDto) {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_apply_discount, null)
        val tvSubtitle = dialogView.findViewById<TextView>(R.id.tvDiscountDialogSubtitle)
        val etRate = dialogView.findViewById<EditText>(R.id.etInsuranceRate)
        val etFixed = dialogView.findViewById<EditText>(R.id.etInsuranceFixedAmount)
        val etDiscount = dialogView.findViewById<EditText>(R.id.etDiscountAmount)
        val etReason = dialogView.findViewById<EditText>(R.id.etDiscountReason)
        val btnCancel = dialogView.findViewById<Button>(R.id.btnCancelDiscount)
        val btnConfirm = dialogView.findViewById<Button>(R.id.btnConfirmDiscount)

        val code = invoice.invoiceCode ?: "HD#${invoice.id}"
        val name = invoice.patientName ?: "Bệnh nhân"
        tvSubtitle.text = "Hóa đơn: $code | Bệnh nhân: $name"

        if (!invoice.healthInsuranceNumber.isNullOrBlank()) {
            etRate.setText("80") // Mặc định 80% nếu có BHYT
        }

        val dialog = AlertDialog.Builder(this)
            .setView(dialogView)
            .setCancelable(false)
            .create()

        btnCancel.setOnClickListener { dialog.dismiss() }

        btnConfirm.setOnClickListener {
            val rate = etRate.text.toString().toDoubleOrNull()
            val fixed = etFixed.text.toString().toDoubleOrNull()
            val discount = etDiscount.text.toString().toDoubleOrNull()
            val reason = etReason.text.toString().trim()

            dialog.dismiss()
            executeApplyDiscount(invoice.id, rate, fixed, discount, reason)
        }

        dialog.show()
    }

    private fun executeApplyDiscount(
        invoiceId: Long,
        insuranceRate: Double?,
        insuranceFixedAmount: Double?,
        discountAmount: Double?,
        discountReason: String?
    ) {
        val api = RetrofitClient.getApiService(this)
        binding.swipeRefreshCashier.isRefreshing = true

        lifecycleScope.launch {
            try {
                val req = ApplyInsuranceDiscountRequest(
                    insuranceRate = insuranceRate,
                    insurancePaidAmount = insuranceFixedAmount,
                    discountAmount = discountAmount,
                    discountReason = if (!discountReason.isNullOrBlank()) discountReason else null
                )

                val res = api.applyInsuranceAndDiscount(invoiceId, req)
                if (res.isSuccessful && res.body() != null) {
                    val updated = res.body()!!
                    Toast.makeText(
                        this@CashierStationActivity,
                        "Đã áp dụng giảm trừ BHYT/Ưu đãi. Số tiền mới: ${currencyFormat.format(updated.finalAmount)} đ",
                        Toast.LENGTH_LONG
                    ).show()
                    loadAllBillingData()
                } else {
                    val err = res.errorBody()?.string() ?: "Lỗi áp dụng giảm giá"
                    Toast.makeText(this@CashierStationActivity, "Thất bại: $err", Toast.LENGTH_LONG).show()
                }
            } catch (e: Exception) {
                Toast.makeText(this@CashierStationActivity, "Lỗi kết nối: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
            } finally {
                binding.swipeRefreshCashier.isRefreshing = false
            }
        }
    }

    // =========================================================================
    // 4. HOÀN TIỀN / HỦY HÓA ĐƠN
    // =========================================================================
    private fun showRefundInvoiceDialog(invoice: InvoiceDetailDto) {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_refund_invoice, null)
        val tvSubtitle = dialogView.findViewById<TextView>(R.id.tvRefundDialogSubtitle)
        val etReason = dialogView.findViewById<EditText>(R.id.etRefundReason)
        val btnCancel = dialogView.findViewById<Button>(R.id.btnCancelRefund)
        val btnConfirm = dialogView.findViewById<Button>(R.id.btnConfirmRefund)

        val code = invoice.invoiceCode ?: "HD#${invoice.id}"
        val name = invoice.patientName ?: "Bệnh nhân"
        tvSubtitle.text = "Hóa đơn: $code | Bệnh nhân: $name (Đã thu: ${currencyFormat.format(invoice.finalAmount)} đ)"

        val dialog = AlertDialog.Builder(this)
            .setView(dialogView)
            .setCancelable(false)
            .create()

        btnCancel.setOnClickListener { dialog.dismiss() }

        btnConfirm.setOnClickListener {
            val reason = etReason.text.toString().trim()
            if (reason.isEmpty()) {
                Toast.makeText(this, "Vui lòng nhập lý do hoàn tiền / hủy giao dịch", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            dialog.dismiss()
            executeRefundInvoice(invoice.id, reason)
        }

        dialog.show()
    }

    private fun executeRefundInvoice(invoiceId: Long, reason: String) {
        val session = RetrofitClient.getSessionManager(this)
        val user = session.getUser()
        val actorId = user?.id

        val api = RetrofitClient.getApiService(this)
        binding.swipeRefreshCashier.isRefreshing = true

        lifecycleScope.launch {
            try {
                val req = RefundInvoiceRequest(
                    invoiceId = invoiceId,
                    actorId = actorId,
                    reason = reason
                )

                val res = api.refundInvoice(req)
                if (res.isSuccessful && res.body() != null) {
                    Toast.makeText(
                        this@CashierStationActivity,
                        "Hóa đơn đã được hoàn tiền và chuyển sang trạng thái ĐÃ HOÀN TIỀN",
                        Toast.LENGTH_LONG
                    ).show()
                    loadAllBillingData()
                } else {
                    val err = res.errorBody()?.string() ?: "Lỗi hoàn tiền hóa đơn"
                    Toast.makeText(this@CashierStationActivity, "Thất bại: $err", Toast.LENGTH_LONG).show()
                }
            } catch (e: Exception) {
                Toast.makeText(this@CashierStationActivity, "Lỗi kết nối: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
            } finally {
                binding.swipeRefreshCashier.isRefreshing = false
            }
        }
    }
}
