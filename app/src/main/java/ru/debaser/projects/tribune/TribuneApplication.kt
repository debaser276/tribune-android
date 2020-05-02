package ru.debaser.projects.tribune

import android.app.Application
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin
import ru.debaser.projects.tribune.di.applicationModule

class TribuneApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidContext(this@TribuneApplication)
            modules(applicationModule)
        }
    }
}