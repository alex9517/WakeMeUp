//  Created : 2018-Dec-03
// Modified : 2021-Aug-19

package net.proj27594.wakemeup;

import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.res.AssetFileDescriptor;
import android.content.res.AssetManager;
import android.media.AudioAttributes;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static net.proj27594.wakemeup.AppConst.KEY_FILENAME;

public class SelectSoundActivity extends AppCompatActivity {

    private static final String TAG = "SelectSoundActivity";

    private String soundFilename;
    private TextView currChoice;
    private AssetManager assetManager;
    private MediaPlayer mediaPlayer = null;
    private List<String> items = new ArrayList<>();


    /////////////
    //
    // ON CREATE
    //
    /////////////

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_sound);

        Intent intent = getIntent();
        soundFilename = intent.getStringExtra(KEY_FILENAME);
        currChoice = findViewById(R.id.current_choice);
        currChoice.setText(soundFilename);
        assetManager = getAssets();

        try {
            String[] filenames = assetManager.list("");

            for (String s : filenames) {
                if (s.endsWith(".mp3") || s.endsWith(".wav"))
                    items.add(s);
            }
        }
        catch (IOException e) {
            if ((getApplicationContext().getApplicationInfo().flags &
                    ApplicationInfo.FLAG_DEBUGGABLE) != 0) {
                Log.e(TAG, e.getMessage());
            }
            Toast.makeText(getApplicationContext(), e.getMessage(),
                    Toast.LENGTH_SHORT).show();
        }

        ListView list = findViewById(R.id.items_list);

        final ArrayAdapter adapter = new ArrayAdapter<>(
                this, android.R.layout.simple_list_item_1, items);
        list.setAdapter(adapter);


        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent,
                                    View view, int position, long id) {
                String item = parent.getItemAtPosition(position).toString();
                TextView txt = (TextView) findViewById(R.id.current_choice);
                txt.setText(item);
            }
        });

    }


    ////////////
    //
    // ON PAUSE
    //
    ////////////

    @Override
    public void onPause() {
        super.onPause();
        if (mediaPlayer != null && mediaPlayer.isPlaying()) mediaPlayer.pause();
    }


    /////////////
    //
    // ON RESUME
    //
    /////////////

    @Override
    protected void onResume() {
        super.onResume();
        if (mediaPlayer != null && (!mediaPlayer.isPlaying())) mediaPlayer.start();
    }


    ///////////
    //
    // ON STOP
    //
    ///////////

    @Override
    public void onStop() {
        super.onStop();
        if (mediaPlayer != null) mediaPlayer.release();
    }


    //////////////////
    //
    // BUTTON CLICKED
    //
    //////////////////

    public void buttonClicked(View view) {
        Intent intent;
        // int id = view.getId();

        if (view.getId() == R.id.btn_sound_ok) {
            // Button "OK";
            currChoice = findViewById(R.id.current_choice);
            soundFilename = currChoice.getText().toString();
            intent = new Intent();
            intent.putExtra(KEY_FILENAME, soundFilename);
            setResult(RESULT_OK, intent);
            finish();
        } else if (view.getId() == R.id.btn_play) {
            if (mediaPlayer != null && mediaPlayer.isPlaying()) {
                mediaPlayer.stop();
                Button btnTryStop = findViewById(R.id.btn_play);
                btnTryStop.setText(R.string.btn_play);
            } else {
                currChoice = findViewById(R.id.current_choice);
                soundFilename = currChoice.getText().toString();

                try {
                    mediaPlayer = new MediaPlayer();
                    mediaPlayer.setAudioAttributes(
                            new AudioAttributes.Builder()
                                    .setUsage(AudioAttributes.USAGE_MEDIA)
                                    .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                                    .build());

                    // Uri mediaFile = Uri.fromFile(new File(filename));
                    // ContentResolver resolver = getContentResolver();
                    // AssetFileDescriptor afd = resolver.openAssetFileDescriptor(mediaFile, "r");

                    AssetFileDescriptor afd = getAssets().openFd(soundFilename);
                    mediaPlayer.setDataSource(
                            afd.getFileDescriptor(), afd.getStartOffset(), afd.getLength());
                    afd.close();

                    mediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                        @Override
                        public void onPrepared(MediaPlayer mp) {
                            mp.setLooping(false);
                            mp.start();
                            Button btnTryStop = findViewById(R.id.btn_play);
                            btnTryStop.setText(R.string.btn_stop);
                        }
                    });

                    mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                        @Override
                        public void onCompletion(MediaPlayer mp) {
                            // if (mp != null) mp.release();
                            Button btnTryStop = findViewById(R.id.btn_play);
                            btnTryStop.setText(R.string.btn_play);
                        }
                    });

                    mediaPlayer.prepareAsync(); //
                    // Toast.makeText(getApplicationContext(), "Playing " + soundFilename + "..",
                    //         Toast.LENGTH_SHORT).show();
                } catch (Exception e) {
                    if ((getApplicationContext().getApplicationInfo().flags &
                            ApplicationInfo.FLAG_DEBUGGABLE) != 0) {
                        Log.e(TAG, e.getMessage());
                    }
                    Toast.makeText(getApplicationContext(), e.getMessage(),
                            Toast.LENGTH_SHORT).show();
                }
//            case R.id.btn_stop:
//                if (mediaPlayer != null && mediaPlayer.isPlaying()) mediaPlayer.stop();
//                break;
            }
        } else {
            // Button "Cancel";
            intent = new Intent();
            setResult(RESULT_CANCELED, intent);
            finish();
        }
    }


    ///////////////
    //
    // ON PREPARED
    //
    ///////////////
/*
    public void onPrepared(MediaPlayer player) {
        //player.setVolume(1f, 1f);
        player.setLooping(false);
        player.start();
    }
*/
}

// -END-
