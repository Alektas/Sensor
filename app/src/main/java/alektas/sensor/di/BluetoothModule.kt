package alektas.sensor.di

import alektas.sensor.bluetooth.BleDeviceManager
import alektas.sensor.bluetooth.scanners.BleDeviceScanner
import alektas.sensor.bluetooth.scanners.LegacyBleDeviceScanner
import alektas.sensor.domain.entities.DeviceManager
import alektas.sensor.domain.entities.DeviceScanner
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.content.Context
import android.os.Build
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Module
class BluetoothModule {

    @Provides
    @Singleton
    fun provideDeviceManager(scanner: DeviceScanner): DeviceManager {
        return BleDeviceManager(scanner)
    }

    @Provides
    @Singleton
    fun provideDeviceScanner(adapter: BluetoothAdapter): DeviceScanner {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            return BleDeviceScanner(adapter)
        }

        return LegacyBleDeviceScanner(adapter)
    }

    @Provides
    @Singleton
    fun provideBluetoothAdapter(context: Context): BluetoothAdapter {
        val bleManager = context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        return bleManager.adapter
    }
}