package alektas.sensor.ui

import alektas.sensor.App
import alektas.sensor.R
import alektas.sensor.domain.entities.DeviceManager
import alektas.sensor.ui.scan.ScanViewModel
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
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import kotlinx.android.synthetic.main.activity_main.*
import javax.inject.Inject

private const val REQUEST_ENABLE_BLUETOOTH = 101

class MainActivity : AppCompatActivity() {
    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory
    private val viewModel: ScanViewModel by viewModels { viewModelFactory }
    @Inject
    lateinit var bleAdapter: BluetoothAdapter
    @Inject
    lateinit var deviceManager: DeviceManager
    private lateinit var appBarConfiguration: AppBarConfiguration

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        if (!packageManager.hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            toast(R.string.device_ble_support_error)
            finish()
        }

        App.component.inject(this)

        setupNavigation()
        subscribeOn(viewModel)
    }

    private fun setupNavigation() {
        val navController = findNavController(R.id.content_container).apply {
            addOnDestinationChangedListener { _, dest, _ ->
                if (dest.id == R.id.scanFragment) bluetooth_scan_btn.show()
                else bluetooth_scan_btn.hide()
            }
        }
        appBarConfiguration = AppBarConfiguration(navController.graph)
        setupActionBarWithNavController(navController, appBarConfiguration)
    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.content_container)
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }

    private fun subscribeOn(viewModel: ScanViewModel) {
        viewModel.enableBleEvent.observe(this, Observer { event ->
            event.getValue()?.let {
                toast(it)
                requestBle()
            }
        })

        viewModel.scanStatus.observe(this, Observer {
            bluetooth_scan_btn.setImageResource(it.btnIconRes)
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

}
