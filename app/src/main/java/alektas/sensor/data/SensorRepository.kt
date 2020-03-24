package alektas.sensor.data

import alektas.sensor.domain.Repository
import io.reactivex.Flowable
import java.util.concurrent.TimeUnit

class SensorRepository : Repository {
    override fun sensorData(): Flowable<Int> {
        return Flowable.interval(1000L, TimeUnit.MILLISECONDS)
            .map { it.toInt() }
    }
}