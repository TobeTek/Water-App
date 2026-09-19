package com.example.monetization

import android.app.Activity
import android.content.Context
import android.util.Log
import com.example.BuildConfig
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import com.revenuecat.purchases.CustomerInfo
import com.revenuecat.purchases.ExperimentalPreviewRevenueCatPurchasesAPI
import com.revenuecat.purchases.LogLevel
import com.revenuecat.purchases.Purchases
import com.revenuecat.purchases.PurchasesConfiguration
import com.revenuecat.purchases.admob.loadAndTrackInterstitialAd
import com.revenuecat.purchases.admob.show
import com.revenuecat.purchases.interfaces.ReceiveCustomerInfoCallback
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * Data model for visual ad presentation when AdMob interstitial is shown or fallback ad dialog displays.
 */
data class InAppAdModel(
    val title: String,
    val sponsor: String,
    val description: String,
    val actionText: String,
    val actionTag: String,
    val placement: String,
    val isRealAdMob: Boolean = false
)

object RevenueCatAdManager {
    private const val TAG = "RevenueCatAdManager"

    // Google's official sample test interstitial ad unit ID
    private const val TEST_INTERSTITIAL_AD_UNIT_ID = "ca-app-pub-3940256099942544/1033173712"

    private var interstitialAd: InterstitialAd? = null
    private var isAdLoading = false

    private val _isRevenueCatReady = MutableStateFlow(false)
    val isRevenueCatReady: StateFlow<Boolean> = _isRevenueCatReady.asStateFlow()

    private val _isAdMobReady = MutableStateFlow(false)
    val isAdMobReady: StateFlow<Boolean> = _isAdMobReady.asStateFlow()

    private val _adCount = MutableStateFlow(0)
    val adCount: StateFlow<Int> = _adCount.asStateFlow()

    private val _lastAdAction = MutableStateFlow<String?>(null)
    val lastAdAction: StateFlow<String?> = _lastAdAction.asStateFlow()

    private val _isProUser = MutableStateFlow(false)
    val isProUser: StateFlow<Boolean> = _isProUser.asStateFlow()

    // State flow for in-app ad overlay (renders in Compose whenever an ad triggers)
    private val _activeInAppAd = MutableStateFlow<InAppAdModel?>(null)
    val activeInAppAd: StateFlow<InAppAdModel?> = _activeInAppAd.asStateFlow()

    private val sampleAds = listOf(
        InAppAdModel(
            title = "Hydrate Smarter with PureDrop",
            sponsor = "PureDrop Smart Tumblers",
            description = "Track electrolyte balance with sensor-enabled stainless steel water bottles. Keep drinks ice-cold for 36 hours.",
            actionText = "Shop PureDrop 20% Off",
            actionTag = "PureDrop",
            placement = "hydration_action"
        ),
        InAppAdModel(
            title = "Boost Hydration with LiquidPlus",
            sponsor = "LiquidPlus Electrolytes",
            description = "Zero-sugar hydration multiplier packs with essential vitamins and Himalayan minerals for instant recovery.",
            actionText = "Claim Free Sample Pack",
            actionTag = "LiquidPlus",
            placement = "hydration_action"
        ),
        InAppAdModel(
            title = "HydroCoach AI - Wellness Coach",
            sponsor = "HealthTrack Global",
            description = "Personalized hydration & wellness coaching directly on your smartwatch. Stay energized throughout your day.",
            actionText = "Install Free Trial",
            actionTag = "HydroCoach",
            placement = "hydration_action"
        )
    )

    fun initialize(context: Context) {
        val appContext = context.applicationContext

        // 1. Initialize RevenueCat SDK
        try {
            Purchases.logLevel = LogLevel.DEBUG
            val apiKey = try {
                BuildConfig.REVENUECAT_API_KEY.ifEmpty { "goog_placeholder_api_key" }
            } catch (e: Exception) {
                "goog_placeholder_api_key"
            }

            Purchases.configure(
                PurchasesConfiguration.Builder(appContext, apiKey)
                    .build()
            )
            _isRevenueCatReady.value = true
            Log.d(TAG, "RevenueCat configured successfully")

            // Check existing entitlements
            checkEntitlements()
        } catch (e: Exception) {
            Log.w(TAG, "RevenueCat init warning (using offline/demo mode): ${e.message}")
            _isRevenueCatReady.value = false
        }

        // 2. Initialize Google Mobile Ads SDK
        try {
            MobileAds.initialize(appContext) { initStatus ->
                _isAdMobReady.value = true
                Log.d(TAG, "AdMob initialized: $initStatus")
                loadInterstitialAd(appContext)
            }
        } catch (e: Exception) {
            Log.w(TAG, "AdMob initialization note: ${e.message}")
            _isAdMobReady.value = false
        }
    }

