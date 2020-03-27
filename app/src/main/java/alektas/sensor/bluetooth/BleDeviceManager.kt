package alektas.sensor.bluetooth

import alektas.sensor.domain.entities.*
import android.annotation.SuppressLint
import android.bluetooth.*
import android.content.Context
import android.util.Log
import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject
import javax.inject.Inject

private val TAG = BleDeviceManager::class.java.simpleName

class BleDeviceManager @Inject constructor(
    private val context: Context,
    private val scanner: DeviceScanner
) :
    DeviceManager {
    private val scanCallback = object :
        DeviceCallback {
        override fun onNext(device: BluetoothDevice, rssi: Int) {
            devices[device.address] = device
            val model = DeviceModel(device.name, device.address, rssi)
            val resource = ScanResource.Data(model)
            scanSource.onNext(resource)
        }

        override fun onStatusChange(isActive: Boolean) {
            scanSource.onNext(ScanResource.Status(isActive))
        }

        override fun onFail(error: ScanResource.Error) {
            scanSource.onNext(error)
        }
    }
    private val gattCallback = object : BluetoothGattCallback() {
        override fun onConnectionStateChange(gatt: BluetoothGatt?, status: Int, newState: Int) {
            when (newState) {
                BluetoothProfile.STATE_CONNECTED -> {
                    deviceSource.onNext(DeviceResource.Connection(true))
                    Log.d(TAG, "Connected to GATT server.")
                    Log.d(
                        TAG, "Attempting to start service discovery: " +
                                gatt?.discoverServices()
                    )
                }
                BluetoothProfile.STATE_DISCONNECTED -> {
                    deviceSource.onNext(DeviceResource.Connection(false))
                    Log.d(TAG, "Disconnected from GATT server.")
                }
            }
        }

        override fun onServicesDiscovered(gatt: BluetoothGatt?, status: Int) {
            when (status) {
                BluetoothGatt.GATT_SUCCESS -> {
                    Log.d(TAG, "Discovered device services")
                    gatt?.services?.let { services ->
                        lastServices = services
                        services.map { it.toModel() }.let {
                            deviceSource.onNext(DeviceResource.Data(it))
                        }
                    }
                }
                else -> {
                    Log.w(TAG, "onServicesDiscovered received: $status")
                    deviceSource.onNext(DeviceResource.Error)
                }
            }
        }

        override fun onCharacteristicChanged(
            gatt: BluetoothGatt?,
            characteristic: BluetoothGattCharacteristic?
        ) {
            Log.d(TAG, "onCharacteristicChanged: ${characteristic?.value}")
        }
    }
    private val devices = HashMap<String, BluetoothDevice>()
    private val scanSource = PublishSubject.create<ScanResource>()
    private val deviceSource = PublishSubject.create<DeviceResource>()
    private var gatt: BluetoothGatt? = null
    private var lastServices: List<BluetoothGattService>? = null
    private val subscribedChars = HashMap<String, BluetoothGattCharacteristic>()

    override fun observeScanning(): Observable<ScanResource> = scanSource

    override fun observeDevice(): Observable<DeviceResource> = deviceSource

    @SuppressLint("CheckResult")
    override fun startScan() {
        devices.clear()
        Observable.fromIterable(scanner.getKnownDevices())
            .map { ScanResource.Data(it) }
            .subscribe {
                scanSource.onNext(it)
            }
        scanner.startScan(scanCallback)
    }

    override fun stopScan() {
        scanner.stopScan()
    }

    override fun connectDevice(address: String) {
        gatt?.let { disconnectDevice() }
        val device = devices[address]
        gatt = device?.connectGatt(context, false, gattCallback)
    }

    override fun disconnectDevice() {
        gatt?.disconnect()
        gatt?.close()
        gatt = null
        lastServices = null
    }

    override fun subscribeOnCharacteristic(serviceUuid: String, charUuid: String) {
        // TODO: if didn't find a char send error
        val char = findCharacteristic(serviceUuid, charUuid) ?: return

        val previous = subscribedChars.put(char.uuid.toString(), char)
        // if previous is not a null the char is already subscribed, so return
        if (previous != null) return

        gatt?.setCharacteristicNotification(char, true)
    }

    override fun unsubscribeFromCharacteristic(charUuid: String) {
        // if there is no char with desired UUID just return (there is no subscribtion on it)
        val char = subscribedChars[charUuid] ?: return
        gatt?.setCharacteristicNotification(char, false)
        subscribedChars.remove(charUuid)
    }

    override fun isCharacteristicSubscribed(uuid: String): Boolean =
        subscribedChars.containsKey(uuid)

    private fun findCharacteristic(
        serviceUuid: String,
        charUuid: String
    ): BluetoothGattCharacteristic? = lastServices?.find { it.uuid.toString() == serviceUuid }
        ?.characteristics?.find { it.uuid.toString() == charUuid }

    private fun BluetoothGattService.toModel(): DeviceServiceModel {
        val type = when (type) {
            BluetoothGattService.SERVICE_TYPE_PRIMARY -> "PRIMARY"
            BluetoothGattService.SERVICE_TYPE_SECONDARY -> "SECONDARY"
            else -> "UNKNOWN"
        }
        return DeviceServiceModel(toString(), uuid.toString(), type, characteristics.toModel())
    }

    private fun List<BluetoothGattCharacteristic>.toModel(): List<CharacteristicModel> = map {
        CharacteristicModel(it.uuid.toString(), it.getStringValue(0))
    }

}