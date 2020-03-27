package alektas.sensor.domain.entities

sealed class DeviceResource {
    data class Data(val services: List<DeviceServiceModel>) : DeviceResource()
    data class Connection(val isConnected: Boolean) : DeviceResource()
    object Error : DeviceResource()
}