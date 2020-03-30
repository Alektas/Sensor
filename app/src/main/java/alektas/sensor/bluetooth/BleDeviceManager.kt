package alektas.sensor.bluetooth

import alektas.sensor.R
import alektas.sensor.domain.entities.*
import android.annotation.SuppressLint
import android.bluetooth.*
import android.content.Context
import android.util.Log
import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject
import java.util.*
import javax.inject.Inject
import kotlin.collections.HashMap

private val TAG = BleDeviceManager::class.java.simpleName
private const val CCCD_UUID = "00002902-0000-1000-8000-00805f9b34fb"

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
                    gatt?.close()
                    this@BleDeviceManager.gatt = null
                    Log.d(TAG, "Disconnected from GATT server.")
                }
            }
        }

        override fun onServicesDiscovered(gatt: BluetoothGatt?, status: Int) {
            when (status) {
                BluetoothGatt.GATT_SUCCESS -> {
                    Log.d(TAG, "Discovered device services")
                    gatt?.services?.let { services ->
                        services.map { it.toModel() }.let {
                            latestServices = it
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
            Log.d(TAG, "onCharChanged: value=${characteristic?.value?.contentToString()}")
            characteristic?.let {
                latestServices = latestServices.updateCharacteristic(it.toModel())
                deviceSource.onNext(DeviceResource.Data(latestServices))
            }
        }
    }
    private val devices = HashMap<String, BluetoothDevice>()
    private val scanSource = PublishSubject.create<ScanResource>()
    private val deviceSource = PublishSubject.create<DeviceResource>()
    private var gatt: BluetoothGatt? = null
    private var latestServices = listOf<DeviceServiceModel>()
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
    }

    override fun subscribeOnCharacteristic(serviceUuid: String, charUuid: String) {
        // TODO: if didn't find a char send error
        val char = findCharacteristic(serviceUuid, charUuid) ?: return

        val previous = subscribedChars.put(char.uuid.toString(), char)
        // if previous is not a null the char is already subscribed, so return
        if (previous != null) return

        findNotifyDescription(char)?.let {
            it.value = BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE
            gatt?.writeDescriptor(it)
            gatt?.setCharacteristicNotification(char, true)
            latestServices = latestServices.updateCharacteristic(char.toModel(isObserved = true))
        }
    }

    override fun unsubscribeFromCharacteristic(charUuid: String) {
        // if there is no char with desired UUID just return (there is no subscribtion on it)
        val char = subscribedChars[charUuid] ?: return

        findNotifyDescription(char)?.let {
            gatt?.setCharacteristicNotification(char, false)
            subscribedChars.remove(charUuid)
            latestServices = latestServices.updateCharacteristic(char.toModel(isObserved = false))
        }
    }

    private fun findNotifyDescription(char: BluetoothGattCharacteristic): BluetoothGattDescriptor? =
        char.descriptors.find { it.uuid == UUID.fromString(CCCD_UUID) }

    override fun isCharacteristicSubscribed(uuid: String): Boolean =
        subscribedChars.containsKey(uuid)

    private fun findCharacteristic(
        serviceUuid: String,
        charUuid: String
    ): BluetoothGattCharacteristic? = gatt?.getService(UUID.fromString(serviceUuid))
        ?.getCharacteristic(UUID.fromString(charUuid))

    private fun BluetoothGattService.toModel(): DeviceServiceModel {
        val type = when (type) {
            BluetoothGattService.SERVICE_TYPE_PRIMARY -> "PRIMARY"
            BluetoothGattService.SERVICE_TYPE_SECONDARY -> "SECONDARY"
            else -> "UNKNOWN"
        }
        return DeviceServiceModel(null, uuid.toString(), type, characteristics.toModel())
    }

    private fun List<BluetoothGattCharacteristic>.toModel(): List<CharacteristicModel> = map {
        it.toModel()
    }

    private fun BluetoothGattCharacteristic.toModel(
        value: String? = null,
        isObserved: Boolean? = null
    ): CharacteristicModel =
        CharacteristicModel(
            uuid.toString(),
            value ?: this.value?.contentToString(),
            parseProperties(properties),
            isObserved ?: subscribedChars.contains(uuid.toString())
        )

    private fun parseProperties(propertiesWord: Int): List<String> {
        val properties = mutableListOf<String>()

        if (propertiesWord and BluetoothGattCharacteristic.PROPERTY_READ > 0) {
            properties.add(context.getString(R.string.char_prop_read))
        }
        if (propertiesWord and (BluetoothGattCharacteristic.PROPERTY_WRITE or
                    BluetoothGattCharacteristic.PROPERTY_WRITE_NO_RESPONSE) > 0
        ) {
            properties.add(context.getString(R.string.char_prop_write))
        }
        if (propertiesWord and BluetoothGattCharacteristic.PROPERTY_NOTIFY > 0) {
            properties.add(context.getString(R.string.char_prop_notify))
        }
        if (propertiesWord and BluetoothGattCharacteristic.PROPERTY_INDICATE > 0) {
            properties.add(context.getString(R.string.char_prop_indicate))
        }

        return properties
    }

    private fun List<DeviceServiceModel>.updateCharacteristic(
        characteristic: CharacteristicModel
    ): List<DeviceServiceModel> =
        this.map { service ->
            val i = service.characteristics.indexOfFirst { it.uuid == characteristic.uuid }
            if (i >= 0) {
                val chars = service.characteristics.toMutableList().apply {
                    set(i, characteristic)
                }
                service.copy(characteristics = chars.toList())
            } else {
                service
            }
        }

}