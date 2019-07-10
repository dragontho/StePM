package com.example.stepm;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.support.constraint.ConstraintLayout;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.example.stepm.StepDetector;
import com.example.stepm.StepListener;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.json.JSONObject;

import java.lang.reflect.Type;
import java.util.ArrayList;

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
    private ArrayList<BPMList> finalList;
    TextView TvSteps;
    TextView tvBPM;
    ConstraintLayout layout;


    @Override
    protected void onCreate(Bundle savedInstanceState) {


        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //bpmList = new ArrayList<BPMList>();
        layout = findViewById(R.id.constraintLayout);
        layout.setBackgroundColor(Color.rgb( 179, 229, 252));





//        loadData();
        final Handler handler = new Handler();

        // Get an instance of the SensorManager
        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        accel = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        simpleStepDetector = new StepDetector();
        simpleStepDetector.registerListener(this);

        TvSteps = (TextView) findViewById(R.id.tv_steps);
        tvBPM = findViewById(R.id.bpm);

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
                    finalList = new ArrayList<BPMList>();
                    for (int i = 0; i < App.bpmList.size(); i++) {

                        if (Integer.parseInt(App.bpmList.get(i).BPM) >= calibratedBPM - 5  && Integer.parseInt(App.bpmList.get(i).BPM) <= calibratedBPM + 5) {
                            Log.e("bpm ", App.bpmList.get(i).BPM);
                            finalList.add(App.bpmList.get(i));
                        }
                    }


                    openSongListActivity(calibratedBPM);

                } else {
                    calibrating = true;
                    numSteps = 0;
                    TvSteps.setText(TEXT_NUM_STEPS + numSteps);
                    startTime = SystemClock.elapsedRealtime();
                    sensorManager.registerListener(MainActivity.this, accel, SensorManager.SENSOR_DELAY_FASTEST);

                }


            }
        });

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
        calibratedBPM = (int) (numSteps / ((double) millisecondTime / 60000));
        //if (calibrating) tvBPM.setText("BPM:" + calibratedBPM );
        //else Toast.makeText(getApplicationContext(), "Calibrating!", Toast.LENGTH_SHORT).show();

        tvBPM.setText("BPM:" + calibratedBPM);
        TvSteps.setText(TEXT_NUM_STEPS + numSteps);

    }

    public void openSongListActivity(int BPM) {
        Intent intent = new Intent(this, SongListActivity.class);
        Bundle bundle = new Bundle();
        bundle.putParcelableArrayList("bpmSongs", finalList);
        bundle.putInt("calibratedBPM", calibratedBPM);
        intent.putExtra("bundle", bundle);
//        saveData();
        startActivity(intent);

    }




}
