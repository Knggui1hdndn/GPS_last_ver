package com.gps.speedometer.odometer.gpsspeedtracker.ui


import android.annotation.SuppressLint
import android.app.Service
import android.content.Intent
import android.content.SharedPreferences
import android.content.res.ColorStateList
import android.content.res.Configuration
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.os.Build
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.vectordrawable.graphics.drawable.Animatable2Compat
import androidx.viewpager2.widget.ViewPager2
import com.access.pro.config.ConfigModel
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.load.resource.gif.GifDrawable
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.google.android.material.tabs.TabLayoutMediator
import com.gps.speedometer.odometer.gpsspeedtracker.application.MyApplication
import com.gps.speedometer.odometer.gpsspeedtracker.R
import com.gps.speedometer.odometer.gpsspeedtracker.biiling.BaseActivity
import com.gps.speedometer.odometer.gpsspeedtracker.biiling.SubVipActivity
import com.gps.speedometer.odometer.gpsspeedtracker.constants.ColorConstants
import com.gps.speedometer.odometer.gpsspeedtracker.constants.MyLocationConstants
import com.gps.speedometer.odometer.gpsspeedtracker.constants.SettingConstants
import com.gps.speedometer.odometer.gpsspeedtracker.databinding.ActivityMain2Binding
import com.gps.speedometer.odometer.gpsspeedtracker.`object`.CheckPermission
import com.gps.speedometer.odometer.gpsspeedtracker.`object`.SharedData
import com.gps.speedometer.odometer.gpsspeedtracker.service.MyService
import com.gps.speedometer.odometer.gpsspeedtracker.ui.adpater.TabAdapter
import com.gps.speedometer.odometer.gpsspeedtracker.utils.ColorUtils


interface onRecever {
    fun sendDataToSecondFragment()

}

class MainActivity2 : BaseActivity(), onRecever, RequestListener<GifDrawable> {

    companion object {
        const val REQUEST_CHECK_SETTING = 1

    }

    private var x = 1
    private var y = 1

