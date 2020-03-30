package alektas.sensor.domain.entities

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class CharacteristicModel(
    val uuid: String,
    val value: String?,
    val properties: List<String>,
    val isObserved: Boolean
) :
    Parcelable