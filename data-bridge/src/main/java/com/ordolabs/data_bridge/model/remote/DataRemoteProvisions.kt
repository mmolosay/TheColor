package com.ordolabs.data_bridge.model.remote

import com.ordolabs.data_remote.api.TheColorApiService
import retrofit2.Retrofit

interface DataRemoteProvisions {

    // region Api services

    val theColorApiService: TheColorApiService

    // endregion

    val retrofit: Retrofit
}