package alektas.sensor.ui.device

import alektas.sensor.App
import alektas.sensor.R
import alektas.sensor.bluetooth.ACTION_DATA_AVAILABLE
import alektas.sensor.bluetooth.ACTION_GATT_CONNECTED
import alektas.sensor.bluetooth.ACTION_GATT_DISCONNECTED
import alektas.sensor.bluetooth.ACTION_GATT_SERVICES_DISCOVERED
import alektas.sensor.di.DEVICE_VM_FACTORY_NAME
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import androidx.fragment.app.Fragment

import android.view.*
import androidx.annotation.StringRes
import androidx.core.content.ContextCompat
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.jjoe64.graphview.series.DataPoint
import com.jjoe64.graphview.series.LineGraphSeries
import kotlinx.android.synthetic.main.fragment_device.*
import javax.inject.Inject
import javax.inject.Named

const val ARG_DEVICE_NAME = "device_name"
const val ARG_DEVICE_MAC = "device_mac"
const val ARG_DEVICE_RSSI = "device_rssi"

class DeviceFragment : Fragment() {
    @Inject
    @field:Named(value = DEVICE_VM_FACTORY_NAME)
    lateinit var viewModelFactory: ViewModelProvider.Factory
    private val viewModel: DeviceViewModel by viewModels { viewModelFactory }
    private var dataSeries = LineGraphSeries<DataPoint>()
    private var name: String? = null
    private var address: String? = null
    private var rssi: Int? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        App.component.inject(this)
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
        setupGraph()

        viewModel.onViewCreated(address)
        viewModel.cashedData.observe(viewLifecycleOwner, Observer {
            dataSeries.resetData(it)
        })
        viewModel.data.observe(viewLifecycleOwner, Observer {
            dataSeries.appendData(it, true, 100)
        })
    }

    private fun setupGraph() {
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
    }

    override fun onResume() {
        super.onResume()
        val filters = IntentFilter().apply {
            addAction(ACTION_GATT_CONNECTED)
            addAction(ACTION_GATT_DISCONNECTED)
            addAction(ACTION_GATT_SERVICES_DISCOVERED)
            addAction(ACTION_DATA_AVAILABLE)
        }
        context?.registerReceiver(gattUpdateReceiver, filters)
    }

    override fun onPause() {
        context?.unregisterReceiver(gattUpdateReceiver)
        super.onPause()
    }

    // Handles various events fired by the Service.
    // ACTION_GATT_CONNECTED: connected to a GATT server.
    // ACTION_GATT_DISCONNECTED: disconnected from a GATT server.
    // ACTION_GATT_SERVICES_DISCOVERED: discovered GATT services.
    // ACTION_DATA_AVAILABLE: received data from the device. This can be a
    // result of read or notification operations.
    private val gattUpdateReceiver = object : BroadcastReceiver() {

//        private lateinit var bluetoothLeService: BluetoothLeService

        override fun onReceive(context: Context, intent: Intent) {
            when (intent.action) {
                ACTION_GATT_CONNECTED -> {
                    updateConnectionState(R.string.connected)
                    viewModel.onDeviceConnected()
                }
                ACTION_GATT_DISCONNECTED -> {
                    updateConnectionState(R.string.disconnected)
                    clearUI()
                    viewModel.onDeviceDisconnected()
                }
                ACTION_GATT_SERVICES_DISCOVERED -> {
                    // Show all the supported services and characteristics on the
                    // user interface.
                    viewModel.onServicesDiscovered()
//                    displayGattServices(bluetoothLeService.getSupportedGattServices())
                }
                ACTION_DATA_AVAILABLE -> {
//                    displayData(intent.getStringExtra(BluetoothLeService.EXTRA_DATA))
                }
            }
        }
    }

    private fun updateConnectionState(@StringRes msgRes: Int) {
        device_connection_status.text = getString(msgRes)
    }

    private fun clearUI() {
//        updateConnectionState(R.string.disconnected)
    }

}
