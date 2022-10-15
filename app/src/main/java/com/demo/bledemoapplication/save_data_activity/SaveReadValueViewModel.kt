package com.demo.bledemoapplication.save_data_activity

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import com.demo.bledemoapplication.R
import com.demo.bledemoapplication.repository.network.callback.BLEGeneralViewModelCallback
import com.demo.bledemoapplication.repository.network.request.BLEDeviceDataToSaveRequest
import com.demo.bledemoapplication.repository.BLERepository

/**
 * The view model class for save the BLE device's data
 */
class SaveReadValueViewModel(application: Application) : AndroidViewModel(application) {
    var dataToSave: String? = null
    var deviceName: String? = null
    val isLoading = MutableLiveData<Boolean>()
    val apiSuccess = MutableLiveData<Int>()
    val apiFailure = MutableLiveData<Int>()
    val noNetwork = MutableLiveData<Boolean>()

    /**
     * To save the data to Cloud or server
     */
    fun saveData() {
        isLoading.value = true
        BLERepository.saveData(BLEDeviceDataToSaveRequest(bleDeviceData = dataToSave), object : BLEGeneralViewModelCallback {
            override fun <T> onSuccess(data: T) {
                isLoading.value = false
                apiSuccess.value = R.string.successfully_stored_data
            }

            override fun <T> onError(errorMessage: T) {
                isLoading.value = false
                apiFailure.value = R.string.failed_api
            }

            override fun <T> onFailure(failureMessage: T) {
                isLoading.value = false
                apiFailure.value = R.string.failed_api
            }

            override fun onNoNetwork() {
                isLoading.value = false
                noNetwork.value = true
            }
        })
    }
}