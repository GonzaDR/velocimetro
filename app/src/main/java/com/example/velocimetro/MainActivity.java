package com.example.velocimetro;

import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private TextView speedTextView, distanceTextView;
    private Button resetButton;
    private LocationService locationService;
    private float totalDistance = 0.0f;
    private Location previousLocation;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        speedTextView = findViewById(R.id.speedTextView);
        distanceTextView = findViewById(R.id.distanceTextView);
        resetButton = findViewById(R.id.resetButton);

        // Inicializar el servicio de ubicación
        locationService = new LocationService(this, new LocationServiceCallback() {
            @Override
            public void onLocationUpdated(Location location) {
                updateSpeedAndDistance(location);
            }
        });

        // Verificar y solicitar permisos de ubicación
        if (PermissionHelper.hasLocationPermission(this)) {
            // Si tenemos permiso, iniciar actualizaciones de ubicación
            locationService.startLocationUpdates(this);
        } else {
            // Si no tenemos permiso, solicitarlo
            PermissionHelper.requestLocationPermission(this, LOCATION_PERMISSION_REQUEST_CODE);
        }

        // Resetear valores cuando se presione el botón
        resetButton.setOnClickListener(v -> resetValues());
    }

    // Método para actualizar la velocidad y distancia
    private void updateSpeedAndDistance(Location location) {
        // Calcular la velocidad en km/h
        float speedKmh = location.getSpeed() * 3.6f;
        speedTextView.setText(String.format(Locale.getDefault(), "%.2f km/h", speedKmh));

        // Calcular la distancia si tenemos una ubicación anterior
        if (previousLocation != null) {
            float distance = previousLocation.distanceTo(location) / 1000; // en km
            totalDistance += distance;
            distanceTextView.setText(String.format(Locale.getDefault(), "%.2f km", totalDistance));
        }

        // Actualizar la ubicación anterior
        previousLocation = location;
    }

    // Método para resetear los valores
    private void resetValues() {
        totalDistance = 0.0f;
        speedTextView.setText("0.00 km/h");
        distanceTextView.setText("0.00 km");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        locationService.stopLocationUpdates();
    }

    // Manejo del resultado de la solicitud de permisos
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permiso concedido, iniciar actualizaciones de ubicación
                locationService.startLocationUpdates(this);
            } else {
                // Permiso denegado, mostrar un mensaje
                Toast.makeText(this, "Permiso de ubicación denegado", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
