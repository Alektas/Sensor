package alektas.sensor.ui.device

import alektas.sensor.R
import alektas.sensor.domain.entities.DeviceServiceModel
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.service_item.view.*

typealias ServiceSelectListener = (DeviceServiceModel) -> Unit

class ServiceAdapter(private val listener: ServiceSelectListener) :
    ListAdapter<DeviceServiceModel, ServiceAdapter.ServiceViewHolder>(
        DIFF_CALLBACK
    ) {

    init {
        setHasStableIds(true)
    }

    override fun getItemId(position: Int): Long {
        return getItem(position).uuid.hashCode().toLong()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ServiceViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.service_item, parent, false)
        return ServiceViewHolder(view)
    }

    override fun onBindViewHolder(holder: ServiceViewHolder, position: Int) {
        holder.bindTo(getItem(position), listener)
    }

    class ServiceViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val name = view.service_name
        private val uuid = view.service_uuid
        private val type = view.service_type

        fun bindTo(device: DeviceServiceModel, listener: ServiceSelectListener) = device.let {
            name.text = it.name
            uuid.text = it.uuid
            type.text = it.type

            itemView.setOnClickListener { v -> listener(it) }
        }
    }

    companion object {
        val DIFF_CALLBACK = object : DiffUtil.ItemCallback<DeviceServiceModel>() {
            override fun areItemsTheSame(oldItem: DeviceServiceModel, newItem: DeviceServiceModel): Boolean {
                return oldItem.uuid == newItem.uuid
            }

            override fun areContentsTheSame(oldItem: DeviceServiceModel, newItem: DeviceServiceModel): Boolean {
                return oldItem == newItem
            }
        }
    }
}