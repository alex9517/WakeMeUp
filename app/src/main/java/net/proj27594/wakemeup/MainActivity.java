//  Created : 2018-Sep-25
// Modified : 2021-Sep-15

// THIS IS IMPORTANT!
// ------------------
// If this app does not trigger signals at the right time,
// the most probable cause is the interference of some OEM or
// 3rd party application that is supposed to save the battery power.
// These apps usually kill anything that tries to start during the
// doze mode. Unfortunately, the standard Android permissions cannot
// solve this problem. That is, the following settings in
// AndroidManifest.xml:
// ...
// <uses-permission android:name="android.permission.WAKE_LOCK" />
// <uses-permission android:name="android.permission.FOREGROUND_SERVICE" />
// <uses-permission android:name="android.permission.SCHEDULE_EXACT_ALARM" />
// <uses-permission android:name="android.permission.REQUEST_IGNORE_BATTERY_OPTIMIZATIONS" />
// ...
// may be necessary but not sufficient. Usually, to solve the problem,
// you have to "whitelist" your app (whatever it means in the context
// of a specific mobile device).

// For example, in Xiaomi Redmi Note 9 smartphone (Android 10)
// this app (WakeMeUp) does not work normally unless you set
// "MIUI Battery saver" to "No restrictions" for this particular
// app. One way to perform this: do a long touch on the WakeMeUp's
// launcher icon, then "AppInfo", then look for "Battery saver ..".

package net.proj27594.wakemeup;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import androidx.preference.PreferenceManager;
import android.view.View;

import static net.proj27594.wakemeup.AppConst.*;

public class MainActivity extends AppCompatActivity {

    private String soundFilename;
    private String preferredTime;
    private String lastRequestCode;
    private String scheduleRecords;

