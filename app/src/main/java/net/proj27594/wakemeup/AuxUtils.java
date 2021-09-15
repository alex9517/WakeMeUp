//  Created : 2018-Dec-24
// Modified : 2021-Aug-24

package net.proj27594.wakemeup;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

public class AuxUtils {

    private static final String TAG = "AuxUtils";

    ////////////////////
    //
    // MAKE TIME STRING
    //
    ////////////////////

    public static String makeTimeString(String hh, String mm, String ss) {

        if (hh == null || mm == null || ss == null) return "";

        try {
            int h = Integer.parseInt(hh);
            int m = Integer.parseInt(mm);
            int s = Integer.parseInt(ss);

            if ((s < 0) || (m < 0) || (h < 0))
                throw new IllegalArgumentException(
                        "makeTimeString(): at least one argument is negative");

            int i = s / 60;
            s = s % 60;
            if (i >= 1) m = m + i;

            i = m / 60;
            m = m % 60;
            if (i >= 1) h = h + i;

            if (h >= 100) return "100:00";

            DecimalFormat df = new DecimalFormat("00");
            StringBuffer timeStr = new StringBuffer();
            String hours = df.format(h);
            timeStr.append(hours);
            timeStr.append(":");
            String minutes = df.format(m);
            timeStr.append(minutes);

            if (s > 0) {
                String seconds = df.format(s);
                timeStr.append(":");
                timeStr.append(seconds);
            }
            return timeStr.toString();
        } catch (IllegalArgumentException e) {
        }
        return "";
    }


    /////////////////////
    //
    // PARSE TIME STRING
    //
    /////////////////////

    public static int[] parseTimeString(String t)
            throws NumberFormatException, IllegalArgumentException {

        // String 't' must be like '10 mm' or '20 min', or '5 minutes',
        // or '1 hour', or '6 hours', or '7:30', ...

        int i;
        int h = 0;
        int m = 0;
        int s = 0;

        if (t == null || t.isEmpty())
            throw new IllegalArgumentException("\"t\" is null or empty");

        if (t.matches("^[0-9]{1,3}\\s*(?i)(mm|min|minute).*")
                && (!t.contains(":"))) {
            String[] buff = t.split("\\s*[mM]");
            m = Integer.parseInt(buff[0]);
            i = m / 60;
            m = m % 60;
            if (i >= 1) h = i;
            return new int[]{h, m, s};
        } else if (t.matches("^[0-9]{1,3}\\s*(?i)(h|hh|hour).*")
                && (!t.contains(":"))) {
            String[] buff = t.split("\\s*[hH]");
            h = Integer.parseInt(buff[0]);
            return new int[]{h, m, s};
        } else if (t.matches("^[0-9]{1,3}:[0-9]{1,2}$")) {
            String[] buff = t.split(":");

            if (buff.length == 2) {
                h = Integer.parseInt(buff[0]);
                m = Integer.parseInt(buff[1]);
                i = m / 60;
                m = m % 60;
                if (i >= 1) h = h + i;
                return new int[]{h, m, s};
            }
        } else if (t.matches("^[0-9]{1,3}:[0-9]{1,2}:[0-9]{1,2}$")) {
            String[] buff = t.split(":");

            if (buff.length == 3) {
                h = Integer.parseInt(buff[0]);
                m = Integer.parseInt(buff[1]);
                s = Integer.parseInt(buff[2]);

                i = s / 60;
                s = s % 60;
                if (i >= 1) m = m + i;

                i = m / 60;
                m = m % 60;
                if (i >= 1) h = h + i;
                return new int[]{h, m, s};
            }
        }
        throw new IllegalArgumentException("\"t\" - bad format");
    }


    /////////////////////
    //
    // BREAK TIME STRING
    //
    /////////////////////

    public static String[] breakTimeString(String t) {

        try {
            int[] temp = parseTimeString(t);
            String[] timeArr = new String[3];
            DecimalFormat df = new DecimalFormat("00");
            timeArr[0] = df.format(temp[0]);
            timeArr[1] = df.format(temp[1]);
            timeArr[2] = df.format(temp[2]);
            return timeArr;
        } catch (Exception e) {
        }
        return new String[]{"00", "00", "00"};
    }


    //////////////
    //
    // GET MILLIS
    //
    //////////////

    public static long getMillis(String t) {

        if (t == null || t.isEmpty()) {
            throw new IllegalArgumentException(
                    TAG + ".getMillis(): arg is null or empty string.");
        }
        final int millisInOneSec = 1000;
        final int secInOneHour = 3600;
        final int secInOneMinute = 60;

        try {
            int[] temp = parseTimeString(t);
            return ((temp[0] * secInOneHour + temp[1] * secInOneMinute + temp[2]) * millisInOneSec);
        } catch (Exception e) {
            // Log.d(TAG, "getMillis(): " + e.getMessage());
            throw e;
        }
    }


    /////////////////////
    //
    // GET FAKE SCHEDULE
    //
    /////////////////////

    // This is only used for debugging!

    public static String getFakeSchedule() {
        List<ScheduledSignal> scheduledSignals = new ArrayList<>();

        // Put some records to scheduledSignals array.

        ScheduledSignal s1 = new ScheduledSignal(
                1,
                "2021-07-12 06:30",
                "2021-07-12 07:00",
                "flute.mp3"
        );
        scheduledSignals.add(s1);

        ScheduledSignal s2 = new ScheduledSignal(
                2,
                "2021-07-12 8:15",
                "2021-07-12 8:20",
                "bell.mp3"
        );
        scheduledSignals.add(s2);

        ScheduledSignal s3 = new ScheduledSignal(
                4,
                "2021-07-12 11:10",
                "2021-07-12 15:55",
                "bell.mp3"
        );
        scheduledSignals.add(s3);

        String jsonStr = SetTimerActivity.scheduleToJson(scheduledSignals);

        if (jsonStr != null) {
            //Toast.makeText(getApplicationContext(), "Schedule: " + jsonStr,
            //      Toast.LENGTH_SHORT).show();
            return jsonStr;
        }
        return "";
    }

}

// -END-
