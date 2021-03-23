package com.dtse.demo.iap.huawei

import android.app.Activity
import android.content.Intent
import android.content.IntentSender
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.dtse.demo.iap.huawei.adapters.SubscriptionsAdapter
import com.dtse.demo.iap.huawei.utils.REQ_CODE_BUY
import com.dtse.demo.iap.huawei.utils.handle
import com.huawei.hms.iap.Iap
import com.huawei.hms.iap.IapApiException
import com.huawei.hms.iap.IapClient
import com.huawei.hms.iap.entity.*
import kotlinx.android.synthetic.main.activity_consumption.progressBar
import kotlinx.android.synthetic.main.activity_subscription.*

class SubscriptionActivity : AppCompatActivity(), SubscriptionsAdapter.OnSubscribeClickListener {

    companion object {
        private const val TAG = "SubscriptionActivity"
    }

    private lateinit var iapClient: IapClient
    private var subscriptionProducts: MutableList<ProductInfo> = mutableListOf()
    private var recyclerViewAdapter: SubscriptionsAdapter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_subscription)
        iapClient = Iap.getIapClient(this)
        initView()
    }

    private fun initView() {
        progressBar.visibility = View.VISIBLE

        recyclerViewAdapter = SubscriptionsAdapter(subscriptionProducts, this)
        subscriptionsListView.apply {
            this.layoutManager = LinearLayoutManager(this@SubscriptionActivity)
            this.addItemDecoration(
                DividerItemDecoration(
                    this@SubscriptionActivity,
                    LinearLayoutManager.VERTICAL
                )
            )
            this.adapter = recyclerViewAdapter
        }

        manageSubscriptionsButton.setOnClickListener {
            showSubscription()
        }

        queryProducts()
    }

    private fun queryProducts() {
        val productsIds = arrayListOf("demosub101", "demosub102", "demosub103", "demosub104")

        val productInfoRequest = ProductInfoReq().apply {
            priceType = IapClient.PriceType.IN_APP_SUBSCRIPTION
            productIds = productsIds
        }

        iapClient.obtainProductInfo(productInfoRequest).apply {
            addOnSuccessListener {
                subscriptionProducts = it.productInfoList
                showProducts()
            }

            addOnFailureListener {
                it.handle(this@SubscriptionActivity)
                showProducts()
            }
        }
    }

    private fun showProducts() {
        progressBar.visibility = View.GONE
        recyclerViewAdapter?.update(subscriptionProducts)
    }

    override fun subscribe(product: ProductInfo) {
        val purchaseIntentRequest = PurchaseIntentReq().apply {
            priceType = IapClient.PriceType.IN_APP_SUBSCRIPTION
            productId = product.productId
            developerPayload = "testPurchase"
        }

        iapClient.createPurchaseIntent(purchaseIntentRequest).apply {
            addOnSuccessListener {
                val status = it.status
                try {
                    status.startResolutionForResult(this@SubscriptionActivity, REQ_CODE_BUY)
                } catch (exception: IntentSender.SendIntentException) {
                    exception.printStackTrace()
                }
            }

            addOnFailureListener {
                if (it is IapApiException && it.statusCode == OrderStatusCode.ORDER_PRODUCT_OWNED) {
                    showSubscription(product.productId)
                } else {
                    it.handle(this@SubscriptionActivity)
                }
            }
        }
    }

    private fun showSubscription(productId: String? = null) {
        val request = StartIapActivityReq()
        if (productId.isNullOrEmpty()) {
            request.type = StartIapActivityReq.TYPE_SUBSCRIBE_MANAGER_ACTIVITY
        } else {
            request.type = StartIapActivityReq.TYPE_SUBSCRIBE_EDIT_ACTIVITY
            request.subscribeProductId = productId
        }

        iapClient.startIapActivity(request).apply {
            addOnSuccessListener {
                it.startActivity(this@SubscriptionActivity)
            }

            addOnFailureListener {
                it.handle(this@SubscriptionActivity)
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == REQ_CODE_BUY) {
            if (data == null) {
                return
            }

            if (resultCode == Activity.RESULT_OK) {
                val purchaseResult = getPurchaseResult(data)
                when (purchaseResult) {
                    OrderStatusCode.ORDER_STATE_SUCCESS -> {
                        Toast.makeText(this, R.string.pay_success, Toast.LENGTH_SHORT).show()
                        refreshSubscription()
                    }

                    OrderStatusCode.ORDER_STATE_CANCEL -> {
                        Toast.makeText(this, R.string.cancel, Toast.LENGTH_SHORT).show()
                    }
                    else -> {
                        Toast.makeText(this, R.string.pay_fail, Toast.LENGTH_SHORT).show()
                    }
                }
            } else {
                Log.i(TAG, "cancel subscribe")
                Toast.makeText(this, R.string.cancel, Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun getPurchaseResult(data: Intent): Int {
        val purchaseResultInfo = iapClient.parsePurchaseResultInfoFromIntent(data)
        val returnCode = purchaseResultInfo.returnCode
        val errMsg = purchaseResultInfo.errMsg
        when (returnCode) {
            OrderStatusCode.ORDER_PRODUCT_OWNED -> {
                Log.w(TAG, "you have owned this product")
                return OrderStatusCode.ORDER_PRODUCT_OWNED
            }
            OrderStatusCode.ORDER_STATE_SUCCESS -> {
                val inAppPurchaseData = InAppPurchaseData(purchaseResultInfo.inAppPurchaseData)
                if (inAppPurchaseData.isSubValid) {
                    return OrderStatusCode.ORDER_STATE_SUCCESS
                }
                return OrderStatusCode.ORDER_STATE_FAILED
            }
            else -> {
                Log.e(TAG, "returnCode: $returnCode , errMsg: $errMsg")
                return returnCode
            }
        }
    }

    private fun refreshSubscription() {
        val ownedPurchasesRequest: OwnedPurchasesReq = OwnedPurchasesReq()
        ownedPurchasesRequest.apply {
            this.priceType = IapClient.PriceType.IN_APP_SUBSCRIPTION
            this.continuationToken = continuationToken
        }

        iapClient.obtainOwnedPurchases(ownedPurchasesRequest).apply {
            addOnSuccessListener {

            }

            addOnFailureListener {

            }
        }
    }
}