package alektas.sensor.ui.device

import alektas.sensor.R
import alektas.sensor.domain.entities.CharacteristicModel
import alektas.sensor.domain.entities.DeviceServiceModel
import android.animation.ObjectAnimator
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.thoughtbot.expandablerecyclerview.ExpandableRecyclerViewAdapter
import com.thoughtbot.expandablerecyclerview.models.ExpandableGroup
import com.thoughtbot.expandablerecyclerview.viewholders.ChildViewHolder
import com.thoughtbot.expandablerecyclerview.viewholders.GroupViewHolder
import kotlinx.android.synthetic.main.characteristic_item.view.*
import kotlinx.android.synthetic.main.service_item.view.*


typealias CharacteristicSelectListener = (CharacteristicModel) -> Unit

class ServiceAdapter(
    groups: List<DeviceServiceModel>,
    private val listener: CharacteristicSelectListener
) :
    ExpandableRecyclerViewAdapter<
            ServiceAdapter.ServiceViewHolder,
            ServiceAdapter.CharacteristicViewHolder>(groups) {

    init {
        setHasStableIds(true)
    }

    override fun onCreateGroupViewHolder(parent: ViewGroup?, viewType: Int): ServiceViewHolder {
        val view = LayoutInflater.from(parent!!.context)
            .inflate(R.layout.service_item, parent, false)
        return ServiceViewHolder(view)
    }

    override fun onBindGroupViewHolder(
        holder: ServiceViewHolder?,
        flatPosition: Int,
        group: ExpandableGroup<*>?
    ) {
        holder?.run {
            if (group is DeviceServiceModel) bindTo(group)
            else bindTo(group?.title ?: "UNKNOWN")
        }
    }

    override fun onCreateChildViewHolder(
        parent: ViewGroup?,
        viewType: Int
    ): CharacteristicViewHolder {
        val view = LayoutInflater.from(parent!!.context)
            .inflate(R.layout.characteristic_item, parent, false)
        return CharacteristicViewHolder(view)
    }

    override fun onBindChildViewHolder(
        holder: CharacteristicViewHolder?,
        flatPosition: Int,
        group: ExpandableGroup<*>?,
        childIndex: Int
    ) {
        holder?.bindTo((group as DeviceServiceModel).items[childIndex]) { listener(it) }
    }

    class ServiceViewHolder(view: View) : GroupViewHolder(view) {
        private val name = view.service_name
        private val uuid = view.service_uuid
        private val type = view.service_type
        private val arrow = view.service_expand_arrow

        fun bindTo(device: DeviceServiceModel) = device.let {
            name.text = it.name
            uuid.text = it.uuid
            type.text = it.type
            arrow.visibility = if (it.characteristics.isEmpty()) View.INVISIBLE else View.VISIBLE
        }

        fun bindTo(uuid: String) {
            this.uuid.text = uuid
        }

        override fun expand() {
            animateExpand()
        }

        override fun collapse() {
            animateCollapse()
        }

        private fun animateExpand() {
            ObjectAnimator.ofFloat(arrow, View.ROTATION, 360f, 180f)
                .setDuration(300L)
                .start()
        }

        private fun animateCollapse() {
            ObjectAnimator.ofFloat(arrow, View.ROTATION, 180f, 360f)
                .setDuration(300L)
                .start()
        }
    }

    class CharacteristicViewHolder(view: View) : ChildViewHolder(view) {
        private val uuid = view.characteristic_uuid
        private val value = view.characteristic_value

        fun bindTo(ch: CharacteristicModel, listener: CharacteristicSelectListener) = ch.let {
            uuid.text = it.uuid
            value.text = it.value ?: "UNKNOWN"

            itemView.setOnClickListener { listener(ch) }
        }

    }

}