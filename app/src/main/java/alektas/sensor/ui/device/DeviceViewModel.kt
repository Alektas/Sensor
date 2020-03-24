package alektas.sensor.ui.device

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.jjoe64.graphview.series.DataPoint
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import java.util.concurrent.TimeUnit
import kotlin.random.Random
import kotlin.random.nextInt

class DeviceViewModel : ViewModel() {
    private var time = 0.0
    private val cache = mutableListOf<DataPoint>()
    private val _cashedData = MutableLiveData<Array<DataPoint>>()
    val cashedData: LiveData<Array<DataPoint>> get() = _cashedData
    private val _data = MutableLiveData<DataPoint>()
    val data: LiveData<DataPoint> get() = _data
    private var disposable: Disposable? = null

    init {
        disposable = Observable.interval(100L, TimeUnit.MILLISECONDS)
            .map { Random.nextInt(-127..128) }
            .map { DataPoint(time++, it.toDouble()) }
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe {
                cache.add(it)
                _data.value = it
            }
    }

    fun onViewCreated() {
        _cashedData.value = cache.toTypedArray()
    }

    override fun onCleared() {
        disposable?.takeUnless { it.isDisposed }?.dispose()
        super.onCleared()
    }

}