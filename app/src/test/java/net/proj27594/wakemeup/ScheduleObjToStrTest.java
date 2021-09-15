//  Created : 2021-Jul-13
// Modified : 2021-Jul-21

package net.proj27594.wakemeup;

import org.junit.Test;
import java.lang.reflect.Type;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.ArrayList;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import net.proj27594.wakemeup.ScheduledSignal;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class ScheduleObjToStrTest {

    // It tests conversion of ArrayList<ScheduledSignal> to
    // a String and vice versa.

    private List<ScheduledSignal> scheduledSignals;

    public List<ScheduledSignal> getSchedule(String s) {
        // It converts JSON string to an array of objects.
        // This is similar to SetTimerActivity#getSchedule()
        // method without some parts!
        if (s == null || s.isEmpty()) {
            return new ArrayList<>();
        }
        // Convert the string from preferences to a list of objects.
        Gson gson = new Gson();
        Type scheduledSignalType = new TypeToken<List<ScheduledSignal>>() {}.getType();
        List<ScheduledSignal> list = gson.fromJson(s, scheduledSignalType);
        return list;
    }


    public String scheduleToJson() {
        // It converts a list of objects to a JSON String.
        // This is similar to SetTimerActivity#scheduleToJson()!

        if (scheduledSignals == null || scheduledSignals.size() == 0) {
            return "";
        }
        Gson gson = new Gson();
        return gson.toJson(scheduledSignals);
    }


    @Test
    public void testConversion() {

        scheduledSignals = new ArrayList<>();

        // Put some records to scheduledSignals array.

        scheduledSignals.add(new ScheduledSignal(
            1,
            "2021-07-12 6:30",
            "2021-07-12 07:00",
            "flute.mp3"
        ));

        scheduledSignals.add(new ScheduledSignal(
            2,
            "2021-07-12 07:15",
            "2021-07-12 07:20",
            "bell.mp3"
        ));


        scheduledSignals.add(new ScheduledSignal(
            4,
            "2021-07-12 11:10",
            "2021-07-12 15:55",
            "bell.mp3"
        ));

        assertEquals(3, scheduledSignals.size());

        // Convert array to a string.

        final String strSchedule = scheduleToJson();

        assertNotNull(strSchedule);
        assertTrue(strSchedule.length() > 20);

        List<ScheduledSignal> scheduledSignalsFromString = getSchedule(strSchedule);

        assertEquals(3, scheduledSignalsFromString.size());

        int count = 0;

        for (ScheduledSignal item : scheduledSignalsFromString) {
            if (item.getId() == 1) {
                assertTrue(item.getSignalName().equals("flute.mp3"));
                assertTrue(item.getMustTriggerAt().equals("2021-07-12 07:00"));
                count++;
            }
            if (item.getId() == 2) {
                assertTrue(item.getSignalName().equals("bell.mp3"));
                assertTrue(item.getPlayTime().equals("07:20 (2021-07-12)"));
                count++;
            }
            if (item.getId() == 4) {
                assertTrue(item.getSignalName().equals("bell.mp3"));
                count++;
            }
        }
        assertEquals(3, count);
    }


    @Test
    public void testDataTimeToStr() {

        String s1 = "2021-07-12T10:30:00";
        String s2 = "2021-07-12T11:00:00";

        LocalDateTime date1 = LocalDateTime.parse(s1);
        LocalDateTime date2 = LocalDateTime.parse(s2);

        String s1_after = date1.format(DateTimeFormatter.ofPattern("yyyy-MM-dd hh:mm"));
        assertTrue(s1_after.equals("2021-07-12 10:30"));

        String s2_after = date2.format(DateTimeFormatter.ofPattern("yyyy-MM-dd hh:mm"));
        assertTrue(s2_after.equals("2021-07-12 11:00"));
/*
        String s3 = "2021-07-12 10:30";
        String s4 = "2021-07-12 11:00";

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd hh:mm");

        LocalDateTime date3 = LocalDateTime.parse(s1, formatter);
        LocalDateTime date4 = LocalDateTime.parse(s2, formatter);

        String s3_after = date3.format(DateTimeFormatter.ofPattern("yyyy-MM-dd hh:mm"));
        assertTrue(s3_after.equals("2021-07-12 10:30"));

        String s4_after = date4.format(DateTimeFormatter.ofPattern("yyyy-MM-dd hh:mm"));
        assertTrue(s4_after.equals("2021-07-12 11:00"));
*/
    }


    @Test
    public void testFormatDate() {
        String date1 = "2021-07-20";
        String date2 = "2021-7-12";
        String date3 = "2021-7-9";
        String date4 = "2000-06-01";
        String date5 = "2020-Jun-01";
        String date6 = "Jun 1, 2020";

        assertTrue(date1.equals(ScheduledSignal.formatDate(date1)));
        assertTrue("2021-07-12".equals(ScheduledSignal.formatDate(date2)));
        assertTrue("2021-07-09".equals(ScheduledSignal.formatDate(date3)));
        assertNull(ScheduledSignal.formatDate(date4));
        assertNull(ScheduledSignal.formatDate(date5));
        assertNull(ScheduledSignal.formatDate(date6));
    }


    @Test
    public void testFormatTime() {
        String time1 = "08:10:20";
        String time2 = "8:30:5";
        String time3 = "8:20";
        String time4 = "8h10m";
        String time5 = "08 20 30";
        String time6 = "08-20-30";

        assertTrue(time1.equals(ScheduledSignal.formatTime(time1)));
        assertTrue("08:30:05".equals(ScheduledSignal.formatTime(time2)));
        assertTrue("08:20:00".equals(ScheduledSignal.formatTime(time3)));
        assertNull(ScheduledSignal.formatTime(time4));
        assertNull(ScheduledSignal.formatTime(time5));
        assertNull(ScheduledSignal.formatTime(time6));
    }

}

// -END-
