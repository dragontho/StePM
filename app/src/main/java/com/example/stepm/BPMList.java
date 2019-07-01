package com.example.stepm;

import android.os.Parcel;
import android.os.Parcelable;

public class BPMList extends SongList implements Parcelable {
    public String BPM;

    public BPMList(long id, String title, String artist, String BPM) {
        super(id, title, artist);
        this.BPM = BPM;
    }

    @Override
    public int describeContents() {
        return CONTENTS_FILE_DESCRIPTOR;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {

    }

    public String getBPM() {
        return BPM;
    }

}
