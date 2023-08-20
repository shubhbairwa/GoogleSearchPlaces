package com.codewithfox.googlesearchplaces;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.codewithfox.googlesearchplaces.databinding.ActivityMainBinding;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.widget.AutocompleteSupportFragment;
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity implements OnMapReadyCallback {
    private static final String TAG = "MainActivity";
    private final int FINE_PERMISSION_CODE = 123;
    Location currentLocation;
    FusedLocationProviderClient fusedLocationProviderClient;
    AutocompleteSupportFragment autocompleteSupportFragment;
    ActivityMainBinding binding;
    ArrayList<Place.Field> listSearch = new ArrayList<>();
    GoogleMap gMap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
        getLastLocation();

        Places.initialize(MainActivity.this, getString(R.string.my_maps_api_key));
        autocompleteSupportFragment = (AutocompleteSupportFragment) getSupportFragmentManager().findFragmentById(R.id.autocomplete_fragment);
        listSearch.add(Place.Field.ID);
        listSearch.add(Place.Field.ADDRESS);
        listSearch.add(Place.Field.LAT_LNG);
        autocompleteSupportFragment.setPlaceFields(listSearch);
        autocompleteSupportFragment.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onError(@NonNull Status status) {
                Toast.makeText(MainActivity.this, "After Some Time"+status.getStatusMessage(), Toast.LENGTH_SHORT).show();
                Log.e(TAG, "onError: "+status.getStatusMessage());
            }

            @Override
            public void onPlaceSelected(@NonNull Place place) {
                zoomOnMap(place.getLatLng());
//                Geocoder geocoder = new Geocoder(MainActivity.this, Locale.getDefault());
//                try {
//                    List<Address> addresses = geocoder.getFromLocation(place.get, longitude, 1);
//                    if (addresses != null && addresses.size() > 0) {
//                        Address address = addresses.get(0);
//
//                        // Now, you can access various address details:
//                        String addressLine = address.getAddressLine(0); // Full address
//                        String city = address.getLocality(); // City
//                        String state = address.getAdminArea(); // State
//                        String country = address.getCountryName(); // Country
//                        String postalCode = address.getPostalCode(); // Postal code
//
//                        // You can use these details as needed.
//                    } else {
//                        // Handle the case where no address was found.
//                    }
//                } catch (IOException e) {
//                    e.printStackTrace();
//                }

                gMap.addMarker(new MarkerOptions()
                        .position(place.getLatLng())
                        .title(place.getAddress()));

            }
        });

    }

    private void zoomOnMap(LatLng latLng){
       CameraUpdate latzoom= CameraUpdateFactory.newLatLngZoom(latLng,12f);
        gMap.animateCamera(latzoom);
    }

    private void getLastLocation() {
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, FINE_PERMISSION_CODE);
            return;
        }
        Task<Location> task = fusedLocationProviderClient.getLastLocation();
        task.addOnSuccessListener(location -> {
            if (location != null) {
                currentLocation = location;
                SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                        .findFragmentById(R.id.map);
                mapFragment.getMapAsync(MainActivity.this);
            }
        });

    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        gMap=googleMap;

        // Add a marker in Sydney and move the camera
        LatLng sydney = new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude());
        googleMap.addMarker(new MarkerOptions()
                .position(sydney)
                .title("My Location"));
        googleMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == FINE_PERMISSION_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getLastLocation();
            } else {
                Toast.makeText(this, "NO Found ", Toast.LENGTH_SHORT).show();
            }
        }
    }
}