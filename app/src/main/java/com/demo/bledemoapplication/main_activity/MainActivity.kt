package com.demo.bledemoapplication.main_activity

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.content.IntentSender
import android.location.LocationManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.demo.bledemoapplication.R
import com.demo.bledemoapplication.adapters.BLEDeviceListAdapter
import com.demo.bledemoapplication.bluetooth_manager.BluetoothLeManager
import com.demo.bledemoapplication.common.interfaces.BLEDeviceConnectCallback
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.LocationSettingsRequest
import com.google.android.gms.location.LocationSettingsStatusCodes
import com.google.android.gms.location.Priority.PRIORITY_HIGH_ACCURACY
import permissions.dispatcher.*
import permissions.dispatcher.ktx.LocationPermission
import permissions.dispatcher.ktx.constructLocationPermissionRequest
import permissions.dispatcher.ktx.constructPermissionsRequest
import com.demo.bledemoapplication.common.BLEApplication
import com.demo.bledemoapplication.common.BLEConstants
import com.demo.bledemoapplication.common.BLEDeviceListData
import com.demo.bledemoapplication.common.BLELocationDisabledEvent
import com.demo.bledemoapplication.common.interfaces.BLEScanStatusCallback
import com.demo.bledemoapplication.common.interfaces.DiscoverBLEDeviceValueCallback
import com.demo.bledemoapplication.databinding.ActivityDeviceListBinding
import com.demo.bledemoapplication.save_data_activity.SaveReadValueActivity
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

/**
 * The main activity for the fetch nearby BLE device list
 */
@SuppressLint("MissingPermission")
class MainActivity : AppCompatActivity(), BLEDeviceConnectCallback, DiscoverBLEDeviceValueCallback, BLEScanStatusCallback {
    private lateinit var mainActivityBinding: ActivityDeviceListBinding
    private lateinit var bluetoothAdapter: BluetoothAdapter
    private lateinit var bleDeviceListAdapter: BLEDeviceListAdapter
    private var selectedDeviceName: String? = null
    private val mainScreenViewModel: MainViewModel by lazy {
        ViewModelProvider(this)[MainViewModel::class.java]
    }
    private val bluetoothLeScanner by lazy { bluetoothAdapter.bluetoothLeScanner }

    companion object {
        private val TAG = this::class.java.simpleName
    }

    override fun onStart() {
        super.onStart()
        EventBus.getDefault().register(this)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mainActivityBinding = ActivityDeviceListBinding.inflate(layoutInflater).apply {
            setContentView(this.root)
        }
        initializeAdapter()
        setListeners()
    }

    override fun onResume() {
        super.onResume()
        enableBluetooth()
    }

    override fun onStop() {
        super.onStop()
        EventBus.getDefault().unregister(this)
    }

    override fun onDestroy() {
        super.onDestroy()
        // Stop the bluetooth Scan and its service
        with(BluetoothLeManager) {
            disconnectService()
            stopBluetoothScan(bleScanCallback, bluetoothLeScanner, this@MainActivity)
        }
    }

    override fun onDeviceClicked(deviceData: BLEDeviceListData) {
        val deviceName = deviceData.deviceName
        if (!deviceName.equals("Unnamed Device")) {
            selectedDeviceName = deviceData.deviceName
            // Connect the bluetooth service
            val isConnected = BluetoothLeManager.connectService(this, deviceData.deviceAddress, this)
            if (isConnected) showToast(R.string.device_trying_to_connect)
            else showToast(R.string.try_again)
        } else showToast(R.string.device_not_supported)
    }

    override fun discoveredSuccess(data: String?) {
        Handler(Looper.getMainLooper()).post {
            if (data.isNullOrBlank()) {
                updateUIForFailedCase()
            } else {
                val intent = Intent(this, SaveReadValueActivity::class.java).apply {
                    putExtra(BLEConstants.BUNDLE_KEY_DATA_TO_SAVE, data)
                    putExtra(BLEConstants.BUNDLE_KEY_DEVICE_NAME, selectedDeviceName)
                }
                startActivity(intent)
            }
        }
    }

    override fun discoveredFailed() {
        showToast(R.string.connection_failed)
    }

    override fun disconnected() {
        showToast(R.string.disconnected)
    }

    override fun scanStarted() {
        with(mainActivityBinding) {
            pbProgress.visible()
            rvDeviceList.gone()
            tvNoResult.gone()
            tvReadyToScan.gone()
        }
        bleDeviceListAdapter.clearDeviceData()
        showToast(R.string.scanning)
    }

