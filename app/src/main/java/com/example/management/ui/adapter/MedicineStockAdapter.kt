package com.example.management.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.management.R
import com.example.management.data.model.MedicineDetailDto
import com.example.management.databinding.ItemMedicineStockBinding
import java.text.DecimalFormat

class MedicineStockAdapter(
    private val onItemClick: ((MedicineDetailDto) -> Unit)? = null
) : RecyclerView.Adapter<MedicineStockAdapter.ViewHolder>() {

    private val items = mutableListOf<MedicineDetailDto>()
    private val currencyFormat = DecimalFormat("#,###")

    fun submitList(newItems: List<MedicineDetailDto>) {
        items.clear()
        items.addAll(newItems)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemMedicineStockBinding.inflate(
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

    inner class ViewHolder(private val binding: ItemMedicineStockBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: MedicineDetailDto) {
            val context = binding.root.context

            binding.tvMedicineBrandName.text = item.brandName ?: "Thuốc chưa đặt tên"
            val codeStr = item.code ?: "MED#${item.id}"
            val categoryStr = item.categoryName ?: "Thuốc thông dụng"
            binding.tvMedicineCodeAndCategory.text = "Mã: $codeStr | Nhóm: $categoryStr"

            val available = item.totalAvailableQuantity ?: 0
            val unitStr = item.unit ?: "đơn vị"
            binding.tvStockBadge.text = "Tồn: $available $unitStr"

            if (available <= 10) {
                binding.tvStockBadge.setBackgroundResource(R.drawable.bg_badge_pending)
                binding.tvStockBadge.setTextColor(ContextCompat.getColor(context, R.color.accent_coral))
            } else {
                binding.tvStockBadge.setBackgroundResource(R.drawable.bg_badge_completed)
                binding.tvStockBadge.setTextColor(ContextCompat.getColor(context, R.color.accent_green))
            }

            val active = item.activeIngredient ?: "Chưa cập nhật hoạt chất"
            val form = item.dosageForm ?: ""
            binding.tvActiveIngredientAndDosage.text = "Hoạt chất: $active ($form)"

            val price = item.sellingPrice ?: 0.0
            val reqPrescription = if (item.isPrescriptionRequired == true) "Thuốc kê đơn (ETC)" else "Không kê đơn (OTC)"
            binding.tvPriceAndUsage.text = "Đơn giá: ${currencyFormat.format(price)} đ / $unitStr | $reqPrescription"

            binding.root.setOnClickListener {
                onItemClick?.invoke(item)
            }
        }
    }
}
