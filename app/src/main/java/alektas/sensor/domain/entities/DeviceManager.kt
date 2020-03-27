package alektas.sensor.domain.entities

import io.reactivex.Observable

interface DeviceManager {
    fun observeScanning(): Observable<ScanResource>
    fun startScan()
    fun stopScan()

    fun observeDevice(): Observable<DeviceResource>
    fun connectDevice(address: String)
    fun disconnectDevice()

    fun subscribeOnCharacteristic(serviceUuid: String, charUuid: String)
    fun unsubscribeFromCharacteristic(charUuid: String)
    fun isCharacteristicSubscribed(uuid: String): Boolean
}