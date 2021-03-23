package com.dtse.demo.iap.huawei

import android.app.Application
import com.dtse.demo.iap.huawei.utils.Preference

class IapDemoApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        Preference.init(this)
    }
}