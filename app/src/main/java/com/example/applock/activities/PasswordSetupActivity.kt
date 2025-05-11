package com.example.applock.activities

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.applock.R
import com.example.applock.databinding.ActivityPasswordSetupBinding
import com.example.applock.utils.PrefManager

class PasswordSetupActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPasswordSetupBinding
    private lateinit var prefManager: PrefManager
    private var isChangingPassword = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPasswordSetupBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        
        prefManager = PrefManager(this)
        
        isChangingPassword = intent.getBooleanExtra("isChangingPassword", false)
        supportActionBar?.title = if (isChangingPassword) "Change Password" else "Set Password"
        
        if (isChangingPassword) {
            binding.layoutCurrentPassword.visibility = android.view.View.VISIBLE
        }
        
        setupTextWatchers()
        
        binding.btnSetPassword.setOnClickListener {
            if (validateInput()) {
                savePassword()
            }
        }
    }
    
    private fun setupTextWatchers() {
        val textWatcher = object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            
            override fun afterTextChanged(s: Editable?) {
                updateButtonState()
            }
        }
        
        binding.etPassword.addTextChangedListener(textWatcher)
        binding.etConfirmPassword.addTextChangedListener(textWatcher)
        if (isChangingPassword) {
            binding.etCurrentPassword.addTextChangedListener(textWatcher)
        }
    }
    
    private fun updateButtonState() {
        val password = binding.etPassword.text.toString()
        val confirmPassword = binding.etConfirmPassword.text.toString()
        
        binding.btnSetPassword.isEnabled = if (isChangingPassword) {
            binding.etCurrentPassword.text.isNotEmpty() && password.isNotEmpty() && confirmPassword.isNotEmpty()
        } else {
            password.isNotEmpty() && confirmPassword.isNotEmpty()
        }
    }
    
    private fun validateInput(): Boolean {
        val password = binding.etPassword.text.toString()
        val confirmPassword = binding.etConfirmPassword.text.toString()
        
        if (password.length < 6) {
            binding.passwordLayout.error = "Password must be at least 6 characters"
            return false
        }
        
        if (password != confirmPassword) {
            binding.confirmPasswordLayout.error = "Passwords don't match"
            return false
        }
        
        if (isChangingPassword) {
            val currentPassword = binding.etCurrentPassword.text.toString()
            if (!prefManager.verifyPassword(currentPassword)) {
                binding.currentPasswordLayout.error = "Current password is incorrect"
                return false
            }
        }
        
        return true
    }
    
    private fun savePassword() {
        val password = binding.etPassword.text.toString()
        prefManager.savePassword(password)
        
        val message = if (isChangingPassword) {
            "Password changed successfully"
        } else {
            "Password set successfully"
        }
        
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
        finish()
    }
    
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            onBackPressed()
            return true
        }
        return super.onOptionsItemSelected(item)
    }
}