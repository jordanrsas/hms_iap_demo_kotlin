package com.dtse.demo.iap.huawei.adapters

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.dtse.demo.iap.huawei.R
import com.huawei.hms.iap.entity.InAppPurchaseData
import org.json.JSONException
import org.json.JSONObject

class BillListAdapter(private val billList: List<String>) :
    RecyclerView.Adapter<BillListAdapter.BillLisHolder>() {

    companion object {
        private const val TAG = "BillListAdapter"
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BillLisHolder {
        val view =
            LayoutInflater.from(parent.context).inflate(R.layout.bill_list_item, parent, false)
        return BillLisHolder(view)
    }

    override fun getItemCount(): Int {
        return billList.size
    }

    override fun onBindViewHolder(holder: BillLisHolder, position: Int) {
        holder.bind(billList[position])
    }

    class BillLisHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val billProductName: TextView = itemView.findViewById(R.id.billProductName)
        private val billProductPrice: TextView = itemView.findViewById(R.id.billProductPrice)
        private val billStatus: TextView = itemView.findViewById(R.id.billStatus)

        fun bind(billElement: String) {
            try {
                val billInformation = JSONObject(billElement)
                val productName = billInformation.optString("productName")
                val productPrice = billInformation.optInt("price")
                val currency = billInformation.optString("currency")
                val orderStatus = billInformation.optInt("purchaseState")

                billProductName.text = productName
                billProductPrice.text = getProductPrice(productPrice, currency)
                billStatus.setText(getOrderStatusString(orderStatus))
            } catch (exception: JSONException) {
                Log.e(TAG, "Json Error occurred!")
            }
        }

        private fun getProductPrice(price: Int, currency: String): String =
            "" + price / 100 + "." + price % 100 + " " + currency


        private fun getOrderStatusString(orderStatus: Int): Int =
            when (orderStatus) {
                InAppPurchaseData.PurchaseState.PURCHASED -> R.string.success_state
                InAppPurchaseData.PurchaseState.REFUNDED -> R.string.refund_state
                else -> R.string.cancel_state
            }

    }
}