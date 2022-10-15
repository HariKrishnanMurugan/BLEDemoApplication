package com.demo.bledemoapplication.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.demo.bledemoapplication.databinding.LayoutBleDeviceListBinding
import com.demo.bledemoapplication.common.BLEDeviceListData
import com.demo.bledemoapplication.common.interfaces.BLEDeviceConnectCallback

/**
 * The Nearby ble device list adapter
 */
class BLEDeviceListAdapter(
    private var deviceListData: MutableList<BLEDeviceListData>?,
    private val deviceConnectCallback: BLEDeviceConnectCallback,
) : RecyclerView.Adapter<BLEDeviceListAdapter.BLEDeviceListViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BLEDeviceListViewHolder {
        return BLEDeviceListViewHolder(LayoutBleDeviceListBinding.inflate(LayoutInflater.from(parent.context), parent, false))
    }

    override fun onBindViewHolder(holder: BLEDeviceListViewHolder, position: Int) {
        holder.bind(deviceListData?.get(holder.adapterPosition))
    }

    override fun getItemCount(): Int {
        return deviceListData?.size ?: 0
    }

    /**
     * To add the newly scanned device from bluetooth scanner
     *
     * @param newDeviceData The new ble device data
     */
    fun addDevice(newDeviceData: MutableList<BLEDeviceListData>) {
        val size = deviceListData?.size ?: 0
        if (size == 0) deviceListData = newDeviceData
        else deviceListData?.addAll(newDeviceData)
        notifyDataSetChanged()
    }

    /**
     * To clear the device data
     */
    fun clearDeviceData() {
        deviceListData?.clear()
        notifyDataSetChanged()
    }

    /**
     * The inner class of the ble device list
     */
    inner class BLEDeviceListViewHolder(private val layoutBleDeviceListBinding: LayoutBleDeviceListBinding) :
        RecyclerView.ViewHolder(layoutBleDeviceListBinding.root) {

        /**
         * To bind the device list and it's details in the view
         *
         * @param bleDeviceData The ble device data
         */
        fun bind(bleDeviceData: BLEDeviceListData?) {
            with(layoutBleDeviceListBinding) {
                bleDeviceData?.let {
                    if (!it.deviceName.isNullOrBlank() && !it.deviceAddress.isNullOrBlank()) {
                        tvDeviceName.text = it.deviceName
                        tvDeviceAddress.text = it.deviceAddress
                        root.setOnClickListener {
                            deviceConnectCallback.onDeviceClicked(bleDeviceData)
                        }
                    }
                }
            }
        }
    }
}