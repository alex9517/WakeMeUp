//  Created : 2021-Jul-05
// Modified : 2021-Aug-15

package net.proj27594.wakemeup;

import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;


public class ScheduledSignal {
    private static final String BAD_DATE_TIME = "1980-06-30 08:00";
    // Actually, there is nothing bad with the above date/time.
    // It is just supposed to signal to a user (or developer) that
    // some date/time is wrong, inappropriate for this application.

    private Integer id;
    private String wasSetAt;
    private String mustTriggerAt;
    private String signalName;

    ScheduledSignal(
            Integer id,
            String wasSetAt,
            String mustTriggerAt,
            String signalName) {
        this.id = id;
        this.wasSetAt = verifyAndFormatDateTime(wasSetAt);
        this.mustTriggerAt = verifyAndFormatDateTime(mustTriggerAt);
        this.signalName = signalName;
    }

    public int getId() {
        return id;
    }

    public String getIdAsString() {
        return Integer.toString(id);
    }

    public String getWasSetAt() {
        if (wasSetAt == null) return BAD_DATE_TIME;
        return wasSetAt;
    }

    public String getMustTriggerAt() {
        if (mustTriggerAt == null) return BAD_DATE_TIME;
        return mustTriggerAt;
    }

    public String getPlayTime() {
        // This is like getMustTriggerAt(), but
        // the format is slightly different (shorter).
        if (mustTriggerAt == null) return BAD_DATE_TIME;
        String[] datetime = mustTriggerAt.split("\\s+");
        if (datetime.length == 2) {
            return datetime[1] + " (" + datetime[0] + ")";
        }
        return mustTriggerAt;
    }

    public String getSignalName() {
        return signalName;
    }


    // NOTE!
    // The following static methods can be (and probably should be)
    // moved to AuxUtils.java, but I've decided to leave them here
    // because they are mostly used by the instances of this class.

    //////////////
    //
    // IS EXPIRED
    //
    //////////////

    public static boolean isExpired(String s) {
        // This method returns TRUE if the specified time is in the past,
        // otherwise it returns FALSE. It only accepts a text string like
        // "2020-06-12 08:20:30", though leading zeroes and seconds can be
        // omitted; missing seconds will be replaced with ':00'.
        // Here is a subtle point: if the specified date/time is exactly
        // LocalDateTime.now(), it is counted as 'expired' (return true).

        if (!(s == null || s.isEmpty())) {
            String[] datetime = s.split("\\s+");
            if (datetime != null && datetime.length == 2) {
                String date = formatDate(datetime[0]);
                if (date != null) {
                    String time = formatTime(datetime[1]);
                    if (time != null) {
                        String iso_datetime = date + "T" + time;
                        try {
                            if (LocalDateTime.parse(iso_datetime)
                                    .isAfter(LocalDateTime.now())) {
                                return false;
                            }
                        } catch (DateTimeParseException e) {
                        }
                    }
                }
            }
        }
        return true;
    }


    //////////////////////////////
    //
    // VERIFY AND FORMAT DATETIME
    //
    //////////////////////////////

    // This method accepts a text string like "2020-06-12 08:20:30"
    // (leading zeroes and seconds can be missing). It checks if the
    // supplied date/time is good and returns a slightly reformatted
    // string (or unmodified, if it was well formed at the beginning).
    // If the supplied date/time string is not good, the method returns
    // "1980-06-30 08:00".

    public static String verifyAndFormatDateTime(final String s) {
        if (isThisValidDateTime(s)) {
            String[] datetime = s.split("\\s+");
            return formatDate(datetime[0]) + " " + formatTime(datetime[1]);
        }
        return BAD_DATE_TIME;
    }


    //////////////////////////
    //
    // IS THIS VALID DATETIME
    //
    //////////////////////////

    // This method accepts a text string like "2020-06-12 08:20:30"
    // (leading zeroes and seconds can be omitted, but will be added auto),
    // splits it into date and time, roughly checks values, adjusts format,
    // creates ISO local date/time string (like "2020-06-12T08:20:30") and
    // tries to create a LocalDateTime (ultimate validity test).
    // Note that dates before 2001 are considered invalid!

    public static boolean isThisValidDateTime(String s) {
        if (!(s == null || s.isEmpty())) {
            String[] datetime = s.split("\\s+");
            if (datetime != null && datetime.length == 2) {
                String date = formatDate(datetime[0]);
                if (date == null) return false;
                String time = formatTime(datetime[1]);
                if (time == null) return false;
                String iso_datetime = date + "T" + time;
                try {
                    LocalDateTime.parse(iso_datetime);
                    return true;
                } catch (DateTimeParseException e) {
                }
            }
        }
        return false;
    }


    ///////////////
    //
    // FORMAT DATE
    //
    ///////////////

    // This method takes a string like "yyyy-MM-dd" and checks if months and
    // days have reasonable values, and year is more than 2000 (since this is
    // a "timer" app, dates of the previous years have no sense).
    // Also, days and months like "1", "6", etc, are replaced with "01", "06",
    // ... because date/time formatter wants exactly "MM" and "dd".

    // If the supplied string is null, empty, or it does not satisfy
    // requirements, this method returns null.

    public static String formatDate(String d) {
        if (!(d == null || d.isEmpty())) {
            String[] date = d.split("-");
            if (date.length == 3) {
                try {
                    int year = Integer.parseInt(date[0]);
                    int month = Integer.parseInt(date[1]);
                    int day = Integer.parseInt(date[2]);
                    if (year > 2000 && year < 3000 &&
                            month > 0 && month <= 12 && day > 0 && day < 32) {
                        return String.format("%4d-%02d-%02d", year, month, day);
                    }
                } catch (NumberFormatException e) {
                    // Just ignore it!
                }
            }
        }
        return null;
    }


    ///////////////
    //
    // FORMAT TIME
    //
    ///////////////

    // This method takes a string like "hh:mm" or "hh:mm:ss" and checks
    // is hours, minutes, seconds have reasonable values. Numbers like 1, 2,
    // ... are converted to "01", "02", etc. If seconds are missing, "00" is
    // added to the resulting string.

    // If the supplied string is null, empty, or it does not satisfy
    // requirements, this method returns null.

    public static String formatTime(String t) {
        if (!(t == null || t.isEmpty())) {
            try {
                String[] ct = t.split(":");
                if (ct.length >= 2) {
                    int hours = Integer.parseInt(ct[0]);
                    int minutes = Integer.parseInt(ct[1]);
                    int seconds = 0;
                    if (ct.length == 3) seconds = Integer.parseInt(ct[2]);
                    if (hours >= 0 && hours < 24 &&
                            minutes >= 0 && minutes < 60 && seconds >= 0 && seconds < 60) {
                        return String.format("%02d:%02d:%02d", hours, minutes, seconds);
                    }
                }
            } catch (NumberFormatException e) {
                // Ignore it!
            }
        }
        return null;
    }

}

// -END-
