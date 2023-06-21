package com.example.gps.ui

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.app.Dialog
import android.content.DialogInterface
import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.gps.R
import com.example.gps.dao.MyDataBase
import com.example.gps.databinding.ActivityMainBinding
import com.example.gps.databinding.DialogDeleteBinding
import com.example.gps.databinding.DialogRateBinding
import com.example.gps.model.MovementData
import com.example.gps.`object`.SharedData
import com.example.gps.ui.adpater.HistoryAdapter
import com.example.gps.ui.adpater.sendHashMapChecked
import com.example.gps.utils.ColorUtils

class HistoryActivity : AppCompatActivity(), sendHashMapChecked {
    private lateinit var adapter: HistoryAdapter
    private lateinit var myDataBase: MyDataBase
    private lateinit var mutableListMovementData: MutableList<MovementData>
    private lateinit var binding: ActivityMainBinding
    val check = AppCompatDelegate.getDefaultNightMode() != AppCompatDelegate.MODE_NIGHT_YES
    val color = if (check) ColorStateList.valueOf(Color.BLACK) else ColorStateList.valueOf(
        Color.WHITE
    )
    private var isChecked = false
    private var itemChecked = mutableMapOf<MovementData, Boolean>()
    var list = mutableMapOf<MovementData, Boolean>()

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setUpActivity()
    }

    private fun setUpActivity() {
        setUpActionBar()
        myDataBase = MyDataBase.getInstance(this)
        mutableListMovementData = myDataBase.movementDao().getAllMovementData()
        if (SharedData.time.value != 0L) mutableListMovementData.removeAt(mutableListMovementData.size - 1)


        val mng = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        adapter = HistoryAdapter(this)
        list = mutableListMovementData.map { it to false }.toMap().toMutableMap()
        val direction = DividerItemDecoration(this, LinearLayoutManager.VERTICAL)
        with(binding) {
            rcy.addItemDecoration(direction)
            rcy.layoutManager = mng
            rcy.adapter = adapter
            adapter.notifyDataSetChanged(list)
            checkbox.setOnCheckedChangeListener { buttonView, isChecked ->
                adapter.setShowCheck()
                this@HistoryActivity.isChecked = isChecked
                checkbox.visibility = View.GONE
                mLinear.visibility = View.VISIBLE
                if (itemChecked.size == 0 || list.size == 0) binding.delete.isEnabled = false
            }
            delete.setOnClickListener {
                getDialog().show()
            }

            var checkAll = true
            btnAll.setOnClickListener {
                list.replaceAll { key, value ->
                    checkAll
                }
                checkAll = !checkAll
                adapter.notifyDataSetChanged(list)
            }
        }
    }

    private fun setUpActionBar() {
        setSupportActionBar(binding.mToolBar)
        supportActionBar?.setHomeAsUpIndicator(R.drawable.baseline_arrow_back_24)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }

    @SuppressLint("NotifyDataSetChanged")
    fun getDialog(): Dialog {
        val dialogBinding = DialogDeleteBinding.inflate(LayoutInflater.from(this))
        val dialog = Dialog(this).apply {
            window?.setBackgroundDrawableResource(android.R.color.transparent);
            setContentView(dialogBinding.root)
            window?.setLayout(
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.WRAP_CONTENT
            )
            if (ColorUtils.isThemeDark()) {

                dialogBinding.btnCancel.setTextColor(Color.WHITE)
                dialogBinding.btnCancel.backgroundTintList = ColorStateList.valueOf(Color.BLACK)
                dialogBinding.btnDelete.setTextColor(Color.BLACK)
                dialogBinding.btnDelete.backgroundTintList = ColorStateList.valueOf(Color.WHITE)
            }
            dialogBinding.btnCancel.setOnClickListener {
                dismiss()
            }
            dialogBinding.btnDelete.setOnClickListener {
                list.keys.removeAll(itemChecked.keys)
                adapter.notifyDataSetChanged(list)
                itemChecked.keys.forEach {
                    myDataBase.movementDao().delete(it)
                }
                if (itemChecked.size == 0 || list.size == 0) binding.delete.isEnabled = false
                dismiss()
            }
        }
        return dialog
    }

    @SuppressLint("NotifyDataSetChanged")
    override fun onResume() {
        super.onResume()
        if (list.size != myDataBase.movementDao().getAllMovementData().size) {
            val hashMap = myDataBase.movementDao().getAllMovementData().map {
                it to false
            }.toMap()
            adapter.notifyDataSetChanged(HashMap(hashMap))
            adapter.notifyDataSetChanged()
        }
    }


    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) handleCheckedStateChanged()
        return true
    }

    @SuppressLint("NotifyDataSetChanged")
    override fun onBackPressed() {
        handleCheckedStateChanged()
    }

    private fun handleCheckedStateChanged() {
        if (isChecked) {
            binding.checkbox.isChecked = false
            isChecked = false;
            binding.mLinear.visibility = View.GONE
            binding.checkbox.visibility = View.VISIBLE
            list.replaceAll { key, value ->
                false
            }
            adapter.notifyDataSetChanged(list)

        } else finish()
    }

    @SuppressLint("NotifyDataSetChanged")
    override fun sendHashMapChecked(movementData: MovementData, isChecked: Boolean) {
        itemChecked.put(movementData, isChecked)
        list.put(movementData, isChecked)
        try {
            adapter.notifyDataSetChanged(list)
        } catch (e: Exception) {
        }
        Log.d("ddddddđ", itemChecked.size.toString())
        itemChecked = itemChecked.filter { it.value }.toMutableMap()
        if (itemChecked.size == 0 || list.size == 0) binding.delete.isEnabled =
            false else binding.delete.isEnabled = true
    }
}