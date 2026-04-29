package com.jarapplication.kiranastore.feature_reports.util;

import java.util.Calendar;
import java.util.Date;

public class DateUtil {
    /**
     * Gets the exact start of the date
     *
     * @param date
     * @return
     */
    public static Date getStartOfDay(Date date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        return calendar.getTime();
    }

    /**
     * Gets the exact end of the date
     *
     * @param date
     * @return
     */
    public static Date getEndOfDay(Date date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.set(Calendar.HOUR_OF_DAY, 23);
        calendar.set(Calendar.MINUTE, 59);
        calendar.set(Calendar.SECOND, 59);
        calendar.set(Calendar.MILLISECOND, 999);
        return calendar.getTime();
    }

    /**
     * Gets the exact start of the week
     *
     * @param weekNumber
     * @param month
     * @param year
     * @return
     */
    public static Date getStartOfWeek(int weekNumber, int month, int year) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.YEAR, year);
        calendar.set(Calendar.MONTH, month - 1);
        calendar.set(Calendar.WEEK_OF_MONTH, weekNumber);
        calendar.set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY);
        return getStartOfDay(calendar.getTime());
    }

    /**
     * Gets the exact end of the week
     *
     * @param weekNumber
     * @param month
     * @param year
     * @return
     */
    public static Date getEndOfWeek(int weekNumber, int month, int year) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.YEAR, year);
        calendar.set(Calendar.MONTH, month - 1); // 0-based index
        calendar.set(Calendar.WEEK_OF_MONTH, weekNumber);
        calendar.set(Calendar.DAY_OF_WEEK, Calendar.SATURDAY);
        return getEndOfDay(calendar.getTime());
    }

    /**
     * Gets the exact start of the month
     *
     * @param month
     * @param year
     * @return
     */
    public static Date getStartOfMonth(int month, int year) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.YEAR, year);
        calendar.set(Calendar.MONTH, month);
        calendar.set(Calendar.DAY_OF_MONTH, 1);
        return getStartOfDay(calendar.getTime());
    }

    /**
     * gets the exact end of the month
     *
     * @param month
     * @param year
     * @return
     */
    public static Date getEndOfMonth(int month, int year) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.YEAR, year);
        calendar.set(Calendar.MONTH, month);
        calendar.set(Calendar.DAY_OF_MONTH, calendar.getActualMaximum(Calendar.DAY_OF_MONTH));
        return getEndOfDay(calendar.getTime());
    }

    /**
     * gets the exact start of the year
     *
     * @param year
     * @return
     */
    public static Date getStartOfYear(int year) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.YEAR, year);
        calendar.set(Calendar.DAY_OF_YEAR, 1);
        return getStartOfDay(calendar.getTime());
    }

    /**
     * gets the exact end of the year
     *
     * @param year
     * @return
     */
    public static Date getEndOfYear(int year) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.YEAR, year);
        calendar.set(Calendar.DAY_OF_YEAR, calendar.getActualMaximum(Calendar.DAY_OF_YEAR));
        return getEndOfDay(calendar.getTime());
    }
}
