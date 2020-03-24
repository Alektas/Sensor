package alektas.sensor.domain

import io.reactivex.Flowable

interface Repository {
    fun sensorData(): Flowable<Int>
}