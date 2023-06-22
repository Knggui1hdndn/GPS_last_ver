package com.gps.speedometer.odometer.gpsspeedtracker.ui

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.recyclerview.widget.LinearLayoutManager
 import com.gps.speedometer.odometer.gpsspeedtracker.model.MoreTip
import com.gps.speedometer.odometer.gpsspeedtracker.ui.adpater.AdapterMoreTip
import com.gps.speedometer.odometer.gpsspeedtracker.databinding.ActivityMoreTipBinding

class MoreTipActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMoreTipBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMoreTipBinding.inflate(layoutInflater)
        setContentView(binding.root)

        with(binding) {
            imgHome.setOnClickListener {
                Log.d("Sssssssssss",intent.getStringExtra("activityLaunchedFrom").toString())
                if (intent.getStringExtra("activityLaunchedFrom") != "Main2") startActivity(
                    Intent(
                        this@MoreTipActivity,
                        MainActivity2::class.java
                    )
                )
                else finish()
            }
            imgRate.setOnClickListener {
            //    MainActivity2().getDialogRate().show()
            }
        }

        val list = listOf<MoreTip>(
            MoreTip(
                "https://road.cc/content/feature/14-fastest-longest-and-maddest-cycling-world-records-214554",
                "https://cdn.road.cc/sites/default/files/styles/main_width/public/images/News/GB%20women%27s%20team%20pursuit%2C%20Track%20Worlds%202015%20%28copyright%20Britishcycling.org.uk%29.jpg",
                "Incredible cycling world records: 14 of the fastest, longest and maddest efforts on a bike"
            ),
            MoreTip(
                "https://s1partscenter.com/blog/entries/on-the-road-why-are-odometers-so-important",
                "https://cdn2.s1partscenter.com/images/source/e5e/195/b49/df793d2f62.png",
                "Why are Odometers so Important?"
            ),
            MoreTip(
                "https://mechanicbase.com/electric/odometer-reading/",
                "https://mechanicbase.com/wp-content/uploads/2022/04/odometer-reading.jpg",
                "Odometer Reading – What is it & How to check it?"
            ),
            MoreTip(
                "https://expertvagabond.com/best-travel-tips/",
                "https://expertvagabond.com/wp-content/uploads/best-travel-tips-guide-768x512.jpg.webp",
                "My 50 Best Travel Tips After 10 Years Traveling The World"
            ),
            MoreTip(
                "https://www.bikeradar.com/advice/beginners-cycling-tips-25-essential-pieces-of-advice-for-new-cyclists/",
                "https://images.immediate.co.uk/production/volatile/sites/21/2022/05/Cube-Axial-WS-12-45369da.jpg?webp=true&quality=90&resize=408%2C271",
                "Beginner’s cycling tips: 25 essential pieces of advice for new cyclists"
            ),
            MoreTip(
                "https://www.wikihow.com/Stop-Speeding",
                "https://www.wikihow.com/images/thumb/d/db/Stop-Speeding-Step-1-Version-2.jpg/v4-460px-Stop-Speeding-Step-1-Version-2.jpg.webp",
                "How to Stop Speeding"
            ),
            MoreTip(
                "https://www.wikihow.com/Drive-a-Car",
                "https://www.wikihow.com/images/thumb/8/84/Drive-a-Car-Step-1-Version-2.jpg/v4-460px-Drive-a-Car-Step-1-Version-2.jpg.webp",
                "https://www.wikihow.com/Stop-Speeding"
            ),
            MoreTip(
                "https://www.redbull.com/int-en/cycling-strength-exercises-gym",
                "https://img.redbull.com/images/c_crop,x_0,y_0,h_4134,w_6201/c_fill,w_1000,h_640/q_auto,f_auto/redbullcom/2017/11/08/7c8abdd5-4465-428e-b26a-530d0075151e/chloe-dygert",
                "9 strength exercises that will make you a better cyclist"
            ),
            MoreTip(
                "https://www.healthline.com/health/how-many-calories-do-you-burn-biking",
                "https://th.bing.com/th/id/R.c6f24e65ca224a16d1a00c3ec4fb02bc?rik=DFgIE1Z2h5h%2bqw&riu=http%3a%2f%2fthuocthang.com.vn%2fvnt_upload%2fFile%2fImage%2fPhan_biet_cac_loai_xe_dap_the_thao_va_cach_chon_mua_phan_2.jpg&ehk=RU6nTzwu4J1U5f449kut%2bFg0kpCLh3uF86gILKr%2fUho%3d&risl=&pid=ImgRaw&r=0",
                "12 Benefits of Cycling, Plus Safety Tips"
            ),
            MoreTip(
                "https://expertvagabond.com/best-travel-tips/",
                "https://expertvagabond.com/wp-content/uploads/best-travel-tips-guide-768x512.jpg.webp",
                "My 50 Best Travel Tips After 10 Years Traveling The World"
            ),
        )
        val adapterMoreTip = AdapterMoreTip(list)
        val mng = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        binding.mRcy.layoutManager = mng
        binding.mRcy.adapter = adapterMoreTip
    }
}