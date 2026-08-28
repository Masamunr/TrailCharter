package com.masamunr.trailcharter.privacy

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Test

class PrivacyStatusTest {
    @Test
    fun foundationDefaultsArePrivacyPreserving() {
        val status = PrivacyStatus.FoundationDefault

        assertFalse(status.locationTrackingEnabled)
        assertEquals(NetworkState.Disabled, status.networkState)
        assertFalse(status.cloudBackupEnabled)
    }
}
