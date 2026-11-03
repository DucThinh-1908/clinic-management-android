package com.example.management.ui.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.management.data.model.VitalSignResponse
import com.example.management.databinding.ItemVitalSignHistoryBinding

class VitalSignHistoryAdapter(
    private var list: List<VitalSignResponse> = emptyList()
) : RecyclerView.Adapter<VitalSignHistoryAdapter.ViewHolder>() {

    fun submitList(newList: List<VitalSignResponse>) {
        list = newList
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemVitalSignHistoryBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(list[position])
    }

    override fun getItemCount(): Int = list.size

    inner class ViewHolder(private val binding: ItemVitalSignHistoryBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: VitalSignResponse) {
            binding.tvHistoryDate.text = item.measuredAt ?: "Gần đây"
            binding.tvHistoryNurse.text = item.nurseName?.let { "ĐD: $it" } ?: "Điều dưỡng"

            val sys = item.bloodPressureSystolic ?: "-"
            val dia = item.bloodPressureDiastolic ?: "-"
            val bpStatus = item.bloodPressureStatus?.let { " ($it)" } ?: ""
            val temp = item.temperature?.let { "$it°C" } ?: "-"
            binding.tvBpAndTemp.text = "Huyết áp: $sys/$dia mmHg$bpStatus | Thân nhiệt: $temp"

            val hr = item.heartRate?.let { "$it bpm" } ?: "-"
            val rr = item.respiratoryRate?.let { "$it l/p" } ?: "-"
            val spo2 = item.spo2?.let { "$it%" } ?: "-"
            binding.tvHeartAndSpo2.text = "Mạch: $hr | Nhịp thở: $rr | SpO2: $spo2"

            val height = item.heightCm?.let { "${it}cm" } ?: "-"
            val weight = item.weightKg?.let { "${it}kg" } ?: "-"
            val bmi = item.bmi?.let { String.format("%.1f", it) } ?: "-"
            val bmiCat = item.bmiCategory?.let { " ($it)" } ?: ""
            binding.tvBmiInfo.text = "Cao: $height | Nặng: $weight | BMI: $bmi$bmiCat"

            if (!item.notes.isNullOrBlank()) {
                binding.tvNotes.visibility = View.VISIBLE
                binding.tvNotes.text = "Ghi chú: ${item.notes}"
            } else {
                binding.tvNotes.visibility = View.GONE
            }
        }
    }
}
