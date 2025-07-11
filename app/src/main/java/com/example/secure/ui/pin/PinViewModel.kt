package com.example.secure.ui.pin

import android.app.Application
import android.content.Context
import androidx.biometric.BiometricManager
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.secure.auth.PinManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class PinScreenMode {
    SETUP_NEW_PIN, // Initial PIN setup
    CONFIRM_NEW_PIN, // Confirming the new PIN during setup
    ENTER_PIN // Entering PIN to unlock
}

enum class BiometricAuthStatus {
    NOT_AVAILABLE, // Hardware not present or no enrolled biometrics
    AVAILABLE_NOT_ENABLED, // Available, but user hasn't enabled it in app settings
    ENABLED_AVAILABLE, // Available and enabled by user
    FAILED,
    SUCCESS,
    ERROR
}

data class PinScreenUiState(
    val enteredPin: String = "",
    val screenMode: PinScreenMode = PinScreenMode.ENTER_PIN,
    val promptText: String = "",
    val errorText: String? = null,
    val firstPinEntered: String? = null, // Used in SETUP_NEW_PIN mode
    val isFingerprintVisible: Boolean = false,
    val biometricAuthStatus: BiometricAuthStatus = BiometricAuthStatus.NOT_AVAILABLE,
    val navigateToMain: Boolean = false,
    val requestBiometricPrompt: Boolean = false // To signal UI to show biometric prompt
)

class PinViewModel(application: Application) : AndroidViewModel(application) {

    private val _uiState = MutableStateFlow(PinScreenUiState())
    val uiState: StateFlow<PinScreenUiState> = _uiState.asStateFlow()

    private val pinManager = PinManager

    init {
        initializeScreenState()
    }

    private fun initializeScreenState() {
        viewModelScope.launch {
            val context = getApplication<Application>().applicationContext
            val isPinSet = pinManager.isPinSet(context)
            val mode = if (isPinSet) PinScreenMode.ENTER_PIN else PinScreenMode.SETUP_NEW_PIN
            _uiState.update { currentState ->
                currentState.copy(
                    screenMode = mode,
                    promptText = getPromptForMode(mode),
                    isFingerprintVisible = determineFingerprintVisibility(context, mode)
                )
            }
            checkBiometricStatus(context)
        }
    }

    private fun getPromptForMode(mode: PinScreenMode): String {
        // This should ideally use string resources
        return when (mode) {
            PinScreenMode.SETUP_NEW_PIN -> "Create a 4-digit PIN"
            PinScreenMode.CONFIRM_NEW_PIN -> "Confirm your 4-digit PIN"
            PinScreenMode.ENTER_PIN -> "Enter your PIN"
        }
    }

