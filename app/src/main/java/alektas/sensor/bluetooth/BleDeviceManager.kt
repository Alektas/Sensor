package alektas.sensor.bluetooth

import alektas.sensor.domain.entities.*
import android.annotation.SuppressLint
import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject
import javax.inject.Inject

class BleDeviceManager @Inject constructor(private val scanner: DeviceScanner) :
    DeviceManager {
    private val callback = object :
        DeviceCallback {
        override fun onNext(device: DeviceModel) {
            devices.onNext(DeviceResource.Data(device))
        }

        override fun onStatusChange(isActive: Boolean) {
            isScanning = isActive
            devices.onNext(DeviceResource.Status(isActive))
        }

        override fun onFail(error: DeviceResource.Error) {
            devices.onNext(error)
        }
    }
    private val devices = PublishSubject.create<DeviceResource>()
    private var isScanning = false

    override fun observeDevices(): Observable<DeviceResource> = devices

    @SuppressLint("CheckResult")
    override fun startScan() {
        Observable.fromIterable(scanner.getKnownDevices())
            .map { DeviceResource.Data(it) }
            .subscribe { devices.onNext(it) }
        scanner.startScan(callback)
    }

    override fun stopScan() {
        scanner.stopScan()
    }

    override fun isScanning(): Boolean = isScanning

}