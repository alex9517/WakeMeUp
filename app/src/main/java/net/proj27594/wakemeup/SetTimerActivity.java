//  Created : 2018-Dec-07
// Modified : 2021-Sep-14

package net.proj27594.wakemeup;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;

import static android.app.PendingIntent.FLAG_NO_CREATE;
import static android.app.PendingIntent.FLAG_UPDATE_CURRENT;
import static net.proj27594.wakemeup.AppConst.*;


public class SetTimerActivity extends AppCompatActivity {

    public static final String TAG = "SetTimerActivity";

    private static final String[] STD_TIMES = {
            "1 minute", "2 minutes", "5 minutes", "10 minutes",
            "15 minutes", "20 minutes", "22 minutes", "25 minutes",
            "30 minutes", "32 minutes", "35 minutes", "45 minutes",
            "60 minutes", "90 minutes", "2 hours", "3 hours", "4 hours",
            "6 hours", "6:15", "6:30", "6:45", "7 hours", "7:15",
            "7:30", "8 hours", "10 hours", "12 hours"
    };

    // Sound file to play when timer triggers.
    private String soundFilename;

    // The most popular time interval at this moment (the last selected).
    private String preferredTime;

    // This request code was used to start the timer the last time.
    private String lastRequestCode;

    // Current schedule of signals to trigger (as fetched from preferences)
    // It must be corrected because some signals may have triggered already.
    private String scheduleRecords;

    // This is the list of objects corresponding to the updated schedule.
    private List<ScheduledSignal> ssignals;

