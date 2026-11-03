package com.example.management.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.management.data.model.MedicalCandidateDto
import com.example.management.databinding.ItemMedicalCandidateBinding

class MedicalCandidateAdapter(
    private val onCreateInvoice: (MedicalCandidateDto) -> Unit
) : RecyclerView.Adapter<MedicalCandidateAdapter.ViewHolder>() {

    private val items = mutableListOf<MedicalCandidateDto>()

    fun submitList(newItems: List<MedicalCandidateDto>) {
        items.clear()
        items.addAll(newItems)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemMedicalCandidateBinding.inflate(
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

    inner class ViewHolder(private val binding: ItemMedicalCandidateBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: MedicalCandidateDto) {
            binding.tvCandidatePatientName.text = item.patientName ?: "Bệnh nhân"
            val pCode = item.patientCode ?: "BN-${item.patientId ?: "N/A"}"
            val rCode = item.recordCode ?: "BA#${item.medicalRecordId}"
            val phone = item.patientPhone ?: ""
            binding.tvCandidateSubInfo.text = "Mã BN: $pCode | BA: $rCode | SĐT: $phone"

            val doctor = item.doctorName ?: "Chưa rõ"
            val diag = item.diagnosis ?: "Khám tổng quát"
            binding.tvCandidateDoctorAndDiagnosis.text = "BS: $doctor | Chẩn đoán: $diag"

            val completed = item.completedAt?.replace("T", " ") ?: ""
            binding.tvCandidateCompletedAt.text = "Hoàn tất khám: $completed"

            binding.btnCreateInvoiceFromCandidate.setOnClickListener {
                onCreateInvoice(item)
            }
        }
    }
}
