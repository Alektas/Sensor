package alektas.sensor.domain.entities

sealed class ScanResource {
    data class Data(val device: DeviceModel) : ScanResource()
    data class Status(val isActive: Boolean) : ScanResource()
    sealed class Error : ScanResource() {
        object BleDisabled : Error()
        data class ScanError(val code: Int) : Error()
    }
}