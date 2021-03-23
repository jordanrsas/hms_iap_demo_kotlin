package com.dtse.demo.iap.huawei.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.dtse.demo.iap.huawei.R
import com.huawei.hms.iap.entity.ProductInfo

class ProductListAdapter(
    private var productInfoList: List<ProductInfo>,
    private val callback: OnProductItemClickListener
) :
    RecyclerView.Adapter<ProductListAdapter.ProductListViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProductListViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_layout, parent, false)
        return ProductListViewHolder(view, callback)
    }

    override fun getItemCount(): Int {
        return productInfoList.size
    }

    override fun onBindViewHolder(holder: ProductListViewHolder, position: Int) {
        holder.bind(productInfoList[position])
    }

    fun update(productList: List<ProductInfo>) {
        this.productInfoList = productList
        this.notifyDataSetChanged()
    }

    class ProductListViewHolder(itemView: View, val callback: OnProductItemClickListener) :
        RecyclerView.ViewHolder(itemView) {
        private val itemName: TextView = itemView.findViewById(R.id.itemName)
        private val itemPrice: TextView = itemView.findViewById(R.id.itemPrice)
        private val itemDescription: TextView = itemView.findViewById(R.id.itemDescription)

        fun bind(product: ProductInfo) {
            itemName.text = product.productName
            itemPrice.text = product.price
            itemDescription.text = product.productDesc

            itemView.setOnClickListener {
                callback.onProductClick(product)
            }
        }
    }

    interface OnProductItemClickListener {
        fun onProductClick(product: ProductInfo)
    }
}