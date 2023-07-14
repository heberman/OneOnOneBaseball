package com.example.oneononebaseball

import android.app.Application
import com.example.oneononebaseball.data.AppContainer
import com.example.oneononebaseball.data.DefaultAppContainer

class BaseballApplication : Application() {
    lateinit var container: AppContainer
    override fun onCreate() {
        super.onCreate()
        container = DefaultAppContainer()
    }
}