package alektas.sensor.di

import alektas.sensor.data.SensorRepository
import alektas.sensor.domain.Repository
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Module
class AppModule {
    @Provides
    @Singleton
    fun provideRepository(): Repository = SensorRepository()
}