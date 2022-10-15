package com.demo.bledemoapplication.repository

import com.demo.bledemoapplication.common.BLEApplication
import com.demo.bledemoapplication.repository.database.BLEDataToSaveInDB
import com.demo.bledemoapplication.repository.database.BLEDatabase
import com.demo.bledemoapplication.repository.network.callback.BLEGeneralViewModelCallback
import com.demo.bledemoapplication.repository.network.callback.BLERetrofitCallback
import com.demo.bledemoapplication.repository.network.request.BLEDeviceDataToSaveRequest
import com.demo.bledemoapplication.repository.network.response.CommonApiResponse
import retrofit2.Response

/**
 * The repository class to handle the Api operations
 */
object BLERepository {
    private val apiManager by lazy { BLEApplication.getInstance().getApiManager() }

    /**
     * To save the BLE device's read data
     *
     * @param bleDataToSaveRequest The request body of the BLE device data
     * @param viewModelCallback    The callback to communicate back to the view model
     */
    fun saveData(bleDataToSaveRequest: BLEDeviceDataToSaveRequest, viewModelCallback: BLEGeneralViewModelCallback) {
        // Store the read value in the preference
        BLEApplication.getInstance().getPrefs().readData = bleDataToSaveRequest.bleDeviceData
        apiManager.saveBLEDeviceData(bleDataToSaveRequest, object : BLERetrofitCallback<CommonApiResponse>(viewModelCallback) {
            override fun <T> onResponse(response: Response<T>) {
                Thread {
                    BLEDatabase.getInstance().bleDao().saveData(BLEDataToSaveInDB(data = bleDataToSaveRequest.bleDeviceData))
                }.start()
                viewModelCallback.onSuccess("Success")
            }
        })
    }
}