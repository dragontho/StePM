package com.example.stepm;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.SystemClock;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import android.os.Handler;

public class MainActivity extends AppCompatActivity implements SensorEventListener, StepListener {
    private TextView textView;
    private StepDetector simpleStepDetector;
    private SensorManager sensorManager;
    private Sensor accel;
    private static final String TEXT_NUM_STEPS = "Number of Steps: ";
    private int numSteps;
    private long millisecondTime, startTime, endTime;
    private int calibratedBPM;
    private boolean isRunning;
    private boolean calibrating = false;

    TextView TvSteps;
    TextView tvBPM;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        final Handler handler = new Handler();


        // Get an instance of the SensorManager
        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        accel = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        simpleStepDetector = new StepDetector();
        simpleStepDetector.registerListener(this);

        TvSteps = (TextView) findViewById(R.id.tv_steps);
        tvBPM = findViewById(R.id.bpm);
        //Button BtnStart = (Button) findViewById(R.id.btn_start);
        //Button BtnStop = (Button) findViewById(R.id.btn_stop);
        ImageButton calibrateBtn = (ImageButton) findViewById(R.id.calibrateBtn);


        calibrateBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                if (calibrating) {
                    endTime = SystemClock.elapsedRealtime();
                    calibrating = false;
                    numSteps = 0;
                    TvSteps.setText(TEXT_NUM_STEPS + numSteps);
                    sensorManager.unregisterListener(MainActivity.this);
                }
                else {
                    calibrating = true;
                    numSteps = 0;
                    TvSteps.setText(TEXT_NUM_STEPS + numSteps);
                    startTime = SystemClock.elapsedRealtime();
                    sensorManager.registerListener(MainActivity.this, accel, SensorManager.SENSOR_DELAY_FASTEST);

                }

               // TvSteps.setText(TEXT_NUM_STEPS + numSteps);




            }
        });




//        BtnStart.setOnClickListener(new View.OnClickListener() {
//
//            @Override
//            public void onClick(View arg0) {
//
//                numSteps = 0;
//                TvSteps.setText(TEXT_NUM_STEPS + numSteps);
//                startTime = SystemClock.elapsedRealtime();
//                sensorManager.registerListener(MainActivity.this, accel, SensorManager.SENSOR_DELAY_FASTEST);
//                isRunning = true;
//
//            }
//        });

//
//        BtnStop.setOnClickListener(new View.OnClickListener() {
//
//            @Override
//            public void onClick(View arg0) {
//
//                sensorManager.unregisterListener(MainActivity.this);
//                endTime = SystemClock.elapsedRealtime();
//                millisecondTime = endTime - startTime;
//                calibratedBPM = (int)(numSteps / ((double)millisecondTime / 60000));
//                if (isRunning) {
//                    tvBPM.setText("BPM" + calibratedBPM);
//                } else {
//                    Toast.makeText(getApplicationContext(), "Start the pedometer first!", Toast.LENGTH_SHORT).show();
//                }
//                isRunning = false;
//
//            }
//        });



    }



    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            simpleStepDetector.updateAccel(
                    event.timestamp, event.values[0], event.values[1], event.values[2]);
        }
    }

    @Override
    public void step(long timeNs) {
        numSteps++;
        millisecondTime = SystemClock.elapsedRealtime() - startTime;
        calibratedBPM = (int)(numSteps / ((double)millisecondTime / 60000));
        //if (calibrating) tvBPM.setText("BPM:" + calibratedBPM );
        //else Toast.makeText(getApplicationContext(), "Calibrating!", Toast.LENGTH_SHORT).show();

        tvBPM.setText("BPM:" + calibratedBPM );
        TvSteps.setText(TEXT_NUM_STEPS + numSteps);

    }

}
