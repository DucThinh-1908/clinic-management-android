package com.example.management.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.management.data.model.HourlyVisitItem
import com.example.management.databinding.ItemHourlyVisitBinding

class HourlyVisitAdapter(
    private var list: List<HourlyVisitItem> = emptyList()
) : RecyclerView.Adapter<HourlyVisitAdapter.ViewHolder>() {

    fun submitList(newList: List<HourlyVisitItem>) {
        list = newList
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemHourlyVisitBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(list[position])
    }

    override fun getItemCount(): Int = list.size

    inner class ViewHolder(private val binding: ItemHourlyVisitBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: HourlyVisitItem) {
            binding.tvHourLabel.text = item.hourLabel ?: ""
            val count = (item.visitCount ?: 0L).toInt()
            binding.tvHourCount.text = "$count"
            binding.pbHourProgress.progress = count.coerceAtMost(20)
        }
    }
}
