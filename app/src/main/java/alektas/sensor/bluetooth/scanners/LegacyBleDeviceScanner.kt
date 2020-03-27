package alektas.sensor.bluetooth.scanners

import alektas.sensor.bluetooth.DeviceCallback
import alektas.sensor.domain.entities.ScanResource
import android.bluetooth.BluetoothAdapter

class LegacyBleDeviceScanner(private val adapter: BluetoothAdapter) :
    BaseDeviceScanner(adapter) {
    private var callback: DeviceCallback? = null
    private val innerCallback =
        BluetoothAdapter.LeScanCallback { device, rssi, _ ->
            callback?.onNext(device, rssi)
        }

    override fun startScan(callback: DeviceCallback) {
        if (!adapter.isEnabled) {
            callback.onStatusChange(false)
            callback.onFail(ScanResource.Error.BleDisabled)
            return
        }

        this.callback = callback
        restartTimeout()
        callback.onStatusChange(true)
        adapter.stopLeScan(innerCallback)
    }

    override fun stopScan() {
        stopTimeout()
        callback?.onStatusChange(false)
        callback = null
        adapter.stopLeScan(innerCallback)
    }
}