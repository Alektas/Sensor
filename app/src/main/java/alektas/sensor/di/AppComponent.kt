package alektas.sensor.di

import alektas.sensor.ui.MainActivity
import alektas.sensor.ui.device.DeviceFragment
import alektas.sensor.ui.device.DeviceViewModel
import alektas.sensor.ui.scan.ScanViewModel
import alektas.sensor.ui.scan.ScanFragment
import android.content.Context
import dagger.BindsInstance
import dagger.Component
import javax.inject.Singleton

@Singleton
@Component(modules = [AppModule::class, ViewModelsModule::class, BluetoothModule::class])
interface AppComponent {
    fun inject(activity: MainActivity)
    fun inject(viewModel: ScanViewModel)
    fun inject(viewModel: DeviceViewModel)
    fun inject(scanFragment: ScanFragment)
    fun inject(deviceFragment: DeviceFragment)

}