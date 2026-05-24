package com.jarapplication.kiranastore.feature_transactions.util;

import java.util.Calendar;

/**
 * DATE UTIL: TTL Calculator for Redis Cache Expiration
 *
 * WHAT IT DOES:
 * ├─ Calculates how many milliseconds are left until the end of the current minute
 * └─ Used by ConversionServiceImp to set Redis cache TTL (time-to-live)
 *
 * WHY IT'S NEEDED:
 * ├─ Exchange rates should refresh at regular intervals (every minute)
 * ├─ Cache TTL aligned to minute boundaries → all users see same rate within a minute
 * ├─ Dynamic TTL: If called at 10:30:45 → TTL = 15 seconds (until 10:31:00)
 * │   └─ If called at 10:30:02 → TTL = 58 seconds (until 10:31:00)
 * └─ Result: Cache naturally expires at minute boundary → next request triggers fresh API call
 *
 * EXAMPLE:
 * ├─ Current time: 10:30:45.300
 * ├─ End of minute: 10:31:00.000
 * ├─ getEndOfMinute() returns: 14700 ms (14.7 seconds until next minute)
 * └─ Redis sets: TTL = 14700ms → key expires at 10:31:00
 *
 * USAGE:
 * ├─ ConversionServiceImp.calculate():
 * │   cacheService.setValueToRedis("USD_INR", "0.012", DateUtil.getEndOfMinute());
 * └─ Redis key "USD_INR" auto-expires at end of minute → next request recalculates
 *
 * NOTE: This is different from feature_reports/util/DateUtil.java
 *       - This DateUtil: TTL calculation for Redis caching (milliseconds)
 *       - Reports DateUtil: Date range calculation for report queries (Date objects)
 */
public class DateUtil {

    /**
     * Calculates milliseconds remaining until the start of the next minute.
     *
     * ALGORITHM:
     * ├─ 1. Get current time in Calendar
     * ├─ 2. Set seconds and milliseconds to 0 (start of current minute)
     * ├─ 3. Add 1 minute → this is the "end of current minute" / "start of next minute"
     * ├─ 4. Subtract current time → remaining milliseconds
     * └─ Returns: number of milliseconds until next minute boundary
     *
     * @return Milliseconds until end of current minute (used as Redis TTL)
     */
    public static long getEndOfMinute() {
        Calendar calendar = Calendar.getInstance();   // ← Current time
        calendar.set(Calendar.SECOND, 0);             // ← Zero out seconds
        calendar.set(Calendar.MILLISECOND, 0);        // ← Zero out milliseconds
        calendar.add(Calendar.MINUTE, 1);             // ← Advance to next minute boundary
        long currentTimeMillis = System.currentTimeMillis(); // ← Current time in millis
        return calendar.getTimeInMillis() - currentTimeMillis; // ← Remaining time until next minute
    }
}
