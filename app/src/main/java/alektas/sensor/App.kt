package alektas.sensor

import alektas.sensor.di.AppComponent
import alektas.sensor.di.DaggerAppComponent
import android.app.Application

class App : Application() {

    override fun onCreate() {
        super.onCreate()
        component = DaggerAppComponent.builder()
            .withContext(this)
            .build()
    }

    companion object {
        lateinit var component: AppComponent
    }
}