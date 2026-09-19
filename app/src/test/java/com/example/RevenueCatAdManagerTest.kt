package com.example

import com.example.monetization.RevenueCatAdManager
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class RevenueCatAdManagerTest {

    @Test
    fun `initial states and pro toggling work as expected`() {
        // Toggle pro state
        val initialPro = RevenueCatAdManager.isProUser.value
        RevenueCatAdManager.toggleProStatus()
        assertEquals(!initialPro, RevenueCatAdManager.isProUser.value)

        // Toggle back to false for testing
        if (RevenueCatAdManager.isProUser.value) {
            RevenueCatAdManager.toggleProStatus()
        }
        assertFalse(RevenueCatAdManager.isProUser.value)
    }

    @Test
    fun `dismiss in-app ad resets active ad`() {
        RevenueCatAdManager.dismissCurrentInAppAd()
        assertEquals(null, RevenueCatAdManager.activeInAppAd.value)
    }
}
