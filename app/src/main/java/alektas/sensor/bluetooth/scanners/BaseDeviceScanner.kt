package alektas.sensor.bluetooth.scanners

import alektas.sensor.domain.entities.DEFAULT_RSSI
import alektas.sensor.domain.entities.DeviceModel
import alektas.sensor.domain.entities.DeviceScanner
import android.bluetooth.BluetoothAdapter
import io.reactivex.Observable
import io.reactivex.disposables.Disposable
import java.util.concurrent.TimeUnit

/**
 * Maximum time in seconds after which the device scanning will be automatically canceled
 */
const val SCANNING_TIMEOUT = 10L

abstract class BaseDeviceScanner(private val adapter: BluetoothAdapter) : DeviceScanner {
    private var scanningTimeout: Disposable? = null

    protected fun startTimeout() {
        scanningTimeout = Observable.just(true)
            .delay(SCANNING_TIMEOUT, TimeUnit.SECONDS)
            .subscribe { stopScan() }
    }

    protected fun stopTimeout() {
        scanningTimeout?.takeUnless { it.isDisposed }?.dispose()
    }

    protected fun restartTimeout() {
        stopTimeout()
        startTimeout()
    }

    override fun getKnownDevices(): List<DeviceModel> = adapter.bondedDevices
        .map { DeviceModel(it.name, it.address, DEFAULT_RSSI) }
}