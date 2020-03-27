package alektas.sensor.domain.entities

import io.reactivex.Observable

interface DeviceManager {
    fun observeDevices(): Observable<DeviceResource>
    fun startScan()
    fun stopScan()
    fun isScanning(): Boolean
    fun observeDeviceServices(): Observable<DeviceServiceResource>
    fun connectDevice(address: String)
    fun disconnectDevice()
}