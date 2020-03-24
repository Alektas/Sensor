package alektas.sensor.domain.entities

interface DeviceScanner {
    fun startScan(callback: DeviceCallback)
    fun stopScan()
    fun getKnownDevices(): List<DeviceModel>
}