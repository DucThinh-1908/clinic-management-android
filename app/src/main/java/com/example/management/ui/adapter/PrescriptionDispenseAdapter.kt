package com.example.management.ui.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.management.R
import com.example.management.data.model.PrescriptionResponse
import com.example.management.databinding.ItemPrescriptionDispenseBinding
import com.example.management.databinding.ItemPrescriptionMedicineRowBinding
import java.text.DecimalFormat

class PrescriptionDispenseAdapter(
    private val onDispenseClick: (PrescriptionResponse) -> Unit
) : RecyclerView.Adapter<PrescriptionDispenseAdapter.ViewHolder>() {

    private val items = mutableListOf<PrescriptionResponse>()
    private val currencyFormat = DecimalFormat("#,###")

    fun submitList(newItems: List<PrescriptionResponse>) {
        items.clear()
        items.addAll(newItems)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemPrescriptionDispenseBinding.inflate(
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

    inner class ViewHolder(private val binding: ItemPrescriptionDispenseBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: PrescriptionResponse) {
            val context = binding.root.context

            // Mã đơn & ngày giờ kê
            binding.tvPrescriptionCode.text = item.prescriptionCode ?: "DT#${item.id}"
            val created = item.createdAt?.replace("T", " ") ?: ""
            binding.tvPrescriptionDate.text = "Kê lúc: $created"

            // Trạng thái đơn
            val isPending = item.status == "PENDING"
            if (isPending) {
                binding.tvPrescriptionStatus.text = "Chờ cấp thuốc"
                binding.tvPrescriptionStatus.setBackgroundResource(R.drawable.bg_badge_pending)
                binding.tvPrescriptionStatus.setTextColor(ContextCompat.getColor(context, R.color.accent_orange))
                binding.btnDispense.visibility = View.VISIBLE
            } else {
                binding.tvPrescriptionStatus.text = "Đã cấp phát"
                binding.tvPrescriptionStatus.setBackgroundResource(R.drawable.bg_badge_completed)
                binding.tvPrescriptionStatus.setTextColor(ContextCompat.getColor(context, R.color.accent_green))
                binding.btnDispense.visibility = View.GONE
            }

            // Thông tin bệnh nhân & bác sĩ
            binding.tvPatientName.text = item.patientName ?: "Bệnh nhân chưa định danh"
            val pCode = item.patientCode ?: "BN-${item.patientId ?: "N/A"}"
            val pPhone = item.patientPhone ?: "Chưa có SĐT"
            binding.tvPatientSubInfo.text = "Mã BN: $pCode | SĐT: $pPhone"

            binding.tvDoctorName.text = "BS: ${item.doctorName ?: "Chưa rõ"}"
            binding.tvDiagnosis.text = "Chẩn đoán: ${item.diagnosis ?: "Khám tổng quát"}"

            // Lời dặn
            if (!item.usageNote.isNullOrBlank()) {
                binding.tvDoctorUsageNote.visibility = View.VISIBLE
                binding.tvDoctorUsageNote.text = "Lời dặn bác sĩ: ${item.usageNote}"
            } else {
                binding.tvDoctorUsageNote.visibility = View.GONE
            }

            // Tổng tiền
            val amount = item.totalAmount ?: 0.0
            binding.tvTotalAmount.text = "${currencyFormat.format(amount)} đ"

            // Đổ danh sách thuốc động
            binding.llPrescriptionItems.removeAllViews()
            val medicineList = item.items
            if (!medicineList.isNullOrEmpty()) {
                val inflater = LayoutInflater.from(context)
                for (med in medicineList) {
                    val rowBinding = ItemPrescriptionMedicineRowBinding.inflate(
                        inflater,
                        binding.llPrescriptionItems,
                        false
                    )

                    rowBinding.tvMedicineName.text = med.brandName ?: med.medicineCode ?: "Thuốc"
                    val unitStr = med.unit ?: "đơn vị"
                    rowBinding.tvMedicineQuantity.text = "SL: ${med.quantity ?: 0} $unitStr"

                    val ingredient = med.activeIngredient ?: ""
                    val dosageForm = med.dosageForm ?: ""
                    rowBinding.tvActiveIngredient.text = "Hoạt chất: $ingredient ($dosageForm)"

                    val m = med.morningDose ?: "0"
                    val n = med.noonDose ?: "0"
                    val a = med.afternoonDose ?: "0"
                    val e = med.nightDose ?: "0"
                    rowBinding.tvDosageSchedule.text = "Liều dùng: Sáng $m - Trưa $n - Chiều $a - Tối $e ($unitStr)"

                    val instructions = med.instructions ?: "Uống theo chỉ dẫn bác sĩ"
                    rowBinding.tvInstructions.text = "Cách dùng: $instructions"

                    binding.llPrescriptionItems.addView(rowBinding.root)
                }
            } else {
                val tvEmpty = TextView(context).apply {
                    text = "Không có thông tin chi tiết thuốc trong đơn"
                    setTextColor(ContextCompat.getColor(context, R.color.text_muted))
                    textSize = 12f
                    setPadding(0, 8, 0, 8)
                }
                binding.llPrescriptionItems.addView(tvEmpty)
            }

            // Nút cấp phát
            binding.btnDispense.setOnClickListener {
                onDispenseClick(item)
            }
        }
    }
}
