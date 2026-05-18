package com.juren233.usagenotify.notification

import org.junit.Assert.assertEquals
import org.junit.Test

class LiveUpdateNotificationManagerTest {

    @Test
    fun calculateBalanceProgressUsesDistanceFromSufficientToEmpty() {
        val progress = LiveUpdateNotificationManager.calculateBalanceProgress(
            totalBalance = 7.5,
            thresholds = BalanceThresholds(
                sufficient = 10.0,
                danger = 2.5,
                insufficient = 1.0,
            ),
        )

        assertEquals(250, progress)
    }

    @Test
    fun calculateBalanceProgressStartsAtZeroWhenBalanceIsSufficient() {
        val progress = LiveUpdateNotificationManager.calculateBalanceProgress(
            totalBalance = 25.0,
            thresholds = BalanceThresholds(
                sufficient = 10.0,
                danger = 2.5,
                insufficient = 1.0,
            ),
        )

        assertEquals(0, progress)
    }

    @Test
    fun calculateBalanceProgressEndsAtEmptyWhenBalanceIsNegative() {
        val progress = LiveUpdateNotificationManager.calculateBalanceProgress(
            totalBalance = -1.0,
            thresholds = BalanceThresholds(
                sufficient = 10.0,
                danger = 2.5,
                insufficient = 1.0,
            ),
        )

        assertEquals(1000, progress)
    }

    @Test
    fun normalizeThresholdsKeepsAscendingSegments() {
        val thresholds = BalanceThresholds(
            sufficient = 0.5,
            danger = 8.0,
            insufficient = 2.0,
        ).normalized()

        assertEquals(8.0, thresholds.sufficient, 0.0)
        assertEquals(2.0, thresholds.danger, 0.0)
        assertEquals(0.5, thresholds.insufficient, 0.0)
    }

    @Test
    fun segmentLengthsUseConfiguredNodes() {
        val segments = LiveUpdateNotificationManager.calculateSegmentLengths(
            BalanceThresholds(
                sufficient = 10.0,
                danger = 2.5,
                insufficient = 1.0,
            ),
        )

        assertEquals(listOf(750, 150, 100), segments)
    }

    @Test
    fun pointPositionsIncludeZeroEndpointAndConfiguredNodes() {
        val points = LiveUpdateNotificationManager.calculatePointPositions(
            BalanceThresholds(
                sufficient = 10.0,
                danger = 2.5,
                insufficient = 1.0,
            ),
        )

        assertEquals(listOf(0, 750, 900, 1000), points)
    }
}
