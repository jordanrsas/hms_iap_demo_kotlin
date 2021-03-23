package com.dtse.demo.iap.huawei

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.dtse.demo.iap.huawei.adapters.BillListAdapter
import com.dtse.demo.iap.huawei.utils.CipherUtil
import com.dtse.demo.iap.huawei.utils.handle
import com.huawei.hms.iap.Iap
import com.huawei.hms.iap.IapClient
import com.huawei.hms.iap.entity.OwnedPurchasesReq
import kotlinx.android.synthetic.main.activity_consumption.progressBar
import kotlinx.android.synthetic.main.activity_purchase_history.*

class PurchaseHistoryActivity : AppCompatActivity() {

    companion object {
        private const val TAG = "PurchaseHistoryActivity"
    }

    private var continuationTokenStr: String? = null
    private var billList: MutableList<String> = mutableListOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_purchase_history)
        progressBar.visibility = View.VISIBLE
        billListRecyclerView.apply {
            this.layoutManager = LinearLayoutManager(this@PurchaseHistoryActivity)
            this.addItemDecoration(
                DividerItemDecoration(
                    this@PurchaseHistoryActivity,
                    LinearLayoutManager.VERTICAL
                )
            )
        }
    }

    override fun onResume() {
        super.onResume()
        queryHistoryInterface()
    }

    private fun queryHistoryInterface() {
        val iapClient = Iap.getIapClient(this)

        val ownedPurchasesRequest = OwnedPurchasesReq().apply {
            this.priceType = IapClient.PriceType.IN_APP_CONSUMABLE
            this.continuationToken = continuationTokenStr
        }

        iapClient.obtainOwnedPurchaseRecord(ownedPurchasesRequest).apply {
            addOnSuccessListener {
                Log.i(TAG, "obtainOwnedPurchaseRecord, success")
                val inAppPurchaseDataList = it.inAppPurchaseDataList
                val signatureList = it.inAppSignature

                if (inAppPurchaseDataList.isNullOrEmpty()) {
                    onFinish()
                    return@addOnSuccessListener
                }

                Log.i(TAG, "list size: + ${inAppPurchaseDataList.size}")
                signatureList.forEachIndexed { index, s ->
                    val success: Boolean =
                        CipherUtil.doCheck(inAppPurchaseDataList[index], s, CipherUtil.PUBLIC_KEY)

                    billList.add(inAppPurchaseDataList[index])
                }

                continuationTokenStr = it.continuationToken
                if (!continuationTokenStr.isNullOrEmpty()) {
                    queryHistoryInterface()
                } else {
                    onFinish()
                }
            }

            addOnFailureListener {
                Log.e(TAG, "obtainOwnedPurchaseRecord, ${it.message}")
                it.handle(this@PurchaseHistoryActivity)
                onFinish()
            }
        }
    }

    private fun onFinish() {
        progressBar.visibility = View.GONE
        Log.i(TAG, "onFinish")
        val billAdapter = BillListAdapter(billList)
        billListRecyclerView.adapter = billAdapter
    }
}