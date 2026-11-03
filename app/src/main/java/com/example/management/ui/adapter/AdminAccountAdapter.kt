package com.example.management.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.management.R
import com.example.management.data.model.UserAccountDto
import com.example.management.databinding.ItemAdminAccountBinding

class AdminAccountAdapter(
    private val onToggleStatus: (UserAccountDto) -> Unit
) : RecyclerView.Adapter<AdminAccountAdapter.ViewHolder>() {

    private val items = mutableListOf<UserAccountDto>()

    fun submitList(newItems: List<UserAccountDto>) {
        items.clear()
        items.addAll(newItems)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemAdminAccountBinding.inflate(
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

    inner class ViewHolder(private val binding: ItemAdminAccountBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: UserAccountDto) {
            val context = binding.root.context
            binding.tvAccountUsername.text = "@${item.username}"
            binding.tvAccountFullName.text = item.fullName ?: "Chưa đặt tên"

            val rolesStr = if (item.roles.isNotEmpty()) item.roles.joinToString(", ") else "USER"
            binding.tvAccountRoles.text = "Vai trò: $rolesStr"

            val phone = item.phone ?: "Chưa có SĐT"
            val email = item.email ?: ""
            binding.tvAccountContact.text = "$phone | $email"

            if (item.isActive) {
                binding.tvAccountStatusBadge.text = "Hoạt động"
                binding.tvAccountStatusBadge.setTextColor(ContextCompat.getColor(context, R.color.accent_green))
                binding.tvAccountStatusBadge.setBackgroundResource(R.drawable.bg_badge_completed)
                binding.btnToggleAccount.text = "Khóa tài khoản"
                binding.btnToggleAccount.setTextColor(ContextCompat.getColor(context, R.color.accent_coral))
            } else {
                binding.tvAccountStatusBadge.text = "Đã bị khóa"
                binding.tvAccountStatusBadge.setTextColor(ContextCompat.getColor(context, R.color.accent_coral))
                binding.tvAccountStatusBadge.setBackgroundResource(R.drawable.bg_badge_pending)
                binding.btnToggleAccount.text = "Kích hoạt lại"
                binding.btnToggleAccount.setTextColor(ContextCompat.getColor(context, R.color.accent_green))
            }

            binding.btnToggleAccount.setOnClickListener {
                onToggleStatus(item)
            }
        }
    }
}
