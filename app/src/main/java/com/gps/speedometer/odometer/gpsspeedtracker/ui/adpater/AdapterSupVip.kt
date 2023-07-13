package com.gps.speedometer.odometer.gpsspeedtracker.ui.adpater

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide

import com.gps.speedometer.odometer.gpsspeedtracker.databinding.ItemSupBinding


class AdapterSupVip(val list: List<Int>) :
    RecyclerView.Adapter<AdapterSupVip.AdapterSupVipViewHolder>() {
    class AdapterSupVipViewHolder(val binding: ItemSupBinding) : RecyclerView.ViewHolder(
        binding.root
    ) {
        fun bind(int: Int) {
            with(binding) {
                img.setImageDrawable(binding.root.context.getDrawable(int))

            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AdapterSupVipViewHolder {
        return AdapterSupVipViewHolder(ItemSupBinding.inflate( LayoutInflater.from(parent.context),
            parent,
            false))
    }

    override fun getItemCount(): Int {
        return list.size
    }

    override fun onBindViewHolder(holder: AdapterSupVipViewHolder, position: Int) {
        holder.bind(list[position])
    }
}