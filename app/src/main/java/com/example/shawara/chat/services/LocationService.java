package com.example.shawara.chat.services;

import android.Manifest;
import android.app.Service;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.util.Log;

import com.example.shawara.chat.model.LocationM;
import com.example.shawara.chat.utils.Constants;
import com.example.shawara.chat.utils.Utils;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;

/**
 * Created by shawara on 12/14/2016.
 */

public class LocationService extends Service implements GoogleApiClient.ConnectionCallbacks
        , GoogleApiClient.OnConnectionFailedListener, LocationListener {
    private GoogleApiClient mGoogleApiClient;
    private LocationRequest mLocationRequest;
    private static final int LOCATION_REQUEST_INTERVAL = 10 * 1000; //10 seconds
    private static final String TAG = "LocationService";
    private double lat = 0.0, lon = 0.0;
    private DatabaseReference mLocationRef;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(LocationServices.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();

        mLocationRef = FirebaseDatabase.getInstance().
                getReferenceFromUrl(Constants.FIREBASE_URL_LOCATIONS + "/" + Utils.getUid() + "/" + Constants.FIREBASE_LOCATION_LOCATIONS);
        mLocationRef.keepSynced(true);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "onStartCommand: ");
        mGoogleApiClient.connect();

        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        if (mGoogleApiClient.isConnected())
            mGoogleApiClient.disconnect();
        super.onDestroy();
    }

    private void initLocationRequest() {
        //  mLocationRequest= LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        mLocationRequest = LocationRequest.create();
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mLocationRequest.setInterval(LOCATION_REQUEST_INTERVAL);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                    ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                return;
            }
        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        initLocationRequest();
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    public void onLocationChanged(Location location) {
        //Log.d(TAG, "onLocationChanged: " + location.getLatitude() + " " + location.getLongitude());
        if (location.getLatitude() != lat || location.getLongitude() != lon) {
            lat = location.getLatitude();
            lon = location.getLongitude();
            mLocationRef.setValue(new LocationM(lat, lon, ServerValue.TIMESTAMP));
        }
    }
}