    ActivityResultLauncher<Intent> selectSoundActivity = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK) {
                    Intent data = result.getData();
                    if (data != null) {
                        String newSound = data.getStringExtra(KEY_FILENAME);
                        if (newSound == null || newSound.isEmpty()) {
                            soundFilename = DEFAULT_SOUND;
                        } else if (!newSound.equals(soundFilename)) {
                            soundFilename = newSound;
                        }
                        TextView currSignal1 = findViewById(R.id.timer_sound);
                        currSignal1.setText(soundFilename);
                    }
                }
            });

    ActivityResultLauncher<Intent> nonStdTimeActivity = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK) {
                    Intent data = result.getData();
                    if (data != null) {
                        String dateAndTime = data.getStringExtra(KEY_DATETIME);
                        if (dateAndTime.isEmpty()) {
                            String newTime = data.getStringExtra(KEY_WAKETIME);
                            if (newTime == null || newTime.isEmpty()) {
                                preferredTime = DEFAULT_PREFTIME;
                            } else if (!newTime.equals(preferredTime)) {
                                preferredTime = newTime;
                            }
                            TextView currChoice1 = findViewById(R.id.timer_current_choice);
                            currChoice1.setText(preferredTime);
                        } else {
                            TextView timeLabel = findViewById(R.id.label_timer_choice);
                            timeLabel.setText(getResources().getString(R.string.timer_at));
                            TextView timeAt = findViewById(R.id.timer_current_choice);
                            timeAt.setText(dateAndTime);
                        }
                    }
                }
            });


    /////////////
    //
    // ON CREATE
    //
    /////////////

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_set_timer);

        Intent intent = getIntent();

        soundFilename = intent.getStringExtra(KEY_FILENAME);

        if (soundFilename != null) {
            TextView currSignal = findViewById(R.id.timer_sound);
            currSignal.setText(soundFilename);
        }

        preferredTime = intent.getStringExtra(KEY_PREFTIME);

        if (preferredTime != null) {
            TextView currChoice = findViewById(R.id.timer_current_choice);
            currChoice.setText(preferredTime);
        }

        lastRequestCode = intent.getStringExtra(KEY_REQ_CODE);
        scheduleRecords = intent.getStringExtra(KEY_SCHEDULE);

        // Convert a string 'scheduleRecords' from preferences to
        // a list of 'ScheduledSignal' objects, and remove expired.
        ssignals = getSchedule(scheduleRecords);

        // Create a list of currently configured signals
        // to be displayed in the ListView (R.id.signals).

        setScheduleList();

        // Create a list of the pre-set standard time options.

        List<String> stdTimeItems = new ArrayList<>(Arrays.asList(STD_TIMES));
        ListView stdTimeList = findViewById(R.id.timer_items_list);

        final ArrayAdapter<String> adapter2 = new ArrayAdapter<>(
                this, android.R.layout.simple_list_item_1, stdTimeItems);
        stdTimeList.setAdapter(adapter2);

        stdTimeList.setOnItemClickListener((parent, view, position, id) -> {
            String item = parent.getItemAtPosition(position).toString();
            TextView txt = (TextView) findViewById(R.id.timer_current_choice);
            txt.setText(item);
            preferredTime = item;
        });
    }


    /////////////////////
    //
    // SET SCHEDULE LIST
    //
    /////////////////////

    private void setScheduleList() {
        // Create a list of currently configured signals
        // to be displayed in the ListView (R.id.signals).

        final int max_item_length = 47;

        List<String> scheduleItems = new ArrayList<>();
        ssignals.forEach(
                item -> {
                    String s = item.getId() + ("  ") + item.getPlayTime() +
                            "  " + item.getSignalName();
                    if (s.length() > max_item_length) {
                        s = s.substring(0, max_item_length - 2) + "...";
                    }
                    scheduleItems.add(s);
                }
        );
        ListView scheduledList = findViewById(R.id.signals);

        final ArrayAdapter<String> adapter1 = new ArrayAdapter<>(
                this, android.R.layout.simple_list_item_1, scheduleItems);
        scheduledList.setAdapter(adapter1);

        scheduledList.setOnItemClickListener((parent, view, position, id) -> {
            String itemToRemove = parent.getItemAtPosition(position).toString();
            //String[] items = item.split("\\s+");
            // Get the request code from that string.
            int rcode = 0;
            StringBuffer dialogMsg = new StringBuffer();

            try {
                int i = itemToRemove.indexOf(' ');
                rcode = Integer.parseInt(itemToRemove.substring(0, i));
                for (ScheduledSignal item : ssignals) {
                    if (item.getId() == rcode) {
                        dialogMsg.append("Req Code : ");
                        dialogMsg.append(rcode);
                        dialogMsg.append("\nStarted : ");
                        dialogMsg.append(item.getWasSetAt());
                        dialogMsg.append("\nTriggers : ");
                        dialogMsg.append(item.getMustTriggerAt());
                        dialogMsg.append("\nSound : ");
                        dialogMsg.append(item.getSignalName());
                        break;
                    }
                }
            } catch (IndexOutOfBoundsException | NumberFormatException e) {
                if ((getApplicationContext().getApplicationInfo().flags &
                        ApplicationInfo.FLAG_DEBUGGABLE) != 0) {
                    Log.d(TAG, e.getMessage());
                }
            }
            final int requestCode = rcode;  // It must be final to be used in onClick();

            // This dialog allows to remove an item from the current schedule.
            new AlertDialog.Builder(SetTimerActivity.this)
                    .setTitle("You want to remove")
                    .setMessage(dialogMsg.toString())
                    .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            if (removeSignal(requestCode)) {
                                // removeSignal(requestCode);
                                ssignals.removeIf(e -> (e.getId() == requestCode));
                                scheduleItems.remove(itemToRemove);
                                adapter1.notifyDataSetChanged();
                                // Toast.makeText(getApplicationContext(),
                                //         "Signal removed.. schedule size = " +
                                //                 scheduleItems.size(), Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(getApplicationContext(),
                                        "Sorry, cannot remove this signal!",
                                        Toast.LENGTH_SHORT).show();
                            }
                        }
                    })
                    .setNegativeButton("No", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.cancel();
                        }
                    })
                    .show();
        });
    }


    //////////////////
    //
    // BUTTON CLICKED
    //
    //////////////////

    public void buttonClicked(View view) {
        Intent intent;
        int id = view.getId();

        if (id == R.id.timer_sound) {
            // Start activity to select sound file to play at the wake time.
            // TextView currSignal = findViewById(R.id.timer_sound);
            // soundFilename = currSignal.getText().toString();
            intent = new Intent(this, SelectSoundActivity.class);
            intent.putExtra(KEY_FILENAME, soundFilename);
            selectSoundActivity.launch(intent);
        } else if (id == R.id.timer_current_choice) {
            // Start activity to select a non-standard wake time.
            TextView tvTime = findViewById(R.id.timer_current_choice);
            String timeStr = tvTime.getText().toString();
            intent = new Intent(this, NonStdTimeActivity.class);
            intent.putExtra(KEY_WAKETIME, timeStr);
            nonStdTimeActivity.launch(intent);
        } else if (id == R.id.btn_timer_start) {
            // Button "Start":
            // - start timer,
            // - return to MainActivity,
            // - modify 'scheduleRecords', etc
            TextView currTime = findViewById(R.id.timer_current_choice);
            String wakeTime = currTime.getText().toString();
            TextView currFile = findViewById(R.id.timer_sound);
            soundFilename = currFile.getText().toString();
            int requestCode = getRequestCode(lastRequestCode);

            setTimer(wakeTime, soundFilename, requestCode);

            preferredTime = wakeTime;
            intent = new Intent();
            intent.putExtra(KEY_PREFTIME, preferredTime);
            intent.putExtra(KEY_FILENAME, soundFilename);
            // If setTimer() was successful, it modified lastRequestCode.
            intent.putExtra(KEY_REQ_CODE, lastRequestCode);
            scheduleRecords = scheduleToJson(ssignals);
            intent.putExtra(KEY_SCHEDULE, scheduleRecords);
            setResult(RESULT_OK, intent);
            finish();
        } else if (id == R.id.btn_timer_return) {
            // Button "Return":
            // - return to MainActivity,
            // - ignore all changes except the updated schedule!
            intent = new Intent();
            intent.putExtra(KEY_PREFTIME, preferredTime);
            intent.putExtra(KEY_FILENAME, soundFilename);
            scheduleRecords = scheduleToJson(ssignals);
            intent.putExtra(KEY_SCHEDULE, scheduleRecords);
            setResult(RESULT_CANCELED, intent);
            finish();
        }
    }


    /////////////
    //
    // SET TIMER
    //
    /////////////

    private void setTimer(String timeStr, String soundFile, int requestCode) {
        // The 'timeStr' can be a time interval, like '07:30:00', or,
        // it can be a point in time, like 'August 22, 2021, 7:22 PM'
        // (note that this date/time format is hard-coded in this app,
        // and you should follow it). So, first of all, we must find
        // what do we actually have and handle it appropriately.

        long wakeTimeMillis;
        long timeToWaitInMillis = 0;
        LocalDateTime dateTime = LocalDateTime.now();

        try {
            if (timeStr == null || timeStr.isEmpty() ||
                    soundFile == null || soundFile.isEmpty() ||
                    soundFile.equals(STRING_VALUE_NONE) || requestCode == 0) {
                throw new IllegalArgumentException("setTimer(): bad argument(s).");
            }

            try {
                // Let's try to parse 'timeStr' assuming that this is
                // a point in time. If parsing throws an exception, then
                // we would try to handle it as a time interval. Notice
                // that AuxUtils.getMillis() also can throw an exception,
                // and in this case it will be considered a total failure!

                dateTime = LocalDateTime.parse(
                        timeStr, DateTimeFormatter.ofPattern("LLLL d, yyyy, h:m a"));
                if (dateTime.isBefore(LocalDateTime.now())) {
                    Toast.makeText(getApplicationContext(),
                            getString(R.string.msg_bad_datetime),
                            Toast.LENGTH_SHORT).show();
                    return;
                }
                OffsetDateTime odt = OffsetDateTime.now();
                    // I have not found better way to get 'ZoneOffset'.
                ZoneOffset zoneOffset = odt.getOffset();
                wakeTimeMillis = 1000 * dateTime.toEpochSecond(zoneOffset);

            } catch (DateTimeParseException e) {
                timeToWaitInMillis = AuxUtils.getMillis(timeStr);
                wakeTimeMillis = System.currentTimeMillis() + timeToWaitInMillis;
            }
            // If the prev block has finished successfully,
            // then we have 'wakeTimeMillis', i.e., the time
            // we're going to use to set the AlarmManager.

            // Note about Intent! Two Intents are considered equal
            // if their action, data, type, class, and categories are the same,
            // extras are completely ignored when comparing Intents (?)

            Intent intent = new Intent(this, MyBroadcastReceiver.class);
            intent.setAction("ACTION_START_SERVICE");  // This is my action.
            intent.addCategory(Intent.CATEGORY_DEFAULT);
            intent.putExtra(KEY_FILENAME, soundFile);
            intent.putExtra(KEY_REQ_CODE, requestCode);

            PendingIntent pendingIntent = PendingIntent.getBroadcast(
                    this.getApplicationContext(), requestCode, intent, FLAG_UPDATE_CURRENT);

            AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);

            // wakeTimeMillis = System.currentTimeMillis() + timeToWaitInMillis;

//            Toast.makeText(getApplicationContext(),
//                    "Intent created, PendingIntent created, AlarmService OK..",
//                    Toast.LENGTH_SHORT).show();

            // setExactAndAllowWhileIdle() can significantly impact the power use of the device
            // [when idle] and cause significant battery blame to the app scheduling them, so they
            // should be used with care. To reduce abuse, there are restrictions on how frequently
            // these alarms will go off for a particular app. Under normal system operation,
            // it will not dispatch these alarms more than about every minute (at which point
            // every such pending alarm is dispatched); when in low-power idle modes this duration
            // may be significantly longer, such as 15 minutes.

            // Note! Starting with Build.VERSION_CODES#S, apps targeting SDK level 31
            // or higher need to request the SCHEDULE_EXACT_ALARM permission to use this API,
            // unless the app is exempt from battery restrictions.

            alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP, wakeTimeMillis, pendingIntent);

            lastRequestCode = new Integer(requestCode).toString();
            sendNotification(wakeTimeMillis, requestCode);
            Toast.makeText(getApplicationContext(),
                    "Starting countdown from " + timeStr + "..",
                    Toast.LENGTH_SHORT).show();

            // Add new 'ScheduledSignal' item to ssignals.
            LocalDateTime currDateTime = LocalDateTime.now();
            String wasSetAt = currDateTime.format(
                    DateTimeFormatter.ofPattern( "yyyy-MM-dd HH:mm:ss" ));
            LocalDateTime trigDateTime = timeToWaitInMillis > 0 ?
                    currDateTime.plusSeconds(timeToWaitInMillis/1000) : dateTime;
            String mustTriggerAt = trigDateTime.format(
                    DateTimeFormatter.ofPattern( "yyyy-MM-dd HH:mm:ss" ));
            ScheduledSignal newSignal =
                    new ScheduledSignal(requestCode, wasSetAt, mustTriggerAt, soundFile);
            ssignals.add(newSignal);

        } catch (Exception e) {
            String msg = "Failed to set timer: " + e.getMessage();
            Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_SHORT).show();
            if ((getApplicationContext().getApplicationInfo().flags &
                    ApplicationInfo.FLAG_DEBUGGABLE) != 0) {
                Log.d(TAG, msg);
            }
        }
    }


    /////////////////
    //
    // REMOVE SIGNAL
    //
    /////////////////

    private boolean removeSignal(int requestCode) {

        try {
            Intent intent = new Intent(this, MyBroadcastReceiver.class);
            intent.setAction(DEFAULT_ACTION_1);
            intent.addCategory(Intent.CATEGORY_DEFAULT);
            PendingIntent pendingIntent = PendingIntent.getBroadcast(
                    this.getApplicationContext(), requestCode, intent, FLAG_NO_CREATE);

            // Cancel PendingIntent.
            if (pendingIntent != null) {
                AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
                alarmManager.cancel(pendingIntent);
                Toast.makeText(getApplicationContext(),
                        "Signal canceled (request = " +
                                requestCode + ")", Toast.LENGTH_SHORT).show();
            }
            // Send notification.
            NotificationManager notificationManager =
                    (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
            notificationManager.cancel(TIMER_NOTIFICATION_ID + requestCode);
            return true;

        } catch (NullPointerException e) {
            String msg = "Cannot access AlarmManager: " + e.getMessage();
            Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_SHORT).show();
            if ((getApplicationContext().getApplicationInfo().flags &
                    ApplicationInfo.FLAG_DEBUGGABLE) != 0) {
                Log.d(TAG, msg);
            }
        } catch (Exception e) {
            String msg = "Failed to reset timer: " + e.getMessage();
            Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_SHORT).show();
            if ((getApplicationContext().getApplicationInfo().flags &
                    ApplicationInfo.FLAG_DEBUGGABLE) != 0) {
                Log.d(TAG, msg);
            }
        }
        return false;
    }


    /////////////////////
    //
    // SEND NOTIFICATION
    //
    /////////////////////

    public void sendNotification(long t, int requestCode) {
        DateFormat formatter = new SimpleDateFormat("EEE, MMM d, hh:mm:ss Z");
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(t);
        String fireTime = formatter.format(calendar.getTime());

        // Intent intent = new Intent(this, ResponseActivity.class);
        // Intent intent = new Intent(this, MainActivity.class);
        Intent intent = new Intent(this, MyBroadcastReceiver.class);
        intent.putExtra(KEY_WAKETIME, fireTime);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        // PendingIntent pendingIntent = PendingIntent.getActivity(this, REQUEST_RESPOND, intent, 0);
        PendingIntent pendingIntent = PendingIntent.getActivity(
                this, REQUEST_RESPOND, intent, PendingIntent.FLAG_CANCEL_CURRENT);

        Notification timerNotification =
                new NotificationCompat.Builder(this, CHANNEL_ID)
                        .setSmallIcon(R.drawable.ic_stat_name)
                        .setContentTitle("WakeMeUp")
                        .setContentText("Timer is set to fire at " + fireTime)
                        // .setLargeIcon(BitmapFactory
                        //         .decodeResource(getApplicationContext()
                                        // .getResources(), R.drawable.ic_stat_name))
                        //                 .getResources(), R.drawable.ic_launcher_round_96))
                        .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                        .setContentIntent(pendingIntent)
                        .setGroup(WAKE_ME_GROUP)
                        .build();

        NotificationManager notificationManager =
                (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

        notificationManager.notify(
                TIMER_NOTIFICATION_ID + requestCode, timerNotification);
    }


    ////////////////////
    //
    // GET REQUEST CODE
    //
    ////////////////////

    // It takes the last used request code, increments, returns.
    // If new request code is larger than 99998, it jumps back to 1.
    // I think 99998 must be sufficient to avoid collisions.

    // NOTE! This is the only place where this limit (99998) is used
    // and can be changed if it's necessary.

    private int getRequestCode(String s) {

        final int MAX_REQUEST_CODE = 99998;

        try {
            if (s == null || s.isEmpty() || s.length() > 6) s = "NaN";

            // Convert String s (it supposed to be the request code
            // saved in preferences) to int rc, and increment it;

            int rc = Integer.parseInt(s) + 1;

            if (rc < 0 || rc > MAX_REQUEST_CODE) rc = 1;
            return rc;
        } catch (NumberFormatException e) {
            if ((getApplicationContext().getApplicationInfo().flags &
                    ApplicationInfo.FLAG_DEBUGGABLE) != 0) {
                Log.d(TAG, "getRequestCode() .. invalid arg.");
            }
        }
        return MAX_REQUEST_CODE + 1;  // This means error, and it should never happen!
    }


    ////////////////
    //
    // GET SCHEDULE
    //
    ////////////////

    private List<ScheduledSignal> getSchedule(String s) {

        // Part # 1: convert a JSON string fetched
        // from the preferences to a list of objects.

        if (s == null || s.isEmpty()) return new ArrayList<>();

        Gson gson = new Gson();
        Type scheduledSignalType = new TypeToken<List<ScheduledSignal>>() {}.getType();
        List<ScheduledSignal> list = gson.fromJson(s, scheduledSignalType);

        // int listSizeBeforeCheck = list.size();

        // List<ScheduledSignal> list = gson.fromJson(s, ArrayList.class);  <- does not work!

        // Part # 2: remove expired signal,
        // i.e., those with the 'mustTriggerAt' before the current time.

        list.removeIf(item -> {
            return ScheduledSignal.isExpired(item.getMustTriggerAt());
        });

        // Part # 3: check if a record in the restored schedule has
        // a corresponding PendingIntent. If not - remove this record from the list.

        list.removeIf(item -> {
            int rc = item.getId();
            Intent intent = new Intent(this, MyBroadcastReceiver.class);
            intent.setAction("ACTION_START_SERVICE");
            intent.addCategory(Intent.CATEGORY_DEFAULT);
            PendingIntent pendingIntent = PendingIntent.getBroadcast(
                    this.getApplicationContext(), rc, intent, FLAG_NO_CREATE);
            return pendingIntent == null;
        });

        // int listSizeAfterCheck = list.size();
        // Toast.makeText(getApplicationContext(),
        //         "getSchedule(): listSizeBeforeCheck = " + listSizeBeforeCheck +
        //         "; listSizeAfterCheck = " + listSizeAfterCheck, Toast.LENGTH_SHORT).show();

        return list;
    }


    ////////////////////
    //
    // SCHEDULE TO JSON
    //
    ////////////////////

    // This method converts a list of objects (schedule) to a JSON string
    // which is supposed to be returned to MainActivity and saved in preferences.

    public static String scheduleToJson(List<ScheduledSignal> signals) {
        if (signals == null || signals.size() == 0) return "";
        Gson gson = new Gson();
        Type scheduledSignalType = new TypeToken<List<ScheduledSignal>>() {}.getType();
        String s = gson.toJson(signals, scheduledSignalType);
        return s;
    }

}

// -END-
