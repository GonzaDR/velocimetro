package com.example.velocimetro;

import android.app.Activity;
import android.content.Context;
import android.location.Location;
import android.os.Looper;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationRequest;

public class LocationService {

    private final FusedLocationProviderClient fusedLocationProviderClient;
    private final LocationRequest locationRequest;
    private final LocationServiceCallback callback;
    private final Context context;
    private LocationCallback locationCallback;

    public LocationService(Context context, LocationServiceCallback callback) {
        this.context = context;
        this.callback = callback;
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(context);

        // Crear LocationRequest
        locationRequest = new LocationRequest.Builder(LocationRequest.PRIORITY_HIGH_ACCURACY, 2000)
                .setMinUpdateIntervalMillis(1000)
                .build();
    }

    // Iniciar las actualizaciones de ubicación
    public void startLocationUpdates(Activity activity) {
        if (PermissionHelper.hasLocationPermission(context)) {
            locationCallback = new LocationCallback() {
                @Override
                public void onLocationResult(LocationResult locationResult) {
                    if (locationResult != null) {
                        for (Location location : locationResult.getLocations()) {
                            callback.onLocationUpdated(location);
                        }
                    }
                }
            };

            fusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper());
        } else {
            // Si no tiene permisos, solicitarlos usando PermissionHelper
            PermissionHelper.requestLocationPermission(activity, 1);
        }
    }

    // Detener las actualizaciones de ubicación
    public void stopLocationUpdates() {
        if (locationCallback != null) {
            fusedLocationProviderClient.removeLocationUpdates(locationCallback);
        }
    }
}
