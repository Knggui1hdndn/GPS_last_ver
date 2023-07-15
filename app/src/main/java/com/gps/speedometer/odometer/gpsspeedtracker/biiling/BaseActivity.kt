package com.gps.speedometer.odometer.gpsspeedtracker.biiling

import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.lifecycle.lifecycleScope
import com.access.pro.adcontrol.AdsBannerView
import com.access.pro.callBack.OnShowNativeListener
import com.android.billingclient.api.AcknowledgePurchaseParams
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.QueryPurchasesParams
import com.android.billingclient.api.queryPurchasesAsync
import com.google.android.gms.ads.nativead.NativeAd
import com.google.firebase.analytics.FirebaseAnalytics
import com.gps.speedometer.odometer.gpsspeedtracker.interfaces.ListenAds
import kotlinx.coroutines.launch

open class BaseActivity : com.access.pro.activity.BaseActivity() {
    private lateinit var firebaseAnalytics: FirebaseAnalytics
    var billingClient: BillingClient? = null


    suspend fun getActivePurchase(): Boolean {
        val subResult = billingClient?.queryPurchasesAsync(
            QueryPurchasesParams.newBuilder()
                .setProductType(BillingClient.ProductType.SUBS).build()
        )
        val inappResult = billingClient?.queryPurchasesAsync(
            QueryPurchasesParams.newBuilder()
                .setProductType(BillingClient.ProductType.INAPP).build()
        )
        proApplication.isSubVip =
            (subResult != null && subResult.purchasesList.isNotEmpty())
                    || (inappResult != null && inappResult.purchasesList.isNotEmpty())
        return proApplication.isSubVip
    }
   open fun showBannerAds(viewContainer: ViewGroup ){
        val banner=AdsBannerView.getView(windowManager,this,viewContainer)
        if (!proApplication.isSubVip){
            AdsBannerView.loadAds(AdsBannerView.BANNER_TOP,banner)
        }
    }
  open  fun showNativeAds(viewContainer: ViewGroup,call:()->Unit){
        nativeRender.prepareNative()
       if (!proApplication.isSubVip){
           nativeRender.loadNativeAds(object : OnShowNativeListener {
               override fun onLoadDone(hasAds: Boolean, currentNativeAd: NativeAd?) {
                    // load dc native
                   call()
               }
           },viewContainer)
       }
    }
}