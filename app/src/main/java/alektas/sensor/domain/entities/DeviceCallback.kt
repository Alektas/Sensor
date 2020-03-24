package alektas.sensor.domain.entities

interface DeviceCallback {
    fun onNext(device: DeviceModel)
    fun onStatusChange(isActive: Boolean)
    fun onFail(error: DeviceResource.Error)
}