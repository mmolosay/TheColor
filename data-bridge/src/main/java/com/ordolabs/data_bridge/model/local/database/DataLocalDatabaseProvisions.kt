package com.ordolabs.data_bridge.model.local.database

import com.ordolabs.data_local.TheColorDatabase
import com.ordolabs.data_local.dao.ColorsHistoryDao

interface DataLocalDatabaseProvisions {

    val database: TheColorDatabase

    // region DAOs

    val colorsHistoryDao: ColorsHistoryDao

    // endregion
}