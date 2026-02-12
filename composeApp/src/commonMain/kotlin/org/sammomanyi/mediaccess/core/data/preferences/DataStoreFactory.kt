package org.sammomanyi.mediaccess.core.data.preferences

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences

expect fun createDataStore(): DataStore<Preferences>

const val DATASTORE_FILE_NAME = "mediaccess_prefs.preferences_pb"