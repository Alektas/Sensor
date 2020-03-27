package alektas.sensor.ui.scan

import alektas.sensor.App
import alektas.sensor.R
import alektas.sensor.domain.DisposableContainer
import alektas.sensor.domain.entities.DeviceManager
import alektas.sensor.domain.entities.DeviceModel
import alektas.sensor.domain.entities.ScanResource
import android.view.View
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import javax.inject.Inject

class ScanViewModel @Inject constructor(
    private val deviceManager: DeviceManager
) : ViewModel() {

    private val deviceMap = HashMap<String, DeviceModel>()
    private val _devices = MutableLiveData<List<DeviceModel>>()
    val devices: LiveData<List<DeviceModel>> get() = _devices
    private val _placeholderState = MutableLiveData(View.VISIBLE)
    val placeholderState: LiveData<Int> get() = _placeholderState
    private val _scanStatus = MutableLiveData<ScanningState>()
    val scanStatus: LiveData<ScanningState> get() = _scanStatus
    private val _errorEvent = MutableLiveData<DisposableContainer<Int>>()
    val errorEvent: LiveData<DisposableContainer<Int>> get() = _errorEvent
    private val _enableBleEvent = MutableLiveData<DisposableContainer<Int>>()
    val enableBleEvent: LiveData<DisposableContainer<Int>> get() = _enableBleEvent
    private val _showDeviceEvent = MutableLiveData<DisposableContainer<DeviceModel>>()
    val showDeviceEvent: LiveData<DisposableContainer<DeviceModel>> get() = _showDeviceEvent
    private var bleScanDisposable: Disposable? = null
    private var isScanning = false

    init {
        App.component.inject(this)

        bleScanDisposable = deviceManager.observeScanning()
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({
                when (it) {
                    is ScanResource.Data -> applyDevice(it.device)
                    is ScanResource.Status -> applyStatus(it.isActive)
                    is ScanResource.Error -> when (it) {
                        is ScanResource.Error.BleDisabled -> applyRequest(R.string.ble_enable_request)
                        is ScanResource.Error.ScanError -> applyError(R.string.ble_scan_error)
                    }
                }
            }, {
                applyError(R.string.ble_scan_unknown_error)
            })
    }

    override fun onCleared() {
        bleScanDisposable?.takeUnless { it.isDisposed }?.dispose()
        super.onCleared()
    }

    fun onSelect(device: DeviceModel) {
        stopScanning()
        _showDeviceEvent.value = DisposableContainer(device)
    }

    fun onBleScanIntent() {
        if (isScanning) {
            stopScanning()
            return
        }
        startScanning()
    }

    private fun stopScanning() {
        deviceManager.stopScan()
    }

    private fun startScanning() {
        _devices.value = listOf()
        _placeholderState.value = View.VISIBLE
        deviceMap.clear()

        deviceManager.startScan()
    }

    private fun applyDevice(device: DeviceModel) {
        deviceMap[device.address] = device
        if (deviceMap.isNotEmpty()) _placeholderState.value = View.INVISIBLE
        _devices.value = deviceMap.values.toList()
    }

    private fun applyStatus(isActive: Boolean) {
        isScanning = isActive
        _scanStatus.value = if (isActive) {
            ScanningState(
                View.VISIBLE,
                R.drawable.ic_close_black_24dp
            )
        } else {
            ScanningState(
                View.INVISIBLE,
                R.drawable.ic_bluetooth_searching_black_24dp
            )
        }
    }

    private fun applyRequest(@StringRes msgRes: Int) {
        _enableBleEvent.value = DisposableContainer(msgRes)
    }

    private fun applyError(@StringRes msgRes: Int) {
        _errorEvent.value = DisposableContainer(msgRes)
    }

    data class ScanningState(val progressVisibility: Int, @DrawableRes val btnIconRes: Int)

}