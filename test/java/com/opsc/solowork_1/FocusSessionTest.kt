package com.opsc.solowork_1

import com.opsc.solowork_1.model.FocusSession
import org.junit.Test
import org.junit.Assert.*

class FocusSessionTest {

    @Test
    fun testGetFormattedDurationMinutesSeconds() {
        // Test 25 minutes, 30 seconds
        val session1 = FocusSession(duration = (25 * 60 + 30) * 1000L)
        assertEquals("25:30", session1.getFormattedDuration())

        // Test 5 minutes, 5 seconds
        val session2 = FocusSession(duration = (5 * 60 + 5) * 1000L)
        assertEquals("05:05", session2.getFormattedDuration())
    }

    @Test
    fun testGetFormattedDurationHours() {
        // Test 1 hour, 15 minutes, 20 seconds
        val session = FocusSession(duration = (1 * 3600 + 15 * 60 + 20) * 1000L)
        assertEquals("01:15:20", session.getFormattedDuration())

        // Test 2 hours, 0 minutes, 5 seconds
        val session2 = FocusSession(duration = (2 * 3600 + 5) * 1000L)
        assertEquals("02:00:05", session2.getFormattedDuration())
    }

    @Test
    fun testGetFormattedDurationZero() {
        // Test zero duration
        val session = FocusSession(duration = 0)
        assertEquals("00:00", session.getFormattedDuration())
    }

    @Test
    fun testGetFormattedDurationLessThanMinute() {
        // Test less than 1 minute
        val session = FocusSession(duration = 45 * 1000L) // 45 seconds
        assertEquals("00:45", session.getFormattedDuration())

        val session2 = FocusSession(duration = 5 * 1000L) // 5 seconds
        assertEquals("00:05", session2.getFormattedDuration())
    }
}