package alektas.sensor.ui

import alektas.sensor.R
import alektas.sensor.domain.entities.DeviceModel
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.device_item.view.*

class DeviceAdapter : ListAdapter<DeviceModel, DeviceAdapter.DeviceViewHolder>(DIFF_CALLBACK) {

    init {
        setHasStableIds(true)
    }

    override fun getItemId(position: Int): Long {
        return getItem(position).address.hashCode().toLong()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DeviceViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.device_item, parent, false)
        return DeviceViewHolder(view)
    }

    override fun onBindViewHolder(holder: DeviceViewHolder, position: Int) {
        holder.bindTo(getItem(position))
    }

    class DeviceViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val name = view.device_name
        private val address = view.device_address
        private val rssi = view.device_rssi

        fun bindTo(device: DeviceModel) = device.let {
            name.text = it.name ?: it.address
            address.text = it.address
            rssi.text = it.rssi.toString()
        }
    }

    companion object {
        val DIFF_CALLBACK = object : DiffUtil.ItemCallback<DeviceModel>() {
            override fun areItemsTheSame(oldItem: DeviceModel, newItem: DeviceModel): Boolean {
                return oldItem.address == newItem.address
            }

            override fun areContentsTheSame(oldItem: DeviceModel, newItem: DeviceModel): Boolean {
                return oldItem == newItem
            }
        }
    }
}