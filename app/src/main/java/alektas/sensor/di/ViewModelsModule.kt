package alektas.sensor.di

import alektas.sensor.domain.entities.DeviceManager
import alektas.sensor.ui.scan.ScanViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import dagger.Module
import dagger.Provides

@Module
class ViewModelsModule {
    @Provides
    fun provideMainViewModelFactory(
        deviceManager: DeviceManager
    ): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            return ScanViewModel(deviceManager) as T
        }
    }
}