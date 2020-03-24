package alektas.sensor.ui

import alektas.sensor.App
import alektas.sensor.R
import alektas.sensor.domain.DisposableContainer
import alektas.sensor.domain.entities.DeviceManager
import alektas.sensor.domain.entities.DeviceModel
import alektas.sensor.domain.entities.DeviceResource
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
    private var bleScanDisposable: Disposable? = null

    init {
        App.component.inject(this)

        bleScanDisposable = deviceManager.observeDevices()
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({
                when (it) {
                    is DeviceResource.Data -> applyDevice(it.device)
                    is DeviceResource.Status -> applyStatus(it.isActive)
                    is DeviceResource.Error -> when (it) {
                        is DeviceResource.Error.BleDisabled -> applyRequest(R.string.ble_enable_request)
                        is DeviceResource.Error.ScanError -> applyError(R.string.ble_scan_error)
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

    fun onBleScanClick() {
        if (deviceManager.isScanning()) {
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
        _scanStatus.value = if (isActive) {
            ScanningState(View.VISIBLE, R.drawable.ic_close_black_24dp)
        } else {
            ScanningState(View.INVISIBLE, R.drawable.ic_bluetooth_searching_black_24dp)
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