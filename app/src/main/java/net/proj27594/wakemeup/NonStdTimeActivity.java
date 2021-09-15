//  Created : 2018-Dec-19
// Modified : 2021-Aug-26

package net.proj27594.wakemeup;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.format.DateUtils;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.TimePicker;

import androidx.appcompat.app.AppCompatActivity;

import java.util.Calendar;

import static net.proj27594.wakemeup.AppConst.KEY_DATETIME;
import static net.proj27594.wakemeup.AppConst.KEY_WAKETIME;
// import static net.proj27594.wakemeup.AppConst.KEY_WARNTIME;

public class NonStdTimeActivity extends AppCompatActivity {

    private static final String TAG = "NonStdTimeActivity";

    TextView currDateTime;
    Calendar dateAndTime = Calendar.getInstance();


    /////////////
    //
    // ON CREATE
    //
    /////////////

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_non_std_time);
        Intent intent = getIntent();
        String wakeTime = intent.getStringExtra(KEY_WAKETIME);
        String[] buff = AuxUtils.breakTimeString(wakeTime);
        EditText hours = findViewById(R.id.time_hh);
        hours.setText(buff[0]);
        EditText minutes = findViewById(R.id.time_mm);
        minutes.setText(buff[1]);
        EditText seconds = findViewById(R.id.time_ss);
        seconds.setText(buff[2]);
        currDateTime = findViewById(R.id.date_time);

        Switch modeSwitch = findViewById(R.id.mode_switch);
        switchMode(modeSwitch.isChecked());

        modeSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                switchMode(buttonView.isChecked());
            }
        });
    }


    //////////////////
    //
    // BUTTON CLICKED
    //
    //////////////////

    public void buttonClicked(View view) {

        Intent intent = new Intent();

        switch (view.getId()) {
            case R.id.btn_pick_date:
                new DatePickerDialog(NonStdTimeActivity.this, d,
                        dateAndTime.get(Calendar.YEAR),
                        dateAndTime.get(Calendar.MONTH),
                        dateAndTime.get(Calendar.DAY_OF_MONTH))
                        .show();
                break;

            case R.id.btn_pick_time:
                new TimePickerDialog(NonStdTimeActivity.this, t,
                        dateAndTime.get(Calendar.HOUR_OF_DAY),
                        dateAndTime.get(Calendar.MINUTE), true)
                        .show();
                break;

            case R.id.btn_time_ok:
                // Button "OK";
                Switch modeSwitch = findViewById(R.id.mode_switch);
                if (modeSwitch.isChecked()) {
                    TextView dt = findViewById(R.id.date_time);
                    intent.putExtra(KEY_DATETIME, dt.getText());
                } else {
                    intent.putExtra(KEY_DATETIME, "");
                    EditText hours = findViewById(R.id.time_hh);
                    EditText minutes = findViewById(R.id.time_mm);
                    EditText seconds = findViewById(R.id.time_ss);
                    String wakeTime = AuxUtils.makeTimeString(
                            hours.getText().toString(),
                            minutes.getText().toString(),
                            seconds.getText().toString());
                    // Give warning 10 min before main signal;
                    // String warnTime = "10";  -- not used anymore.
                    intent.putExtra(KEY_WAKETIME, wakeTime);
                    // intent.putExtra(KEY_WARNTIME, warnTime);
                }
                setResult(RESULT_OK, intent);
                finish();
                break;

            case R.id.btn_time_cancel:
                // Button "Cancel";
                setResult(RESULT_CANCELED, intent);
                finish();
                break;
        }
    }


    ///////////////
    //
    // SWITCH MODE
    //
    ///////////////

    private void switchMode(boolean dateTimeMode) {
        if (dateTimeMode) {
            findViewById(R.id.btn_pick_date).setEnabled(true);
            findViewById(R.id.btn_pick_time).setEnabled(true);
            findViewById(R.id.time_hh).setEnabled(false);
            findViewById(R.id.time_mm).setEnabled(false);
            findViewById(R.id.time_ss).setEnabled(false);
            setInitialDateTime();
        } else {
            findViewById(R.id.time_hh).setEnabled(true);
            findViewById(R.id.time_mm).setEnabled(true);
            findViewById(R.id.time_ss).setEnabled(true);
            findViewById(R.id.btn_pick_date).setEnabled(false);
            findViewById(R.id.btn_pick_time).setEnabled(false);
        }
    }


    /////////////////////////
    //
    // SET INITIAL DATE TIME
    //
    /////////////////////////

    private void setInitialDateTime() {
        currDateTime.setText(DateUtils.formatDateTime(this,
                dateAndTime.getTimeInMillis(),
                DateUtils.FORMAT_SHOW_DATE | DateUtils.FORMAT_SHOW_YEAR
                        | DateUtils.FORMAT_SHOW_TIME));
    }

    
    TimePickerDialog.OnTimeSetListener t =
            new TimePickerDialog.OnTimeSetListener() {
                public void onTimeSet(TimePicker view, int hour, int minute) {
                    dateAndTime.set(Calendar.HOUR_OF_DAY, hour);
                    dateAndTime.set(Calendar.MINUTE, minute);
                    setInitialDateTime();
                }
            };

    // установка обработчика выбора даты

    DatePickerDialog.OnDateSetListener d =
            new DatePickerDialog.OnDateSetListener() {
                public void onDateSet(DatePicker view,
                                      int year, int month, int day) {
                    dateAndTime.set(Calendar.YEAR, year);
                    dateAndTime.set(Calendar.MONTH, month);
                    dateAndTime.set(Calendar.DAY_OF_MONTH, day);
                    setInitialDateTime();
                }
            };
}

// -END-
