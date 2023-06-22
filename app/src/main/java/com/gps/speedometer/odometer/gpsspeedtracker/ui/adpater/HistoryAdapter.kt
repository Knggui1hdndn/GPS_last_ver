package com.gps.speedometer.odometer.gpsspeedtracker.ui.adpater

import android.annotation.SuppressLint
import android.content.Intent
import android.location.Geocoder
import android.os.Build
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CompoundButton
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import com.gps.speedometer.odometer.gpsspeedtracker.model.MovementData
import com.gps.speedometer.odometer.gpsspeedtracker.`object`.SharedData
import com.gps.speedometer.odometer.gpsspeedtracker.ui.ShowActivity
import com.gps.speedometer.odometer.gpsspeedtracker.databinding.ItemBinding
import java.lang.Exception
import java.util.Calendar
import java.util.Locale

interface sendHashMapChecked {
    fun sendHashMapChecked(movementData: MovementData, isChecked: Boolean)
}

class HistoryAdapter(private val sendHashMapChecked: sendHashMapChecked) :
    RecyclerView.Adapter<HistoryAdapter.HistoryViewHolder>() {

    private var list: MutableMap<MovementData, Boolean> = hashMapOf()
    private var isShowCheck = false

    inner class HistoryViewHolder(private val binding: ItemBinding) :
        RecyclerView.ViewHolder(binding.root) {
        @SuppressLint("SetTextI18n")
        fun bind(movementData: MovementData, isChecked: Boolean) {

            with(binding) {
                checkbox.setOnCheckedChangeListener { compoundButton: CompoundButton, b: Boolean ->
                    sendHashMapChecked.sendHashMapChecked(movementData, b)

                }
                checkbox.isChecked = isChecked;
                checkbox.visibility = if (isShowCheck) {
                    View.VISIBLE
                } else {
                    View.GONE
                };

                binding.mLinear.setOnClickListener {
                    if (!isShowCheck) {
                        val intent = Intent(it.context, ShowActivity::class.java)
                        it.context.getSharedPreferences("show", AppCompatActivity.MODE_PRIVATE)
                            .edit().putInt("id", movementData.id).apply()
                        binding.root.context.startActivity(intent)
                    }
                }

                val calendar = Calendar.getInstance()
                txtDate.text =
                    "${calendar.get(Calendar.DAY_OF_MONTH)}/${calendar.get(Calendar.MONTH) + 1}/${
                        calendar.get(
                            Calendar.YEAR
                        )
                    }"
                txtMaxSpeed.text = SharedData.convertSpeed(movementData.maxSpeed).toInt()
                    .toString() + SharedData.toUnit

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

    fun notifyDataSetChanged(map: MutableMap<MovementData, Boolean>) {
        list.clear()
        list.putAll(map)
        list = list.toSortedMap(compareByDescending { it.id })
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HistoryViewHolder {
        return HistoryViewHolder(ItemBinding.inflate(LayoutInflater.from(parent.context)))
    }

    override fun getItemCount(): Int {
        return list.size
    }

    fun setShowCheck() {
        isShowCheck = !isShowCheck
        notifyDataSetChanged()
    }


    override fun onBindViewHolder(holder: HistoryViewHolder, position: Int) {

        holder.bind(list.keys.elementAt(position), list.values.elementAt(position))
    }
}
