package com.jarapplication.kiranastore.feature_transactions.util;

import java.util.Calendar;

public class DateUtil {

    /**
     * to get how many milli sec left for end of the min
     *
     * @return
     */
    public static long getEndOfMinute() {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        calendar.add(Calendar.MINUTE, 1);
        long currentTimeMillis = System.currentTimeMillis();
        return calendar.getTimeInMillis() - currentTimeMillis;
    }
}
