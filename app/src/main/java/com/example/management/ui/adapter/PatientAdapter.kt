package com.example.management.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.management.data.model.PatientDetailDto
import com.example.management.databinding.ItemPatientBinding

class PatientAdapter(
    private var patients: List<PatientDetailDto> = emptyList(),
    private val onQuickReception: (PatientDetailDto) -> Unit,
    private val onPatientClick: (PatientDetailDto) -> Unit
) : RecyclerView.Adapter<PatientAdapter.PatientViewHolder>() {

    fun submitList(newList: List<PatientDetailDto>) {
        patients = newList
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PatientViewHolder {
        val binding = ItemPatientBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return PatientViewHolder(binding)
    }

    override fun onBindViewHolder(holder: PatientViewHolder, position: Int) {
        holder.bind(patients[position])
    }

    override fun getItemCount(): Int = patients.size

    inner class PatientViewHolder(private val binding: ItemPatientBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(patient: PatientDetailDto) {
            binding.tvPatientCode.text = patient.patientCode ?: "BN#${patient.id}"
            binding.tvPatientName.text = patient.fullName ?: "Chưa có tên"

            val genderText = when (patient.gender) {
                "MALE" -> "Nam"
                "FEMALE" -> "Nữ"
                else -> "Khác"
            }
            val ageText = patient.age?.let { "($it tuổi)" } ?: ""
            val dob = patient.dateOfBirth ?: "N/A"
            val phone = patient.phone ?: "N/A"
            binding.tvPatientDetails.text = "$genderText | Ngày sinh: $dob $ageText | SĐT: $phone"

            val bhyt = patient.healthInsuranceNumber ?: "Không có"
            val cccd = patient.identityCard ?: "Chưa cập nhật"
            binding.tvInsuranceAndCccd.text = "BHYT: $bhyt | CCCD: $cccd"

            binding.btnQuickReception.setOnClickListener {
                onQuickReception(patient)
            }

            itemView.setOnClickListener {
                onPatientClick(patient)
            }
        }
    }
}
