package ie.dit.dtw;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

/*
  Created by Karl on 25 Nov 2016.
 */

public class MapFragment extends Fragment {

    MapView mMapView;
    private GoogleMap googleMap;
    float latitude;
    float longitude;

    // Reference: The following code is from
    //https://gist.github.com/joshdholtz/4522551

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.map_view, container, false);

        mMapView = (MapView) rootView.findViewById(R.id.mapView2);
        mMapView.onCreate(savedInstanceState);
        update();


        return rootView;
    }

    @Override
    public void setArguments(Bundle args) {
        super.setArguments(args);
        latitude = args.getFloat("lat");
        longitude = args.getFloat("long");
    }

    //
    private void update() {
        try {
            //change location on map to photo location
            mMapView.getMapAsync(new OnMapReadyCallback() {
                @Override
                public void onMapReady(GoogleMap googleMap) {
                    try {
                        MapsInitializer.initialize(getActivity().getApplicationContext());
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    LatLng local = new LatLng(latitude, longitude);
                    googleMap.addMarker(new MarkerOptions().position(local));

                    CameraPosition cameraPosition = new CameraPosition.Builder().target(local).zoom(15).build();
                    googleMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
                }
            });

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        mMapView.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        mMapView.onPause();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mMapView.onDestroy();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mMapView.onLowMemory();
    }
}
