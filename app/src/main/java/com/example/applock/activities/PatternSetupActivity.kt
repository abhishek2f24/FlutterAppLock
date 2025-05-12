package com.example.applock.activities

import android.os.Bundle
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.andrognito.patternlockview.PatternLockView
import com.andrognito.patternlockview.listener.PatternLockViewListener
import com.andrognito.patternlockview.utils.PatternLockUtils
import com.example.applock.R
import com.example.applock.databinding.ActivityPatternSetupBinding
import com.example.applock.utils.PrefManager

class PatternSetupActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPatternSetupBinding
    private lateinit var prefManager: PrefManager
    private var firstPattern: String? = null
    private var isConfirmPattern = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPatternSetupBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Set Pattern Lock"

        prefManager = PrefManager(this)

        binding.instructionText.text = "Draw a pattern to secure your apps"
        
        binding.patternLockView.addPatternLockListener(object : PatternLockViewListener {
            override fun onStarted() {}

            override fun onProgress(progressPattern: MutableList<PatternLockView.Dot>?) {}

            override fun onComplete(pattern: MutableList<PatternLockView.Dot>?) {
                if (pattern == null || pattern.size < 4) {
                    binding.instructionText.text = "Pattern too short. Try again with at least 4 dots"
                    binding.patternLockView.clearPattern()
                    return
                }

                val patternString = PatternLockUtils.patternToString(binding.patternLockView, pattern)
                
                if (!isConfirmPattern) {
                    firstPattern = patternString
                    isConfirmPattern = true
                    binding.instructionText.text = "Draw the pattern again to confirm"
                    binding.patternLockView.clearPattern()
                } else {
                    if (firstPattern == patternString) {
                        // Pattern confirmed
                        prefManager.savePattern(patternString)
                        prefManager.setPatternEnabled(true)
                        Toast.makeText(this@PatternSetupActivity, 
                            "Pattern set successfully", Toast.LENGTH_SHORT).show()
                        finish()
                    } else {
                        // Pattern doesn't match
                        isConfirmPattern = false
                        firstPattern = null
                        binding.instructionText.text = "Patterns didn't match. Try again"
                        binding.patternLockView.clearPattern()
                    }
                }
            }

            override fun onCleared() {}
        })
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            onBackPressed()
            return true
        }
        return super.onOptionsItemSelected(item)
    }
}