package alektas.sensor.ui.device

import alektas.sensor.App
import alektas.sensor.domain.entities.DeviceManager
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.jjoe64.graphview.series.DataPoint
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
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
    private var disposable: Disposable? = null

    init {
        App.component.inject(this)

        disposable = Observable.interval(100L, TimeUnit.MILLISECONDS)
            .map { Random.nextInt(-127..128) }
            .map { DataPoint(time++, it.toDouble()) }
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe {
                cache.add(it)
                _data.value = it
            }
    }

    fun onViewCreated(deviceAddress: String?) {
        _cashedData.value = cache.toTypedArray()
        deviceAddress?.let { deviceManager.connectDevice(it) }
    }

    fun onDeviceConnected() {
    }

    fun onDeviceDisconnected() {
    }

    fun onServicesDiscovered() {
        deviceManager.observeDevices()
    }

    override fun onCleared() {
        disposable?.takeUnless { it.isDisposed }?.dispose()
        deviceManager.disconnectDevice()
        super.onCleared()
    }

}