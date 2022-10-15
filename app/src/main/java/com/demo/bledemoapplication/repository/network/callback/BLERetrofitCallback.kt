package com.demo.bledemoapplication.repository.network.callback

import com.demo.bledemoapplication.R
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.net.UnknownHostException

/**
 * This interface used for that extends Retrofit callback to perform the success or failure actions
 *
 * @param viewModelCallback The callback to communicate back to the repository
 */
abstract class BLERetrofitCallback<T>(private val viewModelCallback: BLEGeneralViewModelCallback) : Callback<T> {
    override fun onResponse(call: Call<T>, response: Response<T>) {
        if (response.isSuccessful) {
            onResponse(response)
        } else {
            viewModelCallback.onError(response)
        }
    }

    override fun onFailure(call: Call<T>, throwable: Throwable) {
        viewModelCallback.onFailure(if (throwable is UnknownHostException) R.string.unable_to_reach_server else null)
    }

    /**
     * Call when response is successful
     *
     * @param response The response of web request
     */
    abstract fun <T> onResponse(response: Response<T>)

    fun noNetwork() {
        viewModelCallback.onNoNetwork()
    }
}