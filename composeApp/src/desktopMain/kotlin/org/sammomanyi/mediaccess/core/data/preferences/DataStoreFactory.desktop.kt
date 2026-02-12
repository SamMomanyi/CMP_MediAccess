package org.sammomanyi.mediaccess.core.data.preferences

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import okio.Path.Companion.toPath

actual fun createDataStore(): DataStore<Preferences> {
    return PreferenceDataStoreFactory.createWithPath(
        produceFile = {
            val path = System.getProperty("user.home") + "/.mediaccess/$DATASTORE_FILE_NAME"
            path.toPath()
        }
    )
}