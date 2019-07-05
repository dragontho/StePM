package com.example.stepm;

import android.Manifest;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;

import com.example.stepm.MusicService;
import com.example.stepm.MusicService.MusicBinder;
import com.example.stepm.SongList;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.function.Predicate;

public class SongListActivity extends AppCompatActivity /*implements MediaPlayerControl*/{
    private ArrayList<SongList> songList;
    private ListView songView;
    private MusicService musicSrv;
    private Intent playIntent;
    private boolean musicBound=false;
    private ArrayList<BPMList> bpmList;
    private int calibratedBPM;
    private Bundle received;
    



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_song_list);

        //SharedPreferences settings = getSharedPreferences(PREFS_NAME,0);
        //bpmList = settings.ge

        received = getIntent().getBundleExtra("bundle");

        bpmList = received.getParcelableArrayList("bpmSongs");
        calibratedBPM = received.getInt("calibratedBPM");


        //setController();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE)
                    != PackageManager.PERMISSION_GRANTED) {

                requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},1);

// MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE is an
// app-defined int constant

                return;
            }}
        songView = (ListView)findViewById(R.id.song_list);

//        Log.e("testing", bpmList.get(0).BPM);



        Collections.sort(bpmList, new Comparator<BPMList>(){
            public int compare(BPMList a, BPMList b){
                return a.getTitle().compareTo(b.getTitle());
            }
        });



//        for (BPMList entry: bpmList) {
//            if (Integer.parseInt(entry.BPM) < calibratedBPM - 2 || Integer.parseInt(entry.BPM) > calibratedBPM + 2) {
//                bpmList.remove(entry);
//            }
//        }

        SongAdapter songAdt = new SongAdapter(this, bpmList);
        songView.setAdapter(songAdt);

    }

    //connect to the service
    private ServiceConnection musicConnection = new ServiceConnection(){

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            MusicBinder binder = (MusicBinder)service;
            //get service
            musicSrv = binder.getService();
            //pass list

            musicSrv.setList(bpmList);
            musicBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            musicBound = false;
        }
    };


    @Override
    protected void onStart() {
        super.onStart();
        if(playIntent==null){
            playIntent = new Intent(this, MusicService.class);
            bindService(playIntent, musicConnection, Context.BIND_AUTO_CREATE);
            startService(playIntent);
        }
    }

    public void songPicked(View view){
        musicSrv.setSong(Integer.parseInt(view.getTag().toString()));
        musicSrv.playSong();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        //menu item selected
        switch (item.getItemId()) {
            case R.id.action_shuffle:
                //shuffle
                break;
            case R.id.action_end:
                stopService(playIntent);
                musicSrv=null;
                System.exit(0);
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onDestroy() {
        stopService(playIntent);
        musicSrv=null;
        super.onDestroy();
    }


    @Override
    public void onPointerCaptureChanged(boolean hasCapture) {

    }
}
