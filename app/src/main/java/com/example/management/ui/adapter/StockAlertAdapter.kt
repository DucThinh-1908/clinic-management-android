package com.example.management.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.management.R
import com.example.management.data.model.StockAlertItemDto
import com.example.management.databinding.ItemStockAlertBinding

class StockAlertAdapter : RecyclerView.Adapter<StockAlertAdapter.ViewHolder>() {

    private val items = mutableListOf<StockAlertItemDto>()

    fun submitList(newItems: List<StockAlertItemDto>) {
        items.clear()
        items.addAll(newItems)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemStockAlertBinding.inflate(
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

    inner class ViewHolder(private val binding: ItemStockAlertBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: StockAlertItemDto) {
            val context = binding.root.context

            binding.tvAlertMedicineName.text = item.brandName ?: item.medicineCode ?: "Thuốc"

            val typeBadge = when (item.alertType) {
                "LOW_STOCK" -> "Sắp hết hàng"
                "NEAR_EXPIRY" -> "Sắp hết hạn"
                "EXPIRED" -> "Đã hết hạn"
                else -> item.alertType ?: "Cảnh báo"
            }
            binding.tvAlertSeverityBadge.text = typeBadge

            val isCritical = item.severity == "CRITICAL" || item.alertType == "EXPIRED"
            if (isCritical) {
                binding.tvAlertSeverityBadge.setTextColor(ContextCompat.getColor(context, R.color.accent_coral))
                binding.tvAlertSeverityBadge.setBackgroundResource(R.drawable.bg_badge_pending)
            } else {
                binding.tvAlertSeverityBadge.setTextColor(ContextCompat.getColor(context, R.color.accent_orange))
                binding.tvAlertSeverityBadge.setBackgroundResource(R.drawable.bg_badge_pending)
            }

            val qty = item.quantityAvailable ?: 0
            val unitStr = item.unit ?: "đơn vị"
            val threshold = item.threshold ?: 0
            binding.tvAlertDetails.text = "Tồn hiện tại: $qty $unitStr (Ngưỡng an toàn: $threshold $unitStr)"

            val batchNo = item.batchNumber ?: "N/A"
            val exp = item.expiryDate ?: "Không có HSD"
            val days = item.daysRemaining
            val daysStr = if (days != null) " (Còn $days ngày)" else ""
            binding.tvAlertBatchInfo.text = "Lô: $batchNo | HSD: $exp$daysStr"

            binding.tvAlertMessage.text = item.message ?: "Cần kiểm tra và nhập bổ sung tồn kho dược"
        }
    }
}