    private fun determineFingerprintVisibility(context: Context, mode: PinScreenMode): Boolean {
        if (mode == PinScreenMode.ENTER_PIN) {
            val biometricManager = BiometricManager.from(context)
            val canAuthenticate = biometricManager.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_STRONG or BiometricManager.Authenticators.BIOMETRIC_WEAK)
            return pinManager.isFingerprintAuthEnabled(context) && (canAuthenticate == BiometricManager.BIOMETRIC_SUCCESS)
        }
        return false
    }

    private fun checkBiometricStatus(context: Context) {
        val biometricManager = BiometricManager.from(context)
        when (biometricManager.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_STRONG or BiometricManager.Authenticators.BIOMETRIC_WEAK)) {
            BiometricManager.BIOMETRIC_SUCCESS -> {
                if (pinManager.isPinSet(context) && pinManager.isFingerprintAuthEnabled(context)) {
                    _uiState.update { it.copy(biometricAuthStatus = BiometricAuthStatus.ENABLED_AVAILABLE, isFingerprintVisible = true) }
                } else {
                     _uiState.update { it.copy(biometricAuthStatus = BiometricAuthStatus.AVAILABLE_NOT_ENABLED, isFingerprintVisible = false) }
                }
            }
            BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE,
            BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE,
            BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED -> {
                _uiState.update { it.copy(biometricAuthStatus = BiometricAuthStatus.NOT_AVAILABLE, isFingerprintVisible = false) }
            }
            else ->  _uiState.update { it.copy(biometricAuthStatus = BiometricAuthStatus.NOT_AVAILABLE, isFingerprintVisible = false) }
        }
    }


    fun onDigitEntered(digit: String) {
        if (_uiState.value.enteredPin.length < 4) {
            _uiState.update { currentState ->
                currentState.copy(
                    enteredPin = currentState.enteredPin + digit,
                    errorText = null // Clear error on new input
                )
            }
            if (_uiState.value.enteredPin.length == 4) {
                processPinEntry()
            }
        }
    }

    fun onBackspace() {
        if (_uiState.value.enteredPin.isNotEmpty()) {
            _uiState.update { currentState ->
                currentState.copy(
                    enteredPin = currentState.enteredPin.dropLast(1),
                    errorText = null // Clear error
                )
            }
        }
    }

    private fun processPinEntry() {
        val currentPin = _uiState.value.enteredPin
        when (_uiState.value.screenMode) {
            PinScreenMode.SETUP_NEW_PIN -> {
                _uiState.update { currentState ->
                    currentState.copy(
                        screenMode = PinScreenMode.CONFIRM_NEW_PIN,
                        firstPinEntered = currentPin,
                        enteredPin = "",
                        promptText = getPromptForMode(PinScreenMode.CONFIRM_NEW_PIN)
                    )
                }
            }
            PinScreenMode.CONFIRM_NEW_PIN -> {
                if (currentPin == _uiState.value.firstPinEntered) {
                    pinManager.savePin(getApplication(), currentPin)
                    // Optionally enable fingerprint by default after setting a new PIN
                    // pinManager.setFingerprintAuthEnabled(getApplication(), true)
                    _uiState.update { it.copy(navigateToMain = true, errorText = null) }
                } else {
                    _uiState.update { currentState ->
                        currentState.copy(
                            errorText = "PINs do not match. Try again.", // Use string resource
                            enteredPin = "",
                            firstPinEntered = null,
                            screenMode = PinScreenMode.SETUP_NEW_PIN,
                            promptText = getPromptForMode(PinScreenMode.SETUP_NEW_PIN)
                        )
                    }
                }
            }
            PinScreenMode.ENTER_PIN -> {
                if (pinManager.verifyPin(getApplication(), currentPin)) {
                    _uiState.update { it.copy(navigateToMain = true, errorText = null) }
                } else {
                    _uiState.update { currentState ->
                        currentState.copy(
                            errorText = "Incorrect PIN. Try again.", // Use string resource
                            enteredPin = ""
                        )
                    }
                }
            }
        }
    }

    fun onBiometricAuthenticationSucceeded() {
        _uiState.update { it.copy(navigateToMain = true, biometricAuthStatus = BiometricAuthStatus.SUCCESS) }
    }

    fun onBiometricAuthenticationFailed(errorMsg: String? = null) {
         _uiState.update { it.copy(
             errorText = errorMsg ?: "Biometric authentication failed.", // Use string resource
             biometricAuthStatus = BiometricAuthStatus.FAILED
         ) }
    }

    fun onBiometricAuthenticationError(errorCode: Int, errString: CharSequence) {
        _uiState.update { it.copy(
            errorText = "Biometric error: $errString ($errorCode)", // Use string resource
            biometricAuthStatus = BiometricAuthStatus.ERROR)
        }
    }

    fun requestBiometricPrompt() {
        if (_uiState.value.isFingerprintVisible && _uiState.value.biometricAuthStatus == BiometricAuthStatus.ENABLED_AVAILABLE) {
            _uiState.update { it.copy(requestBiometricPrompt = true) }
        }
    }

    fun biometricPromptShown() {
        _uiState.update { it.copy(requestBiometricPrompt = false) }
    }

    fun resetNavigation() {
        _uiState.update { it.copy(navigateToMain = false) }
    }
}
