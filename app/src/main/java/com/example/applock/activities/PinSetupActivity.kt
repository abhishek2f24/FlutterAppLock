package com.example.applock.activities

import android.os.Bundle
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.applock.R
import com.example.applock.databinding.ActivityPinSetupBinding
import com.example.applock.utils.PrefManager

class PinSetupActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPinSetupBinding
    private lateinit var prefManager: PrefManager
    private var firstPin: String? = null
    private var isConfirmPin = false
    private var currentPin = StringBuilder()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPinSetupBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Set PIN Lock"

        prefManager = PrefManager(this)

        binding.instructionText.text = "Enter a 4-digit PIN"

        // Set up number buttons
        setupNumberPad()
        
        // Delete button
        binding.btnDelete.setOnClickListener {
            if (currentPin.isNotEmpty()) {
                currentPin.deleteCharAt(currentPin.length - 1)
                updatePinDisplay()
            }
        }
        
        // Clear button
        binding.btnClear.setOnClickListener {
            currentPin.clear()
            updatePinDisplay()
        }
    }

    private fun setupNumberPad() {
        binding.btn0.setOnClickListener { appendDigit("0") }
        binding.btn1.setOnClickListener { appendDigit("1") }
        binding.btn2.setOnClickListener { appendDigit("2") }
        binding.btn3.setOnClickListener { appendDigit("3") }
        binding.btn4.setOnClickListener { appendDigit("4") }
        binding.btn5.setOnClickListener { appendDigit("5") }
        binding.btn6.setOnClickListener { appendDigit("6") }
        binding.btn7.setOnClickListener { appendDigit("7") }
        binding.btn8.setOnClickListener { appendDigit("8") }
        binding.btn9.setOnClickListener { appendDigit("9") }
    }

    private fun appendDigit(digit: String) {
        if (currentPin.length < 4) {
            currentPin.append(digit)
            updatePinDisplay()
            
            if (currentPin.length == 4) {
                processPin()
            }
        }
    }

    private fun updatePinDisplay() {
        binding.pinDot1.isSelected = currentPin.length >= 1
        binding.pinDot2.isSelected = currentPin.length >= 2
        binding.pinDot3.isSelected = currentPin.length >= 3
        binding.pinDot4.isSelected = currentPin.length >= 4
    }

    private fun processPin() {
        val pin = currentPin.toString()
        
        if (!isConfirmPin) {
            firstPin = pin
            isConfirmPin = true
            binding.instructionText.text = "Confirm your PIN"
            currentPin.clear()
            updatePinDisplay()
        } else {
            if (firstPin == pin) {
                // PIN confirmed
                prefManager.savePin(pin)
                prefManager.setPinEnabled(true)
                Toast.makeText(this, "PIN set successfully", Toast.LENGTH_SHORT).show()
                finish()
            } else {
                // PIN doesn't match
                isConfirmPin = false
                firstPin = null
                binding.instructionText.text = "PINs didn't match. Try again"
                currentPin.clear()
                updatePinDisplay()
            }
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            onBackPressed()
            return true
        }
        return super.onOptionsItemSelected(item)
    }
}