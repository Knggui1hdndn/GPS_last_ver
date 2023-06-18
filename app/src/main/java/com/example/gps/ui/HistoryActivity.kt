package com.example.gps.ui

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.app.Dialog
import android.content.DialogInterface
import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.gps.R
import com.example.gps.dao.MyDataBase
import com.example.gps.databinding.ActivityMainBinding
import com.example.gps.model.MovementData
import java.util.stream.Stream

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
        setSupportActionBar(binding.mToolBar)
        supportActionBar?.setHomeAsUpIndicator(R.drawable.baseline_arrow_back_24)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        myDataBase = MyDataBase.getInstance(this)
        val mng = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        mutableListMovementData = myDataBase.movementDao().getAllMovementData()
        adapter = HistoryAdapter(this)
        list=mutableListMovementData.map { it to false }.toMap().toMutableMap()
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

    @SuppressLint("NotifyDataSetChanged")
    private fun getDialog(): Dialog {
        val mutableList = myDataBase.movementDao().getAllMovementData()
        return AlertDialog.Builder(this@HistoryActivity)
            .setPositiveButton(if (mutableList.size > 0) "Xóa" else "OK") { dialogInterface: DialogInterface, i: Int ->
                list.keys.removeAll(itemChecked.keys)
                adapter.notifyDataSetChanged(list)
                itemChecked.keys.forEach{
                    myDataBase.movementDao().delete(it)
                }
                dialogInterface.dismiss()
            }
            .setNegativeButton("Đóng") { dialogInterface: DialogInterface, i: Int -> dialogInterface.dismiss() }
            .setTitle("Thông báo !")
            .setMessage(if (mutableList.size > 0) "Xác nhận xóa tất cả?" else "Rỗng").create()
    }

    @SuppressLint("NotifyDataSetChanged")
    override fun onResume() {
        super.onResume()
//        if (mutableListMovementData.size != myDataBase.movementDao().getAllMovementData().size) {
//            val hashMap = mutableListMovementData.map {
//                it to false
//            }.toMap()
//            adapter.notifyDataSetChanged(HashMap(hashMap))
//            adapter.notifyDataSetChanged()
//        }
    }


    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) finish()
        return true
    }

    @SuppressLint("NotifyDataSetChanged")
    override fun onBackPressed() {
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
        itemChecked = itemChecked.filter { it.value }.toMutableMap()
    }
}