package com.example.secure.ui.settings

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.secure.R
import com.example.secure.auth.PinManager

class PinChangeActivity : AppCompatActivity() {

    private lateinit var textPinPrompt: TextView
    private lateinit var editTextPin: EditText
    private lateinit var buttonPinNext: Button
    private lateinit var buttonPinCancel: Button

    private enum class PinChangeStage {
        VERIFY_CURRENT_PIN,
        ENTER_NEW_PIN,
        CONFIRM_NEW_PIN
    }

    private var currentStage = PinChangeStage.VERIFY_CURRENT_PIN
    private var newPinAttempt: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pin_change)

        textPinPrompt = findViewById(R.id.text_pin_prompt)
        editTextPin = findViewById(R.id.edit_text_pin)
        buttonPinNext = findViewById(R.id.button_pin_next)
        buttonPinCancel = findViewById(R.id.button_pin_cancel)

        setupCurrentStage()

        buttonPinNext.setOnClickListener {
            handleNextButton()
        }

        buttonPinCancel.setOnClickListener {
            finish()
        }

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Change PIN"
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }

    private fun setupCurrentStage() {
        editTextPin.text.clear()
        when (currentStage) {
            PinChangeStage.VERIFY_CURRENT_PIN -> {
                textPinPrompt.text = getString(R.string.prompt_enter_current_pin) // Add this string
                buttonPinNext.text = "Verify"
            }
            PinChangeStage.ENTER_NEW_PIN -> {
                textPinPrompt.text = getString(R.string.prompt_enter_new_pin) // Add this string
                buttonPinNext.text = "Next"
            }
            PinChangeStage.CONFIRM_NEW_PIN -> {
                textPinPrompt.text = getString(R.string.prompt_confirm_pin)
                buttonPinNext.text = "Save PIN"
            }
        }
    }

    private fun handleNextButton() {
        val enteredPin = editTextPin.text.toString()
        if (enteredPin.length != 4) {
            Toast.makeText(this, R.string.error_pin_too_short, Toast.LENGTH_SHORT).show()
            return
        }

        when (currentStage) {
            PinChangeStage.VERIFY_CURRENT_PIN -> {
                if (PinManager.verifyPin(this, enteredPin)) {
                    currentStage = PinChangeStage.ENTER_NEW_PIN
                    setupCurrentStage()
                } else {
                    Toast.makeText(this, R.string.error_incorrect_pin, Toast.LENGTH_SHORT).show()
                }
            }
            PinChangeStage.ENTER_NEW_PIN -> {
                newPinAttempt = enteredPin
                currentStage = PinChangeStage.CONFIRM_NEW_PIN
                setupCurrentStage()
            }
            PinChangeStage.CONFIRM_NEW_PIN -> {
                if (enteredPin == newPinAttempt) {
                    PinManager.savePin(this, enteredPin)
                    Toast.makeText(this, R.string.pin_changed_successfully, Toast.LENGTH_SHORT).show()
                    finish()
                } else {
                    Toast.makeText(this, R.string.error_pin_mismatch, Toast.LENGTH_SHORT).show()
                    // Reset to entering new PIN stage
                    currentStage = PinChangeStage.ENTER_NEW_PIN
                    newPinAttempt = null
                    setupCurrentStage()
                }
            }
        }
    }
}
