package com.example.secure.ui.lockscreen

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import com.example.secure.MainActivity
import com.example.secure.R
import com.example.secure.AppGlobalState
import com.example.secure.TrackedActivity
import com.example.secure.auth.PinManager
import com.example.secure.databinding.ActivityLockScreenBinding
import android.text.Editable
import android.text.TextWatcher
import android.view.inputmethod.InputMethodManager
import com.example.secure.databinding.LayoutPinInputBinding
import java.util.concurrent.Executor

class LockScreenActivity : TrackedActivity() {

    private lateinit var binding: ActivityLockScreenBinding
    private lateinit var pinInputBinding: LayoutPinInputBinding
    private val enteredPin = StringBuilder()
    private lateinit var pinDigitViews: List<android.widget.TextView>

    private lateinit var executor: Executor
    private lateinit var biometricPrompt: BiometricPrompt
    private lateinit var promptInfo: BiometricPrompt.PromptInfo

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLockScreenBinding.inflate(layoutInflater)
        setContentView(binding.root)

        pinInputBinding = LayoutPinInputBinding.bind(binding.pinInputViewLock.root)

        pinDigitViews = listOf(
            pinInputBinding.pinDigit1, pinInputBinding.pinDigit2,
            pinInputBinding.pinDigit3, pinInputBinding.pinDigit4
        )

        setupKeypad()
        updatePinDisplay()
        pinInputBinding.pinPromptTextview.text = getString(R.string.prompt_enter_pin)

        // Request focus on the hidden EditText to bring up the keyboard
        pinInputBinding.pinEditTextHidden.requestFocus()

        // Set a click listener on the visible PIN display to bring up the keyboard
        pinInputBinding.pinDisplayLayout.setOnClickListener {
            pinInputBinding.pinEditTextHidden.requestFocus()
            val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
            imm.showSoftInput(pinInputBinding.pinEditTextHidden, InputMethodManager.SHOW_IMPLICIT)
        }

        // Add TextWatcher to handle input from the soft keyboard
        pinInputBinding.pinEditTextHidden.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                // Update enteredPin with the current content of the hidden EditText
                enteredPin.clear()
                if (s != null) {
                    enteredPin.append(s.toString())
                }
                updatePinDisplay()
                pinInputBinding.pinErrorTextview.visibility = View.GONE

