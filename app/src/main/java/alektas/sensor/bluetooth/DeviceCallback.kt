package alektas.sensor.bluetooth

import alektas.sensor.domain.entities.DeviceResource
import android.bluetooth.BluetoothDevice

interface DeviceCallback {
    fun onNext(device: BluetoothDevice)
    fun onStatusChange(isActive: Boolean)
    fun onFail(error: DeviceResource.Error)
}