    ActivityResultLauncher<Intent> startSetTimerActivity = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                new ActivityResultCallback<ActivityResult>() {
        @Override
        public void onActivityResult(ActivityResult result) {
            Intent data = result.getData();

            if (data != null) {
                // It does not matter what button was pressed
                // to return from the SetTimerActivity() - these
                // variables must be always updated if they were
                // changed in that activity.

                String newTime = data.getStringExtra(KEY_PREFTIME);
                if (newTime == null || newTime.isEmpty()) {
                    preferredTime = DEFAULT_PREFTIME;
                } else if (!newTime.equals(preferredTime)) {
                    preferredTime = newTime;
                }
                String newSound = data.getStringExtra(KEY_FILENAME);
                if (newSound == null || newSound.isEmpty()) {
                    soundFilename = DEFAULT_SOUND;
                } else if (!newSound.equals(soundFilename)) {
                    soundFilename = newSound;
                }
                // This can only change if a new signal was set.
                if (result.getResultCode() == Activity.RESULT_OK) {
                    String newReqCode = data.getStringExtra(KEY_REQ_CODE);
                    if (newReqCode == null || newReqCode.isEmpty()) {
                        lastRequestCode = DEFAULT_REQ_CODE;
                    } else {
                        lastRequestCode = newReqCode;
                    }
                }
                String newSchedule = data.getStringExtra(KEY_SCHEDULE);
                if (newSchedule == null || newSchedule.isEmpty()) {
                    scheduleRecords = DEFAULT_SCHEDULE;
                } else if (!newSchedule.equals(scheduleRecords)) {
                    scheduleRecords = newSchedule;
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
        setContentView(R.layout.activity_main);

        SharedPreferences prefs =
                PreferenceManager.getDefaultSharedPreferences(this);

        soundFilename = prefs.getString(KEY_FILENAME, DEFAULT_SOUND);
        preferredTime = prefs.getString(KEY_PREFTIME, DEFAULT_PREFTIME);
        lastRequestCode = prefs.getString(KEY_REQ_CODE, DEFAULT_REQ_CODE);
        scheduleRecords = prefs.getString(KEY_SCHEDULE, DEFAULT_SCHEDULE);

        // WARNING! The following statement (getFakeSchedule())
        // is for debugging only, and usually must be commented out!
        // scheduleRecords = AuxUtils.getFakeSchedule();

        createNotificationChannels();
    }


    //////////////////////////
    //
    // ON SAVE INSTANCE STATE
    //
    //////////////////////////

    @Override
    public void onSaveInstanceState(@NonNull Bundle state) {
        super.onSaveInstanceState(state);

        state.putString(KEY_FILENAME, soundFilename);
        state.putString(KEY_PREFTIME, preferredTime);
        state.putString(KEY_REQ_CODE, lastRequestCode);
        state.putString(KEY_SCHEDULE, scheduleRecords);
    }


    /////////////////////////////
    //
    // ON RESTORE INSTANCE STATE
    //
    /////////////////////////////

    @Override
    public void onRestoreInstanceState(Bundle state) {
        super.onRestoreInstanceState(state);

        preferredTime = state.getString(KEY_PREFTIME);
        soundFilename = state.getString(KEY_FILENAME);
        lastRequestCode = state.getString(KEY_REQ_CODE);
        scheduleRecords = state.getString(KEY_SCHEDULE);
    }


    ///////////
    //
    // ON STOP
    //
    ///////////

    @Override
    public void onStop() {
        super.onStop();

        SharedPreferences prefs =
                PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor prefsEditor = prefs.edit();

        prefsEditor.putString(KEY_FILENAME, soundFilename);
        prefsEditor.putString(KEY_PREFTIME, preferredTime);
        prefsEditor.putString(KEY_REQ_CODE, lastRequestCode);
        prefsEditor.putString(KEY_SCHEDULE, scheduleRecords);
        prefsEditor.apply();
    }


    //////////////////
    //
    // BUTTON CLICKED
    //
    //////////////////

    public void buttonClicked(View view) {

        if (view.getId() == R.id.btn_set_stat) {
            Intent intent = new Intent(this, SetTimerActivity.class);
            intent.putExtra(KEY_FILENAME, soundFilename);
            intent.putExtra(KEY_PREFTIME, preferredTime);
            intent.putExtra(KEY_REQ_CODE, lastRequestCode);
            intent.putExtra(KEY_SCHEDULE, scheduleRecords);
            startSetTimerActivity.launch(intent);
        } else {
            finishAndRemoveTask();
        }
    }


    ////////////////////////////////
    //
    // CREATE NOTIFICATION CHANNELS
    //
    ////////////////////////////////

    private void createNotificationChannels() {
        // Creates two NotificationChannels.
        // It only works on API 26 and higher, because
        // NotificationChannel class is new and not in the support lib.

        // On Android 8.0 and higher, you must create the notification
        // channel before posting any notifications. So, you should execute
        // this code as soon as your app starts. It's safe to call this
        // repeatedly because creating an existing notification channel
        // performs no operation.

        // # 1 =====
        // Channel ONE is used to notify that
        // timer is set to a specific date/time.

        NotificationChannel channelOne = new NotificationChannel(
                CHANNEL_ID,
                "WakeMeUp channel",
                NotificationManager.IMPORTANCE_DEFAULT);
        channelOne.setDescription("Shows the scheduled signals");

        // # 2 =====
        // Channel TWO is used by MediaPlayerService to notify
        // that music (sound signal) is being played right now.
        // The IMPORTANCE_LOW is used to turn off a notification
        // sound which should not interfere with the music.

        NotificationChannel channelTwo = new NotificationChannel(
                CHANNEL_TWO_ID,
                "WakeMeUp service channel",
                NotificationManager.IMPORTANCE_LOW);
        channelTwo.setDescription(
                "This is required to start service in the foreground");

        // Register the channels with the system. And after that, you
        // cannot change the importance or other notification behaviors.

        NotificationManager notificationManager =
                getSystemService(NotificationManager.class);
        notificationManager.createNotificationChannel(channelOne);
        notificationManager.createNotificationChannel(channelTwo);
    }

}

// -END-
