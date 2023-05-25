package com.example.gps.ui

import android.annotation.SuppressLint
import android.content.Intent
import android.location.Geocoder
import android.os.Build
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.gps.databinding.ItemBinding
import com.example.gps.model.MovementData
import com.example.gps.utils.TimeUtils
import java.util.Calendar
import java.util.Locale

class HistoryAdapter() :
    RecyclerView.Adapter<HistoryAdapter.HistoryViewHolder>() {
    private   var list: MutableList<MovementData> = mutableListOf()
    class HistoryViewHolder(private val binding: ItemBinding) :
        RecyclerView.ViewHolder(binding.root) {
        @SuppressLint("SetTextI18n")
        fun bind(movementData: MovementData) {

            with(binding) {
                binding.btnSeeMore.setOnClickListener {
                    val intent = Intent(it.context, ShowActivity::class.java)
                    intent.putExtra("movementData", movementData)
                    binding.root.context.startActivity(intent)
                }

                val calendar = Calendar.getInstance()
                txtDate.text =
                    "${calendar.get(Calendar.DAY_OF_MONTH)}\nthg ${calendar.get(Calendar.MONTH) + 1}\n${
                        calendar.get(Calendar.YEAR)
                    }"
                txtMaxSpeed.text = movementData.maxSpeed.toString()
                txtTime.text = TimeUtils.formatTime(movementData.time.toLong())
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    Geocoder(
                        binding.root.context,
                        Locale.getDefault()
                    ).getFromLocation(
                        movementData.startLatitude.toDouble(),
                        movementData.startLongitude.toDouble(),
                        1
                    ) {
                        txtStart.text = it[0].getAddressLine(0)

                    }
                } else {

                    val a = Geocoder(binding.root.context, Locale.getDefault()).getFromLocation(
                        movementData.startLatitude.toDouble(),
                        movementData.startLongitude.toDouble(),
                        1
                    )

                    if (a != null) {
                        txtStart.text = a[0]
                            .getAddressLine(0)
                    }else{
                        txtStart.text="__"
                    }

                }
            }
        }
    }

    fun notifyDataSetChanged(mutableList: MutableList<MovementData>) {
        list.clear()
        list.addAll(mutableList)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HistoryViewHolder {
        return HistoryViewHolder(ItemBinding.inflate(LayoutInflater.from(parent.context)))
    }

    override fun getItemCount(): Int {
        return list.size
    }

    override fun onBindViewHolder(holder: HistoryViewHolder, position: Int) {
        holder.bind(list[position])
    }
}