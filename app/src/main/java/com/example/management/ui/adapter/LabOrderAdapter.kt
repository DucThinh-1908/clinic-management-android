package com.example.management.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.management.R
import com.example.management.data.model.LabOrderSummaryDto
import com.example.management.data.model.LabResultItemDto
import com.example.management.databinding.ItemLabOrderBinding

class LabOrderAdapter(
    private var orders: List<LabOrderSummaryDto> = emptyList(),
    private val onEnterResult: (LabOrderSummaryDto, LabResultItemDto) -> Unit
) : RecyclerView.Adapter<LabOrderAdapter.ViewHolder>() {

    fun submitList(newList: List<LabOrderSummaryDto>) {
        orders = newList
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemLabOrderBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(orders[position])
    }

    override fun getItemCount(): Int = orders.size

    inner class ViewHolder(private val binding: ItemLabOrderBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(order: LabOrderSummaryDto) {
            binding.tvOrderCode.text = order.orderCode ?: "XN#${order.id}"

            val isCompleted = order.status == "COMPLETED"
            binding.tvOrderStatus.text = if (isCompleted) "Đã hoàn thành" else "Chờ thực hiện"
            if (isCompleted) {
                binding.tvOrderStatus.setTextColor(ContextCompat.getColor(itemView.context, R.color.accent_green))
            } else {
                binding.tvOrderStatus.setTextColor(ContextCompat.getColor(itemView.context, R.color.accent_coral))
            }

            binding.tvOrderPatient.text = "Bệnh nhân: ${order.patientName ?: "Khách hàng"}"
            binding.tvOrderDoctor.text = "Bác sĩ chỉ định: ${order.doctorName ?: "Bác sĩ phụ trách"}"

            if (!order.diagnosisNote.isNullOrBlank()) {
                binding.tvOrderDiagnosis.text = "Chẩn đoán: ${order.diagnosisNote}"
            } else {
                binding.tvOrderDiagnosis.text = "Chẩn đoán: Theo dõi chỉ định cận lâm sàng"
            }

            // Inflate các dịch vụ xét nghiệm bên trong
            binding.llLabItems.removeAllViews()
            val inflater = LayoutInflater.from(itemView.context)

            val items = order.items ?: emptyList()
            for (item in items) {
                val itemView = inflater.inflate(R.layout.item_lab_result_service, binding.llLabItems, false)

                val tvName = itemView.findViewById<TextView>(R.id.tvServiceName)
                val tvStatus = itemView.findViewById<TextView>(R.id.tvServiceStatus)
                val tvDetails = itemView.findViewById<TextView>(R.id.tvResultDetails)
                val btnAction = itemView.findViewById<Button>(R.id.btnEnterResult)

                tvName.text = item.serviceName ?: "Dịch vụ xét nghiệm #${item.serviceId}"

                val itemDone = item.status == "COMPLETED"
                if (itemDone) {
                    tvStatus.text = "Đã có KQ"
                    tvStatus.setTextColor(ContextCompat.getColor(this.itemView.context, R.color.accent_green))

                    val valStr = item.testValue ?: "-"
                    val unitStr = item.unit?.let { " $it" } ?: ""
                    val conStr = item.conclusion?.let { " | $it" } ?: ""
                    tvDetails.text = "Kết quả: $valStr$unitStr$conStr"
                    btnAction.text = "Sửa kết quả"
                } else {
                    tvStatus.text = "Chờ làm"
                    tvStatus.setTextColor(ContextCompat.getColor(this.itemView.context, R.color.accent_coral))
                    tvDetails.text = "Kết quả: Chưa có dữ liệu đo"
                    btnAction.text = "Nhập kết quả"
                }

                btnAction.setOnClickListener {
                    onEnterResult(order, item)
                }

                binding.llLabItems.addView(itemView)
            }
        }
    }
}
