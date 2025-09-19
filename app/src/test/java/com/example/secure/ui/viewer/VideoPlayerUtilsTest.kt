package com.example.secure.ui.viewer

import org.junit.Test
import org.junit.Assert.*

class VideoPlayerUtilsTest {

    @Test
    fun formatTime_handlesNegativeTime() {
        assertEquals("00:00", formatTime(-1000))
    }

    @Test
    fun formatTime_handlesZeroTime() {
        assertEquals("00:00", formatTime(0))
    }

    @Test
    fun formatTime_handlesSecondsOnly() {
        assertEquals("00:30", formatTime(30000)) // 30 seconds
    }

    @Test
    fun formatTime_handlesMinutesAndSeconds() {
        assertEquals("02:30", formatTime(150000)) // 2 minutes 30 seconds
    }

    @Test
    fun formatTime_handlesHoursMinutesSeconds() {
        assertEquals("01:30:45", formatTime(5445000)) // 1 hour 30 minutes 45 seconds
    }

    @Test
    fun formatTime_handlesExactHour() {
        assertEquals("01:00:00", formatTime(3600000)) // 1 hour exactly
    }

    @Test
    fun formatTime_handlesLargeValues() {
        assertEquals("10:00:00", formatTime(36000000)) // 10 hours
    }
}