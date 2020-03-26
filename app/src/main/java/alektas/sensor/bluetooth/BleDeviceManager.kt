package alektas.sensor.bluetooth

import alektas.sensor.domain.entities.*
import android.annotation.SuppressLint
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCallback
import android.bluetooth.BluetoothProfile
import android.content.Context
import android.content.Intent
import android.util.Log
import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject
import javax.inject.Inject

private val TAG = BleDeviceManager::class.java.simpleName
private const val STATE_DISCONNECTED = 0
private const val STATE_CONNECTING = 1
private const val STATE_CONNECTED = 2
const val ACTION_GATT_CONNECTED = "com.example.bluetooth.le.ACTION_GATT_CONNECTED"
const val ACTION_GATT_DISCONNECTED = "com.example.bluetooth.le.ACTION_GATT_DISCONNECTED"
const val ACTION_GATT_SERVICES_DISCOVERED =
    "com.example.bluetooth.le.ACTION_GATT_SERVICES_DISCOVERED"
const val ACTION_DATA_AVAILABLE = "com.example.bluetooth.le.ACTION_DATA_AVAILABLE"
const val EXTRA_DATA = "com.example.bluetooth.le.EXTRA_DATA"

class BleDeviceManager @Inject constructor(
    private val context: Context,
    private val scanner: DeviceScanner
) :
    DeviceManager {
    private val scanCallback = object :
        DeviceCallback {
        override fun onNext(device: BluetoothDevice) {
            devices[device.address] = device
            val model = DeviceModel(device.name, device.address, 0)
            val resource = DeviceResource.Data(model)
            devicesSource.onNext(resource)
        }

        override fun onStatusChange(isActive: Boolean) {
            isScanning = isActive
            devicesSource.onNext(DeviceResource.Status(isActive))
        }

        override fun onFail(error: DeviceResource.Error) {
            devicesSource.onNext(error)
        }
    }
    private val gattCallback = object : BluetoothGattCallback() {
        override fun onConnectionStateChange(gatt: BluetoothGatt?, status: Int, newState: Int) {
            when (newState) {
                BluetoothProfile.STATE_CONNECTED -> {
                    connectionState = STATE_CONNECTED
                    broadcastUpdate(ACTION_GATT_CONNECTED)
                    Log.i(TAG, "Connected to GATT server.")
                    Log.i(TAG, "Attempting to start service discovery: " +
                            gatt?.discoverServices())
                }
                BluetoothProfile.STATE_DISCONNECTED -> {
                    connectionState = STATE_DISCONNECTED
                    Log.i(TAG, "Disconnected from GATT server.")
                    broadcastUpdate(ACTION_GATT_DISCONNECTED)
                }
            }
        }

        override fun onServicesDiscovered(gatt: BluetoothGatt?, status: Int) {
            when (status) {
                BluetoothGatt.GATT_SUCCESS -> {
//                    gatt?.services?.let {
//                        Observable.fromIterable(it)
//                            .map { DeviceServiceModel(it.uuid.toString(), it.) }
//                    }
//                    deviceServicesSource.onNext()
                    broadcastUpdate(ACTION_GATT_SERVICES_DISCOVERED)
                }
                else -> Log.w(TAG, "onServicesDiscovered received: $status")
            }
        }
    }
    private val devices = HashMap<String, BluetoothDevice>()
    private val devicesSource = PublishSubject.create<DeviceResource>()
    private val deviceServicesSource = PublishSubject.create<DeviceServiceResource>()
    private var isScanning = false
    private var gatt: BluetoothGatt? = null
    private var connectionState = STATE_DISCONNECTED

    override fun observeDevices(): Observable<DeviceResource> = devicesSource

//    override fun observeDeviceServices(): Observable<DeviceServiceResource> = deviceServicesSource

    @SuppressLint("CheckResult")
    override fun startScan() {
        devices.clear()
        Observable.fromIterable(scanner.getKnownDevices())
            .map { DeviceResource.Data(it) }
            .subscribe {
                devicesSource.onNext(it)
            }
        scanner.startScan(scanCallback)
    }

    override fun stopScan() {
        scanner.stopScan()
    }

    override fun isScanning(): Boolean = isScanning

    override fun connectDevice(address: String) {
        gatt?.let { disconnectDevice() }
        val device = devices[address]
        gatt = device?.connectGatt(context, true, gattCallback)
    }

    override fun disconnectDevice() {
        gatt?.disconnect()
        gatt?.close()
        gatt = null
    }

    private fun broadcastUpdate(action: String) {
        context.sendBroadcast(Intent(action))
    }

}