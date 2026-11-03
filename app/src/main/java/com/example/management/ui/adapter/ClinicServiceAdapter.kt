package com.example.management.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.management.data.model.ClinicalServiceDto
import com.example.management.databinding.ItemClinicServiceBinding
import java.text.DecimalFormat

class ClinicServiceAdapter : RecyclerView.Adapter<ClinicServiceAdapter.ViewHolder>() {

    private val items = mutableListOf<ClinicalServiceDto>()
    private val currencyFormat = DecimalFormat("#,###")

    fun submitList(newItems: List<ClinicalServiceDto>) {
        items.clear()
        items.addAll(newItems)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemClinicServiceBinding.inflate(
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

    inner class ViewHolder(private val binding: ItemClinicServiceBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: ClinicalServiceDto) {
            binding.tvServiceName.text = item.name ?: "Dịch vụ y tế"
            binding.tvServicePrice.text = "${currencyFormat.format(item.price)} đ"
            val code = item.code ?: "DV#${item.id}"
            binding.tvServiceCodeAndCategory.text = "Mã: $code | Dịch vụ kỹ thuật CarePlus"

            if (item.insuranceCovered) {
                binding.tvServiceInsuranceInfo.text = "Bảo hiểm Y tế: Hỗ trợ thanh toán theo quy định BHYT"
            } else {
                binding.tvServiceInsuranceInfo.text = "Dịch vụ tự nguyện (Không thuộc danh mục BHYT chi trả)"
            }
        }
    }
}
