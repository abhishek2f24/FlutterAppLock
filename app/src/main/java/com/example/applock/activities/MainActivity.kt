package com.example.applock.activities

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Button
import android.widget.FrameLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.applock.R
import com.example.applock.services.AppLockService
import com.example.applock.utils.AdsManager
import com.example.applock.utils.LockUtils
import com.example.applock.utils.PreferenceUtils
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private lateinit var adsManager: AdsManager
    private lateinit var preferenceUtils: PreferenceUtils
    private lateinit var btnSelectApps: Button
    private lateinit var btnSetPattern: Button
    private lateinit var btnSettings: Button
    private lateinit var adContainer: FrameLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Initialize views
        btnSelectApps = findViewById(R.id.btn_select_apps)
        btnSetPattern = findViewById(R.id.btn_set_pattern)
        btnSettings = findViewById(R.id.btn_settings)
        adContainer = findViewById(R.id.ad_container)

        // Initialize utils
        adsManager = (application as com.example.applock.App).adsManager
        preferenceUtils = PreferenceUtils(this)

        // Load banner ad
        adsManager.loadBannerAd(adContainer)

        // Check if it's first launch
        if (preferenceUtils.isFirstTime()) {
            if (!LockUtils.hasUsageStatsPermission(this)) {
                Toast.makeText(this, "App Lock needs usage access permission", Toast.LENGTH_LONG).show()
                LockUtils.openUsageAccessSettings(this)
            }
            preferenceUtils.setFirstTime(false)
        }

        setupButtons()
        checkLockService()
    }

    private fun setupButtons() {
        btnSelectApps.setOnClickListener {
            // Check if pattern is set
            if (LockUtils.isPatternSet(this)) {
                // Show interstitial ad and then navigate
                adsManager.showInterstitialAd(this) {
                    startActivity(Intent(this, AppSelectionActivity::class.java))
                }
            } else {
                Toast.makeText(this, "Please set up a pattern lock first", Toast.LENGTH_SHORT).show()
            }
        }

        btnSetPattern.setOnClickListener {
            startActivity(Intent(this, PatternSetupActivity::class.java))
        }

        btnSettings.setOnClickListener {
            startActivity(Intent(this, SettingsActivity::class.java))
        }
    }

    private fun checkLockService() {
        lifecycleScope.launch {
            // Only start service if pattern is set
            if (LockUtils.isPatternSet(this@MainActivity) &&
                !preferenceUtils.isServiceRunning()) {
                startAppLockService()
            }
        }
    }

    private fun startAppLockService() {
        val serviceIntent = Intent(this, AppLockService::class.java)
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            startForegroundService(serviceIntent)
        } else {
            startService(serviceIntent)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_settings -> {
                startActivity(Intent(this, SettingsActivity::class.java))
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}
