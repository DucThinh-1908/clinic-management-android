package com.example.management.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.management.R
import com.example.management.data.model.AttendanceResponse
import com.example.management.databinding.ItemAttendanceRecordBinding

class AttendanceAdapter : RecyclerView.Adapter<AttendanceAdapter.ViewHolder>() {

    private val items = mutableListOf<AttendanceResponse>()

    fun submitList(newItems: List<AttendanceResponse>) {
        items.clear()
        items.addAll(newItems)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemAttendanceRecordBinding.inflate(
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

    inner class ViewHolder(private val binding: ItemAttendanceRecordBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: AttendanceResponse) {
            val context = binding.root.context
            binding.tvAttendanceDate.text = "Ngày: ${item.workDate ?: ""}"

            val isOntime = item.status == "ON_TIME"
            if (isOntime) {
                binding.tvAttendanceStatusBadge.text = "Đúng giờ"
                binding.tvAttendanceStatusBadge.setTextColor(ContextCompat.getColor(context, R.color.accent_green))
                binding.tvAttendanceStatusBadge.setBackgroundResource(R.drawable.bg_badge_completed)
            } else {
                binding.tvAttendanceStatusBadge.text = "Vào muộn"
                binding.tvAttendanceStatusBadge.setTextColor(ContextCompat.getColor(context, R.color.accent_coral))
                binding.tvAttendanceStatusBadge.setBackgroundResource(R.drawable.bg_badge_pending)
            }

            val inTime = item.checkInTime?.substringAfter("T")?.take(5) ?: "--:--"
            val outTime = item.checkOutTime?.substringAfter("T")?.take(5) ?: "--:--"
            binding.tvAttendanceTime.text = "Vào ca: $inTime | Ra ca: $outTime"

            binding.tvAttendanceNotes.text = "Ghi chú: ${item.notes ?: "Hoàn tất ca làm việc"}"
        }
    }
}
