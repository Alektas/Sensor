package alektas.sensor.ui.device

import alektas.sensor.App
import alektas.sensor.R
import alektas.sensor.domain.entities.DeviceManager
import android.os.Bundle
import androidx.fragment.app.Fragment

import android.view.*
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.fragment_device.*
import javax.inject.Inject

const val ARG_DEVICE_NAME = "device_name"
const val ARG_DEVICE_MAC = "device_mac"
const val ARG_DEVICE_RSSI = "device_rssi"

class DeviceFragment : Fragment() {
    @Inject
    lateinit var deviceManager: DeviceManager
    private lateinit var viewModelFactory: ViewModelProvider.Factory
    private val viewModel: DeviceViewModel by viewModels { viewModelFactory }
    private lateinit var serviceAdapter: ServiceAdapter
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
        viewModelFactory = object : ViewModelProvider.Factory {
            override fun <T : ViewModel?> create(modelClass: Class<T>): T {
                return DeviceViewModel(deviceManager, address) as T
            }
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

        initServiceList()
        subscribeOn(viewModel)
    }

    private fun initServiceList() {
        serviceAdapter = ServiceAdapter { service ->

        }
        with(device_services_list) {
            setHasFixedSize(true)
            layoutManager = LinearLayoutManager(requireContext())
            adapter = serviceAdapter
        }
    }

    private fun subscribeOn(viewModel: DeviceViewModel) {
        viewModel.services.observe(viewLifecycleOwner, Observer {
            serviceAdapter.submitList(it)
        })
        viewModel.connection.observe(viewLifecycleOwner, Observer {
            device_connection_status.text = getString(it)
        })
        viewModel.error.observe(viewLifecycleOwner, Observer {
            it.getValue()?.let { showError() }
        })
    }

    private fun showError() {
        Snackbar.make(
            device_root,
            R.string.device_services_discovering_error,
            Snackbar.LENGTH_SHORT
        ).show()
    }

}
