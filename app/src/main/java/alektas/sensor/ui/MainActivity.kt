package alektas.sensor.ui

import alektas.sensor.App
import alektas.sensor.R
import alektas.sensor.domain.entities.DeviceManager
import alektas.sensor.ui.scan.ScanViewModel
import android.Manifest
import android.app.Activity
import android.app.Dialog
import android.bluetooth.BluetoothAdapter
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.LocationManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.Settings
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.annotation.StringRes
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat.requestPermissions
import androidx.core.content.ContextCompat
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import kotlinx.android.synthetic.main.activity_main.*
import javax.inject.Inject

private const val REQUEST_ENABLE_BLUETOOTH = 101
private const val REQUEST_ENABLE_LOCATION = 102
private const val REQUEST_PERMISSION_LOCATION = 202

class MainActivity : AppCompatActivity() {
    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory
    private val viewModel: ScanViewModel by viewModels { viewModelFactory }

    @Inject
    lateinit var bleAdapter: BluetoothAdapter

    @Inject
    lateinit var deviceManager: DeviceManager
    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var locationManager: LocationManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        if (!packageManager.hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            toast(R.string.device_ble_support_error)
            finish()
        }

        App.component.inject(this)

        locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
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

        when (requestCode) {
            REQUEST_ENABLE_BLUETOOTH -> {
                if (Activity.RESULT_OK != resultCode) {
                    toast(R.string.ble_enable_request)
                    return
                }
                viewModel.onBleScanIntent()
            }
        }
    }

    fun onBleScanClick(view: View) {
        if (view.id != R.id.bluetooth_scan_btn) return

        if (!isHasLocationPermission()) {
            requestLocationPermission()
            return
        }

        if (!isHasLocationAccess()) {
            showLocationAccessDialog()
            return
        }

        viewModel.onBleScanIntent()
    }

    private fun isHasLocationPermission(): Boolean =
        ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) ==
                PackageManager.PERMISSION_GRANTED

    private fun requestLocationPermission() {
        requestPermissions(
            this,
            arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
            REQUEST_PERMISSION_LOCATION
        )
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        if (REQUEST_PERMISSION_LOCATION != requestCode) {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults)
            return
        }

        if (grantResults.isEmpty() || grantResults[0] != PackageManager.PERMISSION_GRANTED) {
            toast(R.string.location_permission_deny_error)
            return
        }

        viewModel.onBleScanIntent()
    }

    private fun isHasLocationAccess(): Boolean =
        locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)

    private fun showLocationAccessDialog() {
        LocationDialog { if (it) requestLocation() else toast(R.string.location_enable_request_msg) }
            .show(supportFragmentManager, "Location Dialog")
    }

    class LocationDialog(private val listener: (Boolean) -> Unit) : DialogFragment() {
        override fun onCreateDialog(savedInstanceState: Bundle?): Dialog =
            AlertDialog.Builder(requireContext())
                .setTitle(R.string.location_enable_request_title)
                .setMessage(R.string.location_enable_request_msg)
                .setPositiveButton(R.string.location_enable_request_btn_positive) { _, _ ->
                    listener(true)
                }
                .setNegativeButton(android.R.string.cancel) { _, _ ->
                    listener(false)
                }
                .create()
    }

    private fun requestLocation() {
        val locationEnableIntent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
        startActivityForResult(locationEnableIntent, REQUEST_ENABLE_LOCATION)
    }

    private fun toast(@StringRes msg: Int) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
    }

}
