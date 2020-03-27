package alektas.sensor.bluetooth

import alektas.sensor.domain.entities.ScanResource
import android.bluetooth.BluetoothDevice

interface DeviceCallback {
    fun onNext(device: BluetoothDevice, rssi: Int)
    fun onStatusChange(isActive: Boolean)
    fun onFail(error: ScanResource.Error)
}