//  Created : 2018-Dec-21
// Modified : 2021-Aug-24

package net.proj27594.wakemeup;

import org.junit.Test;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static net.proj27594.wakemeup.AuxUtils.*;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class TimeStringUnitTest {

    @Test
    public void makeTimeStringNullArg() {
        assertEquals("", makeTimeString(null, "00", "00"));
        assertEquals("", makeTimeString(null, null, "00"));
        assertEquals("", makeTimeString(null, null, null));
    }

    @Test
    public void makeTimeStringNegativeArg() {
        assertEquals("", makeTimeString("6", "-9", "30"));
        assertEquals("", makeTimeString("-6", "0", "0"));
        assertEquals("", makeTimeString("0", "1", "-1"));
    }

    @Test
    public void makeTimeStringBadNumFormat() {
        assertEquals("", makeTimeString("6h", "0", "0"));
        assertEquals("", makeTimeString("6", "min", "0"));
        assertEquals("", makeTimeString("00", "00", "Sec"));
    }

    @Test
    public void makeTimeStringGood1() {
        assertEquals("06:20:30", makeTimeString("6", "20", "30"));
        assertEquals("100:00", makeTimeString("100", "0", "0"));
        assertEquals("100:00", makeTimeString("100", "1", "0"));
        assertEquals("100:00", makeTimeString("100", "0", "1"));
        assertEquals("100:00", makeTimeString("99", "90", "300"));
    }

    @Test
    public void makeTimeStringGood2() {

        Map<String, String[]> testData = new HashMap<>();

        testData.put("00:00", new String[]{"0", "0", "0"});
        testData.put("00:00:01", new String[]{"0", "0", "1"});
        testData.put("00:10", new String[]{"0", "0", "600"});
        testData.put("00:15:02", new String[]{"0", "15", "2"});
        testData.put("21:43:20", new String[]{"20", "100", "200"});
        testData.put("00:45", new String[]{"0", "45", "0"});
        testData.put("08:10:25", new String[]{"008", "010", "025"});
        testData.put("02:30", new String[]{"01", "90", "00"});
        testData.put("01:30", new String[]{"0", "90", "0"});
        testData.put("00:33", new String[]{"0", "33", "0"});
        testData.put("100:00", new String[]{"98", "0600", "0300"});
        testData.put("48:20", new String[]{"048", "20", "0"});

        for (Map.Entry<String, String[]> entry : testData.entrySet()) {
            String key = entry.getKey();
            String[] values = entry.getValue();
            assertEquals(key, makeTimeString(values[0], values[1], values[2]));
        }
    }

    @Test
    public void breakTimeStringGood1() {
        String[] buff = breakTimeString("");
        assertEquals("00", buff[0]);
        assertEquals("00", buff[1]);
        assertEquals("00", buff[2]);

        buff = breakTimeString(null);
        assertEquals("00", buff[0]);
        assertEquals("00", buff[1]);
        assertEquals("00", buff[2]);

        buff = breakTimeString("20 minutes");
        assertEquals("00", buff[0]);
        assertEquals("20", buff[1]);
        assertEquals("00", buff[2]);
    }

    @Test
    public void breakTimeStringGood2() {

        Map<String, String[]> testData = new HashMap<>();

        testData.put("20mm", new String[]{"00", "20", "00"});
        testData.put("22 min", new String[]{"00", "22", "00"});
        testData.put("1h", new String[]{"01", "00", "00"});
        testData.put("2 h", new String[]{"02", "00", "00"});
        testData.put("4 hh", new String[]{"04", "00", "00"});
        testData.put("30 MM", new String[]{"00", "30", "00"});
        testData.put("33 mm", new String[]{"00", "33", "00"});
        testData.put("25Mm", new String[]{"00", "25", "00"});
        testData.put("5 minutes", new String[]{"00", "05", "00"});
        testData.put("7 hours", new String[]{"07", "00", "00"});
        testData.put("00:45", new String[]{"00", "45", "00"});
        testData.put("6:15", new String[]{"06", "15", "00"});
        testData.put("1:1:1", new String[]{"01", "01", "01"});
        testData.put("2:5:45", new String[]{"02", "05", "45"});
        testData.put("2p:5", new String[]{"00", "00", "00"});
        testData.put("2h:20m", new String[]{"00", "00", "00"});
        testData.put("20", new String[]{"00", "00", "00"});
        testData.put("30 M", new String[]{"00", "00", "00"});

        for (Map.Entry<String, String[]> entry : testData.entrySet()) {
            String key = entry.getKey();
            String[] values = entry.getValue();
            String[] buff = breakTimeString(key);
            assertEquals(values[0], buff[0]);
            assertEquals(values[1], buff[1]);
            assertEquals(values[2], buff[2]);
        }
    }

    @Test(expected = IllegalArgumentException.class)
    public void getMillisNullArg() {
        long t = getMillis(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void getMillisEmptyArg() {
        long t = getMillis("");
    }

    @Test(expected = IllegalArgumentException.class)
    public void getMillisBadArg_1() {
        long t = getMillis("n8 fe hjiwe6");
    }

    @Test(expected = IllegalArgumentException.class)
    public void getMillisBadArg_2() {
        long t = getMillis("22");
    }

    @Test(expected = IllegalArgumentException.class)
    public void getMillisBadArg_3() {
        long t = getMillis("16:20 hours");
    }

    @Test
    public void getMillisGoodTime_1() {
        long t = getMillis("1 min");
        assertEquals(60000L, t);
    }

    @Test
    public void getMillisGoodTime_2() {
        long t = getMillis("0:0:1");
        assertEquals(1000L, t);
    }

    @Test
    public void getMillisGoodTime_3() {
        long t = getMillis("1H");
        assertEquals(3600000L, t);
    }

    @Test
    public void getMillisGoodTime_4() {

        Map<String, Long> testData = new HashMap<>();

        testData.put("1 minute", 60000L);
        testData.put("5 min", 300000L);
        testData.put("10 min", 600000L);
        testData.put("15 Min", 900000L);
        testData.put("20 mm", 1200000L);
        testData.put("25min", 1500000L);
        testData.put("30Min", 1800000L);
        testData.put("45 minutes", 2700000L);
        testData.put("90Minutes", 5400000L);
        testData.put("0:0:45", 45000L);
        testData.put("00:00:30", 30000L);
        testData.put("0:2:30", 150000L);
        testData.put("0:02:30", 150000L);
        testData.put("0:10", 600000L);
        testData.put("00:20", 1200000L);
        testData.put("6:15", 22500000L);
        testData.put("7:00", 25200000L);
        testData.put("07:30", 27000000L);
        testData.put("1 hour", 3600000L);
        testData.put("1 hh", 3600000L);
        testData.put("1 H", 3600000L);
        testData.put("1hour", 3600000L);
        testData.put("1Hour", 3600000L);
        testData.put("7 hours", 25200000L);
        testData.put("7hh", 25200000L);
        testData.put("7h", 25200000L);

        for (String key : testData.keySet()) {
            assertEquals((long) testData.get(key), getMillis(key));
        }
    }

    @Test
    public void convertTextStringToLocalDatTime() {
        String sampleStr = "August 23, 2021, 7:22 PM";
        LocalDateTime dateTime = LocalDateTime.parse(sampleStr, DateTimeFormatter.ofPattern("LLLL d, yyyy, h:m a"));
        assertNotNull(dateTime);
        assertTrue(dateTime.isBefore(LocalDateTime.now()));
    }

}

// -END-
