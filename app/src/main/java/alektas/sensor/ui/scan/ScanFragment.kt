package alektas.sensor.ui.scan

import alektas.sensor.App
import alektas.sensor.R
import alektas.sensor.domain.entities.DeviceModel
import alektas.sensor.ui.device.ARG_DEVICE_MAC
import alektas.sensor.ui.device.ARG_DEVICE_NAME
import alektas.sensor.ui.device.ARG_DEVICE_RSSI
import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.fragment_scan.*
import javax.inject.Inject

class ScanFragment : Fragment() {
    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory
    private val viewModel: ScanViewModel by viewModels { viewModelFactory }
    private lateinit var deviceAdapter: DeviceAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
//        setHasOptionsMenu(true)
        App.component.inject(this)
    }

//    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
//        super.onCreateOptionsMenu(menu, inflater)
//    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_scan, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        initDeviceList()
        subscribeOn(viewModel)
    }

    private fun initDeviceList() {
        deviceAdapter = DeviceAdapter { device ->
            viewModel.onSelect(device)
        }
        with(scan_device_list) {
            setHasFixedSize(true)
            layoutManager = LinearLayoutManager(requireContext())
            adapter = deviceAdapter
        }
    }

    private fun subscribeOn(viewModel: ScanViewModel) {
        viewModel.devices.observe(viewLifecycleOwner, Observer {
            deviceAdapter.submitList(it)
        })

        viewModel.placeholderState.observe(viewLifecycleOwner, Observer {
            scan_placeholder_text.visibility = it
        })

        viewModel.scanStatus.observe(viewLifecycleOwner, Observer {
            scan_activity_bar.visibility = it.progressVisibility
        })

        viewModel.errorEvent.observe(viewLifecycleOwner, Observer { msg ->
            msg.getValue()?.let {
                Snackbar.make(scan_device_list, it, Snackbar.LENGTH_SHORT)
            }
        })

        viewModel.showDeviceEvent.observe(viewLifecycleOwner, Observer { event ->
            event.getValue()?.let {
                showDeviceDetails(it)
            }
        })
    }

    private fun showDeviceDetails(device: DeviceModel) {
        val args = Bundle().apply {
            putString(ARG_DEVICE_NAME, device.name)
            putString(ARG_DEVICE_MAC, device.address)
            putInt(ARG_DEVICE_RSSI, device.rssi)
        }
        findNavController().navigate(R.id.action_scanFragment_to_deviceFragment, args)
    }

}