    override fun scanStopped() {
        mainActivityBinding.pbProgress.gone()
        with(mainScreenViewModel) {
            // Remove duplicates based upon the device address
            distinctDeviceData = deviceData.distinctBy { it.deviceAddress } as MutableList<BLEDeviceListData>
            if (distinctDeviceData != null) {
                updateUIForSuccessCase()
                bleDeviceListAdapter.addDevice(distinctDeviceData!!)
            } else {
                updateUIForFailedCase()
            }
        }
    }


    /**
     * To get the results from nearby devices via bluetooth scanning
     */
    private val bleScanCallback = object : ScanCallback() {
        override fun onScanResult(callbackType: Int, result: ScanResult?) {
            with(mainActivityBinding) {
                val dataList = mutableListOf<BLEDeviceListData>()
                result?.device?.let {
                    with(mainScreenViewModel) {
                        dataList.add(BLEDeviceListData(deviceResult = it, deviceName = it.name ?: "Unnamed Device", deviceAddress = it.address))
                        deviceData.addAll(dataList)
                    }
                } ?: run {
                    updateUIForFailedCase()
                }
            }
        }

        override fun onScanFailed(errorCode: Int) {
            updateUIForFailedCase()
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onLocationDisabledEvent(locationDisabledEvent: BLELocationDisabledEvent) {
        checkLocationSettings()
    }

    /**
     * The activity result for the bluetooth permission
     */
    private val bluetoothPermissionResult = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result != null && result.resultCode == RESULT_OK) {
            checkNeededPermissionAndAsk()
        } else {
            showToast(R.string.try_again)
            enableBluetooth()
        }
    }

