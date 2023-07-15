package com.gps.speedometer.odometer.gpsspeedtracker.biiling

import androidx.lifecycle.lifecycleScope
import com.android.billingclient.api.AcknowledgePurchaseParams
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.BillingClientStateListener
import com.android.billingclient.api.BillingResult
import com.android.billingclient.api.Purchase
import com.android.billingclient.api.PurchasesUpdatedListener
import com.android.billingclient.api.QueryPurchasesParams
import com.android.billingclient.api.queryPurchasesAsync
import com.google.firebase.analytics.FirebaseAnalytics
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
}