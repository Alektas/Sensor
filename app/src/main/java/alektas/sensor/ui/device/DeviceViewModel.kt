package alektas.sensor.ui.device

import alektas.sensor.R
import alektas.sensor.domain.DisposableContainer
import alektas.sensor.domain.entities.CharacteristicModel
import alektas.sensor.domain.entities.DeviceManager
import alektas.sensor.domain.entities.DeviceServiceModel
import alektas.sensor.domain.entities.DeviceResource
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.plusAssign

class DeviceViewModel constructor(
    private val deviceManager: DeviceManager,
    private val address: String?
) : ViewModel() {
    private val _services = MutableLiveData<List<DeviceServiceModel>>()
    val services: LiveData<List<DeviceServiceModel>> get() = _services
    private val _connection = MutableLiveData<Int>()
    val connection: LiveData<Int> get() = _connection
    private val _error = MutableLiveData<DisposableContainer<Boolean>>()
    val error: LiveData<DisposableContainer<Boolean>> get() = _error
    private var disposable = CompositeDisposable()

    init {
        disposable += deviceManager.observeDevice()
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe {
                when (it) {
                    is DeviceResource.Data -> applyServices(it.services)
                    is DeviceResource.Connection -> applyConnection(it.isConnected)
                    is DeviceResource.Error -> applyError()
                }
            }

        address?.let { deviceManager.connectDevice(it) }
    }

    private fun applyServices(services: List<DeviceServiceModel>) {
        _services.value = services
    }

    private fun applyConnection(isConnected: Boolean) {
        _connection.value = if (isConnected) R.string.connected else R.string.disconnected
    }

    private fun applyError() {
        _error.value = DisposableContainer(true)
    }

    fun onCharacteristicNotifyClick(
        service: DeviceServiceModel,
        char: CharacteristicModel,
        isChecked: Boolean
    ) = if (isChecked) {
        deviceManager.subscribeOnCharacteristic(service.uuid, char.uuid)
    } else {
        deviceManager.unsubscribeFromCharacteristic(char.uuid)
    }

    override fun onCleared() {
        disposable.clear()
        deviceManager.disconnectDevice()
        super.onCleared()
    }

}