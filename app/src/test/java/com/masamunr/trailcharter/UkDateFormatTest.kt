package com.masamunr.trailcharter

import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.LocalDate

class UkDateFormatTest {
    @Test
    fun formatsDatesInUkOrder() {
        val epochDay = LocalDate.of(2026, 9, 5).toEpochDay()

        assertEquals("05/09/2026", formatEpochDay(epochDay))
    }

    @Test
    fun formatsDateRangesInUkOrder() {
        val start = LocalDate.of(2026, 9, 5).toEpochDay()
        val end = LocalDate.of(2026, 9, 12).toEpochDay()

        assertEquals("05/09/2026 to 12/09/2026", formatDateRange(start, end))
    }

    @Test
    fun calendarMillisRoundTripWithoutChangingDay() {
        val epochDay = LocalDate.of(2026, 12, 31).toEpochDay()

        assertEquals(epochDay, utcMillisToEpochDay(epochDayToUtcMillis(epochDay)))
    }
}
