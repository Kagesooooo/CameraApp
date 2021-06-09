package com.example.cameraapp;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.AudioManager;
import android.media.ToneGenerator;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Vibrator;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.util.Locale;

public class TimerApp extends AppCompatActivity implements SensorEventListener {

    private static final long START_TIME = 10000;

    private TextView mTextViewCountDown;
    private Button mButtonStartPause;
    private Button mButtonReset;

    private TextView mTextViewMsg;
    private Button mButtonMinUp;
    private Button mButtonMinDown;
    private Button mButtonSecUp;
    private Button mButtonSecDown;

    private CountDownTimer mCountDownTimer;
    private boolean mTimerRunning;

    private boolean mIsStudyTime;

    private long mTimerLeftInMillis = START_TIME;

    private float[] mAccelerationValue = new float[3];
    private float[] mGeoMagneticValue = new float[3];
    private float[] mOrientationValue = new float[3];
    private float[] mInRotationMatrix = new float[9];
    private float[] mOutRotationMatrix = new float[9];
    private float[] mInclinationMatrix = new float[9];

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_timer_app);

        mTextViewCountDown = findViewById(R.id.textView);
        mButtonStartPause = findViewById(R.id.button_start);
        mButtonReset = findViewById(R.id.button_reset);

        mTextViewMsg = findViewById(R.id.text_view_msg);
        mButtonMinUp = findViewById(R.id.button_min_up);
        mButtonMinDown = findViewById(R.id.button_min_down);
        mButtonSecUp = findViewById(R.id.button_sec_up);
        mButtonSecDown = findViewById(R.id.button_sec_down);

        SensorManager sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        Sensor accelerationSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        Sensor magneticSensor = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
        sensorManager.registerListener(this, accelerationSensor, SensorManager.SENSOR_DELAY_UI);
        sensorManager.registerListener(this, magneticSensor, SensorManager.SENSOR_DELAY_UI);

        mButtonStartPause.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                System.out.println(mTimerRunning);
                if (mTimerRunning) {
                    mIsStudyTime = false;
                    pauseTimer();
                } else {
                    startTimer();
                }
            }
        });
        mButtonReset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                resetTimer();
            }
        });

        mButtonMinUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mTimerLeftInMillis = mTimerLeftInMillis + 60000;
                updateCountDownText();
            }
        });
        mButtonMinDown.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mTimerLeftInMillis >= 60000) {
                    mTimerLeftInMillis = mTimerLeftInMillis - 60000;
                    updateCountDownText();
                }
            }
        });
        mButtonSecUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mTimerLeftInMillis = mTimerLeftInMillis + 10000;
                updateCountDownText();
            }
        });
        mButtonSecDown.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mTimerLeftInMillis >= 10000) {
                    mTimerLeftInMillis = mTimerLeftInMillis - 10000;
                    updateCountDownText();
                }
            }
        });

        updateCountDownText();
    }

    private void startTimer(){
        mCountDownTimer = new CountDownTimer(mTimerLeftInMillis, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                mTimerLeftInMillis = millisUntilFinished;
                updateCountDownText();
            }
            @Override
            public void onFinish() {
                mTimerRunning = false;
                mIsStudyTime = false;
                mButtonStartPause.setText("START");
                mButtonReset.setVisibility(View.VISIBLE);
                visibleButton();
                ((Vibrator)getSystemService(VIBRATOR_SERVICE)).vibrate(500);
            }
        }.start();

        mTimerRunning = true;
        mIsStudyTime = true;
        mButtonStartPause.setText("STOP");
        mButtonReset.setVisibility(View.INVISIBLE);
        invisibleButton();
    }

    private void pauseTimer(){
        System.out.println("一時停止処理前："+ mTimerRunning);
        mCountDownTimer.cancel();
        mTimerRunning = false;
        System.out.println("一時停止処理後：" + mTimerRunning);
        mButtonStartPause.setText("START");
        mButtonReset.setVisibility(View.VISIBLE);
        visibleButton();
    }
    private void resetTimer(){
        mTimerLeftInMillis = START_TIME;
        mIsStudyTime = false;
        updateCountDownText();
        mButtonStartPause.setVisibility(View.VISIBLE);
        mButtonReset.setVisibility(View.INVISIBLE);
        visibleButton();
    }

    private void updateCountDownText(){
        int minutes = (int) (mTimerLeftInMillis/1000)/60;
        int seconds = (int) (mTimerLeftInMillis/1000)%60;
        String timerLeftFormatted = String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds);
        mTextViewCountDown.setText(timerLeftFormatted);
    }

    private void visibleButton(){
        mButtonMinUp.setVisibility(View.VISIBLE);
        mButtonMinDown.setVisibility(View.VISIBLE);
        mButtonSecUp.setVisibility(View.VISIBLE);
        mButtonSecDown.setVisibility(View.VISIBLE);
    }

    private void invisibleButton(){
        mTextViewMsg.setText("がんばれ！");
        mButtonMinUp.setVisibility(View.INVISIBLE);
        mButtonMinDown.setVisibility(View.INVISIBLE);
        mButtonSecUp.setVisibility(View.INVISIBLE);
        mButtonSecDown.setVisibility(View.INVISIBLE);
    }

    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        switch (sensorEvent.sensor.getType()) {
            case Sensor.TYPE_MAGNETIC_FIELD:mGeoMagneticValue = sensorEvent.values.clone();break;
            case Sensor.TYPE_ACCELEROMETER:mAccelerationValue = sensorEvent.values.clone();break;
        }
        if (mIsStudyTime) {
            SensorManager.getRotationMatrix(mInRotationMatrix, mInclinationMatrix, mAccelerationValue, mGeoMagneticValue);
            SensorManager.remapCoordinateSystem(mInRotationMatrix, SensorManager.AXIS_X, SensorManager.AXIS_Z, mOutRotationMatrix);
            SensorManager.getOrientation(mOutRotationMatrix, mOrientationValue);
            if (Math.toDegrees((double) mOrientationValue[1]) > 50) {
                if(mTimerRunning) {
                    mTextViewMsg.setText("寝たらあかん！"); pauseTimer();
                    ((Vibrator)getSystemService(VIBRATOR_SERVICE)).vibrate(500);
                    ToneGenerator toneGenerator = new ToneGenerator(AudioManager.STREAM_SYSTEM, ToneGenerator.MAX_VOLUME);
                    toneGenerator.startTone(ToneGenerator.TONE_PROP_BEEP);
                }
            } else {
                if(!mTimerRunning) startTimer();
            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }
}