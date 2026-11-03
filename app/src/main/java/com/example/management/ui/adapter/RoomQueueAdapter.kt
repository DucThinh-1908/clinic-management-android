package com.example.management.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.management.R
import com.example.management.data.model.RoomQueueOverviewDto
import com.example.management.databinding.ItemRoomQueueBinding

class RoomQueueAdapter(
    private var rooms: List<RoomQueueOverviewDto> = emptyList(),
    private val onCallNext: (RoomQueueOverviewDto) -> Unit,
    private val onViewQueue: (RoomQueueOverviewDto) -> Unit
) : RecyclerView.Adapter<RoomQueueAdapter.RoomQueueViewHolder>() {

    fun submitList(newList: List<RoomQueueOverviewDto>) {
        rooms = newList
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RoomQueueViewHolder {
        val binding = ItemRoomQueueBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return RoomQueueViewHolder(binding)
    }

    override fun onBindViewHolder(holder: RoomQueueViewHolder, position: Int) {
        holder.bind(rooms[position])
    }

    override fun getItemCount(): Int = rooms.size

    inner class RoomQueueViewHolder(private val binding: ItemRoomQueueBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: RoomQueueOverviewDto) {
            val roomDisplay = item.roomName ?: item.roomNumber ?: "Phòng khám #${item.roomId}"
            binding.tvRoomName.text = roomDisplay

            val doctorDisplay = item.doctorName ?: "Chưa có bác sĩ trực"
            val deptDisplay = item.departmentName ?: "Đa khoa"
            binding.tvDoctorName.text = "Bác sĩ: $doctorDisplay | Khoa: $deptDisplay"

            // Trạng thái phòng
            if (item.isAvailable != false) {
                binding.tvRoomStatusBadge.text = "Sẵn sàng"
                binding.tvRoomStatusBadge.setTextColor(ContextCompat.getColor(itemView.context, R.color.accent_green))
            } else {
                binding.tvRoomStatusBadge.text = "Tạm dừng"
                binding.tvRoomStatusBadge.setTextColor(ContextCompat.getColor(itemView.context, R.color.accent_coral))
            }

            // Bệnh nhân đang khám
            val current = item.currentServing ?: item.currentCalled
            if (current != null) {
                val stt = String.format("%02d", current.ticketNumber)
                binding.tvCurrentServing.text = "STT #$stt - ${current.patientName ?: "Bệnh nhân"}"
            } else {
                binding.tvCurrentServing.text = "Chưa có bệnh nhân"
            }

            binding.tvWaitingCount.text = "Chờ: ${item.waitingCount}"

            binding.btnCallNext.setOnClickListener {
                onCallNext(item)
            }

            binding.btnViewWaitingList.setOnClickListener {
                onViewQueue(item)
            }
        }
    }
}
