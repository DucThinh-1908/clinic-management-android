package com.example.management.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.management.data.model.MedicalRecordItem
import com.example.management.databinding.ItemMedicalRecordBinding

class MedicalRecordAdapter(
    private var records: List<MedicalRecordItem> = emptyList(),
    private val onItemClick: ((MedicalRecordItem) -> Unit)? = null
) : RecyclerView.Adapter<MedicalRecordAdapter.ViewHolder>() {

    fun submitList(newList: List<MedicalRecordItem>) {
        records = newList
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemMedicalRecordBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(records[position])
    }

    override fun getItemCount(): Int = records.size

    inner class ViewHolder(private val binding: ItemMedicalRecordBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: MedicalRecordItem) {
            binding.tvRecordCode.text = item.recordCode ?: "HS-ID#${item.id}"
            binding.tvRecordDate.text = item.createdAt?.take(10) ?: ""
            binding.tvDiagnosis.text = "Chẩn đoán: ${item.finalDiagnosis ?: item.initialDiagnosis ?: "Đang theo dõi"}"
            binding.tvDoctorAndDept.text = "Bác sĩ: ${item.doctorName ?: "Bác sĩ phụ trách"} | ${item.departmentName ?: "Đa khoa"}"
            binding.tvAdvice.text = "Lời dặn: ${item.doctorAdvice ?: "Uống thuốc theo đơn và tái khám đúng hẹn"}"

            binding.root.setOnClickListener {
                onItemClick?.invoke(item)
            }
        }
    }
}
