package com.example.shawara.chat.ui;

import android.Manifest;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.support.v7.app.AlertDialog;
import android.widget.Toast;

import com.example.shawara.chat.R;
import com.example.shawara.chat.model.LocationM;
import com.example.shawara.chat.model.User;
import com.example.shawara.chat.services.LocationService;
import com.example.shawara.chat.utils.Constants;
import com.example.shawara.chat.utils.Utils;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class LocationActivity extends FragmentActivity implements OnMapReadyCallback {
    public static final int LOCATION_PERMISSION_REQUEST = 23;
    public static final String EXTRA_USER = "user_data";
    private static final String TAG = "LocationActivity";
    private SupportMapFragment mMapFragment;
    private GoogleMap mGoogleMap;
    private DatabaseReference mLocationRef;
    private User mUser;
    private Marker mMarker;
    private MarkerOptions mMarkerOptions = new MarkerOptions().position(new LatLng(0, 0));
    private boolean isZoomed = false;

    public static Intent newIntent(Context c, User user) {
        Intent i = new Intent(c, LocationActivity.class);
        i.putExtra(EXTRA_USER, user);
        return i;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_location);

        mUser = (User) getIntent().getSerializableExtra(EXTRA_USER);
        mLocationRef = FirebaseDatabase.getInstance()
                .getReferenceFromUrl(Constants.FIREBASE_URL_LOCATIONS
                        + "/" + mUser.getUid() + "/" + Constants.FIREBASE_LOCATION_LOCATIONS);

        mMapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map_fragment);
        mMapFragment.getMapAsync(this);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
            getGPSPermission();
        else
            startService(new Intent(this, LocationService.class));

    }

    @Override
    protected void onStart() {
        super.onStart();
        final LocationManager manager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        if (!manager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            buildAlertMessageNoGps();
        }
    }


    private void buildAlertMessageNoGps() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Your GPS seems to be disabled, do you want to enable it?")
                .setCancelable(false)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(@SuppressWarnings("unused") final DialogInterface dialog, @SuppressWarnings("unused") final int id) {
                        startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS));
                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    public void onClick(final DialogInterface dialog, @SuppressWarnings("unused") final int id) {
                        dialog.cancel();
                    }
                });
        final AlertDialog alert = builder.create();
        alert.show();
    }

    @TargetApi(Build.VERSION_CODES.M)
    private void getGPSPermission() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) ==
                PackageManager.PERMISSION_GRANTED
                ) {
            startService(new Intent(this, LocationService.class));

        } else {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION)) {
                Toast.makeText(this, "please open GPS", Toast.LENGTH_SHORT).show();
            }

            requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION
            }, LOCATION_PERMISSION_REQUEST);

        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == LOCATION_PERMISSION_REQUEST) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startService(new Intent(this, LocationService.class));
            } else {
                Toast.makeText(this, "Please open gps and allow permission", Toast.LENGTH_SHORT).show();
            }
        } else {
            super.onRequestPermissionsResult(LOCATION_PERMISSION_REQUEST, permissions, grantResults);
        }
    }


    @Override
    public void onMapReady(GoogleMap googleMap) {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        googleMap.setMyLocationEnabled(true);
        mGoogleMap = googleMap;
        mMarker = mGoogleMap.addMarker(mMarkerOptions);
        startLocationListener();

//        double la=30.198979, lo=31.121541;
//        CameraPosition cameraPosition = new CameraPosition.Builder()
//                .target(new LatLng(la, lo)).zoom(14).build();
//        googleMap.animateCamera(CameraUpdateFactory
//                .newCameraPosition(cameraPosition));
    }

    private void startLocationListener() {
        mLocationRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (!dataSnapshot.exists())
                    return;

                LocationM location = dataSnapshot.getValue(LocationM.class);


                mMarker.setPosition(new LatLng(location.getLatitude(), location.getLongtude()));

                CameraPosition cameraPosition = new CameraPosition.Builder()
                        .target(new LatLng(location.getLatitude(), location.getLongtude())).zoom(14).build();

                if (!isZoomed) {
                    mGoogleMap.animateCamera(CameraUpdateFactory
                            .newCameraPosition(cameraPosition));
                    isZoomed = true;

                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }
}
