package com.example.gps.ui

import android.annotation.SuppressLint
import android.content.Intent
import android.content.res.ColorStateList
import android.location.Geocoder
import android.os.Build
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.gps.databinding.ItemBinding
import com.example.gps.model.MovementData
import com.example.gps.utils.ColorUtils
import com.example.gps.utils.TimeUtils
import java.lang.Exception
import java.util.Calendar
import java.util.Locale

class HistoryAdapter(private val i: Int) :
    RecyclerView.Adapter<HistoryAdapter.HistoryViewHolder>() {
    private var list: HashMap<MovementData,Boolean> = hashMapOf()

    inner class HistoryViewHolder(private val binding: ItemBinding) :
        RecyclerView.ViewHolder(binding.root) {
        @SuppressLint("SetTextI18n")
        fun bind(movementData: MovementData) {
            with(binding) {
                binding.mLinear.setOnClickListener {
                    val intent = Intent(it.context, ShowActivity::class.java)
                    intent.putExtra("movementData", movementData)
                    binding.root.context.startActivity(intent)
                }

                val calendar = Calendar.getInstance()
                txtDate.text =
                    "${calendar.get(Calendar.DAY_OF_MONTH)}/${calendar.get(Calendar.MONTH) + 1}/${
                        calendar.get(
                            Calendar.YEAR
                        )
                    }"
                txtMaxSpeed.text = movementData.maxSpeed.toString()

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    Geocoder(binding.root.context, Locale.getDefault()).getFromLocation(
                        movementData.startLatitude,
                        movementData.startLongitude,
                        1
                    ) { addresses ->
                        val addressLine =
                            if (addresses.size > 0 && addresses[0].getAddressLine(0).trim()
                                    .isNotEmpty()
                            ) {
                                addresses[0].getAddressLine(0)
                            } else {
                                "__"
                            }
                        txtStart.text = addressLine
                    }
                } else {
                    val address =
                        getAddressLine(movementData.startLatitude, movementData.startLongitude)
                    val addressLine = if (address != null && address.trim().isNotEmpty()) {
                        address
                    } else {
                        "_ _"
                    }

                    txtStart.text = addressLine
                }
            }
        }

        private fun getAddressLine(endLatitude: Double, endLongitude: Double): String? {
            return try {
                val geocoder = Geocoder(
                    binding.root.context,
                    Locale.getDefault()
                ).getFromLocation(endLatitude, endLongitude, 1)
                if (geocoder != null) {
                    return if (geocoder.size > 0) geocoder[0]?.getAddressLine(0)
                        ?.toString() else null
                }
                null
            } catch (e: Exception) {
                Log.d("addressLine", e.message.toString())
                null
            }
        }
    }

    fun notifyDataSetChanged(map:HashMap<MovementData,Boolean>) {
        list.clear()
        list.putAll(map)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HistoryViewHolder {
        return HistoryViewHolder(ItemBinding.inflate(LayoutInflater.from(parent.context)))
    }

    override fun getItemCount(): Int {
        return list.size
    }

    override fun onBindViewHolder(holder: HistoryViewHolder, position: Int) {
        holder.bind(list[p] )
    }
}
