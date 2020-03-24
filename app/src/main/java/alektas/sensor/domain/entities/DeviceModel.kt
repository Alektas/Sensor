package alektas.sensor.domain.entities

const val DEFAULT_RSSI = 0

data class DeviceModel(val name: String?, val address: String, val rssi: Int)