    /**
     * The activity permission result for location
     */
    private val settingsPermissionResult = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result != null) {
            checkNeededPermissionAndAsk()
        }
    }

    /**
     * The location settings result
     */
    private val locationSettingsLauncher = registerForActivityResult(ActivityResultContracts.StartIntentSenderForResult()) { _result ->
        when (_result.resultCode) {
            Activity.RESULT_OK -> showToast(R.string.enabled_gps)
            Activity.RESULT_CANCELED -> checkLocationSettings()
        }
    }

    /**
     * To initialize the bluetooth adapter
     */
    private fun initializeAdapter() {
        bleDeviceListAdapter = BLEDeviceListAdapter(null, this)
        mainActivityBinding.rvDeviceList.apply {
            layoutManager = LinearLayoutManager(this@MainActivity)
            adapter = bleDeviceListAdapter
        }
        val bluetoothManager = getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        bluetoothAdapter = bluetoothManager.adapter
    }

    /**
     * To set the listeners
     */
    private fun setListeners() {
        mainActivityBinding.btnStartScan.setOnClickListener {
            BluetoothLeManager.startBluetoothScan(bleScanCallback, bluetoothLeScanner, this)
        }
    }

    /**
     * To enable the bluetooth device
     */
    private fun enableBluetooth() {
        if (!bluetoothAdapter.isEnabled) {
            if (checkSDKVersionAbove31()) checkNeededPermissionAndAsk()
            else {
                val intent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
                bluetoothPermissionResult.launch(intent)
            }
        } else {
            checkNeededPermissionAndAsk()
        }
    }

    /**
     * To ask the bluetooth permission
     */
    @SuppressLint("InlinedApi")
    private fun askBluetoothPermissions() {
        val constructBluetoothPermissionResult = constructPermissionsRequest(
            permissions = arrayOf(Manifest.permission.BLUETOOTH_CONNECT, Manifest.permission.BLUETOOTH_SCAN),
            requiresPermission = ::askLocationPermissions,
            onNeverAskAgain = { goToAppPermissionSettings(R.string.need_bluetooth_permission) },
            onShowRationale = ::showRationaleDialog,
        )
        constructBluetoothPermissionResult.launch()
    }

    /**
     * To ask the location permissions
     */
    private fun askLocationPermissions() {
        val constructLocationPermissionResult = constructLocationPermissionRequest(
            permissions = arrayOf(LocationPermission.COARSE, LocationPermission.FINE),
            requiresPermission = ::checkLocationSettings,
            onNeverAskAgain = { goToAppPermissionSettings(R.string.need_location_permission) },
            onShowRationale = ::showRationaleDialog,
        )
        constructLocationPermissionResult.launch()
    }

    /**
     * To check if the device's location settings are adequate for the app's needs using
     */
    private fun checkLocationSettings() {
        val locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager?
        val isGpsProviderEnabled = locationManager?.isProviderEnabled(LocationManager.GPS_PROVIDER) ?: false
        if (!isGpsProviderEnabled) {
            if (BLEApplication.isGpsEnabled) {
                // No need to handle
            } else {
                BLEApplication.isGpsEnabled = true
                val locationRequest = LocationRequest.create().apply {
                    interval = BLEConstants.ONE_SECOND_IN_MILLISECONDS
                    priority = PRIORITY_HIGH_ACCURACY
                }
                val builder = LocationSettingsRequest.Builder().apply {
                    addLocationRequest(locationRequest)
                    setAlwaysShow(true)
                }
                val locationSettingsRequest = builder.build()
                val result = LocationServices.getSettingsClient(this).checkLocationSettings(locationSettingsRequest)
                result.addOnCompleteListener { task ->
                    try {
                        //If this line is successfully executed( without exception) all location settings are satisfied.
                        if (task.isComplete) {
                            task.getResult(ApiException::class.java)
                            with(mainActivityBinding) {
                                rvDeviceList.gone()
                                tvNoResult.gone()
                                tvReadyToScan.visible()
                            }
                            Log.d(TAG, "All location settings are satisfied.")
                        }
                    } catch (exception: ApiException) {
                        when (exception.statusCode) {
                            LocationSettingsStatusCodes.RESOLUTION_REQUIRED -> try {
                                //Open the settings page to change the settings.
                                val resolvable = exception as ResolvableApiException
                                val intentSenderRequest = IntentSenderRequest.Builder(resolvable.resolution).build()
                                locationSettingsLauncher.launch(intentSenderRequest)
                            } catch (e: IntentSender.SendIntentException) {
                                val sendIntentExceptionMessage = "checkLocationSettings: SendIntentException: ${e.message}"
                                Log.e(TAG, sendIntentExceptionMessage, e)
                            } catch (e: ClassCastException) {
                                val classCastExceptionMessage = "checkLocationSettings: ClassCastException: ${e.message}"
                                Log.e(TAG, classCastExceptionMessage, e)
                            }
                            LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE -> {
                                // Settings page not available
                                val apiExceptionMessage = "checkLocationSettings: ApiException: ${exception.message}"
                                Log.e(TAG, apiExceptionMessage, exception)
                            }
                        }
                    }
                }
            }
        }
    }

    /**
     * To show the rationale dialog for location permission
     *
     * @param request The permission request
     */
    private fun showRationaleDialog(request: PermissionRequest) {
        AlertDialog.Builder(this).setPositiveButton(R.string.allow) { _, _ -> request.proceed() }.setCancelable(false)
            .setMessage(R.string.app_permission_rationale).show()
    }

    /**
     * To show the toast
     *
     * @param messageResId The toast message id
     */
    private fun showToast(messageResId: Int) {
        Toast.makeText(this, getString(messageResId), Toast.LENGTH_SHORT).show()
    }

    /**
     * To update UI for success result from bluetooth scanning
     */
    private fun updateUIForSuccessCase() {
        with(mainActivityBinding) {
            rvDeviceList.visible()
            tvNoResult.gone()
            tvReadyToScan.gone()
        }
    }

    /**
     * To update UI for no result from bluetooth scanning
     */
    private fun updateUIForFailedCase() {
        with(mainActivityBinding) {
            rvDeviceList.gone()
            tvReadyToScan.gone()
            tvNoResult.visible()
        }
    }

    /**
     * To set the view visibility as [View.VISIBLE]
     */
    private fun View.visible() {
        this.visibility = View.VISIBLE
    }

    /**
     * To set the view visibility as [View.GONE]
     */
    private fun View.gone() {
        this.visibility = View.GONE
    }

    /**
     * To go to app permission settings page
     */
    private fun goToAppPermissionSettings(toastMessageId: Int) {
        try {
            showToast(toastMessageId)
            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
            val uri = Uri.fromParts("package", this.packageName, null)
            intent.data = uri
            settingsPermissionResult.launch(intent)
        } catch (e: ActivityNotFoundException) {
            Log.e(TAG, "goToAppPermissionSettings: Caught exception: ${e.message}")
        } catch (e: Exception) {
            Log.e(TAG, "goToAppPermissionSettings: Caught exception: ${e.message}")
        }
    }

    /**
     * To check and ask the needed permission
     */
    private fun checkNeededPermissionAndAsk() {
        if (checkSDKVersionAbove31()) {
            if (haveBluetoothScanPermissionGranted()) askLocationPermissions()
            else askBluetoothPermissions()
        } else askLocationPermissions()
    }

    /**
     * To check the SDK version above android 12
     *
     * @return Whether the device sdk is 31 and above
     */
    private fun checkSDKVersionAbove31(): Boolean {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.S
    }

    /**
     * The necessary bluetooth permissions are granted or not
     *
     * @return Whether the bluetooth permissions are granted or not
     */
    private fun haveBluetoothScanPermissionGranted(): Boolean {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && PermissionUtils.hasSelfPermissions(this,
            Manifest.permission.BLUETOOTH_SCAN) && PermissionUtils.hasSelfPermissions(this, Manifest.permission.BLUETOOTH_CONNECT)
    }
}