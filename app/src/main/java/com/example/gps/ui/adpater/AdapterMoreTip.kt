package com.example.gps.ui.adpater

import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.gps.R
import com.example.gps.databinding.ActivityMainBinding
import com.example.gps.databinding.ItemMoreTipBinding
import com.example.gps.model.MoreTip
import com.example.gps.ui.ShowWebActitvity

class AdapterMoreTip(val list: List<MoreTip>) :
    RecyclerView.Adapter<AdapterMoreTip.AdapterMoreTipViewHolder>() {
    class AdapterMoreTipViewHolder(val binding: ItemMoreTipBinding) : RecyclerView.ViewHolder(
        binding.root
    ) {
        fun bind(moreTip: MoreTip) {
            with(binding) {
                Glide.with(binding.root.context).load(moreTip.img).into(img)
                    .onLoadFailed(binding.root.context.getDrawable(R.drawable.img_3))
                txtTitle.text = moreTip.title
                mRcy.setOnClickListener {
                    val intent = Intent(it.context, ShowWebActitvity::class.java)
                    intent.putExtra("link", moreTip.link)
                    it.context.startActivity(intent)
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AdapterMoreTipViewHolder {
        return AdapterMoreTipViewHolder(ItemMoreTipBinding.inflate(LayoutInflater.from(parent.context)))
    }

    override fun getItemCount(): Int {
        return list.size
    }

    override fun onBindViewHolder(holder: AdapterMoreTipViewHolder, position: Int) {
        holder.bind(list[position])
    }
}