package com.baqterya.wroclawtripplanner.view.fragment

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.location.Location
import androidx.fragment.app.Fragment

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.Toast
import androidx.core.content.ContextCompat
import com.baqterya.wroclawtripplanner.R
import com.baqterya.wroclawtripplanner.databinding.FragmentMapBinding
import com.baqterya.wroclawtripplanner.model.Place
import com.baqterya.wroclawtripplanner.view.activity.MainActivity
import com.firebase.geofire.GeoFireUtils
import com.firebase.geofire.GeoLocation
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.*
import com.google.android.gms.maps.CameraUpdateFactory

import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.Tasks
import com.google.android.material.bottomappbar.BottomAppBar
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.QuerySnapshot
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import java.util.*


class MapFragment : Fragment(), OnMapReadyCallback {
    private var _binding: FragmentMapBinding? = null
        private val binding get() = _binding!!

    private lateinit var map: GoogleMap
    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    private lateinit var lastKnownLocation: Location
    private lateinit var locationCallback: LocationCallback

    private val db = Firebase.firestore
    private val user = Firebase.auth.currentUser!!


    @SuppressLint("MissingPermission")
    override fun onMapReady(googleMap: GoogleMap) {
        map = googleMap
        map.isMyLocationEnabled = true
        map.uiSettings.isMyLocationButtonEnabled = true
        map.uiSettings.isCompassEnabled = false
        map.isBuildingsEnabled = true
        map.setMapStyle(MapStyleOptions.loadRawResourceStyle(requireContext(), R.raw.map_style))
        locationButtonSettings()


        val latLngBounds = LatLngBounds(
            LatLng(51.047, 16.936),
            LatLng(51.143, 17.1)
        )
        map.setLatLngBoundsForCameraTarget(latLngBounds)
        map.setMinZoomPreference(MIN_ZOOM)
        map.setMaxZoomPreference(MAX_ZOOM)

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
        mapFragment?.getMapAsync(this)

        binding.buttonFindPlaces.setOnClickListener {
            refreshMapMarkers()
        }
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        requireActivity().findViewById<FloatingActionButton>(R.id.fab_add_pin_show_map)
            .setOnClickListener {
                addPlace()
            }
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

    @SuppressLint("ResourceType")
    private fun locationButtonSettings() {
        val myLocationButton = requireView().findViewById<View>(R.id.map).findViewById<ImageView>(2)
        val buttonParams = myLocationButton.layoutParams as RelativeLayout.LayoutParams
        buttonParams.addRule(RelativeLayout.ALIGN_PARENT_TOP, 0)
        buttonParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM, RelativeLayout.TRUE)
        buttonParams.setMargins(0, 0, 30, 170)
    }

    private fun addPlace() {
        val hash = GeoFireUtils.getGeoHashForLocation(GeoLocation(
            map.cameraPosition.target.latitude,
            map.cameraPosition.target.longitude
        ))

        db.collection("users").whereEqualTo("userId", user.uid)
            .get()
            .addOnSuccessListener {
                for (user in it) {
                    val newPlace = Place(
                        placeName = "testPlace",
                        placeGeoHash = hash,
                        placeLatitude = map.cameraPosition.target.latitude,
                        placeLongitude = map.cameraPosition.target.longitude,
                        placeDescription = "test description",
                        placeOwnerId = user["userId"] as String,
                        placeOwnerName = user["userName"] as String
                    )
                    db.collection("places")
                        .add(newPlace)
                        .addOnSuccessListener { place ->
                            db.collection("places").document(place.id)
                                .update("placeId", place.id)
                        }
                    map.addMarker(
                        MarkerOptions()
                            .title(newPlace.placeName)
                            .position(LatLng(newPlace.placeLatitude!!, newPlace.placeLongitude!!))
                            .icon(bitmapDescriptorFromVector(requireContext(), R.drawable.ic_map_pin))
                    )
                }
            }

    }

    private fun refreshMapMarkers() {
        map.clear()
        val location = GeoLocation(map.cameraPosition.target.latitude, map.cameraPosition.target.longitude)
        val bounds = GeoFireUtils.getGeoHashQueryBounds(location, SEARCH_RADIUS_IN_M)
        val tasks = arrayListOf<Task<QuerySnapshot>>()
        for (bound in bounds) {
            val query = db.collection("places")
                .orderBy("placeGeoHash")
                .startAt(bound.startHash)
                .endAt(bound.endHash)
            tasks.add(query.get())
        }

        Tasks.whenAllComplete(tasks)
            .addOnCompleteListener {
                for (task in tasks) {
                    val snapshot = task.result
                    for (document in snapshot) {
                        val latLng = LatLng(
                            document["placeLatitude"] as Double,
                            document["placeLongitude"] as Double
                        )

                        map.addMarker(
                            MarkerOptions()
                                .title(document["placeName"] as String).position(latLng)
                                .icon(bitmapDescriptorFromVector(requireContext(), R.drawable.ic_map_pin))
                        )
                    }
                }
            }
    }

    private fun bitmapDescriptorFromVector(context: Context, vectorResId: Int): BitmapDescriptor? {
        return ContextCompat.getDrawable(context, vectorResId)?.run {
            setBounds(0, 0, intrinsicWidth, intrinsicHeight)
            val bitmap = Bitmap.createBitmap(intrinsicWidth, intrinsicHeight, Bitmap.Config.ARGB_8888)
            draw(Canvas(bitmap))
            BitmapDescriptorFactory.fromBitmap(bitmap)
        }
    }

    companion object {
        private const val TAG = "MAP_FRAGMENT"
        private const val MAX_ZOOM: Float = 25F
        private const val DEFAULT_ZOOM: Float = 18F
        private const val MIN_ZOOM: Float = 14F
        private const val SEARCH_RADIUS_IN_M: Double = 300.0
    }
}
