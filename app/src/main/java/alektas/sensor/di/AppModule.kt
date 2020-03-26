package alektas.sensor.di

import alektas.sensor.data.SensorRepository
import alektas.sensor.domain.Repository
import android.content.Context
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Module
class AppModule(val context: Context) {
    @Provides
    fun appContext(): Context = context

    @Provides
    @Singleton
    fun provideRepository(): Repository = SensorRepository()
}