package com.example.secure.ui.pin

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import com.example.secure.MainActivity
import com.example.secure.R
import com.example.secure.auth.PinManager
import com.example.secure.AppGlobalState
import com.example.secure.TrackedActivity
import com.example.secure.databinding.ActivityPinSetupBinding
import android.text.Editable
import android.text.TextWatcher
import android.view.inputmethod.InputMethodManager
import com.example.secure.databinding.LayoutPinInputBinding

class PinSetupActivity : TrackedActivity() {

    private lateinit var binding: ActivityPinSetupBinding
    private lateinit var pinInputBinding: LayoutPinInputBinding

    private val enteredPin = StringBuilder()
    private var firstPin: String? = null
    private var isConfirmingPin = false

    private lateinit var pinDigitViews: List<TextView>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPinSetupBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // The include tag in XML needs to be accessed via the binding of the inflated layout
        // or by finding the root of the included layout and then creating a binding for it.
        // Here, we assume 'pin_input_view' is the ID of the include tag's root in activity_pin_setup.xml
        pinInputBinding = LayoutPinInputBinding.bind(binding.pinInputView.root)


        pinDigitViews = listOf(
            pinInputBinding.pinDigit1,
            pinInputBinding.pinDigit2,
            pinInputBinding.pinDigit3,
            pinInputBinding.pinDigit4
        )

        setupKeypad()
        updatePinDisplay() // Initialize empty dots

        binding.pinSetupInstructionTextview.text = getString(R.string.prompt_set_pin)
        pinInputBinding.pinPromptTextview.visibility = View.GONE // Hide prompt from included layout
        pinInputBinding.keypadFingerprint.visibility = View.GONE // No fingerprint during setup

        binding.pinSetupConfirmButton.setOnClickListener {
            // This button is currently GONE, logic might change if it's used.
            // For now, PIN is set once 4 digits are entered in confirmation.
        }

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
                    processPinEntry()
                }
            }

            override fun afterTextChanged(s: Editable?) {}
        })
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
                processPinEntry()
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
                //pinDigitViews[i].text = "*" // Or keep them blank
            } else {
                pinDigitViews[i].setBackgroundResource(R.drawable.bg_pin_digit_empty)
                //pinDigitViews[i].text = ""
            }
        }
    }

    private fun processPinEntry() {
        if (!isConfirmingPin) {
            firstPin = enteredPin.toString()
            binding.pinSetupInstructionTextview.text = getString(R.string.prompt_confirm_pin)
            enteredPin.clear()
            pinInputBinding.pinEditTextHidden.setText("") // Clear the hidden EditText
            updatePinDisplay()
            isConfirmingPin = true
        } else {
            if (firstPin == enteredPin.toString()) {
                PinManager.savePin(this, enteredPin.toString())
                // Also enable fingerprint by default on setup, or ask user
                PinManager.setFingerprintAuthEnabled(this, true)
                AppGlobalState.isLocked = false // Unlock the app
                Toast.makeText(this, getString(R.string.pin_set_successfully), Toast.LENGTH_SHORT).show()
                navigateToMainApp()
            } else {
                pinInputBinding.pinErrorTextview.text = getString(R.string.error_pin_mismatch)
                pinInputBinding.pinErrorTextview.visibility = View.VISIBLE
                // Reset to first PIN entry stage
                isConfirmingPin = false
                firstPin = null
                binding.pinSetupInstructionTextview.text = getString(R.string.prompt_set_pin)
                enteredPin.clear()
                updatePinDisplay()
            }
        }
    }

    private fun navigateToMainApp() {
        val intent = Intent(this, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }
}
