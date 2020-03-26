package alektas.sensor.domain.entities

sealed class DeviceServiceResource {
    data class Data(val services: List<DeviceServiceModel>) : DeviceServiceResource()
    object Error : DeviceServiceResource()
}