package com.example.management.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.management.data.model.QueueTicketResponse
import com.example.management.databinding.ItemDoctorQueueTicketBinding

class DoctorQueueAdapter(
    private val onCallPatient: (QueueTicketResponse) -> Unit
) : RecyclerView.Adapter<DoctorQueueAdapter.ViewHolder>() {

    private val items = mutableListOf<QueueTicketResponse>()

    fun submitList(newItems: List<QueueTicketResponse>) {
        items.clear()
        items.addAll(newItems)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemDoctorQueueTicketBinding.inflate(
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

    inner class ViewHolder(private val binding: ItemDoctorQueueTicketBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(ticket: QueueTicketResponse) {
            val num = String.format("%02d", ticket.ticketNumber ?: 0)
            binding.tvDoctorTicketNumber.text = "STT\n$num"
            binding.tvDoctorPatientName.text = ticket.patientName ?: "Bệnh nhân"
            val code = ticket.patientCode ?: "BN#${ticket.patientId ?: ""}"
            val service = ticket.serviceType ?: "Khám tổng quát"
            binding.tvDoctorPatientSub.text = "$code | $service"

            binding.btnCallIntoRoom.setOnClickListener {
                onCallPatient(ticket)
            }
        }
    }
}
