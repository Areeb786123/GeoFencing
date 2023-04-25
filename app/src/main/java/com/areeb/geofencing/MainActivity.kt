package com.areeb.geofencing

import android.Manifest
import android.app.PendingIntent
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.areeb.geofencing.GeoFencingReciver.GeoFencingReceiver
import com.areeb.geofencing.databinding.ActivityMainBinding
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofencingClient
import com.google.android.gms.location.GeofencingRequest
import com.google.android.gms.location.LocationServices

class MainActivity : AppCompatActivity() {
    private var _binding: ActivityMainBinding? = null
    private val binding get() = _binding!!
    private lateinit var geofencingClient: GeofencingClient
    private val geoFencingRadius = 1000
    private val geofenceLatitude = 24.794870
    private val geofenceLongitude = 84.990400
    private val geofenceTransitionPendingIntent: PendingIntent by lazy {
        val intent = Intent(this, GeoFencingReceiver::class.java)
        PendingIntent.getBroadcast(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(_binding!!.root)

        geofencingClient = LocationServices.getGeofencingClient(this)

        val geofence = Geofence.Builder()
            .setRequestId(GEO_FENCING_ID)
            .setCircularRegion(
                geofenceLatitude,
                geofenceLongitude,
                geoFencingRadius.toFloat(),
            )
            .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER or Geofence.GEOFENCE_TRANSITION_EXIT)
            .setExpirationDuration(Geofence.NEVER_EXPIRE)
            .build()

        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION,
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            addGeofence(geofence)
        } else {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                REQUEST_LOCATION_PERMISSION,
            )
        }
    }

    private fun addGeofence(geofence: Geofence) {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
            == PackageManager.PERMISSION_GRANTED
        ) {
            // Permission is granted, add geofences
            geofencingClient.addGeofences(
                getGeofencingRequest(geofence),
                geofenceTransitionPendingIntent,
            )
                .addOnSuccessListener {
                    Log.d(TAG, "Geofence added")
                }
                .addOnFailureListener {
                    Log.d(TAG, "Geofence not added")
                }
        } else {
            // Permission is not granted, request it
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                REQUEST_LOCATION_PERMISSION,
            )
        }
    }

    private fun getGeofencingRequest(geofence: Geofence): GeofencingRequest {
        return GeofencingRequest.Builder()
            .setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER)
            .addGeofence(geofence)
            .build()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray,
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_LOCATION_PERMISSION) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                val geofence = Geofence.Builder()
                    .setRequestId("MyGeofence")
                    .setCircularRegion(
                        24.794870, // latitude
                        84.990400, // longitude
                        5000f, // radius in meters
                    )
                    .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER)
                    .setExpirationDuration(Geofence.NEVER_EXPIRE)
                    .build()

                addGeofence(geofence)
            }
        }
    }

    companion object {
        private const val TAG = "MainActivity"
        private val GEO_FENCING_ID = "geo_fencing_id"
        private val REQUEST_LOCATION_PERMISSION = 1
    }
}
