package alektas.sensor.ui

import alektas.sensor.App
import alektas.sensor.R
import alektas.sensor.domain.entities.DeviceManager
import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.content.Intent
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.annotation.StringRes
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.content_devices.*
import javax.inject.Inject

private const val REQUEST_ENABLE_BLUETOOTH = 101

class MainActivity : AppCompatActivity() {
    @Inject
    lateinit var bleAdapter: BluetoothAdapter

    @Inject
    lateinit var deviceManager: DeviceManager

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory
    private val viewModel: ScanViewModel by viewModels { viewModelFactory }
    private lateinit var deviceAdapter: DeviceAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        if (!packageManager.hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            toast(R.string.device_ble_support_error)
            finish()
        }

        App.component.inject(this)

        initDeviceList()
        subscribeOn(viewModel)
    }

    private fun initDeviceList() {
        deviceAdapter = DeviceAdapter()
        with(device_list) {
            setHasFixedSize(true)
            layoutManager = LinearLayoutManager(this@MainActivity)
            adapter = deviceAdapter
        }
    }

    private fun subscribeOn(viewModel: ScanViewModel) {
        viewModel.devices.observe(this, Observer {
            deviceAdapter.submitList(it)
        })

        viewModel.placeholderState.observe(this, Observer {
            scan_placeholder_text.visibility = it
        })

        viewModel.scanStatus.observe(this, Observer {
            scan_activity_bar.visibility = it.progressVisibility
            bluetooth_scan_btn.setImageResource(it.btnIconRes)
        })

        viewModel.errorEvent.observe(this, Observer { msg ->
            msg.getValue()?.let {
                Snackbar.make(bluetooth_scan_btn, it, Snackbar.LENGTH_SHORT)
            }
        })

        viewModel.enableBleEvent.observe(this, Observer { event ->
            event.getValue()?.let {
                toast(it)
                requestBle()
            }
        })
    }

    private fun requestBle() {
        val bleEnableIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
        startActivityForResult(bleEnableIntent, REQUEST_ENABLE_BLUETOOTH)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (REQUEST_ENABLE_BLUETOOTH != requestCode) return

        if (Activity.RESULT_OK != resultCode) {
            toast(R.string.ble_enable_request)
            return
        }

        viewModel.onBleScanClick()
    }

    fun onBleScanClick(view: View) {
        if (view.id != R.id.bluetooth_scan_btn) return

        viewModel.onBleScanClick()
    }

    private fun toast(@StringRes msg: Int) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
    }

    private fun toast(msg: String) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
    }
}
