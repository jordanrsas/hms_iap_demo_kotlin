package com.dtse.demo.iap.huawei.utils

import android.content.Context
import android.content.SharedPreferences

object Preference {
    private const val NAME = "HmsIapDemo"
    private const val MODE = Context.MODE_PRIVATE
    private lateinit var preferences: SharedPreferences

    private val GEMS_COUNT_KEY = Pair("gems_count", 0)
    private val PURCHASE_TOKEN_KEY = Pair("purchase_token_set", "")

    fun init(context: Context) {
        preferences = context.getSharedPreferences(NAME, MODE)
    }

    private inline fun SharedPreferences.edit(operation: (SharedPreferences.Editor) -> Unit) {
        val editor = edit()
        operation(editor)
        editor.apply()
    }

    var countGems: Int
        get() = preferences.getInt(GEMS_COUNT_KEY.first, GEMS_COUNT_KEY.second)
        set(value) = preferences.edit {
            it.putInt(GEMS_COUNT_KEY.first, value)
        }

    var purchaseToken: String?
        get() = preferences.getString(PURCHASE_TOKEN_KEY.first, PURCHASE_TOKEN_KEY.second)
        set(value) = preferences.edit {
            it.putString(PURCHASE_TOKEN_KEY.first, value)
        }
}