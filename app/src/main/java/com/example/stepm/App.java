package com.example.stepm;

import android.app.Application;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.SystemClock;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.json.JSONObject;

import java.lang.reflect.Type;
import java.util.ArrayList;

public class App extends Application {

    public static ArrayList<BPMList> bpmList;
    private int numOfSongs = 0;
    private int intermediateProgress = 0;
    private static final String NOTIF_CHANNEL = "notifChannel1";
    private static final int NOTIF_ID = 1;

    @Override
    public void onCreate() {
        super.onCreate();
        Intent notifIntent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notifIntent, 0);

        if (loadData() != getNumOfSongs() || bpmList.size() == 0) getBPMList();

        final int totalSongs = getNumOfSongs();

        final NotificationCompat.Builder builder = new NotificationCompat.Builder(this, NOTIF_CHANNEL)
            .setSmallIcon(R.drawable.icons8_download_24)
            .setContentTitle("Getting your BPMs")
            .setContentText("Downloading from database...")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setProgress(totalSongs, 0, false)
            .setOnlyAlertOnce(true)
            .setOngoing(true);

        createNotificationChannel();
        final NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);

        // notificationId is a unique int for each notification that you must define
        notificationManager.notify(NOTIF_ID, builder.build());

        new Thread(new Runnable() {
            @Override
            public void run() {
                SystemClock.sleep(2000);
                while (intermediateProgress < 1000) {
                    builder.setProgress(totalSongs, intermediateProgress, false);
                    notificationManager.notify(NOTIF_ID, builder.build());
                }
                builder.setContentText("Finished getting BPMs")
                       .setProgress(0, 0, false)
                       .setOngoing(false);
                notificationManager.notify(NOTIF_ID, builder.build());
            }
        }).start();

    }

    private void createNotificationChannel() {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(NOTIF_CHANNEL, "Notif Channel", importance);
            channel.setDescription("To display loading");
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

    private int getNumOfSongs() {
        numOfSongs = 0;
        ContentResolver musicResolver = getContentResolver();
        Uri musicUri = android.provider.MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        Cursor musicCursor = musicResolver.query(musicUri, null, null, null, null);

        while (musicCursor.moveToNext()) numOfSongs++;
        Log.e("songs", numOfSongs + "");
        return numOfSongs;
    }

    private void getBPMList() {
        RequestQueue requestQueue = Volley.newRequestQueue(this);
        SharedPreferences sharedPreferences = getSharedPreferences("shared preferences",MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.clear();
        editor.apply();


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
                        "https://api.getsongbpm.com/search/?api_key=8ece8c1663797a5f4dde5a95d171543f&type=both&lookup=song:" + thisTitle.toLowerCase().replace(" " ,"+") + "artist:" + thisArtist.toLowerCase().replace(" " ,"+"),
                        null,
                        new Response.Listener<JSONObject>() {
                            @Override
                            public void onResponse(JSONObject response) {
                                //TvSteps.setText(response.toString());
                                intermediateProgress++;
                                Log.e("Songs processed", intermediateProgress + "");
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
                                intermediateProgress++;
                                Log.e("Songs processed", intermediateProgress + "");
                            }
                        }


                );
                requestQueue.add(objectRequest);

            }
            while (musicCursor.moveToNext());
        }
        //retrieve song info
    }

    private void saveData() {
        SharedPreferences sharedPreferences = getSharedPreferences("shared preferences",MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        Gson gson = new Gson();
        String json = gson.toJson(bpmList);
        editor.putString("bpm list", json);
        editor.putInt("num of songs",getNumOfSongs());
        editor.apply();
    }

    private int loadData() {
        SharedPreferences sharedPreferences = getSharedPreferences("shared preferences",MODE_PRIVATE);
        Gson gson = new Gson();
        String json = sharedPreferences.getString("bpm list", null);
        Type type = new TypeToken<ArrayList<BPMList>>() {}.getType();
        bpmList = gson.fromJson(json, type);

        if (bpmList == null) {
            bpmList = new ArrayList<>();
        }
        // Log.e(sharedPreferences.getInt("num of songs",0));
        return sharedPreferences.getInt("num of songs",0);

    }
}
