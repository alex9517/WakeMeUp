//  Created : 2018-Dec-13
// Modified : 2021-Sep-15

package net.proj27594.wakemeup;

public class AppConst {

    static final String STRING_VALUE_EMPTY = "";
    static final String STRING_VALUE_NONE = "none";
    static final String STRING_VALUE_OTHER = "other";

    // Default values;
    static final String DEFAULT_ACTION_1 = "WakeMeUpPlay";
    static final String DEFAULT_SOUND = "bell.mp3";
    static final String DEFAULT_PREFTIME = "20 min";
    static final String DEFAULT_REQ_CODE = "1";
    static final String DEFAULT_SCHEDULE = "";

    // The keys used by preferences, intents, bundles;
    static final String KEY_FILENAME = "filename";
    static final String KEY_PREFTIME = "preferredTime";
    static final String KEY_REQ_CODE = "requestCode";
    static final String KEY_WAKETIME = "wakeTime";
    static final String KEY_SCHEDULE = "schedule";
    static final String KEY_DATETIME = "dateAndTime";

    // Notification channels, groups, etc.
    static final String CHANNEL_ID = "WakeMeUp";
    static final String CHANNEL_TWO_ID = "WakeMeUp Service";
    static final String WAKE_ME_GROUP = "WakeMeUp group";

    // These numbers are used to identify intents;
    static final int REQUEST_SOUND = 101;
    static final int REQUEST_START = 102;
    static final int REQUEST_TIME = 103;
    static final int REQUEST_RESPOND = 104;
    static final int TIMER_NOTIFICATION_ID = 25470;
    static final int PLAYER_NOTIFICATION_ID = 392470;

}

// -END-
