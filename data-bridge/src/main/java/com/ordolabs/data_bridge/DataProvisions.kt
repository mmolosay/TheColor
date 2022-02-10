package com.ordolabs.data_bridge

import com.ordolabs.data_bridge.model.local.database.DataLocalDatabaseProvisions
import com.ordolabs.data_bridge.model.local.preferences.DataLocalPreferencesProvisions
import com.ordolabs.data_bridge.model.remote.DataRemoteProvisions
import com.ordolabs.data_bridge.model.repository.DataRepositoryProvisions

interface DataProvisions :
    DataLocalDatabaseProvisions,
    DataLocalPreferencesProvisions,
    DataRemoteProvisions,
    DataRepositoryProvisions