package com.dtse.demo.iap.huawei.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.dtse.demo.iap.huawei.R
import com.huawei.hms.iap.entity.ProductInfo

class SubscriptionsAdapter(
    private var subscriptionList: List<ProductInfo>,
    private val callback: OnSubscribeClickListener
) :
    RecyclerView.Adapter<SubscriptionsAdapter.SubscriptionViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SubscriptionViewHolder {
        val view =
            LayoutInflater.from(parent.context).inflate(R.layout.item_subscription, parent, false)
        return SubscriptionViewHolder(view, callback)
    }

    override fun getItemCount(): Int {
        return subscriptionList.size
    }

    override fun onBindViewHolder(holder: SubscriptionViewHolder, position: Int) {
        holder.bind(subscriptionList[position])
    }

    fun update(productList: List<ProductInfo>) {
        this.subscriptionList = productList
        this.notifyDataSetChanged()
    }

    class SubscriptionViewHolder(itemView: View, private val callback: OnSubscribeClickListener) :
        RecyclerView.ViewHolder(itemView) {
        private val productName = itemView.findViewById<TextView>(R.id.productName)
        private val productDescription = itemView.findViewById<TextView>(R.id.productDescription)
        private val productPrice = itemView.findViewById<TextView>(R.id.productPrice)
        private val subscribeButton = itemView.findViewById<Button>(R.id.subscribeButton)

        fun bind(product: ProductInfo) {
            productName.text = product.productName
            productDescription.text = product.productDesc
            productPrice.text = product.price
            subscribeButton.setOnClickListener {
                callback.subscribe(product)
            }
        }
    }

    interface OnSubscribeClickListener {
        fun subscribe(product: ProductInfo)
    }
}