package com.example.management.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.management.R
import com.example.management.data.model.ScheduleResponse
import com.example.management.databinding.ItemStaffScheduleBinding

class StaffScheduleAdapter : RecyclerView.Adapter<StaffScheduleAdapter.ViewHolder>() {

    private val items = mutableListOf<ScheduleResponse>()

    fun submitList(newItems: List<ScheduleResponse>) {
        items.clear()
        items.addAll(newItems)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemStaffScheduleBinding.inflate(
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

    inner class ViewHolder(private val binding: ItemStaffScheduleBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: ScheduleResponse) {
            val context = binding.root.context
            binding.tvScheduleDate.text = "Ngày: ${item.workDate ?: ""}"
            binding.tvScheduleShiftBadge.text = item.shiftName ?: "Ca trực"
            binding.tvScheduleShiftBadge.setTextColor(ContextCompat.getColor(context, R.color.primary))
            binding.tvScheduleShiftBadge.setBackgroundResource(R.drawable.bg_badge)

            binding.tvScheduleStaffName.text = "Nhân sự: ${item.staffName ?: "Chưa phân công"}"
            val dept = item.departmentName ?: "Đa khoa"
            val room = item.roomNumber ?: "Phòng khám"
            binding.tvScheduleLocation.text = "Vị trí: $room | $dept"
        }
    }
}
