package alektas.sensor.ui.device

import alektas.sensor.R
import android.os.Bundle
import androidx.fragment.app.Fragment

import android.view.*
import kotlinx.android.synthetic.main.fragment_device.*

const val ARG_DEVICE_NAME = "device_name"
const val ARG_DEVICE_MAC = "device_mac"
const val ARG_DEVICE_RSSI = "device_rssi"

class DeviceFragment : Fragment() {
    private var name: String? = null
    private var address: String? = null
    private var rssi: Int? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            name = it.getString(ARG_DEVICE_NAME)
            address = it.getString(ARG_DEVICE_MAC)
            rssi = it.getInt(ARG_DEVICE_RSSI)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_device, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        device_name.text = name ?: getString(R.string.unknown_device_name)
        device_address.text = address
        device_rssi.text = rssi.toString()
    }

}
