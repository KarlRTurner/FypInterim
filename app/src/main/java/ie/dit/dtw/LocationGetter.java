package ie.dit.dtw;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.os.Bundle;
import android.widget.Toast;

/*
  Created by Karl on 28 Nov 2016.
 */

// Reference: The following code is from
//Susan McKeever's Mobile development Class

public class LocationGetter implements LocationListener {
    private Context context;
    private double latitude;
    private double longitude;
    private Location location;
    LocationManager locationManager;

    public LocationGetter(Context context) {
        this.context = context;
        setUpLocation();
    }

    public void onLocationChanged(Location location) {
        if (location != null) {
            latitude = location.getLatitude();
            longitude = location.getLongitude();
            this.location = location;
        }
    }

    public Location getLocation() {
        return location;
    }

    public double getLongitude() {
        return longitude;
    }

    public double getLatitude() {
        return latitude;
    }

    public void onProviderDisabled(String provider) {
        //make user turn on location so app cant retrieve it
        if (provider.equals("gps")) {
            Toast.makeText(context, "GPS is off", Toast.LENGTH_LONG).show();

            context.startActivity(new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS));
        }
    }

    public void onProviderEnabled(String provider) {

        // Code
    }

    public void onStatusChanged(String provider, int status, Bundle extras) {
        // Code..

    }


    private void setUpLocation() {
        Criteria criteria;
        String provider;


        if (ContextCompat.checkSelfPermission(context, android.Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale((Activity) context,
                    android.Manifest.permission.ACCESS_FINE_LOCATION)) {

            } else {
                ActivityCompat.requestPermissions((Activity) context,
                        new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                        2);
            }

        } else {
            //get app to make constant location requests
            locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);

            criteria = new Criteria();
            criteria.setAccuracy(Criteria.ACCURACY_FINE);
            provider = locationManager.getBestProvider(criteria, true);

            location = locationManager.getLastKnownLocation(provider);
            locationManager.requestLocationUpdates(
                    LocationManager.GPS_PROVIDER,
                    1,
                    0,
                    this);
        }
    }

    public void close() {
        if (ContextCompat.checkSelfPermission(context, android.Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale((Activity) context,
                    android.Manifest.permission.ACCESS_FINE_LOCATION)) {

            } else {
                ActivityCompat.requestPermissions((Activity) context,
                        new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                        2);
            }

        } else {
            //stop from constanly updating location when out of the activity
            if (locationManager != null) {
                locationManager.removeUpdates(this);
                locationManager = null;
            }
        }
    }
}