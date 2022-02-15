package com.baqterya.wroclawtripplanner.view.fragment

import android.annotation.SuppressLint
import android.content.Intent
import android.location.Location
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.core.view.iterator
import androidx.fragment.app.Fragment
import androidx.viewpager2.widget.ViewPager2
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.customview.customView
import com.baqterya.wroclawtripplanner.R
import com.baqterya.wroclawtripplanner.databinding.FragmentMapBinding
import com.baqterya.wroclawtripplanner.model.Place
import com.baqterya.wroclawtripplanner.utils.bitmapDescriptorFromVector
import com.baqterya.wroclawtripplanner.utils.createLocationRequest
import com.baqterya.wroclawtripplanner.utils.inputCheck
import com.baqterya.wroclawtripplanner.view.fragment.wrappers.PlaceBottomSheetWrapper
import com.baqterya.wroclawtripplanner.view.fragment.wrappers.TagsBottomSheetWrapper
import com.baqterya.wroclawtripplanner.viewmodel.PlaceViewModel
import com.firebase.geofire.GeoFireUtils
import com.firebase.geofire.GeoLocation
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.*
import com.google.android.gms.maps.*
import com.google.android.gms.maps.model.*
import com.google.android.gms.tasks.Tasks
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.switchmaterial.SwitchMaterial


@Suppress("DEPRECATION")
class MapFragment : Fragment(), OnMapReadyCallback {
    private var _binding: FragmentMapBinding? = null
        private val binding get() = _binding!!

    private lateinit var map: GoogleMap
    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    private lateinit var lastKnownLocation: Location
    private lateinit var locationCallback: LocationCallback

    private val placeViewModel = PlaceViewModel()

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

        val mapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

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
        requireActivity().findViewById<FloatingActionButton>(R.id.fab_add_pin)
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

    @SuppressLint("ResourceType")
    private fun locationButtonSettings() {
        val myLocationButton = requireView().findViewById<View>(R.id.map).findViewById<ImageView>(2)
        val buttonParams = myLocationButton.layoutParams as RelativeLayout.LayoutParams
        buttonParams.addRule(RelativeLayout.ALIGN_PARENT_TOP, 0)
        buttonParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM, RelativeLayout.TRUE)
        buttonParams.setMargins(0, 0, 30, 170)
    }

    private fun addPlace(newPlace: Place) {
        placeViewModel.addPlaceToFirestore(newPlace)
        map.addMarker(
            MarkerOptions()
                .title(newPlace.placeName)
                .position(LatLng(newPlace.placeLatitude!!, newPlace.placeLongitude!!))
                .icon(bitmapDescriptorFromVector(requireContext(), R.drawable.ic_map_pin))
        )
    }

    private fun refreshMapMarkers(category: String? = null) {
        map.clear()
        val location = GeoLocation(map.cameraPosition.target.latitude, map.cameraPosition.target.longitude)
        val bounds = GeoFireUtils.getGeoHashQueryBounds(location, SEARCH_RADIUS_IN_M)
        val tasks = placeViewModel.createFindPlacesTask(bounds, category)

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

                addPlace(newPlace)
                dialog.dismiss()
            } else {
                Toast.makeText(requireContext(), "Please fill all fields.", Toast.LENGTH_SHORT).show()
            }
        }
        dialog.show()
    }

    private fun openPlaceBrowserDrawer(marker: Marker) {
        val location = GeoLocation(map.cameraPosition.target.latitude, map.cameraPosition.target.longitude)
        val bounds = GeoFireUtils.getGeoHashQueryBounds(location, SEARCH_RADIUS_IN_M)
        val tasks = placeViewModel.createFindPlacesTask(bounds)

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
                val bottomSheetWrapper = PlaceBottomSheetWrapper(requireView(), places, idx)
                bottomSheetWrapper.createPlaceBottomSheet()
                bottomSheetWrapper.viewPager2.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback(){
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
                bottomSheetWrapper.placeBottomSheet.show()
            }
    }

    private fun openTagSelectorSheet() {
        val tagsBottomSheetWrapper = TagsBottomSheetWrapper(requireView())
        tagsBottomSheetWrapper.createTagBottomSheet()
        tagsBottomSheetWrapper.fabTagsProceed.setOnClickListener {
            val tags = tagsBottomSheetWrapper.selectedTags
            val tagStrings = arrayListOf<String>()
            for (tag in tags) {
                tagStrings.add(tag.tagName!!)
            }
            tagsBottomSheetWrapper.tagsBottomSheet.dismiss()
            if (tags.isNotEmpty()) findMarkersByTags(tagStrings)
        }
    }

    private fun findMarkersByTags(tags: ArrayList<String>) {
        map.clear()
        val location = GeoLocation(map.cameraPosition.target.latitude, map.cameraPosition.target.longitude)
        val bounds = GeoFireUtils.getGeoHashQueryBounds(location, SEARCH_RADIUS_IN_M)
        val tasks = placeViewModel.createFindPlacesByTagTask(bounds)

        Tasks.whenAllComplete(tasks)
            .addOnCompleteListener {
                for (task in tasks) {
                    val snapshot = task.result
                    for (document in snapshot) {
                        val place = document.toObject(Place().javaClass)
                        val placeTags  = arrayListOf<String>()
                        for (tag in place.placeTagList) {
                            placeTags.add(tag.tagName!!)
                        }
                        if (placeTags.containsAll(tags)) {
                            val latLng = LatLng(
                                document["placeLatitude"] as Double,
                                document["placeLongitude"] as Double
                            )

                            map.addMarker(
                                MarkerOptions()
                                    .title(document["placeName"] as String).position(latLng)
                                    .icon(
                                        bitmapDescriptorFromVector(
                                            requireContext(),
                                            R.drawable.ic_map_pin
                                        )
                                    )
                            )
                        }
                    }
                }
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
