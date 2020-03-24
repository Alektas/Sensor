package alektas.sensor.bluetooth.scanners

import alektas.sensor.domain.entities.DeviceCallback
import alektas.sensor.domain.entities.DeviceModel
import alektas.sensor.domain.entities.DeviceResource
import android.bluetooth.BluetoothAdapter
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.os.Build
import androidx.annotation.RequiresApi

@RequiresApi(Build.VERSION_CODES.LOLLIPOP)
class BleDeviceScanner(private val adapter: BluetoothAdapter) :
    BaseDeviceScanner(adapter) {
    private var callback: DeviceCallback? = null
    private val innerCallback: ScanCallback = object : ScanCallback() {
        override fun onScanResult(callbackType: Int, result: ScanResult?) {
            result?.device?.let {
                callback?.onNext(DeviceModel(it.name, it.address, result.rssi))
            }
            super.onScanResult(callbackType, result)
        }

        override fun onScanFailed(errorCode: Int) {
            callback?.onStatusChange(false)
            callback?.onFail(DeviceResource.Error.ScanError(errorCode))
            super.onScanFailed(errorCode)
        }
    }

    override fun startScan(callback: DeviceCallback) {
        if (!adapter.isEnabled) {
            callback.onStatusChange(false)
            callback.onFail(DeviceResource.Error.BleDisabled)
            return
        }

        this.callback = callback
        restartTimeout()
        callback.onStatusChange(true)
        adapter.bluetoothLeScanner?.startScan(innerCallback)
    }

    override fun stopScan() {
        stopTimeout()
        callback?.onStatusChange(false)
        adapter.bluetoothLeScanner?.stopScan(innerCallback)
        this.callback = null
    }

}
