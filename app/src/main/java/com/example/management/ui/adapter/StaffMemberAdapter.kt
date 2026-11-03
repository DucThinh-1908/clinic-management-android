package com.example.management.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.management.R
import com.example.management.data.model.StaffResponse
import com.example.management.databinding.ItemStaffMemberBinding

class StaffMemberAdapter : RecyclerView.Adapter<StaffMemberAdapter.ViewHolder>() {

    private val items = mutableListOf<StaffResponse>()

    fun submitList(newItems: List<StaffResponse>) {
        items.clear()
        items.addAll(newItems)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemStaffMemberBinding.inflate(
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

    inner class ViewHolder(private val binding: ItemStaffMemberBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: StaffResponse) {
            val context = binding.root.context
            binding.tvStaffName.text = item.fullName ?: "Nhân sự"
            val code = item.staffCode ?: "NV#${item.id}"
            val dept = item.departmentName ?: item.position ?: "Đa khoa"
            binding.tvStaffCodeAndDept.text = "Mã: $code | $dept"

            val phone = item.phone ?: "Chưa có SĐT"
            val email = item.email ?: ""
            binding.tvStaffContact.text = "SĐT: $phone | Email: $email"

            val status = item.status ?: "ACTIVE"
            if (status == "ACTIVE") {
                binding.tvStaffStatusBadge.text = "Đang công tác"
                binding.tvStaffStatusBadge.setTextColor(ContextCompat.getColor(context, R.color.accent_green))
                binding.tvStaffStatusBadge.setBackgroundResource(R.drawable.bg_badge_completed)
            } else {
                binding.tvStaffStatusBadge.text = if (status == "ON_LEAVE") "Nghỉ phép" else "Thôi việc"
                binding.tvStaffStatusBadge.setTextColor(ContextCompat.getColor(context, R.color.accent_orange))
                binding.tvStaffStatusBadge.setBackgroundResource(R.drawable.bg_badge_pending)
            }
        }
    }
}
