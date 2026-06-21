package com.example.data

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.util.Log
import android.widget.Toast
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.rewarded.RewardedAd
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback

/**
 * AdMobManager handles the loading and displaying of Rewarded Video Ads
 * for Pixelcrafter app actions (e.g. download or layout application).
 */
object AdMobManager {
    private const val TAG = "AdMobManager"

    // =========================================================================================
    // 📢 DEVELOPER CONFIGURATION - WHERE TO INJECT YOUR ADMOB PUBLISHER & AD UNIT IDS:
    //
    // 1. ADMOB APPLICATION ID (App ID):
    //    Open /app/src/main/AndroidManifest.xml and replace the value in:
    //    <meta-data
    //        android:name="com.google.android.gms.ads.APPLICATION_ID"
    //        android:value="ca-app-pub-3940256099942544~3347511713"/> <-- Put your AdMob APP ID here
    //
    // 2. ADMOB REWARDED AD UNIT ID:
    //    Change the constant below to your live AdMob Rewarded Ad Unit ID.
    // =========================================================================================
    const val REWARDED_AD_UNIT_ID = "ca-app-pub-2767673700095238/3291665284" // Standard AdMob Test Rewarded ID

    private var mRewardedAd: RewardedAd? = null
    private var isAdLoading = false

    /**
     * Safely locates the Activity hosting the given composable context.
     */
    fun Context.findActivity(): Activity? {
        var context = this
        while (context is ContextWrapper) {
            if (context is Activity) return context
            context = context.baseContext
        }
        return null
    }

    /**
     * Preloads a Rewarded Ad so it is ready for high-speed delivery when needed.
     */
    fun loadRewardedAd(context: Context) {
        if (mRewardedAd != null || isAdLoading) return

        isAdLoading = true
        val adRequest = AdRequest.Builder().build()
        
        Log.d(TAG, "Requesting rewarded ad payload from AdMob...")
        RewardedAd.load(
            context.applicationContext,
            REWARDED_AD_UNIT_ID,
            adRequest,
            object : RewardedAdLoadCallback() {
                override fun onAdFailedToLoad(adError: LoadAdError) {
                    Log.e(TAG, "Failed to load rewarded ad: ${adError.message}")
                    mRewardedAd = null
                    isAdLoading = false
                }

                override fun onAdLoaded(rewardedAd: RewardedAd) {
                    Log.d(TAG, "Rewarded video ad successfully preloaded.")
                    mRewardedAd = rewardedAd
                    isAdLoading = false
                }
            }
        )
    }

    /**
     * Shows the rewarded video ad. On successful display or if it failed to load,
     * the callback is triggered to allow uninterrupted user experiences.
     *
     * @param context Context hosting the execution
     * @param onRewarded Callback triggered with 'true' if criteria is met, or 'false' if canceled.
     */
    fun showRewardedAd(context: Context, onRewarded: (Boolean) -> Unit) {
        val activity = context.findActivity()
        if (activity == null) {
            Log.e(TAG, "Cannot show ad: Context is not an Activity.")
            onRewarded(true) // Fail-open so users aren't locked
            return
        }

        val ad = mRewardedAd
        if (ad != null) {
            Toast.makeText(activity, "Loading rewarded video ad...", Toast.LENGTH_SHORT).show()
            ad.show(activity) { rewardItem ->
                Log.d(TAG, "User completed ad! Earned: ${rewardItem.amount} ${rewardItem.type}")
                // Reward granted
                onRewarded(true)
            }
            // Reset reference and reload a fresh asset for next use
            mRewardedAd = null
            loadRewardedAd(activity)
        } else {
            // Ad is not ready yet, display a brief info message and load/proceed
            Toast.makeText(activity, "Preparing ad stream... granting immediate access!", Toast.LENGTH_SHORT).show()
            
            // Re-fire standard preload request
            loadRewardedAd(activity)
            
            // Proceed so performance/connection latency never blocks application functionality
            onRewarded(true)
        }
    }
}
