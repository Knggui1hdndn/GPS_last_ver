package com.gps.speedometer.odometer.gpsspeedtracker.biiling

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.android.billingclient.api.AcknowledgePurchaseParams
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.BillingClientStateListener
import com.android.billingclient.api.BillingFlowParams
import com.android.billingclient.api.BillingResult
import com.android.billingclient.api.ProductDetails
import com.android.billingclient.api.Purchase
import com.android.billingclient.api.PurchasesUpdatedListener
import com.android.billingclient.api.QueryProductDetailsParams
import com.android.billingclient.api.queryProductDetails
import com.gps.speedometer.odometer.gpsspeedtracker.R
import com.gps.speedometer.odometer.gpsspeedtracker.databinding.ActivitySupVipBinding
import com.gps.speedometer.odometer.gpsspeedtracker.interfaces.SubVipInterface
import com.gps.speedometer.odometer.gpsspeedtracker.presenter.SubVipPresenter
import com.gps.speedometer.odometer.gpsspeedtracker.ui.ShowWebActitvity
import com.gps.speedometer.odometer.gpsspeedtracker.ui.adpater.AdapterSupVip
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


class SubVipActivity : BaseActivity(), SubVipInterface.View {
    private var _binding: ActivitySupVipBinding? = null
    private val binding get() = _binding!!
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = ActivitySupVipBinding.inflate(layoutInflater)
        setContentView(binding.root)
        val adapter = AdapterSupVip(
            listOf(
                R.drawable.user_star_1,
                R.drawable.user_star_2,
                R.drawable.user_star_3,
                R.drawable.user_star_4
            )
        )
        with(binding)
        {
            handelClick(1)
            mLinearMonth.setOnClickListener {
                handelClick(1)
                currentProduct = subMonthProduct
                setupBilling()
                showPurchaseDialog()
            }
            mLinearWeek.setOnClickListener { handelClick(2) }
            mLinearLifeTime.setOnClickListener { handelClick(3) }
            txtPolicy.setOnClickListener {
                val intent = Intent(this@SubVipActivity, ShowWebActitvity::class.java)
                intent.putExtra("link", "https://sites.google.com/view/policytosforgpsspeedometer/")
                startActivity(intent)
            }
        }
        binding.viewPager2.adapter = adapter
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }

    override fun setBackGroundDefault(draw: Int) {
        with(binding) {
            val drawable = getDrawable(draw)
            mLinearMonth.background = drawable
            mLinearWeek.background = drawable
            mLinearLifeTime.background = drawable
        }
    }

    override fun setTextColorDefault(color: Int) {
        with(binding) {
            val color = getColor(color)
            txt1.setTextColor(color)
            txt2.setTextColor(color)
            txt3.setTextColor(color)
        }
    }

    override fun setBackGroundClick(draw: Int, position: Int) {
        with(binding) {
            val drawable = getDrawable(draw)
            when (position) {
                1 -> mLinearMonth.background = drawable
                2 -> mLinearWeek.background = drawable
                3 -> mLinearLifeTime.background = drawable
            }
        }
    }

    override fun setTextColorClick(color: Int, position: Int) {
        with(binding) {
            val color = getColor(color)
            when (position) {
                1 -> txt1.setTextColor(color)
                2 -> txt2.setTextColor(color)
                3 -> txt3.setTextColor(color)
            }
        }
    }

    override fun handelClick(position: Int) {
        setBackGroundDefault(R.drawable.boder_buy_unclick)
        setBackGroundClick(R.drawable.border_buy_click, position)
        setTextColorDefault(R.color.white)
        setTextColorClick(R.color.yellow, position)
    }

    private var restore = false

    private val purchasesUpdatedListener =
        PurchasesUpdatedListener { billingResult, purchases ->
            if (billingResult.responseCode == BillingClient.BillingResponseCode.OK && purchases != null) {
                for (purchase in purchases) {
                    if (purchase.purchaseState == Purchase.PurchaseState.PURCHASED) {
                        proApplication.isSubVip = true
                        //update UI hiển thị đã mua
                        if (!purchase.isAcknowledged) {
                            val acknowledgePurchaseParams =
                                AcknowledgePurchaseParams.newBuilder()
                                    .setPurchaseToken(purchase.purchaseToken).build()

                            billingClient?.acknowledgePurchase(acknowledgePurchaseParams) { billingResult ->
                            }
                        }
                    }
                }
            } else if (billingResult.responseCode == BillingClient.BillingResponseCode.USER_CANCELED) {
            } else {
            }
        }

    private val billingClientStateListener = object : BillingClientStateListener {
        override fun onBillingSetupFinished(billingResult: BillingResult) {
            if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                lifecycleScope.launch {
                    processPurchases()
                    getActivePurchase()
                    runOnUiThread {
                        if (proApplication.isSubVip) {
                            //update UI hien thi da mua vip
                        } else {
                            //update UI hien thi chua mua
                        }

                        if (restore) {
                            Toast.makeText(
                                this@SubVipActivity,
                                "Restore Success",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                }
            } else {
                Toast.makeText(
                    this@SubVipActivity,
                    billingResult.debugMessage,
                    Toast.LENGTH_SHORT
                ).show()
                Log.d("ssssssssssssss", billingResult.debugMessage)
            }
        }

        override fun onBillingServiceDisconnected() {
            Toast.makeText(
                this@SubVipActivity,
                "Connect billing service failed! Check Your network connection.",
                Toast.LENGTH_SHORT
            ).show()
        }

    }

    private var subWeekProduct: ProductDetails? = null
    private var subMonthProduct: ProductDetails? = null
    private var lifeTimeProduct: ProductDetails? = null
    private var currentProduct: ProductDetails? = null


    private fun nextActivity() {

    }

    private fun setupBilling() {
        billingClient = BillingClient.newBuilder(this)
            .setListener(purchasesUpdatedListener)
            .enablePendingPurchases()
            .build()
        billingClient!!.startConnection(billingClientStateListener)
    }

    suspend fun processPurchases() {
        subMonthProduct =
            getPurchasesProductDetail("pack_sub_month", BillingClient.ProductType.SUBS)
        subWeekProduct = getPurchasesProductDetail("pack_sub_week", BillingClient.ProductType.SUBS)
        lifeTimeProduct =
            getPurchasesProductDetail("pack_life_time", BillingClient.ProductType.INAPP)
        currentProduct = subWeekProduct
//        when (ConfigModel.subDefaultPack) {
//            "pack_sub_week" -> {
//                currentProduct = subWeekProduct
//                listSubItem[0].setSelected(false)
//                listSubItem[1].setSelected(true)
//                listSubItem[2].setSelected(false)
//            }
//            "pack_sub_month" -> {
//                currentProduct = subMonthProduct
//                listSubItem[0].setSelected(true)
//                listSubItem[1].setSelected(false)
//                listSubItem[2].setSelected(false)
//            }
//            "pack_life_time" -> {
//                currentProduct = lifeTimeProduct
//                listSubItem[0].setSelected(false)
//                listSubItem[1].setSelected(false)
//                listSubItem[2].setSelected(true)
//            }
//            else -> {
//                currentProduct = subWeekProduct
//            }
//        }
        //  runOnUiThread {
        fillDataToUI()
        // }
    }

    private fun fillDataToUI() {

    }

    private suspend fun getPurchasesProductDetail(
        packId: String,
        productType: String
    ): ProductDetails? {
        val productList = mutableListOf<QueryProductDetailsParams.Product>()
        productList.add(
            QueryProductDetailsParams.Product.newBuilder().setProductId(packId)
                .setProductType(productType).build()
        )
        val paramsSub = QueryProductDetailsParams.newBuilder()
        paramsSub.setProductList(productList)
        val productDetailsResult = withContext(Dispatchers.IO) {
            billingClient!!.queryProductDetails(paramsSub.build())
        }
        if (!productDetailsResult.productDetailsList.isNullOrEmpty()) {
            return productDetailsResult.productDetailsList!!.first()
        } else {
            return null
        }
    }


    private fun onClick() {
        // logic click hien thi subdialog

//        binding.btnClose.setOnClickListener {
//            nextActivity()
//        }
//        binding.btnContinue.setOnClickListener {
//            if (proApplication.isSubVip) {
//                nextActivity()
//            } else {
//                showPurchaseDialog()
//            }
//        }
//        binding.btnSubMonth.setOnClickListener {
//            currentProduct = subMonthProduct
//            showPurchaseDialog()
//        }
//        binding.btnSubWeek.setOnClickListener {
//            currentProduct = subWeekProduct
//            showPurchaseDialog()
//        }
//        binding.btnLifeTime.setOnClickListener {
//            currentProduct = lifeTimeProduct
//            showPurchaseDialog()
//        }
//
//        binding.btnReStore.setOnClickListener {
//            restore = true
//            billingClient!!.startConnection(billingClientStateListener)
//        }
    }

    private fun showPurchaseDialog() {
        if (currentProduct != null) {
            val productDetailsParamsList: List<BillingFlowParams.ProductDetailsParams>
            if (currentProduct!!.productType == BillingClient.ProductType.SUBS) {
                productDetailsParamsList = listOf(
                    BillingFlowParams.ProductDetailsParams.newBuilder()
                        .setProductDetails(currentProduct!!)
                        .setOfferToken(currentProduct!!.subscriptionOfferDetails!!.first().offerToken)
                        .build()
                )
            } else {
                productDetailsParamsList = listOf(
                    BillingFlowParams.ProductDetailsParams.newBuilder()
                        .setProductDetails(currentProduct!!)
                        .build()
                )
            }
            val billingFlowParams = BillingFlowParams.newBuilder()
                .setProductDetailsParamsList(productDetailsParamsList)
                .build()
            billingClient!!.launchBillingFlow(this@SubVipActivity, billingFlowParams)
        } else {
            restore = false
            billingClient!!.startConnection(billingClientStateListener)
        }
    }

}