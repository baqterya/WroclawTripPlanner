package com.baqterya.wroclawtripplanner.view.fragment

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.location.Location
import androidx.fragment.app.Fragment

import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.core.content.ContextCompat
import androidx.core.view.children
import androidx.core.view.forEach
import androidx.core.view.iterator
import androidx.viewpager2.widget.ViewPager2
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.customview.customView
import com.baqterya.wroclawtripplanner.R
import com.baqterya.wroclawtripplanner.databinding.FragmentMapBinding
import com.baqterya.wroclawtripplanner.model.Place
import com.baqterya.wroclawtripplanner.utils.PlaceViewPagerAdapter
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
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.switchmaterial.SwitchMaterial
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.QuerySnapshot
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase


@Suppress("DEPRECATION")
class MapFragment : Fragment(), OnMapReadyCallback {
    private var _binding: FragmentMapBinding? = null
        private val binding get() = _binding!!

    private lateinit var map: GoogleMap
    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    private lateinit var lastKnownLocation: Location
    private lateinit var locationCallback: LocationCallback

    private val db = Firebase.firestore
    private val user = Firebase.auth.currentUser

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

        map.setOnMarkerClickListener { marker : Marker ->
            openPlaceBrowserDrawer(marker)
            false
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

        for (chip in binding.chipGroupMapFilters.iterator()) {
            if (chip.id != R.id.chip_open_tags) {
                chip.setOnClickListener {
                    refreshMapMarkers(category = (it as Chip).text.toString())
                }
            } else {
                chip.setOnClickListener {
                    openTagSelectorSheet()
                }
            }
        }
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        requireActivity().findViewById<FloatingActionButton>(R.id.fab_add_pin_show_map)
            .setOnClickListener {
                showAddPlaceDialog()
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
            .addOnSuccessListener { location : Location? ->
                if (location != null) {
                    lastKnownLocation = location
                    map.moveCamera(
                        CameraUpdateFactory.newLatLngZoom(
                            LatLng(lastKnownLocation.latitude, lastKnownLocation.longitude),
                            DEFAULT_ZOOM
                        )
                    )
                } else {
                    val locationRequest = createLocationRequest()
                    locationCallback = object : LocationCallback() {
                        override fun onLocationResult(locationResult: LocationResult?) {
                            super.onLocationResult(locationResult)
                            if (locationResult == null) {
                                return
                            }
                            lastKnownLocation = locationResult.lastLocation
                            map.moveCamera(
                                CameraUpdateFactory.newLatLngZoom(
                                    LatLng(
                                        lastKnownLocation.latitude,
                                        lastKnownLocation.longitude
                                    ),
                                    DEFAULT_ZOOM
                                )
                            )
                            fusedLocationProviderClient.removeLocationUpdates(
                                locationCallback
                            )
                        }
                    }
                    fusedLocationProviderClient.requestLocationUpdates(
                        locationRequest,
                        locationCallback,
                        null
                    )
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

    private fun addPlaceToFirestore(newPlace: Place) {
        db.collection("users").whereEqualTo("userId", user?.uid)
            .get()
            .addOnSuccessListener {
                for (user in it) {
                    newPlace.placeOwnerId = user["userId"] as String
                    newPlace.placeOwnerName = user["userName"] as String
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

    private fun openPlaceBrowserDrawer(marker: Marker) {
        val location = GeoLocation(marker.position.latitude, marker.position.longitude)
        val bounds = GeoFireUtils.getGeoHashQueryBounds(location, SEARCH_RADIUS_IN_M)
        val tasks = arrayListOf<Task<QuerySnapshot>>()
        for (bound in bounds) {
            val query = db.collection("places")
                .orderBy("placeGeoHash")
                .whereEqualTo("placeIsPrivate", false)
                .startAt(bound.startHash)
                .endAt(bound.endHash)
            tasks.add(query.get())
        }

        Tasks.whenAllComplete(tasks)
            .addOnCompleteListener {
                val places = arrayListOf<Place>()
                var idx = 0
                for (task in tasks) {
                    val snapshot = task.result
                    for (document in snapshot) {
                        val place = document.toObject(Place::class.java)
                        places.add(place)
                        if (place.placeLatitude == marker.position.latitude && place.placeLongitude == marker.position.longitude) {
                            idx = places.indexOf(place)
                        }
                    }
                }
                val bottomSheet = BottomSheetDialog(requireContext())
                bottomSheet.setContentView(R.layout.fragment_place_bottom_sheet)
                val adapter = PlaceViewPagerAdapter(places)
                val viewPager = bottomSheet.findViewById<ViewPager2>(R.id.view_pager_2_places)!!
                viewPager.adapter = adapter
                viewPager.currentItem = idx
                viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback(){
                    override fun onPageScrolled(
                        position: Int,
                        positionOffset: Float,
                        positionOffsetPixels: Int
                    ) {
                        super.onPageScrolled(position, positionOffset, positionOffsetPixels)
                        map.animateCamera(CameraUpdateFactory.newLatLngZoom(
                            LatLng(
                                places[position].placeLatitude!!,
                                places[position].placeLongitude!!
                            ),
                            DEFAULT_ZOOM
                        ))
                    }
                })
                bottomSheet.behavior.peekHeight = this.requireView().height

                bottomSheet.show()
            }
    }

    private fun refreshMapMarkers(category: String? = null) {
        map.clear()
        val location = GeoLocation(map.cameraPosition.target.latitude, map.cameraPosition.target.longitude)
        val bounds = GeoFireUtils.getGeoHashQueryBounds(location, SEARCH_RADIUS_IN_M)
        val tasks = arrayListOf<Task<QuerySnapshot>>()
        for (bound in bounds) {
            if (category == null) {
                val query = db.collection("places")
                    .orderBy("placeGeoHash")
                    .whereEqualTo("placeIsPrivate", false)
                    .startAt(bound.startHash)
                    .endAt(bound.endHash)
                tasks.add(query.get())
            } else {
                val query = db.collection("places")
                    .orderBy("placeGeoHash")
                    .whereEqualTo("placeIsPrivate", false)
                    .whereArrayContains("placeCategories", category)
                    .startAt(bound.startHash)
                    .endAt(bound.endHash)
                tasks.add(query.get())
            }
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

    private fun showAddPlaceDialog() {
        val dialog = MaterialDialog(requireContext())
            .noAutoDismiss()
            .customView(R.layout.dialog_add_place)

        val newPlace = Place(
            placeLatitude = map.cameraPosition.target.latitude,
            placeLongitude = map.cameraPosition.target.longitude,
        )

        val dialogChips = dialog.findViewById<ChipGroup>(R.id.chip_group_place_category_picker)

        for (dialogChip in dialogChips.iterator()) {
            dialog.findViewById<Chip>(dialogChip.id).setOnCheckedChangeListener { chip, isChecked ->
                if (isChecked) {
                    newPlace.placeCategories.add(chip.text.toString())
                } else {
                    newPlace.placeCategories.remove(chip.text.toString())
                }
            }
        }

        dialog.findViewById<Button>(R.id.button_add_place).setOnClickListener {
            val placeName = dialog.findViewById<EditText>(R.id.edit_text_add_place_name).text.toString()
            val placeDescription = dialog.findViewById<EditText>(R.id.edit_text_add_place_description).text.toString()
            val placeIsPrivate = dialog.findViewById<SwitchMaterial>(R.id.switch_add_place_is_private).isChecked

            val isCategoryPicked = dialogChips.checkedChipIds.isNotEmpty()

            if (inputCheck(placeName) && inputCheck(placeDescription) && isCategoryPicked) {
                val hash = GeoFireUtils.getGeoHashForLocation(GeoLocation(
                    map.cameraPosition.target.latitude,
                    map.cameraPosition.target.longitude
                ))

                newPlace.placeName = placeName
                newPlace.placeGeoHash = hash
                newPlace.placeDescription = placeDescription
                newPlace.placeIsPrivate = placeIsPrivate

                addPlaceToFirestore(newPlace)
                dialog.dismiss()
            } else {
                Toast.makeText(requireContext(), "Please fill all fields.", Toast.LENGTH_SHORT).show()
            }
        }
        dialog.show()
    }

    private fun openTagSelectorSheet() {
        val bottomSheet = BottomSheetDialog(requireContext())
        bottomSheet.setContentView(R.layout.fragment_tags_bottom_sheet)
        bottomSheet.behavior.peekHeight = this.requireView().height
        bottomSheet.behavior.isDraggable = false
        bottomSheet.show()
    }

    private fun inputCheck(name: String): Boolean {
        return !(TextUtils.isEmpty(name))
    }

    companion object {
        private const val TAG = "MAP_FRAGMENT"
        private const val MAX_ZOOM: Float = 25F
        private const val DEFAULT_ZOOM: Float = 18F
        private const val MIN_ZOOM: Float = 14F
        private const val SEARCH_RADIUS_IN_M: Double = 300.0
    }
}
