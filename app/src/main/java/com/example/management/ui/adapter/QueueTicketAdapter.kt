package com.example.management.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.management.R
import com.example.management.data.model.QueueTicketResponse
import com.example.management.databinding.ItemQueueTicketBinding

class QueueTicketAdapter(
    private var tickets: List<QueueTicketResponse> = emptyList(),
    private val onTransfer: (QueueTicketResponse) -> Unit,
    private val onSkip: (QueueTicketResponse) -> Unit,
    private val onRecordVitals: ((QueueTicketResponse) -> Unit)? = null
) : RecyclerView.Adapter<QueueTicketAdapter.QueueTicketViewHolder>() {

    fun submitList(newList: List<QueueTicketResponse>) {
        tickets = newList
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): QueueTicketViewHolder {
        val binding = ItemQueueTicketBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return QueueTicketViewHolder(binding)
    }

    override fun onBindViewHolder(holder: QueueTicketViewHolder, position: Int) {
        holder.bind(tickets[position])
    }

    override fun getItemCount(): Int = tickets.size

    inner class QueueTicketViewHolder(private val binding: ItemQueueTicketBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(ticket: QueueTicketResponse) {
            val stt = String.format("%02d", ticket.ticketNumber)
            binding.tvTicketNumber.text = "STT #$stt"
            binding.tvTicketCode.text = "(${ticket.ticketCode ?: "PK"})"

            val statusText = when (ticket.status) {
                "WAITING" -> "Đang chờ"
                "CALLED" -> "Đã gọi"
                "SERVING" -> "Đang khám"
                "COMPLETED" -> "Đã khám"
                "SKIPPED" -> "Bỏ qua"
                else -> ticket.status ?: "Đang chờ"
            }
            binding.tvTicketStatus.text = statusText
            if (ticket.status == "CALLED" || ticket.status == "SERVING") {
                binding.tvTicketStatus.setTextColor(ContextCompat.getColor(itemView.context, R.color.primary))
            } else {
                binding.tvTicketStatus.setTextColor(ContextCompat.getColor(itemView.context, R.color.accent_coral))
            }

            val genderText = when (ticket.patientGender) {
                "MALE" -> "Nam"
                "FEMALE" -> "Nữ"
                else -> "Khác"
            }
            val ageText = ticket.patientAge?.let { "$it tuổi" } ?: ""
            binding.tvPatientName.text = "${ticket.patientName ?: "Bệnh nhân"} - $genderText - $ageText"

            val phoneText = ticket.patientPhone ?: "Không có SĐT"
            val channel = if (ticket.isAppointment == true) "Có lịch hẹn" else "Vãng lai"
            binding.tvPatientContact.text = "SĐT: $phoneText | $channel"

            binding.btnRecordVitals.setOnClickListener {
                onRecordVitals?.invoke(ticket)
            }

            binding.btnTransferTicket.setOnClickListener {
                onTransfer(ticket)
            }

            binding.btnSkipTicket.setOnClickListener {
                onSkip(ticket)
            }
        }
    }
}
