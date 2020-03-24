package alektas.sensor.ui.device

import alektas.sensor.R
import android.os.Bundle
import androidx.fragment.app.Fragment

import android.view.*
import androidx.core.content.ContextCompat
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import com.jjoe64.graphview.series.DataPoint
import com.jjoe64.graphview.series.LineGraphSeries
import kotlinx.android.synthetic.main.fragment_device.*

const val ARG_DEVICE_NAME = "device_name"
const val ARG_DEVICE_MAC = "device_mac"
const val ARG_DEVICE_RSSI = "device_rssi"

class DeviceFragment : Fragment() {
    private val viewModel: DeviceViewModel by viewModels()
    private var dataSeries = LineGraphSeries<DataPoint>()
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

        dataSeries.color = ContextCompat.getColor(requireContext(), R.color.colorGraphLines)
        device_data_graph.apply {
            viewport.apply {
                isXAxisBoundsManual = true
                maxXAxisSize = 100.0
                setMaxX(100.0)
                isYAxisBoundsManual = true
                setMaxY(128.0)
                setMinY(-128.0)
            }
            addSeries(dataSeries)
        }

        viewModel.onViewCreated()
        viewModel.cashedData.observe(viewLifecycleOwner, Observer {
            dataSeries.resetData(it)
        })
        viewModel.data.observe(viewLifecycleOwner, Observer {
            println(it)
            dataSeries.appendData(it, true, 100)
        })
    }

}
