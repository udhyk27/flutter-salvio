package com.ydh.salvio

import android.app.Application
import com.ydh.salvio.data.local.AppDatabase
import com.ydh.salvio.data.local.TokenDataStore

class SalvioApplication : Application() {
    lateinit var tokenDataStore: TokenDataStore
    lateinit var database: AppDatabase

    override fun onCreate() {
        super.onCreate()
        tokenDataStore = TokenDataStore(this)
        database = AppDatabase.getInstance(this)
    }
}
