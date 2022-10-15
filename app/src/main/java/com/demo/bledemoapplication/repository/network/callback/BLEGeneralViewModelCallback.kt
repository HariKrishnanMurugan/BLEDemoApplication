package com.demo.bledemoapplication.repository.network.callback

/**
 * This interface used for communication between repository and view model
 */
interface BLEGeneralViewModelCallback {
    /**
     * This method will be invoked when the web service call is success.
     *
     * @param data The data retrieved from the web service
     */
    fun <T> onSuccess(data: T)

    /**
     * This method will be invoked when there is an error thrown by the web service.
     *
     * @param errorMessage The error message
     */
    fun <T> onError(errorMessage: T)

    /**
     * This method will be invoked when there is an failure in web service call.
     *
     * @param failureMessage The failure message
     */
    fun <T> onFailure(failureMessage: T)

    /**
     * This method will be invoked when there is no network while making the web service.
     */
    fun onNoNetwork()
}