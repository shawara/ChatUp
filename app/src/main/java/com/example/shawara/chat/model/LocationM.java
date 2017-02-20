package com.example.shawara.chat.model;

/**
 * Created by shawara on 12/16/2016.
 */

public class LocationM {
    private double latitude;
    private double longtude;
    private Object time;

    public LocationM() {

    }

    public LocationM(double latitude, double longtude, Object time) {
        this.latitude = latitude;
        this.longtude = longtude;
        this.time = time;
    }

    public double getLongtude() {
        return longtude;
    }

    public void setLongtude(double longtude) {
        this.longtude = longtude;
    }

    public Object getTime() {
        return time;
    }

    public void setTime(Object time) {
        this.time = time;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }
}
