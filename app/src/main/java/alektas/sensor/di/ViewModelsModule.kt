package alektas.sensor.di

import alektas.sensor.domain.entities.DeviceManager
import alektas.sensor.ui.device.DeviceViewModel
import alektas.sensor.ui.scan.ScanViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import dagger.Module
import dagger.Provides
import javax.inject.Named

const val SCAN_VM_FACTORY_NAME = "ScanViewModelFactory"
const val DEVICE_VM_FACTORY_NAME = "DeviceViewModelFactory"

@Module
class ViewModelsModule {

    @Provides
    @Named(value = SCAN_VM_FACTORY_NAME)
    fun provideMainViewModelFactory(
        deviceManager: DeviceManager
    ): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            return ScanViewModel(deviceManager) as T
        }
    }

}