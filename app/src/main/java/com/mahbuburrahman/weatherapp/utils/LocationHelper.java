package com.mahbuburrahman.weatherapp.utils;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.support.v4.app.ActivityCompat;
import android.util.Log;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;

/**
 * Created by Mahbuburrahman on 1/4/18.
 */

public class LocationHelper {

    private Context mContext;

    //Location
    private FusedLocationProviderClient mClient;
    private LocationCallback mCallback;
    private LocationRequest mLocationRequest;

    public LocationHelper(Context context) {
        mContext = context;
        mClient = LocationServices.getFusedLocationProviderClient(mContext);

    }

    public Location getLocation() {
        final Location[] currentLocation = {null};

        if (ActivityCompat.checkSelfPermission(mContext, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(mContext, Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            //ActivityCompat.requestPermissions();

        }
        mClient.requestLocationUpdates(mLocationRequest, mCallback, null);

        mLocationRequest = new LocationRequest()
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                .setInterval(3000)
                .setFastestInterval(1000);

        mCallback = new LocationCallback(){
            @Override
            public void onLocationResult(LocationResult locationResult) {
                super.onLocationResult(locationResult);

                for (Location location: locationResult.getLocations()) {
                    currentLocation[0] = location;
                }

            }
        };
        Log.d("location", "getLocation: "+currentLocation[0].getLatitude());
        return currentLocation[0];
    }

}
