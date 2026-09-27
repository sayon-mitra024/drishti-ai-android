package com.example

import android.app.Application
import com.example.data.AppDatabase
import com.example.data.ScreeningRepository

class DrishtiApp : Application() {
    val database: AppDatabase by lazy { AppDatabase.getDatabase(this) }
    val repository: ScreeningRepository by lazy { ScreeningRepository(database.screeningDao()) }

    override fun onCreate() {
        super.onCreate()
    }
}
