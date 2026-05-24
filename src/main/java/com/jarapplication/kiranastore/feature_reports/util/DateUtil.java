package com.jarapplication.kiranastore.feature_reports.util;

import java.util.Calendar;
import java.util.Date;

/**
 * DATE UTIL (Reports): Date Range Calculator for Report Queries
 *
 * WHAT IT DOES:
 * ├─ Calculates precise start/end Date objects for report date ranges
 * ├─ Supports: day, week, month, and year boundaries
 * └─ Used by ReportDao to construct BETWEEN queries for PostgreSQL
 *
 * WHY IT'S NEEDED:
 * ├─ SQL BETWEEN queries need exact start/end timestamps
 * ├─ Start of day: 00:00:00.000 (midnight)
 * ├─ End of day: 23:59:59.999 (just before midnight)
 * ├─ Without precise boundaries: Transactions at 00:00:00 or 23:59:59 might be missed
 * └─ Reusability: Same boundary logic reused for week, month, year ranges
 *
 * DIFFERENT FROM feature_transactions/util/DateUtil:
 * ├─ This DateUtil: Date range boundaries (Date objects) → for SQL BETWEEN queries
 * └─ Transaction DateUtil: Cache TTL in milliseconds → for Redis expiration
 *
 * CALENDAR API (java.util.Calendar):
 * ├─ 0-based months: January=0, February=1, ..., December=11
 * │   └─ getStartOfWeek/getEndOfWeek adjusts with (month - 1) for external 1-based input
 * │   └─ getStartOfMonth/getEndOfMonth uses 0-based directly (ReportService passes Calendar.MONTH)
 * ├─ Calendar.DAY_OF_WEEK: Sunday=1, Monday=2, ..., Saturday=7
 * ├─ Calendar.WEEK_OF_MONTH: Which week of the month (1-5)
 * └─ Calendar.getActualMaximum(): Gets the actual max for that field (e.g., 28/29/30/31 for DAY_OF_MONTH)
 *
 * PRECISION STRATEGY:
 * ├─ Start of period: 00:00:00.000 (first millisecond of the first day)
 * ├─ End of period: 23:59:59.999 (last millisecond of the last day)
 * └─ This ensures complete coverage with BETWEEN (inclusive on both ends)
 *
 * NOTE: Uses legacy java.util.Calendar/Date API.
 *       Modern alternative: java.time.LocalDate, ZonedDateTime (Java 8+)
 *       Benefits of modern API: Immutable, thread-safe, cleaner API, timezone-aware
 */
public class DateUtil {
    /**
     * Returns the very beginning of the given date (00:00:00.000).
     *
     * EXAMPLE: Input: 2024-05-24 14:30:45.123 → Output: 2024-05-24 00:00:00.000
     *
     * @param date ← Any date/time within the target day
     * @return Date set to midnight (start of day)
     */
    public static Date getStartOfDay(Date date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.set(Calendar.HOUR_OF_DAY, 0);   // ← 12 AM
        calendar.set(Calendar.MINUTE, 0);          // ← 0 minutes
        calendar.set(Calendar.SECOND, 0);          // ← 0 seconds
        calendar.set(Calendar.MILLISECOND, 0);     // ← 0 milliseconds
        return calendar.getTime();
    }

    /**
     * Returns the very end of the given date (23:59:59.999).
     *
     * EXAMPLE: Input: 2024-05-24 14:30:45.123 → Output: 2024-05-24 23:59:59.999
     *
     * @param date ← Any date/time within the target day
     * @return Date set to last millisecond of the day
     */
    public static Date getEndOfDay(Date date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.set(Calendar.HOUR_OF_DAY, 23);   // ← 11 PM
        calendar.set(Calendar.MINUTE, 59);         // ← 59 minutes
        calendar.set(Calendar.SECOND, 59);         // ← 59 seconds
        calendar.set(Calendar.MILLISECOND, 999);   // ← 999 milliseconds
        return calendar.getTime();
    }

