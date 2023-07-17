package com.gps.speedometer.odometer.gpsspeedtracker.biiling

import android.view.ViewGroup
import androidx.lifecycle.lifecycleScope
import com.access.pro.adcontrol.AdsBannerView
import com.access.pro.callBack.OnShowInterstitialListener
import com.access.pro.callBack.OnShowNativeListener
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.BillingClientStateListener
import com.android.billingclient.api.BillingResult
import com.android.billingclient.api.PurchasesUpdatedListener
import com.android.billingclient.api.QueryPurchasesParams
import com.android.billingclient.api.queryPurchasesAsync
import com.google.android.gms.ads.nativead.NativeAd
import com.google.firebase.analytics.FirebaseAnalytics
import kotlinx.coroutines.launch


open class BaseActivity : com.access.pro.activity.BaseActivity() {
    private lateinit var firebaseAnalytics: FirebaseAnalytics
    var billingClient: BillingClient? = null


    fun setupBilling(purchasesUpdatedListener: PurchasesUpdatedListener) {
        billingClient = BillingClient.newBuilder(this)
            .setListener(purchasesUpdatedListener)
            .enablePendingPurchases()
            .build()
        billingClient?.startConnection(object : BillingClientStateListener {
            override fun onBillingSetupFinished(billingResult: BillingResult) {
                if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                    lifecycleScope.launch {
                        getActivePurchase()
                    }
                }
            }

            override fun onBillingServiceDisconnected() {
                // Try to restart the connection on the next request to
                // Google Play by calling the startConnection() method.
            }
        })
    }


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

    open fun showBannerAds(viewContainer: ViewGroup) {
        if (!proApplication.isSubVip) {
            val banner = AdsBannerView.getView(windowManager, this, viewContainer)
            AdsBannerView.loadAds(AdsBannerView.BANNER_TOP, banner)
        }
    }

    open fun showInterstitial(now: Boolean, call: (Boolean) -> Unit) {
        if (!proApplication.isSubVip) {
            showAds(now, object : OnShowInterstitialListener {
                override fun onCloseAds(hasAds: Boolean) {
                    call(hasAds)
                }
            })
        }
    }

    open fun showNativeAds(viewContainer: ViewGroup, call: () -> Unit) {
        nativeRender.prepareNative()
        if (!proApplication.isSubVip) {
            nativeRender.loadNativeAds(object : OnShowNativeListener {
                override fun onLoadDone(hasAds: Boolean, currentNativeAd: NativeAd?) {
                    // load dc native
                    call()
                }
            }, viewContainer)
        }
    }
}