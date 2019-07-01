package com.example.stepm;

import android.os.Parcel;
import android.os.Parcelable;

public class BPMList implements Parcelable {
    public long id;
    public String title;
    public String artist;
    public String BPM;

    public BPMList(long id, String title, String artist, String BPM) {
        this.id = id;
        this.title = title;
        this.artist = artist;
        this.BPM = BPM;
    }

    public BPMList(Parcel in) {
        this.id = in.readLong();
        this.title = in.readString();
        this.artist = in.readString();
        this.BPM = in.readString();
    }

    public static final Creator<BPMList> CREATOR =
            new Creator<BPMList>() {
                @Override
                public BPMList createFromParcel(Parcel parcel) {
                    return new BPMList(parcel);
                }

                @Override
                public BPMList[] newArray(int i) {
                    return new BPMList[i];
                }
            };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeLong(this.id);
        parcel.writeString(this.title);
        parcel.writeString(this.artist);
        parcel.writeString(this.BPM);
    }

    public String getBPM() {
        return BPM;
    }

    public long getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getArtist() {
        return artist;
    }
}
