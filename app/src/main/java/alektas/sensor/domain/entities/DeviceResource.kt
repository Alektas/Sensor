package alektas.sensor.domain.entities

sealed class DeviceResource {
    data class Data(val device: DeviceModel) : DeviceResource()
    data class Status(val isActive: Boolean) : DeviceResource()
    sealed class Error : DeviceResource() {
        object BleDisabled : Error()
        data class ScanError(val code: Int) : Error()
    }
}