package com.dtse.demo.iap.huawei

import android.content.Intent
import android.content.IntentSender
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.dtse.demo.iap.huawei.adapters.ProductListAdapter
import com.dtse.demo.iap.huawei.utils.Preference
import com.dtse.demo.iap.huawei.utils.REQ_CODE_BUY
import com.dtse.demo.iap.huawei.utils.handle
import com.huawei.hms.iap.Iap
import com.huawei.hms.iap.IapApiException
import com.huawei.hms.iap.IapClient
import com.huawei.hms.iap.entity.*
import kotlinx.android.synthetic.main.activity_consumption.*

class ConsumptionActivity : AppCompatActivity(), ProductListAdapter.OnProductItemClickListener {

    companion object {
        private const val TAG = "IapConsumptionActivity"
    }

    private lateinit var iapClient: IapClient

    private var consumableProducts: List<ProductInfo> = ArrayList()
    private var recyclerViewAdapter: ProductListAdapter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_consumption)
        iapClient = Iap.getIapClient(this)
        initView()
        queryPurchases()
    }

    private fun initView() {
        progressBar.visibility = View.VISIBLE
        val countGems = Preference.countGems
        gemsCountTextView.text = countGems.toString()

        recyclerViewAdapter = ProductListAdapter(consumableProducts, this)

        consumableProductsListView.apply {
            this.layoutManager = LinearLayoutManager(this@ConsumptionActivity)
            this.addItemDecoration(
                DividerItemDecoration(
                    this@ConsumptionActivity,
                    LinearLayoutManager.VERTICAL
                )
            )
            this.adapter = recyclerViewAdapter
        }

        historyButton.setOnClickListener {
            startActivity(Intent(this, PurchaseHistoryActivity::class.java))
        }

        queryProducts()
    }

    private fun queryProducts() {
        val productsIds = arrayListOf("gem_001", "gem_002", "gem_003", "gem_004")

        val productInfoRequest = ProductInfoReq().apply {
            priceType = IapClient.PriceType.IN_APP_CONSUMABLE
            productIds = productsIds
        }

        iapClient.obtainProductInfo(productInfoRequest).apply {
            addOnSuccessListener {
                Log.i(TAG, "obtainProductInfo, success")
                consumableProducts = it.productInfoList
                showProducts()
            }

            addOnFailureListener {
                Log.e(TAG, "obtainProductInfo, fail")
                it.handle(this@ConsumptionActivity)
                showProducts()
            }
        }
    }

    private fun showProducts() {
        progressBar.visibility = View.GONE
        recyclerViewAdapter?.update(consumableProducts)
    }

    private fun queryPurchases(continuationToken: String? = null) {
        val ownedPurchasesRequest: OwnedPurchasesReq = OwnedPurchasesReq()
        ownedPurchasesRequest.apply {
            this.priceType = IapClient.PriceType.IN_APP_CONSUMABLE
            this.continuationToken = continuationToken
        }
        iapClient.obtainOwnedPurchases(ownedPurchasesRequest).apply {
            addOnSuccessListener {
                Log.i(TAG, "obtainOwnedPurchases, success")
                if (it.inAppPurchaseDataList != null) {
                    val inAppSignature = it.inAppSignature
                    it.inAppPurchaseDataList.forEachIndexed { index, inAppPurchaseData ->
                        val inAppPurchaseDataSignature = inAppSignature[index]
                        deliverProduct(inAppPurchaseData, inAppPurchaseDataSignature)
                    }
                }

                if (!continuationToken.isNullOrBlank()) {
                    queryPurchases(it.continuationToken)
                }
            }

            addOnFailureListener {
                Log.e(TAG, "obtainOwnedPurchases, fail")
                Log.e(
                    TAG,
                    "obtainOwnedPurchases, type= ${IapClient.PriceType.IN_APP_CONSUMABLE}, ${it.message} "
                )
                it.handle(this@ConsumptionActivity)
            }
        }
    }

    private fun deliverProduct(inAppPurchaseData: String, inAppPurchaseDataSignature: String) {
        val inAppPurchaseDataBean = InAppPurchaseData(inAppPurchaseData)

        val consumeOwnedPurchaseRequest = ConsumeOwnedPurchaseReq()
        consumeOwnedPurchaseRequest.apply {
            purchaseToken = inAppPurchaseDataBean.purchaseToken
            developerChallenge = "testConsume"
        }

        iapClient.consumeOwnedPurchase(consumeOwnedPurchaseRequest).apply {
            addOnSuccessListener {
                Log.i(TAG, "consumeOwnedPurchase success")
            }

            addOnFailureListener {
                it.handle(this@ConsumptionActivity)
            }
        }
    }

    override fun onProductClick(product: ProductInfo) {
        val purchaseIntentRequest = PurchaseIntentReq().apply {
            priceType = IapClient.PriceType.IN_APP_CONSUMABLE
            productId = product.productId
            developerPayload = "testPurchase"
        }

        iapClient.createPurchaseIntent(purchaseIntentRequest).apply {
            addOnSuccessListener {
                val status = it.status
                try {
                    status.startResolutionForResult(this@ConsumptionActivity, REQ_CODE_BUY)
                } catch (exception: IntentSender.SendIntentException) {
                    exception.printStackTrace()
                }
            }

            addOnFailureListener {
                if (it is IapApiException && it.statusCode == OrderStatusCode.ORDER_PRODUCT_OWNED) {
                    queryPurchases(null)
                } else {
                    it.handle(this@ConsumptionActivity)
                }
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        Log.i(TAG, "onActivityResult");
        if (requestCode == REQ_CODE_BUY) {
            data?.let {
                iapClient.parsePurchaseResultInfoFromIntent(it).let { purchaseResultInfo ->
                    when (purchaseResultInfo.returnCode) {
                        OrderStatusCode.ORDER_STATE_CANCEL -> Toast.makeText(
                            this,
                            "Order has been canceled!",
                            Toast.LENGTH_SHORT
                        ).show()

                        OrderStatusCode.ORDER_STATE_FAILED, OrderStatusCode.ORDER_PRODUCT_OWNED ->
                            queryPurchases(null)

                        OrderStatusCode.ORDER_STATE_SUCCESS -> deliverProduct(
                            purchaseResultInfo.inAppPurchaseData,
                            purchaseResultInfo.inAppDataSignature
                        )
                    }
                }
            }
        }
    }
}