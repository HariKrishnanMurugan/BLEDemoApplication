package com.demo.bledemoapplication.repository.network.manager

import com.demo.bledemoapplication.common.BLEApplication
import com.demo.bledemoapplication.repository.network.callback.BLERetrofitCallback
import com.demo.bledemoapplication.repository.network.request.BLEDeviceDataToSaveRequest
import com.demo.bledemoapplication.repository.network.response.CommonApiResponse
import com.google.gson.GsonBuilder
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.POST
import java.util.concurrent.TimeUnit

/**
 * The manager class through which we can access access the web service or cloud service
 */
class BLEApiManager {
    companion object {
        private const val KEY_CONTENT_TYPE = "content-type"
        private const val KEY_ACCEPT = "Accept"
        private const val APPLICATION_JSON = "application/json"
        private const val TIMEOUT: Long = 60
        private const val BASE_URL = ""

        val apiClient: BLEApiInterface by lazy {
            buildRetrofit().create(BLEApiInterface::class.java)
        }

        /**
         * To builds the retrofit client with baseUrl and Client sent
         *
         * @return Retrofit reference retrofit builder
         */
        private fun buildRetrofit(): Retrofit {
            // Provide a http client to send request
            val okHttpClient = OkHttpClient().newBuilder().apply {
                readTimeout(TIMEOUT, TimeUnit.SECONDS)
                writeTimeout(TIMEOUT, TimeUnit.SECONDS)
                addInterceptor { chain ->
                    chain.proceed(getRequest(chain.request()))
                }
                addInterceptor(HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BODY))
            }.build()
            return Retrofit.Builder().apply {
                baseUrl(BASE_URL)
                client(okHttpClient)
                addConverterFactory(GsonConverterFactory.create(GsonBuilder().setLenient().create()))
            }.build()
        }

        /**
         * To builds the Request header
         *
         * @param request The sent request
         * @return The request with header
         */
        private fun getRequest(request: Request): Request = request.newBuilder().apply {
            header(KEY_CONTENT_TYPE, APPLICATION_JSON)
            header(KEY_ACCEPT, APPLICATION_JSON)
        }.build()
    }

    /**
     * To save the ble device's read data
     *
     * @param request  The ble device read data
     * @param callback Communicate back to repository
     */
    fun saveBLEDeviceData(request: BLEDeviceDataToSaveRequest, callback: BLERetrofitCallback<CommonApiResponse>) {
        apiRequest(apiClient.saveData(request), callback)
    }

    /**
     * To check the availability of the network before making the API call and handle no network scenarios
     *
     * @param call     Api call endpoint which defined in Retrofit API interface
     * @param callback Communicate back to the repository
     */
    private fun <T> apiRequest(call: Call<T>, callback: BLERetrofitCallback<T>) {
        if (BLEApplication.isNetworkConnected) {
            call.enqueue(callback)
        } else {
            callback.noNetwork()
        }
    }
}

/**
 * The interface defining the API endpoints
 */
interface BLEApiInterface {
    @POST("")
    fun saveData(@Body bleReadDataRequest: BLEDeviceDataToSaveRequest): Call<CommonApiResponse>
}