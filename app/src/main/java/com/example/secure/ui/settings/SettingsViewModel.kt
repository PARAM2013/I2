package com.example.secure.ui.settings

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.example.secure.auth.PinManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class SettingsViewModel(application: Application) : AndroidViewModel(application) {

    private val appContext = application.applicationContext

    // Fingerprint Enabled State
    private val _isFingerprintEnabled = MutableStateFlow(PinManager.isFingerprintAuthEnabled(appContext))
    val isFingerprintEnabled: StateFlow<Boolean> = _isFingerprintEnabled.asStateFlow()

    fun setFingerprintEnabled(enabled: Boolean) {
        PinManager.setFingerprintAuthEnabled(appContext, enabled)
        _isFingerprintEnabled.value = PinManager.isFingerprintAuthEnabled(appContext) // Re-fetch to ensure consistency
    }

    // Metadata Removal State
    private val _isMetadataRemovalEnabled = MutableStateFlow(PinManager.isMetadataRemovalEnabled(appContext))
    val isMetadataRemovalEnabled: StateFlow<Boolean> = _isMetadataRemovalEnabled.asStateFlow()

    fun setMetadataRemovalEnabled(enabled: Boolean) {
        PinManager.setMetadataRemovalEnabled(appContext, enabled)
        _isMetadataRemovalEnabled.value = PinManager.isMetadataRemovalEnabled(appContext) // Re-fetch
    }
}
