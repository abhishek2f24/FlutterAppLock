package com.example.applock.activities

import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.FrameLayout
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.applock.R
import com.example.applock.adapters.AppListAdapter
import com.example.applock.database.AppLockRepository
import com.example.applock.models.AppInfo
import com.example.applock.models.LockInfo
import com.example.applock.utils.AdsManager
import com.example.applock.utils.AppUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class AppSelectionActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: AppListAdapter
    private lateinit var progressBar: ProgressBar
    private lateinit var emptyText: TextView
    private lateinit var adContainer: FrameLayout
    private lateinit var repository: AppLockRepository
    private lateinit var adsManager: AdsManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_app_selection)

        // Enable back button
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Select Apps to Lock"

        // Initialize views
        recyclerView = findViewById(R.id.recycler_view)
        progressBar = findViewById(R.id.progress_bar)
        emptyText = findViewById(R.id.empty_text)
        adContainer = findViewById(R.id.ad_container)

        // Get repository from App
        repository = (application as com.example.applock.App).repository
        adsManager = (application as com.example.applock.App).adsManager

        // Load banner ad
        adsManager.loadBannerAd(adContainer)

        setupRecyclerView()
        loadApps()
    }

    private fun setupRecyclerView() {
        adapter = AppListAdapter { appInfo, isLocked ->
            // Handle lock/unlock action
            lifecycleScope.launch {
                if (isLocked) {
                    // Lock the app
                    val lockInfo = LockInfo(appInfo.packageName, appInfo.appName)
                    repository.insert(lockInfo)
                } else {
                    // Unlock the app
                    val lockInfo = repository.getLockInfoByPackage(appInfo.packageName)
                    lockInfo?.let {
                        repository.delete(it)
                    }
                }
            }
        }

        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter
    }

    private fun loadApps() {
        lifecycleScope.launch {
            progressBar.visibility = View.VISIBLE
            recyclerView.visibility = View.GONE
            emptyText.visibility = View.GONE

            // Get installed apps in background
            val appList = withContext(Dispatchers.IO) {
                val apps = AppUtils.getInstalledApps(this@AppSelectionActivity)

                // Check which apps are locked
                for (app in apps) {
                    app.isLocked = repository.isAppLocked(app.packageName)
                }

                apps
            }

            // Update UI
            progressBar.visibility = View.GONE

            if (appList.isEmpty()) {
                emptyText.visibility = View.VISIBLE
            } else {
                recyclerView.visibility = View.VISIBLE
                adapter.submitList(appList)
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