    private fun checkEntitlements() {
        try {
            Purchases.sharedInstance.getCustomerInfo(object : ReceiveCustomerInfoCallback {
                override fun onReceived(customerInfo: CustomerInfo) {
                    val hasPro = customerInfo.entitlements["pro"]?.isActive == true ||
                            customerInfo.entitlements["remove_ads"]?.isActive == true
                    _isProUser.value = hasPro
                    Log.d(TAG, "Customer info received. IsPro: $hasPro")
                }

                override fun onError(error: com.revenuecat.purchases.PurchasesError) {
                    Log.d(TAG, "Could not fetch customer info: ${error.message}")
                }
            })
        } catch (e: Exception) {
            Log.d(TAG, "checkEntitlements bypassed: ${e.message}")
        }
    }

    @OptIn(ExperimentalPreviewRevenueCatPurchasesAPI::class)
    fun loadInterstitialAd(context: Context) {
        if (isAdLoading || interstitialAd != null) return
        isAdLoading = true

        val adRequest = AdRequest.Builder().build()

        try {
            if (_isRevenueCatReady.value && Purchases.isConfigured) {
                // Use RevenueCat's loadAndTrackInterstitialAd method from purchases-admob
                Purchases.sharedInstance.adTracker.loadAndTrackInterstitialAd(
                    context = context,
                    adUnitId = TEST_INTERSTITIAL_AD_UNIT_ID,
                    adRequest = adRequest,
                    placement = "water_tracker_action",
                    loadCallback = object : InterstitialAdLoadCallback() {
                        override fun onAdLoaded(ad: InterstitialAd) {
                            interstitialAd = ad
                            isAdLoading = false
                            Log.d(TAG, "AdMob Interstitial loaded and tracked with RevenueCat")
                        }

                        override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                            interstitialAd = null
                            isAdLoading = false
                            Log.w(TAG, "AdMob Interstitial failed to load: ${loadAdError.message}")
                        }
                    }
                )
            } else {
                // Fallback standard AdMob load if RevenueCat isn't configured yet
                InterstitialAd.load(
                    context,
                    TEST_INTERSTITIAL_AD_UNIT_ID,
                    adRequest,
                    object : InterstitialAdLoadCallback() {
                        override fun onAdLoaded(ad: InterstitialAd) {
                            interstitialAd = ad
                            isAdLoading = false
                            Log.d(TAG, "Standard AdMob Interstitial loaded")
                        }

                        override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                            interstitialAd = null
                            isAdLoading = false
                            Log.w(TAG, "AdMob Interstitial failed to load: ${loadAdError.message}")
                        }
                    }
                )
            }
        } catch (e: Exception) {
            isAdLoading = false
            Log.w(TAG, "Error loading InterstitialAd: ${e.message}")
        }
    }

    /**
     * Trigger an ad whenever a user completes an action (e.g. logging water, changing goals, deleting logs).
     * If user is Pro/Subscriber, the ad is skipped.
     */
    @OptIn(ExperimentalPreviewRevenueCatPurchasesAPI::class)
    fun showAdOnAction(
        activity: Activity,
        actionDescription: String,
        onAdDismissed: () -> Unit = {}
    ) {
        if (_isProUser.value) {
            Log.d(TAG, "User has Pro entitlement; skipping ad for action: $actionDescription")
            onAdDismissed()
            return
        }

        _lastAdAction.value = actionDescription
        _adCount.value += 1

        val currentAd = interstitialAd
        if (currentAd != null) {
            currentAd.fullScreenContentCallback = object : FullScreenContentCallback() {
                override fun onAdDismissedFullScreenContent() {
                    interstitialAd = null
                    Log.d(TAG, "Ad dismissed by user")
                    loadInterstitialAd(activity)
                    onAdDismissed()
                }

                override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                    interstitialAd = null
                    Log.w(TAG, "Ad failed to show: ${adError.message}")
                    loadInterstitialAd(activity)
                    // Fallback to in-app ad presentation
                    showInAppVisualAd(actionDescription, onAdDismissed)
                }

                override fun onAdShowedFullScreenContent() {
                    Log.d(TAG, "AdMob Interstitial displayed for action: $actionDescription")
                }
            }

            try {
                // Display and track placement using RevenueCat's show extension
                currentAd.show(activity, placement = actionDescription)
            } catch (e: Exception) {
                Log.w(TAG, "Failed calling currentAd.show: ${e.message}")
                showInAppVisualAd(actionDescription, onAdDismissed)
            }
        } else {
            // If interstitial is still loading or device is running without Google Play Services,
            // show the rich In-App Ad banner/dialog so the user experiences the ad on action!
            showInAppVisualAd(actionDescription, onAdDismissed)
            loadInterstitialAd(activity)
        }
    }

    private fun showInAppVisualAd(actionDescription: String, onAdDismissed: () -> Unit) {
        val adIndex = (_adCount.value - 1).coerceAtLeast(0) % sampleAds.size
        val selectedAd = sampleAds[adIndex].copy(
            placement = actionDescription
        )
        _activeInAppAd.value = selectedAd
    }

    fun dismissCurrentInAppAd(onDismissed: () -> Unit = {}) {
        _activeInAppAd.value = null
        onDismissed()
    }

    fun toggleProStatus() {
        _isProUser.value = !_isProUser.value
    }
}
