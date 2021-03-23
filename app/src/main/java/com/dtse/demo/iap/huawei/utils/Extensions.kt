package com.dtse.demo.iap.huawei.utils

import android.app.Activity
import android.content.IntentSender
import android.util.Log
import android.widget.Toast
import com.huawei.hms.iap.IapApiException
import com.huawei.hms.iap.entity.OrderStatusCode


const val TAG: String = "HmsIapDemo"
const val REQ_CODE_LOGIN = 2021
const val REQ_CODE_BUY = 4002

fun Exception.handle(activity: Activity) {
    if (this is IapApiException) {
        Log.e(TAG, "returnCode: ${this.statusCode}")
        when (this.statusCode) {
            OrderStatusCode.ORDER_STATE_CANCEL -> Toast.makeText(
                activity,
                "Order has been canceled!",
                Toast.LENGTH_SHORT
            ).show()
            OrderStatusCode.ORDER_STATE_PARAM_ERROR -> {
                Toast.makeText(activity, "Order state param error!", Toast.LENGTH_SHORT).show()
            }
            OrderStatusCode.ORDER_STATE_NET_ERROR -> {
                Toast.makeText(activity, "Order state net error!", Toast.LENGTH_SHORT).show()
            }
            OrderStatusCode.ORDER_VR_UNINSTALL_ERROR -> {
                Toast.makeText(activity, "Order vr uninstall error!", Toast.LENGTH_SHORT).show()
            }
            OrderStatusCode.ORDER_HWID_NOT_LOGIN -> {
                this.status?.let { status ->
                    try {
                        status.startResolutionForResult(activity, REQ_CODE_LOGIN)
                    } catch (exp: IntentSender.SendIntentException) {

                    }
                } ?: Log.e(TAG, "status is null")
            }
            OrderStatusCode.ORDER_PRODUCT_OWNED -> {
                Toast.makeText(activity, "Product already owned error!", Toast.LENGTH_SHORT).show()
            }
            OrderStatusCode.ORDER_PRODUCT_NOT_OWNED -> {
                Toast.makeText(activity, "Product not owned error!", Toast.LENGTH_SHORT).show()
            }
            OrderStatusCode.ORDER_PRODUCT_CONSUMED -> {
                Toast.makeText(activity, "Product consumed error!", Toast.LENGTH_SHORT).show()
            }
            OrderStatusCode.ORDER_ACCOUNT_AREA_NOT_SUPPORTED -> {
                Toast.makeText(
                    activity,
                    "Order account area not supported error!",
                    Toast.LENGTH_SHORT
                ).show()
            }
            OrderStatusCode.ORDER_NOT_ACCEPT_AGREEMENT -> {
                Toast.makeText(activity, "User does not agree the agreement", Toast.LENGTH_SHORT)
                    .show()
            }
            else -> {
                Toast.makeText(activity, "Order unknown error!", Toast.LENGTH_SHORT).show()
            }
        }
    } else {
        this.message?.let { Log.e(TAG, it) }
    }
}