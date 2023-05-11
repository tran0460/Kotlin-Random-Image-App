package com.example.assignment1_son_tran

import android.annotation.SuppressLint
import android.app.Application
import android.content.Context

class TheApp: Application() {

    override fun onCreate() {
        super.onCreate()
        context = applicationContext
    }

    companion object {
        @SuppressLint("StaticFieldLeak")
        lateinit var context: Context
            private set
    }
}