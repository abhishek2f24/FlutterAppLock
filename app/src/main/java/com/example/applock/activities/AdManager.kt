package com.example.applock.activities

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.util.Log
import android.view.ViewGroup
import android.widget.Toast
import androidx.annotation.RequiresPermission
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import com.google.android.gms.ads.rewarded.RewardedAd
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback

class AdManager private constructor(private val context: Context) {
    
    private var interstitialAd: InterstitialAd? = null
    private var rewardedAd: RewardedAd? = null
    private var adCounter = 0
    private val adfrequency = 3 // Show interstitial every 3 actions
    
    companion object {
        private const val TAG = "AdManager"
        
        // Replace these with your actual ad unit IDs
        private const val BANNER_AD_UNIT_ID = "ca-app-pub-3940256099942544/6300978111" // Test ID
        private const val INTERSTITIAL_AD_UNIT_ID = "ca-app-pub-3940256099942544/1033173712" // Test ID
        private const val REWARDED_AD_UNIT_ID = "ca-app-pub-3940256099942544/5224354917" // Test ID
        
        @SuppressLint("StaticFieldLeak")
        @Volatile
        private var instance: AdManager? = null
        
        fun getInstance(context: Context): AdManager {
            return instance ?: synchronized(this) {
                instance ?: AdManager(context.applicationContext).also { instance = it }
            }
        }
    }
    
    fun initialize() {
        MobileAds.initialize(context) {
            Log.d(TAG, "Mobile Ads initialized successfully")
            loadInterstitialAd()
        }
    }
    
    @RequiresPermission(Manifest.permission.INTERNET)
    fun loadBannerAd(adContainer: ViewGroup) {
        val adView = AdView(context)
        adView.adUnitId = BANNER_AD_UNIT_ID
        
        adContainer.removeAllViews()
        adContainer.addView(adView)
        
        val adRequest = AdRequest.Builder().build()
        adView.loadAd(adRequest)
        
        adView.adListener = object : AdListener() {
            override fun onAdLoaded() {
                Log.d(TAG, "Banner ad loaded successfully")
            }
            
            override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                Log.e(TAG, "Banner ad failed to load: ${loadAdError.message}")
            }
        }
    }
    
    private fun loadInterstitialAd() {
        val adRequest = AdRequest.Builder().build()
        InterstitialAd.load(
            context,
            INTERSTITIAL_AD_UNIT_ID,
            adRequest,
            object : InterstitialAdLoadCallback() {
                override fun onAdLoaded(ad: InterstitialAd) {
                    Log.d(TAG, "Interstitial ad loaded successfully")
                    interstitialAd = ad
                    setupInterstitialAdCallbacks()
                }
                
                override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                    Log.e(TAG, "Interstitial ad failed to load: ${loadAdError.message}")
                    interstitialAd = null
                }
            }
        )
    }
    
    private fun setupInterstitialAdCallbacks() {
        interstitialAd?.fullScreenContentCallback = object : FullScreenContentCallback() {
            override fun onAdDismissedFullScreenContent() {
                Log.d(TAG, "Interstitial ad dismissed")
                interstitialAd = null
                loadInterstitialAd() // Load the next ad
            }
            
            override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                Log.e(TAG, "Interstitial ad failed to show: ${adError.message}")
                interstitialAd = null
                loadInterstitialAd() // Try to load another ad
            }
            
            override fun onAdShowedFullScreenContent() {
                Log.d(TAG, "Interstitial ad showed successfully")
            }
        }
    }
    
    fun showInterstitialIfAvailable(activity: Activity) {
        adCounter++
        
        if (adCounter % adfrequency != 0) {
            return
        }
        
        if (interstitialAd != null) {
            interstitialAd?.show(activity)
        } else {
            Log.d(TAG, "Interstitial ad not ready yet")
            loadInterstitialAd()
        }
    }
    
    fun loadRewardedAd() {
        val adRequest = AdRequest.Builder().build()
        RewardedAd.load(
            context,
            REWARDED_AD_UNIT_ID,
            adRequest,
            object : RewardedAdLoadCallback() {
                override fun onAdLoaded(ad: RewardedAd) {
                    Log.d(TAG, "Rewarded ad loaded successfully")
                    rewardedAd = ad
                }
                
                override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                    Log.e(TAG, "Rewarded ad failed to load: ${loadAdError.message}")
                    rewardedAd = null
                }
            }
        )
    }
    
    fun showRewardedAd(activity: Activity, onRewarded: () -> Unit) {
        if (rewardedAd != null) {
            rewardedAd?.fullScreenContentCallback = object : FullScreenContentCallback() {
                override fun onAdDismissedFullScreenContent() {
                    Log.d(TAG, "Rewarded ad dismissed")
                    rewardedAd = null
                    loadRewardedAd() // Load the next ad
                }
                
                override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                    Log.e(TAG, "Rewarded ad failed to show: ${adError.message}")
                    rewardedAd = null
                }
            }
            
            rewardedAd?.show(activity) { rewardItem ->
                // Handle the reward
                val rewardAmount = rewardItem.amount
                val rewardType = rewardItem.type
                Log.d(TAG, "User earned reward: $rewardAmount $rewardType")
                onRewarded()
            }
        } else {
            Toast.makeText(context, "Rewarded ad not ready yet", Toast.LENGTH_SHORT).show()
            loadRewardedAd()
        }
    }
}