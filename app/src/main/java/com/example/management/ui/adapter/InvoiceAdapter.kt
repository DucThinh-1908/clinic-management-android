package com.example.management.ui.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.management.R
import com.example.management.data.model.InvoiceDetailDto
import com.example.management.databinding.ItemInvoiceBillBinding
import com.example.management.databinding.ItemInvoiceServiceRowBinding
import java.text.DecimalFormat

class InvoiceAdapter(
    private val onProcessPayment: (InvoiceDetailDto) -> Unit,
    private val onApplyDiscount: (InvoiceDetailDto) -> Unit,
    private val onRefundInvoice: (InvoiceDetailDto) -> Unit
) : RecyclerView.Adapter<InvoiceAdapter.ViewHolder>() {

    private val items = mutableListOf<InvoiceDetailDto>()
    private val currencyFormat = DecimalFormat("#,###")

    fun submitList(newItems: List<InvoiceDetailDto>) {
        items.clear()
        items.addAll(newItems)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemInvoiceBillBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int = items.size

    inner class ViewHolder(private val binding: ItemInvoiceBillBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: InvoiceDetailDto) {
            val context = binding.root.context

            // Mã HĐ & Ngày giờ tạo
            binding.tvInvoiceCode.text = item.invoiceCode ?: "HD#${item.id}"
            val created = item.createdAt?.replace("T", " ") ?: ""
            binding.tvInvoiceCreatedAt.text = "Ngày lập: $created"

            // Trạng thái hóa đơn
            when (item.status) {
                "PAID" -> {
                    binding.tvInvoiceStatusBadge.text = "Đã thanh toán"
                    binding.tvInvoiceStatusBadge.setBackgroundResource(R.drawable.bg_badge_completed)
                    binding.tvInvoiceStatusBadge.setTextColor(ContextCompat.getColor(context, R.color.accent_green))
                    binding.btnProcessPayment.visibility = View.GONE
                    binding.btnApplyDiscount.visibility = View.GONE
                    binding.btnRefundInvoice.visibility = View.VISIBLE
                }
                "REFUNDED" -> {
                    binding.tvInvoiceStatusBadge.text = "Đã hoàn tiền"
                    binding.tvInvoiceStatusBadge.setBackgroundResource(R.drawable.bg_badge_pending)
                    binding.tvInvoiceStatusBadge.setTextColor(ContextCompat.getColor(context, R.color.accent_coral))
                    binding.btnProcessPayment.visibility = View.GONE
                    binding.btnApplyDiscount.visibility = View.GONE
                    binding.btnRefundInvoice.visibility = View.GONE
                }
                "CANCELLED" -> {
                    binding.tvInvoiceStatusBadge.text = "Đã hủy bỏ"
                    binding.tvInvoiceStatusBadge.setBackgroundResource(R.drawable.bg_badge_pending)
                    binding.tvInvoiceStatusBadge.setTextColor(ContextCompat.getColor(context, R.color.text_muted))
                    binding.btnProcessPayment.visibility = View.GONE
                    binding.btnApplyDiscount.visibility = View.GONE
                    binding.btnRefundInvoice.visibility = View.GONE
                }
                else -> { // UNPAID
                    binding.tvInvoiceStatusBadge.text = "Chờ thanh toán"
                    binding.tvInvoiceStatusBadge.setBackgroundResource(R.drawable.bg_badge_pending)
                    binding.tvInvoiceStatusBadge.setTextColor(ContextCompat.getColor(context, R.color.accent_orange))
                    binding.btnProcessPayment.visibility = View.VISIBLE
                    binding.btnApplyDiscount.visibility = View.VISIBLE
                    binding.btnRefundInvoice.visibility = View.GONE
                }
            }

            // Thông tin bệnh nhân & bác sĩ
            binding.tvInvoicePatientName.text = item.patientName ?: "Bệnh nhân chưa định danh"
            val pCode = item.patientCode ?: "BN-${item.patientId ?: "N/A"}"
            val pPhone = item.patientPhone ?: "Chưa có SĐT"
            binding.tvInvoicePatientSubInfo.text = "Mã BN: $pCode | SĐT: $pPhone"

            if (!item.healthInsuranceNumber.isNullOrBlank()) {
                binding.tvInvoiceInsuranceNumber.visibility = View.VISIBLE
                binding.tvInvoiceInsuranceNumber.text = "Thẻ BHYT: ${item.healthInsuranceNumber}"
            } else {
                binding.tvInvoiceInsuranceNumber.visibility = View.GONE
            }

            binding.tvInvoiceDoctorName.text = "BS: ${item.doctorName ?: "Chưa rõ"}"
            binding.tvInvoiceDiagnosis.text = "CĐ: ${item.diagnosis ?: "Khám bệnh"}"

            // Chi tiết số tiền
            binding.tvTotalService.text = "${currencyFormat.format(item.totalServiceAmount)} đ"
            binding.tvTotalMedicine.text = "${currencyFormat.format(item.totalMedicineAmount)} đ"

            if (item.insurancePaidAmount > 0) {
                binding.llInsuranceRow.visibility = View.VISIBLE
                binding.tvInsurancePaid.text = "- ${currencyFormat.format(item.insurancePaidAmount)} đ"
            } else {
                binding.llInsuranceRow.visibility = View.GONE
            }

            if (item.discountAmount > 0) {
                binding.llDiscountRow.visibility = View.VISIBLE
                binding.tvDiscountAmount.text = "- ${currencyFormat.format(item.discountAmount)} đ"
            } else {
                binding.llDiscountRow.visibility = View.GONE
            }

            binding.tvFinalAmount.text = "${currencyFormat.format(item.finalAmount)} đ"

            // Đổ danh sách dịch vụ con động
            binding.llInvoiceItemRows.removeAllViews()
            val itemList = item.items
            if (itemList.isNotEmpty()) {
                val inflater = LayoutInflater.from(context)
                for (it in itemList) {
                    val rowBinding = ItemInvoiceServiceRowBinding.inflate(
                        inflater,
                        binding.llInvoiceItemRows,
                        false
                    )
                    rowBinding.tvInvoiceItemName.text = it.itemName ?: "Dịch vụ y tế"
                    rowBinding.tvInvoiceItemQuantity.text = "x${it.quantity}"
                    rowBinding.tvInvoiceItemSubtotal.text = "${currencyFormat.format(it.subtotal)} đ"
                    rowBinding.tvInvoiceItemType.text = it.itemTypeDesc ?: it.itemType ?: "Khoản mục"
                    binding.llInvoiceItemRows.addView(rowBinding.root)
                }
            } else {
                val tvEmpty = TextView(context).apply {
                    text = "Chưa có danh mục dịch vụ chi tiết"
                    setTextColor(ContextCompat.getColor(context, R.color.text_muted))
                    textSize = 11f
                    setPadding(0, 4, 0, 4)
                }
                binding.llInvoiceItemRows.addView(tvEmpty)
            }

            // Xử lý sự kiện nút
            binding.btnProcessPayment.setOnClickListener {
                onProcessPayment(item)
            }

            binding.btnApplyDiscount.setOnClickListener {
                onApplyDiscount(item)
            }

            binding.btnRefundInvoice.setOnClickListener {
                onRefundInvoice(item)
            }
        }
    }
}