    lateinit var binding: ActivityMain2Binding
    private lateinit var tabAdapter: TabAdapter
    lateinit var viewPager: ViewPager2
    private lateinit var sharedPreferences: SharedPreferences
    var color = if (ColorUtils.isThemeDark()) Color.WHITE else Color.BLACK

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CHECK_SETTING) {
            if (resultCode == RESULT_CANCELED) {
                Toast.makeText(
                    this,
                    "You cannot use this feature without granting the necessary permissions.",
                    Toast.LENGTH_SHORT
                ).show()
                val frag =
                    supportFragmentManager.findFragmentByTag("f" + binding.viewPager2.currentItem)?.childFragmentManager!!.findFragmentById(
                        R.id.frag
                    ) as ParameterFragment
                frag.stopService()

                getSharedPreferences("state", Service.MODE_PRIVATE).edit().putString(
                    MyLocationConstants.STATE, MyLocationConstants.STOP
                ).apply()
            } else {
                val intent = Intent(applicationContext, MyService::class.java)
                intent.action = MyLocationConstants.START
                applicationContext.startService(intent)
            }
        }
    }

    @SuppressLint("MissingPermission")
    @RequiresApi(Build.VERSION_CODES.P)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        getConfigData(false)
        setUpActivity()

    }

    @SuppressLint("MissingPermission", "ResourceType")
    private fun setUpActivity() {
        binding = ActivityMain2Binding.inflate(layoutInflater)
        SharedData.activity = this
        setContentView(binding.root)
        binding.toolbar.title = "ODOMETER"
        binding.toolbar.setTitleTextColor(color)
        setSupportActionBar(binding.toolbar)
        sharedPreferences = getSharedPreferences(SettingConstants.SETTING, MODE_PRIVATE)
        tabAdapter = TabAdapter(supportFragmentManager, lifecycle)
        viewPager = binding.viewPager2
        viewPager.setPageTransformer { _, position ->
            if (position == 2F) {
                binding.viewPager2.isUserInputEnabled = false
            }
        }

        SharedData.color.observe(this) {
            binding.tabLayout.setTabTextColors(getColor(R.color.unchanged), getColor(getColorRes()))
            binding.tabLayout.setSelectedTabIndicatorColor(getColor(getColorRes()))
        }

        binding.viewPager2.adapter = tabAdapter
        TabLayoutMediator(binding.tabLayout, viewPager) { tab, position ->
            when (position) {
                0 -> tab.text = "Analog"
                1 -> tab.text = "Digital"
                2 -> tab.text = "Map"
            }
        }.attach()
        val viewMode = sharedPreferences.getInt(SettingConstants.ViEW_MODE, 0)
        if (MyApplication.check) binding.viewPager2.currentItem = viewMode - 1;MyApplication.check =
            false
    }


    private fun getColorRes(): Int {
        val intColor = getSharedPreferences(
            SettingConstants.SETTING,
            Service.MODE_PRIVATE
        ).getInt(SettingConstants.COLOR_DISPLAY, ColorConstants.COLOR_DEFAULT)
        if (intColor == 0) {
            return if (ColorUtils.isThemeDark()) R.color.white else R.color.black
        }
        when (intColor) {
            2 -> return R.color.color_2
            3 -> return R.color.color_3
            4 -> return R.color.color_4
            5 -> return R.color.color_5
            6 -> return R.color.color_6
        }
        return R.color.color_2
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        if (resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            menu?.getItem(0)?.isVisible = true
        }
        for (i in 0 until menu!!.size()) {
            if (menu.getItem(i).itemId != R.id.subcribe) {
                menu.getItem(i).iconTintList = ColorStateList.valueOf(color)
            } else {
                var check = ConfigModel.showSub
                if (check) {
                    check =  !proApplication.isSubVip
                }
                menu.getItem(i).isVisible = check
            }
        }

        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.settings -> startActivity(Intent(this, Setting::class.java))
            R.id.history -> startActivity(Intent(this, HistoryActivity::class.java))
            R.id.tip -> {
//                val intent = Intent         (this, MoreTipActivity::class.java)
//                intent.putExtra("activityLaunchedFrom", "Main2")
//                startActivity(intent)
            }

            R.id.subcribe -> {
                val intent = Intent(this, SubVipActivity::class.java)
                startActivity(intent)
            }

            R.id.rotation -> {
                val check = binding.container.rotationX
                binding.container.rotationX = if (check == 0F) 180F else 0F
            }
        }
        return true
    }

    override fun onPause() {
        super.onPause()
        if (!CheckPermission.hasLocationSetting(this)) {
            stopService(Intent(this, MyService::class.java))
        }
    }


    override fun sendDataToSecondFragment() {
        when (binding.viewPager2.currentItem) {
            0 -> {
                val frag =
                    supportFragmentManager.findFragmentByTag("f0")?.childFragmentManager!!.findFragmentById(
                        R.id.signal
                    ) as FragmentSignal
                frag.onStrengthGPSDataReceived(0, 0)
            }

            1 -> {
                val frag =
                    supportFragmentManager.findFragmentByTag("f1")?.childFragmentManager!!.findFragmentById(
                        R.id.signal
                    ) as FragmentSignal
                frag.onStrengthGPSDataReceived(0, 0)
            }
        }
    }

    @SuppressLint("CheckResult")
    override fun onResume() {
        super.onResume()
        binding.mess.visibility = View.GONE
        val shared = getSharedPreferences("state", MODE_PRIVATE)
        val speed = SharedData.currentSpeedLiveData.value?.keys?.first()
        if (shared.getString(
                MyLocationConstants.STATE,
                null
            ) == MyLocationConstants.START || SharedData.convertSpeed(speed!!).toInt() >= 1
        ) {
            showWhenRun()
        }
        if (shared.getString(
                MyLocationConstants.STATE,
                null
            ) == MyLocationConstants.START && SharedData.convertSpeed(speed!!).toInt() == 0
        ) {
            showWhenStart()
        }
    }

    fun showMess() {
        x = 1
        y = 1
        SharedData.currentSpeedLiveData.observe(this) {

            it.keys.first().let { it1 ->
                if (y == 1 && SharedData.convertSpeed(it1).toInt() >= 1) {
                    showWhenRun()
                    y = 2
                } else if (SharedData.convertSpeed(it1).toInt() == 0 && x == 1) {
                    showWhenStart()
                    x = 2
                }
            }
        }
    }

    @SuppressLint("CheckResult")
    fun showWhenRun() {
        val glide = Glide.with(this).asGif()
        glide.load(R.drawable.start)
        binding.mess.visibility = View.VISIBLE
        glide.listener(this).into(binding.mess)
    }

    @SuppressLint("CheckResult")
    fun showWhenStart() {
        x = 1
        val glide = Glide.with(this).asGif()
        glide.load(R.drawable.not_start)
        binding.mess.visibility = View.VISIBLE
        glide.listener(this).into(binding.mess)
    }


    override fun onLoadFailed(
        e: GlideException?,
        model: Any?,
        target: Target<GifDrawable>?,
        isFirstResource: Boolean
    ): Boolean {
        return false
    }

    override fun onResourceReady(
        resource: GifDrawable?,
        model: Any?,
        target: Target<GifDrawable>?,
        dataSource: DataSource?,
        isFirstResource: Boolean
    ): Boolean {
        resource?.setLoopCount(1);
        resource?.registerAnimationCallback(object : Animatable2Compat.AnimationCallback() {
            override fun onAnimationEnd(drawable: Drawable?) {
                binding.mess.visibility = View.GONE
            }
        })
        return false
    }
}