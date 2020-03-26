package alektas.sensor.domain.entities

import alektas.sensor.bluetooth.DeviceCallback

interface DeviceScanner {
    fun startScan(callback: DeviceCallback)
    fun stopScan()
    fun getKnownDevices(): List<DeviceModel>
}