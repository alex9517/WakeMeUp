//  Created : 2018-Dec-10
// Modified : 2021-Sep-15

package net.proj27594.wakemeup;

import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.util.Log;

import java.util.concurrent.TimeUnit;

import static android.content.Context.NOTIFICATION_SERVICE;
import static net.proj27594.wakemeup.AppConst.KEY_FILENAME;
import static net.proj27594.wakemeup.AppConst.KEY_REQ_CODE;
import static net.proj27594.wakemeup.AppConst.TIMER_NOTIFICATION_ID;


public class MyBroadcastReceiver extends BroadcastReceiver {

    private static final String TAG = "MyBroadcastReceiver";


    //////////////
    //
    // ON RECEIVE
    //
    //////////////

    @Override
    public void onReceive(Context context, Intent intent) {

        try {
            // Cancel notification that was sent by SetTimerActivity#setTimer.

            int request = intent.getIntExtra(KEY_REQ_CODE, 0);

            NotificationManager notificationManager =
                    (NotificationManager) context.getSystemService(NOTIFICATION_SERVICE);
            notificationManager.cancel(TIMER_NOTIFICATION_ID + request);

            // Get sound filename to be used by MediaPlayer.

            String filename = intent.getStringExtra(KEY_FILENAME);

            Intent svcIntent = new Intent(context, MediaPlayerService.class);
            svcIntent.putExtra(KEY_FILENAME, filename);
            svcIntent.putExtra(KEY_REQ_CODE, request);
            context.startForegroundService(svcIntent);

            // Note! The following stmt is not necessary in the
            // current app version and must stay commented
            // (it's just left here as an example of ...).
            // This [ 16 sec ] timeout was supposed to postpone
            // return from onReceive() while music was playing.

            // TimeUnit.SECONDS.sleep(16);

        } catch (Exception e) {
            boolean isDebug = ((context.getApplicationInfo().flags &
                    ApplicationInfo.FLAG_DEBUGGABLE) != 0);
            if (isDebug) Log.d(TAG, e.getMessage());
            //Toast.makeText(context, "onReceive(), " +
            //        e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

}

// - END-
