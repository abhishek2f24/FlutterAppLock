package com.example.applock.utils

import android.app.Activity
import android.content.Context
import android.view.ViewGroup
import android.widget.Toast
import com.google.android.gms.ads.*
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback

class AdsManager(private val context: Context) {

    private var interstitialAd: InterstitialAd? = null

    // Test AdMob IDs (Replace with real IDs for production)
    companion object {
        private const val TEST_BANNER_ID = "ca-app-pub-3940256099942544/6300978111"
        private const val TEST_INTERSTITIAL_ID = "ca-app-pub-3940256099942544/1033173712"
    }

    init {
        // Load interstitial ad on initialization
        loadInterstitialAd()
    }

    fun loadBannerAd(adContainer: ViewGroup) {
        val adView = AdView(context)
        adView.adUnitId = TEST_BANNER_ID
        adView.adSize = AdSize.BANNER

        val adRequest = AdRequest.Builder().build()
        adView.loadAd(adRequest)

        // Add AdView to the layout
        adContainer.removeAllViews()
        adContainer.addView(adView)

        // Ad load listener
        adView.adListener = object : AdListener() {
            override fun onAdFailedToLoad(error: LoadAdError) {
                super.onAdFailedToLoad(error)
                // Retry loading after delay
                adContainer.postDelayed({ loadBannerAd(adContainer) }, 60000)
            }
        }
    }

    private fun loadInterstitialAd() {
        val adRequest = AdRequest.Builder().build()

        InterstitialAd.load(
            context,
            TEST_INTERSTITIAL_ID,
            adRequest,
            object : InterstitialAdLoadCallback() {
                override fun onAdLoaded(ad: InterstitialAd) {
                    interstitialAd = ad
                }

                override fun onAdFailedToLoad(error: LoadAdError) {
                    interstitialAd = null
                    // Retry loading after delay
                    context.mainExecutor.execute {
                        loadInterstitialAd()
                    }
                }
            }
        )
    }

    fun showInterstitialAd(activity: Activity, onAdClosed: () -> Unit) {
        if (interstitialAd != null) {
            interstitialAd?.fullScreenContentCallback = object : FullScreenContentCallback() {
                override fun onAdDismissedFullScreenContent() {
                    // Load new ad for next time
                    loadInterstitialAd()
                    // Call callback
                    onAdClosed()
                }

                override fun onAdFailedToShowFullScreenContent(error: AdError) {
                    // Load new ad
                    loadInterstitialAd()
                    // Call callback anyway
                    onAdClosed()
                }
            }

            interstitialAd?.show(activity)
        } else {
            // If ad not loaded, just continue
            onAdClosed()
        }
    }
}