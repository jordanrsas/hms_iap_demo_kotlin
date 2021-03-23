package com.dtse.demo.iap.huawei

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.dtse.demo.iap.huawei.utils.REQ_CODE_LOGIN
import com.dtse.demo.iap.huawei.utils.handle
import com.huawei.hms.iap.Iap
import com.huawei.hms.iap.entity.OrderStatusCode.ORDER_ACCOUNT_AREA_NOT_SUPPORTED
import com.huawei.hms.iap.entity.OrderStatusCode.ORDER_STATE_SUCCESS
import com.huawei.hms.iap.util.IapClientHelper
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    companion object {
        private const val TAG = "IapMainActivity"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        queryIsReady()
    }

    private fun queryIsReady() {
        val iapClient = Iap.getIapClient(this)
        iapClient.isEnvReady.apply {
            addOnSuccessListener {
                initView()
            }

            addOnFailureListener {
                it.handle(this@MainActivity)
            }
        }
    }

    private fun initView() {
        setContentView(R.layout.activity_main)
        consumablesButton.setOnClickListener {
            startActivity(Intent(this, ConsumptionActivity::class.java))
        }

        subscriptionButton.setOnClickListener {
            startActivity(Intent(this, SubscriptionActivity::class.java))
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQ_CODE_LOGIN) {
            val returnCode = IapClientHelper.parseRespCodeFromIntent(data)
            Log.i(TAG, "onActivityResult, returnCode: $returnCode")
            when (returnCode) {
                ORDER_STATE_SUCCESS -> initView()
                ORDER_ACCOUNT_AREA_NOT_SUPPORTED -> {
                    Toast.makeText(
                        this,
                        "This is unavailable in your country/region.",
                        Toast.LENGTH_LONG
                    ).show()
                }
                else -> {
                    Toast.makeText(this, "User cancel login.", Toast.LENGTH_LONG).show()
                }
            }
        }
    }
}