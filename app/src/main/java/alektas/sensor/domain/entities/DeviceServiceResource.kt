package alektas.sensor.domain.entities

sealed class DeviceServiceResource {
    data class Data(val services: List<DeviceServiceModel>) : DeviceServiceResource()
    data class Status(val isConnected: Boolean) : DeviceServiceResource()
    object Error : DeviceServiceResource()
}