                if (enteredPin.length == 4) {
                    verifyPin()
                }
            }

            override fun afterTextChanged(s: Editable?) {}
        })


        executor = ContextCompat.getMainExecutor(this)
        setupBiometricPrompt()

        if (PinManager.isFingerprintAuthEnabled(this) && canAuthenticateWithBiometric()) {
            pinInputBinding.keypadFingerprint.visibility = View.VISIBLE
            pinInputBinding.keypadFingerprint.setOnClickListener {
                biometricPrompt.authenticate(promptInfo)
            }
            // Optionally, trigger biometric prompt automatically
            // biometricPrompt.authenticate(promptInfo)
        } else {
            pinInputBinding.keypadFingerprint.visibility = View.GONE
        }
    }

    private fun canAuthenticateWithBiometric(): Boolean {
        val biometricManager = BiometricManager.from(this)
        when (biometricManager.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_STRONG or BiometricManager.Authenticators.BIOMETRIC_WEAK)) {
            BiometricManager.BIOMETRIC_SUCCESS -> return true
            BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE -> {
                Toast.makeText(this, getString(R.string.biometric_auth_error_hw_not_available), Toast.LENGTH_LONG).show()
                return false
            }
            BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE -> {
                Toast.makeText(this, getString(R.string.biometric_auth_error_hw_not_available), Toast.LENGTH_LONG).show()
                return false;
            }
            BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED -> {
                 Toast.makeText(this, getString(R.string.biometric_auth_error_none_enrolled), Toast.LENGTH_LONG).show()
                // Promot user to create credentials here.
                return false
            }
            else -> {
                return false
            }
        }
    }


    private fun setupBiometricPrompt() {
        biometricPrompt = BiometricPrompt(this, executor,
            object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                    super.onAuthenticationError(errorCode, errString)
                    Toast.makeText(applicationContext,
                        "${getString(R.string.biometric_auth_error_prefix)} $errString ($errorCode)", Toast.LENGTH_SHORT)
                        .show()
                }

                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                    super.onAuthenticationSucceeded(result)
                    AppGlobalState.isLocked = false // Unlock the app
                    Toast.makeText(applicationContext,
                        getString(R.string.biometric_auth_succeeded), Toast.LENGTH_SHORT).show()
                    navigateToMainApp()
                }

                override fun onAuthenticationFailed() {
                    super.onAuthenticationFailed()
                    Toast.makeText(applicationContext, getString(R.string.biometric_auth_failed),
                        Toast.LENGTH_SHORT)
                        .show()
                }
            })

        promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle(getString(R.string.prompt_fingerprint_title))
            .setSubtitle(getString(R.string.prompt_fingerprint_subtitle))
            .setNegativeButtonText(getString(R.string.fingerprint_dialog_negative_button_text))
            // .setConfirmationRequired(false) // Show "OK" button after successful auth
            .setAllowedAuthenticators(BiometricManager.Authenticators.BIOMETRIC_STRONG or BiometricManager.Authenticators.BIOMETRIC_WEAK)
            .build()
    }


    private fun setupKeypad() {
        val keypadButtons = mapOf(
            pinInputBinding.keypad1 to "1", pinInputBinding.keypad2 to "2", pinInputBinding.keypad3 to "3",
            pinInputBinding.keypad4 to "4", pinInputBinding.keypad5 to "5", pinInputBinding.keypad6 to "6",
            pinInputBinding.keypad7 to "7", pinInputBinding.keypad8 to "8", pinInputBinding.keypad9 to "9",
            pinInputBinding.keypad0 to "0"
        )

        keypadButtons.forEach { (button, digit) ->
            button.setOnClickListener { onDigitPressed(digit) }
        }

        pinInputBinding.keypadBackspace.setOnClickListener { onBackspacePressed() }
    }

    private fun onDigitPressed(digit: String) {
        if (enteredPin.length < 4) {
            enteredPin.append(digit)
            updatePinDisplay()
            pinInputBinding.pinErrorTextview.visibility = View.GONE

            if (enteredPin.length == 4) {
                verifyPin()
            }
        }
    }

    private fun onBackspacePressed() {
        if (enteredPin.isNotEmpty()) {
            enteredPin.deleteCharAt(enteredPin.length - 1)
            updatePinDisplay()
            pinInputBinding.pinErrorTextview.visibility = View.GONE
        }
    }

    private fun updatePinDisplay() {
        for (i in pinDigitViews.indices) {
            if (i < enteredPin.length) {
                pinDigitViews[i].setBackgroundResource(R.drawable.bg_pin_digit_filled)
            } else {
                pinDigitViews[i].setBackgroundResource(R.drawable.bg_pin_digit_empty)
            }
        }
    }

    private fun verifyPin() {
        if (PinManager.verifyPin(this, enteredPin.toString())) {
            AppGlobalState.isLocked = false // Unlock the app
            navigateToMainApp()
        } else {
            pinInputBinding.pinErrorTextview.text = getString(R.string.error_incorrect_pin)
            pinInputBinding.pinErrorTextview.visibility = View.VISIBLE
            enteredPin.clear()
            updatePinDisplay()
            // You might want to add a delay or shake animation for incorrect PIN
        }
    }

    private fun navigateToMainApp() {
        val intent = Intent(this, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }

    // Prevent going back to PinSetupActivity or other unprotected states
    @Deprecated("This method is deprecated in favor of using the OnBackPressedDispatcher to handle back button events.")
    override fun onBackPressed() {
        // To prevent bypassing the lock screen, typically you'd minimize the app
        // or finish the activity stack if it's the root.
        // For simplicity here, we'll just minimize.
        moveTaskToBack(true)
    }
}
