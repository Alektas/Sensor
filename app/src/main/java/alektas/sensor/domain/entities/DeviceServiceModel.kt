package alektas.sensor.domain.entities

import com.thoughtbot.expandablerecyclerview.models.ExpandableGroup

data class DeviceServiceModel(
    val name: String?,
    val uuid: String,
    val type: String,
    val characteristics: List<CharacteristicModel>
) : ExpandableGroup<CharacteristicModel>(uuid, characteristics)