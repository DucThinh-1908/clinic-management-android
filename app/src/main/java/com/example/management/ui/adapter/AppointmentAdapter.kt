package com.example.management.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.management.data.model.AppointmentItem
import com.example.management.databinding.ItemAppointmentBinding

class AppointmentAdapter(
    private var appointments: List<AppointmentItem> = emptyList(),
    private val onItemClick: ((AppointmentItem) -> Unit)? = null
) : RecyclerView.Adapter<AppointmentAdapter.ViewHolder>() {

    fun submitList(newList: List<AppointmentItem>) {
        appointments = newList
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemAppointmentBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(appointments[position])
    }

    override fun getItemCount(): Int = appointments.size

    inner class ViewHolder(private val binding: ItemAppointmentBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: AppointmentItem) {
            binding.tvCode.text = item.appointmentCode ?: "LH-ID#${item.id}"
            
            // Trạng thái tiếng Việt
            binding.tvStatus.text = when (item.status) {
                "CONFIRMED" -> "Đã xác nhận"
                "PENDING" -> "Chờ xác nhận"
                "COMPLETED" -> "Đã khám"
                "CANCELLED" -> "Đã hủy"
                "NO_SHOW" -> "Vắng khám"
                else -> item.status ?: "Đang xử lý"
            }

            binding.tvDoctor.text = "Bác sĩ: ${item.doctorName ?: "Chưa chỉ định"}"
            binding.tvDepartment.text = "Chuyên khoa: ${item.departmentName ?: "Khoa Khám bệnh"}"
            binding.tvDateTime.text = "Thời gian: ${item.appointmentTime ?: ""} - ${item.appointmentDate ?: ""}"
            binding.tvSymptoms.text = "Triệu chứng: ${item.symptomsSummary ?: "Khám tổng quát"}"

            binding.root.setOnClickListener {
                onItemClick?.invoke(item)
            }
        }
    }
}