    /**
     * Returns the start of a specific week in a month (Sunday 00:00:00.000).
     *
     * EXAMPLE: getStartOfWeek(2, 5, 2024) → Sunday of 2nd week of May 2024 at midnight
     *
     * @param weekNumber ← Week number within the month (1-5)
     * @param month      ← Month (1-based: 1=Jan, 12=Dec; adjusted to 0-based internally)
     * @param year       ← Year (e.g., 2024)
     * @return Date representing start of the specified week
     */
    public static Date getStartOfWeek(int weekNumber, int month, int year) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.YEAR, year);
        calendar.set(Calendar.MONTH, month - 1);         // ← Convert 1-based to 0-based
        calendar.set(Calendar.WEEK_OF_MONTH, weekNumber); // ← Which week in the month
        calendar.set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY); // ← Start of week = Sunday
        return getStartOfDay(calendar.getTime());
    }

    /**
     * Returns the end of a specific week in a month (Saturday 23:59:59.999).
     *
     * @param weekNumber ← Week number within the month (1-5)
     * @param month      ← Month (1-based)
     * @param year       ← Year (e.g., 2024)
     * @return Date representing end of the specified week
     */
    public static Date getEndOfWeek(int weekNumber, int month, int year) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.YEAR, year);
        calendar.set(Calendar.MONTH, month - 1); // 0-based index
        calendar.set(Calendar.WEEK_OF_MONTH, weekNumber);
        calendar.set(Calendar.DAY_OF_WEEK, Calendar.SATURDAY); // ← End of week = Saturday
        return getEndOfDay(calendar.getTime());
    }

    /**
     * Returns the first moment of a given month (1st day at 00:00:00.000).
     *
     * EXAMPLE: getStartOfMonth(4, 2024) → 2024-05-01 00:00:00.000 (May, 0-based month=4)
     *
     * @param month ← Month (0-based: 0=Jan, 11=Dec, from Calendar.MONTH)
     * @param year  ← Year (e.g., 2024)
     * @return Date representing start of the specified month
     */
    public static Date getStartOfMonth(int month, int year) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.YEAR, year);
        calendar.set(Calendar.MONTH, month);          // ← 0-based (from Calendar.MONTH)
        calendar.set(Calendar.DAY_OF_MONTH, 1);       // ← First day of month
        return getStartOfDay(calendar.getTime());
    }

    /**
     * Returns the last moment of a given month (last day at 23:59:59.999).
     *
     * Uses Calendar.getActualMaximum() to handle variable month lengths:
     * ├─ January → 31, February → 28 or 29 (leap year), April → 30, etc.
     *
     * @param month ← Month (0-based: 0=Jan, 11=Dec)
     * @param year  ← Year (e.g., 2024)
     * @return Date representing end of the specified month
     */
    public static Date getEndOfMonth(int month, int year) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.YEAR, year);
        calendar.set(Calendar.MONTH, month);
        calendar.set(Calendar.DAY_OF_MONTH, calendar.getActualMaximum(Calendar.DAY_OF_MONTH)); // ← Last day
        return getEndOfDay(calendar.getTime());
    }

    /**
     * Returns the first moment of a given year (January 1st at 00:00:00.000).
     *
     * @param year ← Year (e.g., 2024)
     * @return Date representing start of the specified year
     */
    public static Date getStartOfYear(int year) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.YEAR, year);
        calendar.set(Calendar.DAY_OF_YEAR, 1); // ← January 1st
        return getStartOfDay(calendar.getTime());
    }

    /**
     * Returns the last moment of a given year (December 31st at 23:59:59.999).
     *
     * Uses getActualMaximum(DAY_OF_YEAR) to handle leap years:
     * ├─ Normal year → 365
     * └─ Leap year → 366
     *
     * @param year ← Year (e.g., 2024)
     * @return Date representing end of the specified year
     */
    public static Date getEndOfYear(int year) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.YEAR, year);
        calendar.set(Calendar.DAY_OF_YEAR, calendar.getActualMaximum(Calendar.DAY_OF_YEAR)); // ← Dec 31st
        return getEndOfDay(calendar.getTime());
    }
}
