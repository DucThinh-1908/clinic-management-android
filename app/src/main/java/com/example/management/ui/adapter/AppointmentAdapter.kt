package com.example.management.ui.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.management.R
import com.example.management.data.model.AppointmentItem
import com.example.management.databinding.ItemAppointmentBinding

class AppointmentAdapter(
    private var appointments: List<AppointmentItem> = emptyList(),
    private val onItemClick: ((AppointmentItem) -> Unit)? = null,
    private val onConfirm: ((AppointmentItem) -> Unit)? = null,
    private val onCheckIn: ((AppointmentItem) -> Unit)? = null,
    private val onNoShow: ((AppointmentItem) -> Unit)? = null,
    private val onCancel: ((AppointmentItem) -> Unit)? = null
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
            val statusText = when (item.status) {
                "CONFIRMED" -> "Đã xác nhận"
                "PENDING" -> "Chờ xác nhận"
                "COMPLETED" -> "Đã khám"
                "CANCELLED" -> "Đã hủy"
                "NO_SHOW" -> "Vắng khám"
                else -> item.status ?: "Đang xử lý"
            }
            binding.tvStatus.text = statusText
            if (item.status == "CONFIRMED") {
                binding.tvStatus.setTextColor(ContextCompat.getColor(itemView.context, R.color.accent_green))
            } else if (item.status == "PENDING") {
                binding.tvStatus.setTextColor(ContextCompat.getColor(itemView.context, R.color.primary))
            } else {
                binding.tvStatus.setTextColor(ContextCompat.getColor(itemView.context, R.color.text_muted))
            }

            val pName = item.patientName ?: "Bệnh nhân"
            val pPhone = item.patientPhone?.let { " ($it)" } ?: ""
            binding.tvPatient.text = "Bệnh nhân: $pName$pPhone"

            binding.tvDoctor.text = "Bác sĩ: ${item.doctorName ?: "Chưa chỉ định"}"
            binding.tvDepartment.text = "Chuyên khoa: ${item.departmentName ?: "Khoa Khám bệnh"}"
            binding.tvDateTime.text = "Thời gian: ${item.appointmentTime ?: ""} - ${item.appointmentDate ?: ""}"
            binding.tvSymptoms.text = "Triệu chứng: ${item.symptomsSummary ?: "Khám tổng quát"}"

            // Thao tác nghiệp vụ Lễ tân
            when (item.status) {
                "PENDING" -> {
                    binding.layoutReceptionActions.visibility = View.VISIBLE
                    binding.btnConfirm.visibility = View.VISIBLE
                    binding.btnCheckIn.visibility = View.GONE
                    binding.btnNoShow.visibility = View.GONE
                    binding.btnCancel.visibility = View.VISIBLE
                }
                "CONFIRMED" -> {
                    binding.layoutReceptionActions.visibility = View.VISIBLE
                    binding.btnConfirm.visibility = View.GONE
                    binding.btnCheckIn.visibility = View.VISIBLE
                    binding.btnNoShow.visibility = View.VISIBLE
                    binding.btnCancel.visibility = View.VISIBLE
                }
                else -> {
                    binding.layoutReceptionActions.visibility = View.GONE
                }
            }

            binding.btnConfirm.setOnClickListener { onConfirm?.invoke(item) }
            binding.btnCheckIn.setOnClickListener { onCheckIn?.invoke(item) }
            binding.btnNoShow.setOnClickListener { onNoShow?.invoke(item) }
            binding.btnCancel.setOnClickListener { onCancel?.invoke(item) }

            binding.root.setOnClickListener {
                onItemClick?.invoke(item)
            }
        }
    }
}
