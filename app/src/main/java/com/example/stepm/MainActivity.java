package com.example.stepm;

import android.content.ContentResolver;
import android.content.Intent;
import android.database.Cursor;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
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

import org.json.JSONObject;

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
    private ArrayList<BPMList> bpmList;
    private ArrayList<BPMList> finalList;
    TextView TvSteps;
    TextView tvBPM;


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        bpmList = new ArrayList<BPMList>();
        finalList = new ArrayList<BPMList>();
        getBPMList();
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

                    for (int i = 0; i < bpmList.size(); i++) {

                        if (Integer.parseInt(bpmList.get(i).BPM) >= calibratedBPM - 5  && Integer.parseInt(bpmList.get(i).BPM) <= calibratedBPM + 5) {
                            Log.e("bpm ", bpmList.get(i).BPM);
                            finalList.add(bpmList.get(i));
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
        startActivity(intent);

    }

    private void getBPMList() {
        RequestQueue requestQueue = Volley.newRequestQueue(this);

        ContentResolver musicResolver = getContentResolver();
        Uri musicUri = android.provider.MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        Cursor musicCursor = musicResolver.query(musicUri, null, null, null, null);
        if(musicCursor!=null && musicCursor.moveToFirst()){
            //get columns
            int titleColumn = musicCursor.getColumnIndex
                    (android.provider.MediaStore.Audio.Media.TITLE);
            int idColumn = musicCursor.getColumnIndex
                    (android.provider.MediaStore.Audio.Media._ID);
            int artistColumn = musicCursor.getColumnIndex
                    (android.provider.MediaStore.Audio.Media.ARTIST);
            //add songs to list
            do {
                final long thisId = musicCursor.getLong(idColumn);
                final String thisTitle = musicCursor.getString(titleColumn);
                final String thisArtist = musicCursor.getString(artistColumn);
                JsonObjectRequest objectRequest = new JsonObjectRequest(
                        Request.Method.GET,
                        "https://api.getsongbpm.com/search/?api_key=ca6432926fb5a35c17fa22ee91dcd2c0&type=both&lookup=song:" + thisTitle.toLowerCase().replace(" " ,"+") + "artist:" + thisArtist.toLowerCase().replace(" " ,"+"),
                        null,
                        new Response.Listener<JSONObject>() {
                            @Override
                            public void onResponse(JSONObject response) {
                                //TvSteps.setText(response.toString());
                                if (!response.toString().equals("{\"search\":{\"error\":\"no result\"}}" )) {
                                    Log.e(thisTitle + "-" + thisArtist, response.toString());
                                    Gson gson = new Gson();
                                    SongBPM songBPM = gson.fromJson(response.toString(), SongBPM.class);

                                    if (songBPM.search[0].tempo != null) {
                                        bpmList.add(new BPMList(thisId, thisTitle, thisArtist, songBPM.search[0].tempo));
                                        Log.e("test", bpmList.get(bpmList.size()-1).BPM);
                                    }
                                }
                            }
                        },
                        new Response.ErrorListener() {
                            @Override
                            public void onErrorResponse(VolleyError error) {
                                Log.e("Rest Response", error.toString());
                            }
                        }


                );
                requestQueue.add(objectRequest);

            }
            while (musicCursor.moveToNext());
        }
        //retrieve song info
    }



}
