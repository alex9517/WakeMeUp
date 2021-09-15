//  Created : 2021-Aug-30
// Modified : 2021-Sep-15

package net.proj27594.wakemeup;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.res.AssetFileDescriptor;
import android.content.res.AssetManager;
import android.media.AudioAttributes;
import android.media.MediaPlayer;
import android.os.IBinder;
import android.os.PowerManager;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import static net.proj27594.wakemeup.AppConst.CHANNEL_TWO_ID;
import static net.proj27594.wakemeup.AppConst.DEFAULT_SOUND;
import static net.proj27594.wakemeup.AppConst.KEY_FILENAME;
import static net.proj27594.wakemeup.AppConst.KEY_REQ_CODE;
import static net.proj27594.wakemeup.AppConst.PLAYER_NOTIFICATION_ID;
import static net.proj27594.wakemeup.AppConst.WAKE_ME_GROUP;

public class MediaPlayerService extends Service {

    private static final String TAG = "MediaPlayerService";

    private MediaPlayer mediaPlayer;


    ////////////////////
    //
    // ON START COMMAND
    //
    ////////////////////

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        String filename = intent.getStringExtra(KEY_FILENAME);
        if (filename == null || filename.isEmpty()) filename = DEFAULT_SOUND;
        int requestCode = intent.getIntExtra(KEY_REQ_CODE, 0);

        try {
            if (requestCode == 0) {
                throw new IllegalArgumentException(
                        "requestCode = 0; it was extracted from intent" +
                        " supplied by MyBroadcastReceiver.class. This" +
                        " is absolutely wrong and should never happen!");
            }
            String currDateTime = LocalDateTime.now().format(
                    DateTimeFormatter.ofPattern("LLLL d, yyyy, h:m a"));
            startForeground(PLAYER_NOTIFICATION_ID + requestCode,
                    createNotification(currDateTime));

            AssetManager assetManager = getApplicationContext().getAssets();

            mediaPlayer = new MediaPlayer();
            mediaPlayer.setAudioAttributes(
                    new AudioAttributes.Builder()
                            .setUsage(AudioAttributes.USAGE_MEDIA)
                            .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                            .build());

            mediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                @Override
                public void onPrepared(MediaPlayer mp) {
                    mp.setLooping(false);
                    mp.setWakeMode(getApplicationContext(), PowerManager.PARTIAL_WAKE_LOCK);
                    mp.start();
                }
            });
            mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mp) {
                    if (mp != null) mp.release();
                    stopForeground(true);
                    stopSelf();
                }
            });
            mediaPlayer.setOnErrorListener(new MediaPlayer.OnErrorListener() {
                @Override
                public boolean onError(MediaPlayer mp, int what, int extra) {
                    if (mp != null) mp.release();
                    stopForeground(true);
                    stopSelf();
                    return false;
                }
            });

            AssetFileDescriptor afd = assetManager.openFd(filename);
            mediaPlayer.setDataSource(
                    afd.getFileDescriptor(), afd.getStartOffset(), afd.getLength());
            afd.close();
            mediaPlayer.prepareAsync();

        } catch (Exception e) {
            boolean isDebug = ((getApplicationContext().getApplicationInfo().flags &
                    ApplicationInfo.FLAG_DEBUGGABLE) != 0);
            if (isDebug) Log.d(TAG, e.getMessage());

            if (mediaPlayer != null) mediaPlayer.release();
            stopForeground(true);
            stopSelf();
        }
        return START_NOT_STICKY;
    }


    // I don't need this method, but it's required.
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }


    //////////////
    //
    // ON DESTROY
    //
    //////////////

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mediaPlayer != null) mediaPlayer.release();
    }


    /////////////////////
    //
    // SEND NOTIFICATION
    //
    /////////////////////

    private Notification createNotification(String dateTime) {
        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        //PendingIntent pendingIntent = PendingIntent.getActivity(
        //        this, REQUEST_RESPOND, intent, PendingIntent.FLAG_CANCEL_CURRENT);
        PendingIntent pendingIntent =
                PendingIntent.getActivity(this, 0, intent, 0);

        return new NotificationCompat.Builder(this, CHANNEL_TWO_ID)
                .setSmallIcon(R.drawable.ic_stat_name_svc)
                .setContentTitle("WakeMeUp")
                .setContentText("MediaPlayerService has started to play at " +
                        dateTime + " .. you can ignore this!")
                // .setLargeIcon(BitmapFactory
                //         .decodeResource(getApplicationContext()
                //                 .getResources(), R.drawable.ic_stat_name_svc))
                //                 .getResources(), R.drawable.ic_launcher_round_96))
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setContentIntent(pendingIntent)
                .setGroup(WAKE_ME_GROUP)
                .build();
    }

}

// -END-
