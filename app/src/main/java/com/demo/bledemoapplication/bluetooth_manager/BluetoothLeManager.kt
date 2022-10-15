package com.demo.bledemoapplication.bluetooth_manager

import android.annotation.SuppressLint
import android.bluetooth.*
import android.bluetooth.le.BluetoothLeScanner
import android.bluetooth.le.ScanCallback
import android.content.Context
import android.os.Handler
import android.os.Looper
import android.util.Log
import com.demo.bledemoapplication.common.BLEConstants
import com.demo.bledemoapplication.common.interfaces.BLEScanStatusCallback
import com.demo.bledemoapplication.common.interfaces.DiscoverBLEDeviceValueCallback
import com.demo.bledemoapplication.common.util.*
import java.util.*

/**
 * The service class for bluetooth
 */
@SuppressLint("MissingPermission")
object BluetoothLeManager {
    private val TAG = this::class.java.simpleName
    private var isScanning = false
    private var bluetoothAdapter: BluetoothAdapter? = null
    private var bluetoothGatt: BluetoothGatt? = null
    private var showBLEDeviceReadValue: DiscoverBLEDeviceValueCallback? = null

    private val gattCallback: BluetoothGattCallback = object : BluetoothGattCallback() {
        override fun onConnectionStateChange(gatt: BluetoothGatt, status: Int, newState: Int) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                bluetoothGatt = gatt
                when (newState) {
                    BluetoothProfile.STATE_CONNECTED -> {
                        Log.d(TAG, "BluetoothGatt Successfully Connected")
                        // successfully connected to the GATT Server
                        gatt.requestMtu(517)
                        // discover the services
                        gatt.discoverServices()
                    }
                    BluetoothProfile.STATE_DISCONNECTED -> {
                        Log.d(TAG, "BluetoothGatt  DisConnected")
                        // disconnected from the GATT Server
                        disconnectService()
                        showBLEDeviceReadValue?.disconnected()
                    }
                    else -> {
                        Log.d(TAG, "Gatt Connect Failed - $status")
                        disconnectService()
                        showBLEDeviceReadValue?.discoveredFailed()
                    }
                }
            } else {
                Log.d(TAG, " Gatt Connect Failed - $status")
                disconnectService()
                showBLEDeviceReadValue?.discoveredFailed()
            }
        }

        override fun onServicesDiscovered(gatt: BluetoothGatt, status: Int) {
            val value = gatt.printGattTable()
            value.first?.forEach { service ->
                service.characteristics.forEach { char ->
                    readCharacteristic(service.uuid, char.uuid)
                }
            } ?: run {
                Log.d(TAG, "No Value Discovered")
            }
            if (status == BluetoothGatt.GATT_SUCCESS) {
                Log.d(TAG, "Service Discovered Successfully")
            } else {
                Log.w(TAG, "Service Discovered Failed: $status")
                disconnectService()
                showBLEDeviceReadValue?.discoveredFailed()
            }
        }

        override fun onCharacteristicRead(gatt: BluetoothGatt, characteristic: BluetoothGattCharacteristic, status: Int) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                Log.d(TAG, "Read Characteristic Gatt success")
            }

            with(characteristic) {
                when (status) {
                    BluetoothGatt.GATT_SUCCESS -> {
                        val toHexString = this.value.toHexString()
                        val bytes = toHexString.toByteArray()
                        val numericalValue =
                            ((bytes[3].toInt()) and 0xFF shl 24) + ((bytes[2].toInt()) and 0xFF shl 16) + ((bytes[1].toInt()) and 0xFF shl 8) + ((bytes[0].toInt()) and 0xFF)
                        // The device value
                        showBLEDeviceReadValue?.discoveredSuccess(numericalValue.toString())
                    }
                    BluetoothGatt.GATT_READ_NOT_PERMITTED -> {
                        showBLEDeviceReadValue?.discoveredFailed()
                        Log.d(TAG, "Characteristic Read not permitted")
                    }
                    else -> {
                        showBLEDeviceReadValue?.discoveredFailed()
                        Log.e(TAG, "Characteristic Read Failed - $status")
                    }
                }
            }
        }

        override fun onCharacteristicWrite(gatt: BluetoothGatt, characteristic: BluetoothGattCharacteristic, status: Int) {
            when (status) {
                BluetoothGatt.GATT_SUCCESS -> {
                    // Handled when write characteristic gatt success
                }
                BluetoothGatt.GATT_INVALID_ATTRIBUTE_LENGTH -> {
                    // Handled when write characteristic gatt length is invalid
                }
                BluetoothGatt.GATT_WRITE_NOT_PERMITTED -> {
                    // Handled when write characteristic gatt not permitted
                }
                else -> {
                    // Handled when write characteristic gatt failed or other status
                }
            }
        }
    }

    /**
     * To start the bluetoothScan
     *
     * @param bleScanCallback    The scan callback
     * @param bluetoothLeScanner The bluetooth Scanner
     * @param scanStatusCallback The callback to communicate back to activity about the scan status
     */
    fun startBluetoothScan(bleScanCallback: ScanCallback, bluetoothLeScanner: BluetoothLeScanner, scanStatusCallback: BLEScanStatusCallback) {
        Log.d(TAG, "Bluetooth Scan Started")
        if (!isScanning) {
            // Stops scanning after 10 seconds `
            Handler(Looper.getMainLooper()).postDelayed({ stopBluetoothScan(bleScanCallback, bluetoothLeScanner, scanStatusCallback) }, BLEConstants.SCAN_PERIOD)
            isScanning = true
            bluetoothLeScanner.startScan(bleScanCallback)
            scanStatusCallback.scanStarted()
        } else {
            stopBluetoothScan(bleScanCallback, bluetoothLeScanner, scanStatusCallback)
        }
    }

    /**
     * To stop the bluetooth scan
     *
     * @param bleScanCallback    The scan callback
     * @param bluetoothLeScanner The bluetooth Scanner
     * @param scanStatusCallback The callback to communicate back to activity about the scan status
     */
    fun stopBluetoothScan(bleScanCallback: ScanCallback, bluetoothLeScanner: BluetoothLeScanner, scanStatusCallback: BLEScanStatusCallback) {
        Log.d(TAG, "Bluetooth Scan Stopped")
        isScanning = false
        bluetoothLeScanner.stopScan(bleScanCallback)
        scanStatusCallback.scanStopped()
    }

    /**
     * To read the characteristics of the gatt data
     *
     * @param serviceUUID The service UUID
     * @param charUUID    The char UUID
     */
    private fun readCharacteristic(serviceUUID: UUID, charUUID: UUID) {
        val batteryLevelChar = bluetoothGatt?.getService(serviceUUID)?.getCharacteristic(charUUID)
        if (batteryLevelChar?.isReadable() == true) {
            bluetoothGatt?.readCharacteristic(batteryLevelChar)
        }
        if (batteryLevelChar?.isWritable() == true) {
            writeCharacteristic(batteryLevelChar)
        }
    }

    /**
     * To write the characteristics of the gatt data
     *
     * @param characteristic The Bluetooth gatt characteristic
     */
    private fun writeCharacteristic(characteristic: BluetoothGattCharacteristic) {
        val value = ByteArray(1)
        value[0] = (21 and 0xFF).toByte()
        val writeType = when {
            characteristic.isWritable() -> {
                BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT
            }
            characteristic.isWritableWithoutResponse() -> {
                BluetoothGattCharacteristic.WRITE_TYPE_NO_RESPONSE
            }
            else -> {
                Log.d(TAG, "writeCharacteristic Failed")
            }
        }

        bluetoothGatt?.let { gatt ->
            characteristic.writeType = writeType
            characteristic.value = value
            gatt.writeCharacteristic(characteristic)
        }
    }

    /**
     * To connect the service with device based upon the device address
     *
     * @param context  The activity context
     * @param address  The device address
     * @param callback The callback for communication
     *
     * @return Whether the ble device is successfully connected or not
     */
    fun connectService(context: Context, address: String?, callback: DiscoverBLEDeviceValueCallback): Boolean {
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
        showBLEDeviceReadValue = callback
        bluetoothAdapter?.let { adapter ->
            try {
                val device = adapter.getRemoteDevice(address)
                // Connect to the GATT server on the device
                bluetoothGatt = device.connectGatt(context, false, gattCallback)
            } catch (e: Exception) {
                Log.e(TAG, "connectService: Caught Exception: ${e.message}")
            }
            return true
        } ?: run {
            return false
        }
    }

    /**
     * To close the service
     */
    fun disconnectService() {
        bluetoothGatt?.let { gatt ->
            gatt.close()
            bluetoothGatt = null
        }
    }
}