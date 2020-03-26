package alektas.sensor.ui.device

import alektas.sensor.App
import alektas.sensor.domain.DisposableContainer
import alektas.sensor.domain.entities.DeviceManager
import alektas.sensor.domain.entities.DeviceServiceModel
import alektas.sensor.domain.entities.DeviceServiceResource
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.jjoe64.graphview.series.DataPoint
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.plusAssign
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import kotlin.random.Random
import kotlin.random.nextInt

class DeviceViewModel @Inject constructor(
    private val deviceManager: DeviceManager
) : ViewModel() {
    private var time = 0.0
    private val cache = mutableListOf<DataPoint>()
    private val _cashedData = MutableLiveData<Array<DataPoint>>()
    val cashedData: LiveData<Array<DataPoint>> get() = _cashedData
    private val _data = MutableLiveData<DataPoint>()
    val data: LiveData<DataPoint> get() = _data
    private val _services = MutableLiveData<List<DeviceServiceModel>>()
    val services: LiveData<List<DeviceServiceModel>> get() = _services
    private val _error = MutableLiveData<DisposableContainer<Boolean>>()
    val error: LiveData<DisposableContainer<Boolean>> get() = _error
    private var disposable = CompositeDisposable()

    init {
        App.component.inject(this)

        disposable += Observable.interval(100L, TimeUnit.MILLISECONDS)
            .map { Random.nextInt(-127..128) }
            .map { DataPoint(time++, it.toDouble()) }
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe {
                cache.add(it)
                _data.value = it
            }

        disposable += deviceManager.observeDeviceServices()
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe {
                when (it) {
                    is DeviceServiceResource.Data -> applyServices(it.services)
                    is DeviceServiceResource.Error -> applyError()
                }
            }
    }

    fun onViewCreated(deviceAddress: String?) {
        _cashedData.value = cache.toTypedArray()
        deviceAddress?.let { deviceManager.connectDevice(it) }
    }

    private fun applyServices(services: List<DeviceServiceModel>) {
        _services.value = services
    }

    private fun applyError() {
        _error.value = DisposableContainer(true)
    }

    override fun onCleared() {
        disposable.clear()
        deviceManager.disconnectDevice()
        super.onCleared()
    }

    fun onSelect(service: DeviceServiceModel) {

    }

}