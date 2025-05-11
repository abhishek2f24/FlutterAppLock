package com.example.applock.activities

import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.widget.Button
import android.widget.FrameLayout
import android.widget.SeekBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.applock.R
import com.example.applock.services.AppLockService
import com.example.applock.utils.AdsManager
import com.example.applock.utils.LockUtils
import com.example.applock.utils.PreferenceUtils

class SettingsActivity : AppCompatActivity() {

    private lateinit var btnResetPattern: Button
    private lateinit var btnStopService: Button
    private lateinit var btnStartService: Button
    private lateinit var seekBarTimeout: SeekBar
    private lateinit var textTimeout: TextView
    private lateinit var adContainer: FrameLayout
    private lateinit var preferenceUtils: PreferenceUtils
    private lateinit var adsManager: AdsManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        // Enable back button
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        // Initialize views
        btnResetPattern = findViewById(R.id.btn_reset_pattern)
        btnStopService = findViewById(R.id.btn_stop_service)
        btnStartService = findViewById(R.id.btn_start_service)
        seekBarTimeout = findViewById(R.id.seekbar_timeout)
        textTimeout = findViewById(R.id.text_timeout)
        adContainer = findViewById(R.id.ad_container)

        // Initialize utils
        preferenceUtils = PreferenceUtils(this)
        adsManager = (application as com.example.applock.App).adsManager

        // Load banner ad
        adsManager.loadBannerAd(adContainer)

        setupViews()
    }

    private fun setupViews() {
        // Timeout seekbar
        val currentTimeout = preferenceUtils.getUnlockTimeout()
        seekBarTimeout.progress = currentTimeout / 30 // Intervals of 30 seconds
        updateTimeoutText(currentTimeout)

        seekBarTimeout.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                val timeout = progress * 30 // Convert to seconds
                updateTimeoutText(timeout)
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {
                // Not needed
            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
                val timeout = seekBar?.progress?.times(30) ?: 30
                preferenceUtils.setUnlockTimeout(timeout)
            }
        })

        // Reset pattern button
        btnResetPattern.setOnClickListener {
            // Show interstitial ad
            adsManager.showInterstitialAd(this) {
                startActivity(Intent(this, PatternSetupActivity::class.java))
            }
        }

        // Service control buttons
        updateServiceButtons()

        btnStartService.setOnClickListener {
            if (LockUtils.isPatternSet(this)) {
                startAppLockService()
                updateServiceButtons()
                Toast.makeText(this, "App Lock service started", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Please set up a pattern first", Toast.LENGTH_SHORT).show()
                startActivity(Intent(this, PatternSetupActivity::class.java))
            }
        }

        btnStopService.setOnClickListener {
            stopAppLockService()
            updateServiceButtons()
            Toast.makeText(this, "App Lock service stopped", Toast.LENGTH_SHORT).show()
        }
    }

    private fun updateTimeoutText(seconds: Int) {
        val minutes = seconds / 60
        val remainingSeconds = seconds % 60

        if (minutes > 0) {
            textTimeout.text = "Auto-lock timeout: $minutes min $remainingSeconds sec"
        } else {
            textTimeout.text = "Auto-lock timeout: $seconds sec"
        }
    }

    private fun updateServiceButtons() {
        if (preferenceUtils.isServiceRunning()) {
            btnStartService.isEnabled = false
            btnStopService.isEnabled = true
        } else {
            btnStartService.isEnabled = true
            btnStopService.isEnabled = false
        }
    }

    private fun startAppLockService() {
        val serviceIntent = Intent(this, AppLockService::class.java)
        startForegroundService(serviceIntent)
        preferenceUtils.setServiceRunning(true)
    }

    private fun stopAppLockService() {
        val serviceIntent = Intent(this, AppLockService::class.java)
        stopService(serviceIntent)
        preferenceUtils.setServiceRunning(false)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            onBackPressed()
            return true
        }
        return super.onOptionsItemSelected(item)
    }
}