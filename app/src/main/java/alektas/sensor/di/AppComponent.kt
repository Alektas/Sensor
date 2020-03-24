package alektas.sensor.di

import alektas.sensor.ui.MainActivity
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
    fun inject(vm: ScanViewModel)
    fun inject(scanFragment: ScanFragment)

    @Component.Builder
    interface Builder {
        @BindsInstance
        fun withContext(context: Context): Builder
        fun build(): AppComponent
    }
}