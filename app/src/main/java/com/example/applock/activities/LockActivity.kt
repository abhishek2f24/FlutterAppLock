package com.example.applock.activities

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.FrameLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.andrognito.patternlockview.PatternLockView
import com.andrognito.patternlockview.listener.PatternLockViewListener
import com.example.applock.App
import com.example.applock.R
import com.example.applock.services.AppLockService
import com.example.applock.utils.AdsManager
import com.example.applock.utils.LockUtils
import java.util.*

class LockActivity : AppCompatActivity() {

    private lateinit var packageName: String
    private lateinit var patternLockView: PatternLockView
    private lateinit var textPrompt: TextView
    private lateinit var btnCancel: Button
    private lateinit var adContainer: FrameLayout
    private lateinit var adsManager: AdsManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_lock)

        // Get package name from intent
        packageName = intent.getStringExtra("package_name") ?: ""
        if (packageName.isEmpty()) {
            finish()
            return
        }

        // Initialize views
        patternLockView = findViewById(R.id.pattern_lock_view)
        textPrompt = findViewById(R.id.text_prompt)
        btnCancel = findViewById(R.id.btn_cancel)
        adContainer = findViewById(R.id.ad_container)

        // Initialize AdManager
        adsManager = (application as App).adsManager

        // Load banner ad
        adsManager.loadBannerAd(adContainer)

        setupPatternLock()

        btnCancel.setOnClickListener {
            // Return to home screen
            val intent = Intent(Intent.ACTION_MAIN)
            intent.addCategory(Intent.CATEGORY_HOME)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            startActivity(intent)
            finish()
        }
    }

    private fun setupPatternLock() {
        patternLockView.addPatternLockListener(object : PatternLockViewListener {
            override fun onStarted() {
                // No action needed
            }

            override fun onProgress(progressPattern: MutableList<PatternLockView.Dot>?) {
                // No action needed
            }

            override fun onComplete(pattern: MutableList<PatternLockView.Dot>?) {
                if (pattern != null) {
                    val patternString = patternToString(pattern)
                    validatePattern(patternString)
                }
            }

            override fun onCleared() {
                // No action needed
            }
        })
    }

    private fun patternToString(pattern: List<PatternLockView.Dot>): String {
        val result = StringBuilder()
        for (dot in pattern) {
            result.append(dot.id)
        }
        return result.toString()
    }

    private fun validatePattern(patternString: String) {
        if (LockUtils.validatePattern(this, patternString)) {
            // Pattern correct - update last unlocked app
            val appLockService = Intent(this, AppLockService::class.java)
            startService(appLockService)

            // Show interstitial ad occasionally (20% chance)
            if (Random().nextInt(5) == 0) {
                adsManager.showInterstitialAd(this) {
                    unlockAndLaunchApp()
                }
            } else {
                unlockAndLaunchApp()
            }
        } else {
            // Pattern incorrect
            patternLockView.setViewMode(PatternLockView.PatternViewMode.WRONG)
            Toast.makeText(this, "Incorrect pattern", Toast.LENGTH_SHORT).show()

            // Reset after a delay
            patternLockView.postDelayed({
                patternLockView.clearPattern()
                patternLockView.setViewMode(PatternLockView.PatternViewMode.CORRECT)
            }, 1000)
        }
    }

    private fun unlockAndLaunchApp() {
        // Mark app as temporarily unlocked
        try {
            val service = application as? App
            val appLockService = Intent(this, AppLockService::class.java)
            appLockService.putExtra("last_unlocked_package", packageName)
            startService(appLockService)

            // Launch the app
            val launchIntent = packageManager.getLaunchIntentForPackage(packageName)
            if (launchIntent != null) {
                startActivity(launchIntent)
            }
        } catch (e: Exception) {
            Toast.makeText(this, "Error launching app", Toast.LENGTH_SHORT).show()
        }

        finish()
    }

    override fun onBackPressed() {
        // Return to home screen instead of previous app
        val intent = Intent(Intent.ACTION_MAIN)
        intent.addCategory(Intent.CATEGORY_HOME)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        startActivity(intent)
        finish()
    }
}