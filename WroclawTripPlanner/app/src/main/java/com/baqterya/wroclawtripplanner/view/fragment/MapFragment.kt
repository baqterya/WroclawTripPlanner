package com.baqterya.wroclawtripplanner.view.fragment

import android.annotation.SuppressLint
import android.content.Intent
import android.location.Location
import androidx.fragment.app.Fragment

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.baqterya.wroclawtripplanner.R
import com.baqterya.wroclawtripplanner.databinding.FragmentMapBinding
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.*
import com.google.android.gms.maps.CameraUpdateFactory

import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng


class MapFragment : Fragment() {
    private var _binding: FragmentMapBinding? = null
        private val binding get() = _binding!!

    private lateinit var map: GoogleMap
    private lateinit var mapView: View
    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    private lateinit var lastKnownLocation: Location
    private lateinit var locationCallback: LocationCallback


    @SuppressLint("MissingPermission")
    private val callback = OnMapReadyCallback { googleMap ->
        map = googleMap
        googleMap.isMyLocationEnabled = true
        googleMap.uiSettings.isMyLocationButtonEnabled = true

        val locationRequest = createLocationRequest()
        val builder = LocationSettingsRequest.Builder()
            .addLocationRequest(locationRequest)

        val settingsClient = LocationServices.getSettingsClient(requireActivity())
        settingsClient.checkLocationSettings(builder.build())
            .addOnSuccessListener {
                getDeviceLocation()
            }
            .addOnFailureListener { exception ->
                if (exception is ResolvableApiException) {
                    exception.startResolutionForResult(requireActivity(), 1000)
                }
            }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMapBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(requireContext())

        val mapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment?
        mapFragment?.getMapAsync(callback)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 1000) {
            getDeviceLocation()
        }
    }

    @SuppressLint("MissingPermission")
    private fun getDeviceLocation() {
        fusedLocationProviderClient.lastLocation
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    lastKnownLocation = task.result
                    if (lastKnownLocation != null) {
                        map.moveCamera(
                            CameraUpdateFactory.newLatLngZoom(
                                LatLng(lastKnownLocation.latitude, lastKnownLocation.longitude),
                                DEFAULT_ZOOM
                            )
                        )
                    } else {
                        val locationRequest = LocationRequest.create()
                        locationRequest.interval = 10000
                        locationRequest.fastestInterval = 5000
                        locationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
                        locationCallback = object: LocationCallback() {
                            override fun onLocationResult(locationResult: LocationResult?) {
                                super.onLocationResult(locationResult)
                                if (locationResult == null) {
                                    return
                                }
                                lastKnownLocation = locationResult.lastLocation
                                map.moveCamera(CameraUpdateFactory.newLatLngZoom(
                                    LatLng(lastKnownLocation.latitude, lastKnownLocation.longitude),
                                    DEFAULT_ZOOM
                                ))
                                fusedLocationProviderClient.removeLocationUpdates(locationCallback)
                            }
                        }
                        fusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallback, null)
                    }
                } else {
                    Toast.makeText(requireContext(), "Unable to get your last location", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener { exception ->
                Log.e(TAG, "getDeviceLocation error occurred: ", exception)
            }
    }

    private fun createLocationRequest(): LocationRequest {
        val locationRequest = LocationRequest.create()
        locationRequest.interval = 10000
        locationRequest.fastestInterval = 5000
        locationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        return locationRequest
    }

    companion object {
        private const val TAG = "MAP_FRAGMENT"
        private const val DEFAULT_ZOOM: Float = 18F
    }
}
