package com.example.gps.ui

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.app.Dialog
import android.content.DialogInterface
import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatDelegate
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.gps.R
import com.example.gps.SettingConstants
import com.example.gps.dao.MyDataBase
import com.example.gps.model.MovementData

class HistoryActivity : AppCompatActivity() {
    private lateinit var toolbar: Toolbar
    private lateinit var rcy: RecyclerView
    private lateinit var adapter: HistoryAdapter
    private lateinit var myDataBase: MyDataBase
    private lateinit var mutableListMovementData: MutableList<MovementData>
    val check = AppCompatDelegate.getDefaultNightMode() != AppCompatDelegate.MODE_NIGHT_YES
    val color = if (check) ColorStateList.valueOf(Color.BLACK) else ColorStateList.valueOf(
        Color.WHITE
    )

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setUpActivity()
    }

    private fun setUpActivity() {
        toolbar = findViewById(R.id.mToolBar)
        rcy = findViewById(R.id.rcy)
        toolbar.setTitleTextColor(color)
        setSupportActionBar(toolbar)
        supportActionBar?.setHomeAsUpIndicator(R.drawable.baseline_arrow_back_24)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        myDataBase = MyDataBase.getInstance(this)
        val mng = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        mutableListMovementData = myDataBase.movementDao().getAllMovementData()
        mutableListMovementData.reverse()
        adapter = HistoryAdapter(
            getSharedPreferences(
                SettingConstants.SETTING,
                MODE_PRIVATE
            ).getInt(
                SettingConstants.COLOR_DISPLAY,
                2
            )
        )
        adapter.notifyDataSetChanged(mutableListMovementData)
        val direction = DividerItemDecoration(this, LinearLayoutManager.VERTICAL)
        rcy.addItemDecoration(direction)
        rcy.layoutManager = mng
        rcy.adapter = adapter
    }

    private fun getDialog(): Dialog {
        val mutableList = myDataBase.movementDao().getAllMovementData()
        return AlertDialog.Builder(this@HistoryActivity)
            .setPositiveButton(if (mutableList.size > 0) "Xóa" else "OK") { dialogInterface: DialogInterface, i: Int ->
                myDataBase.movementDao().deleteAll()
                adapter.notifyDataSetChanged(mutableListOf())
                dialogInterface.dismiss()
            }
            .setNegativeButton("Đóng") { dialogInterface: DialogInterface, i: Int -> dialogInterface.dismiss() }
            .setTitle("Thông báo !")
            .setMessage(if (mutableList.size > 0) "Xác nhận xóa tất cả?" else "Rỗng").create()
    }

    override fun onResume() {
        super.onResume()
        Log.d(
            "NotifyDataSetChanged",
            "onResume${mutableListMovementData.size}   ${
                myDataBase.movementDao().getAllMovementData().size
            }"
        )
        if (mutableListMovementData.size != myDataBase.movementDao().getAllMovementData().size) {
            adapter.notifyDataSetChanged(myDataBase.movementDao().getAllMovementData())
            adapter.notifyDataSetChanged()
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.history_delete_all, menu)
        menu!!.getItem(0).iconTintList =color


            return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.delete) getDialog().show()
        if (item.itemId == android.R.id.home) finish()
        return true
    }
}