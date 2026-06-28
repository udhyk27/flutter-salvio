package com.ydh.salvio

import android.app.Application
import com.ydh.salvio.data.local.TokenDataStore

class SalvioApplication : Application() {
    lateinit var tokenDataStore: TokenDataStore

    override fun onCreate() {
        super.onCreate()
        tokenDataStore = TokenDataStore(this)
    }
}
