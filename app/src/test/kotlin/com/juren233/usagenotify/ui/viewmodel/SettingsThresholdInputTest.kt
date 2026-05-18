package com.juren233.usagenotify.ui.viewmodel

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class SettingsThresholdInputTest {

    @Test
    fun formatThresholdInputRemovesTrailingZeros() {
        assertEquals("10", formatThresholdInput(10.0))
        assertEquals("2.5", formatThresholdInput(2.5))
        assertEquals("1.25", formatThresholdInput(1.25))
    }

    @Test
    fun parseThresholdInputAllowsEmptyEditingState() {
        assertNull(parseThresholdInput(""))
        assertNull(parseThresholdInput("   "))
    }

    @Test
    fun parseThresholdInputAcceptsDecimalText() {
        assertEquals(0.5, parseThresholdInput(".5") ?: -1.0, 0.0)
        assertEquals(12.34, parseThresholdInput("12.34") ?: -1.0, 0.0)
    }

    @Test
    fun parseThresholdInputRejectsNegativeAndInvalidText() {
        assertNull(parseThresholdInput("-1"))
        assertNull(parseThresholdInput("abc"))
    }
}
