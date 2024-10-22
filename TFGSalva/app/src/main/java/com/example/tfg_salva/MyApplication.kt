package com.example.tfg_salva

import android.annotation.SuppressLint
import android.app.Application
import android.content.Context
import androidx.lifecycle.ViewModelProvider.NewInstanceFactory.Companion.instance

@SuppressLint("StaticFieldLeak")
object MyApplication : Application() {

    lateinit var mContext: Context

    init{
        // mContext = applicationContext
    }

    @SuppressLint("StaticFieldLeak")
    override fun onCreate() {
        super.onCreate()
        mContext = applicationContext
    }

    fun getContext(): Context{
        return mContext
    }
}