package com.baqterya.wroclawtripplanner.viewmodel

import android.content.Context
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.core.view.get
import androidx.core.view.isVisible
import androidx.core.view.iterator
import androidx.fragment.app.FragmentActivity
import com.baqterya.wroclawtripplanner.model.Place
import com.baqterya.wroclawtripplanner.model.Tag
import com.baqterya.wroclawtripplanner.model.Trip
import com.firebase.geofire.GeoQueryBounds
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.google.android.gms.tasks.Task
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.QuerySnapshot
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

/**
 * A view model class that facilitates the communication between User Interface and
 * the Firestore Database.
 *
 * @property db: instance of the application's Firestore Database
 * @property user: instance of the currently logged in user
 */
class FirestoreViewModel {
    private val db = Firebase.firestore
    private val user = Firebase.auth.currentUser

    /**
     * Adds a new place to the database.
     */
    fun addPlaceToFirestore(newPlace: Place) {
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

                }
            }
    }

    /**
     * Updates the place's tags according to chips selected by the user.
     */
    fun updatePlaceTags(
        place: Place,
        tagsToAdd: ArrayList<Tag>,
        tagsToRemove: ArrayList<Tag>,
        chipGroup: ChipGroup
    ) {
        if (user != null) {
            for (tag in tagsToAdd) {
                tag.tagAddedByUserIds.add(user.uid)
            }

            db.collection("places").document(place.placeId!!)
                .get()
                .addOnSuccessListener {
                    val placeToUpdate = it.toObject(Place().javaClass)!!
                    var placeTagListToUpdate = placeToUpdate.placeTagList
                    if (placeTagListToUpdate.isEmpty()) {
                        placeTagListToUpdate = tagsToAdd
                    } else {
                        for (newTag in tagsToAdd) {
                            var notFound = true
                            for (oldTag in placeTagListToUpdate) {
                                if (newTag.tagName == oldTag.tagName) {
                                    if (user.uid !in oldTag.tagAddedByUserIds) {
                                        oldTag.tagCounter++
                                        oldTag.tagAddedByUserIds.add(user.uid)
                                    }
                                    notFound = false
                                    break
                                }
                            }
                            if (notFound) {
                                placeTagListToUpdate.add(newTag)
                            }
                        }
                        for (tagToRemove in tagsToRemove) {
                            val itr = placeTagListToUpdate.iterator()
                            while (itr.hasNext()) {
                                val oldTag = itr.next()
                                if (oldTag.tagName == tagToRemove.tagName) {
                                    oldTag.tagAddedByUserIds.remove(user.uid)
                                    if (oldTag.tagAddedByUserIds.isEmpty())
                                        itr.remove()
                                }
                            }
                        }
                    }
                    for (chip in chipGroup) {
                        val idx = chipGroup.indexOfChild(chip)
                        try {
                            val tag = placeTagListToUpdate[idx]
                            val tagString = tag.tagName.plus(" ").plus(tag.tagCounter)
                            (chipGroup[idx] as Chip).text = tagString
                            (chipGroup[idx] as Chip).isVisible = true
                        } catch (e: Exception) {
                            (chipGroup[idx] as Chip).text = ""
                            (chipGroup[idx] as Chip).isVisible = false
                        }

                    }
                    db.collection("places").document(place.placeId!!)
                        .update("placeTagList", placeTagListToUpdate)
                }
        }
    }

    /**
     * Finds the tags already added by the user and checks their chips.
     */
    fun checkUserTags(currentPlace: Place, chipGroups: ArrayList<ChipGroup>) {
        if (user != null) {
            db.collection("places").document(currentPlace.placeId!!)
                .get()
                .addOnSuccessListener {
                    val place = it.toObject(Place().javaClass)!!
                    for (tag in place.placeTagList) {
                        if (user.uid in tag.tagAddedByUserIds) {
                            for (chipGroup in chipGroups) {
                                for (chip in chipGroup) {
                                    if ((chip as Chip).text == tag.tagName) {
                                        chip.isChecked = true
                                    }
                                }
                            }
                        }
                    }
                }
        }
    }

    /**
     * Finds places in GeoBounds around the user.
     *
     * @param category: optional argument that specifies the category of places desired by the user.
     * @return array list of firebase tasks with required documents from the database.
     */
    fun createFindPlacesTask(
        bounds: List<GeoQueryBounds>,
        category: String? = null
    ): ArrayList<Task<QuerySnapshot>> {
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
        return tasks
    }

    /**
     * Finds out whether the place is added to favourites by the current user.
     */
    fun isPlaceFav(currentPlace: Place): Boolean {
        return user?.uid in currentPlace.placeFavUsersId
    }

    /**
     * Updates place's like counter and the list of users that liked it.
     */
    fun updatePlaceIsFav(currentPlace: Place, favCounter: TextView? = null) {
        if (user != null) {
            if (isPlaceFav(currentPlace)) {
                currentPlace.placeFavUsersId.remove(user.uid)
                db.collection("places").document(currentPlace.placeId!!)
                    .update("placeLikes", FieldValue.increment(-1))
                db.collection("places").document(currentPlace.placeId!!)
                    .update("placeFavUsersId", FieldValue.arrayRemove(user.uid))
            } else {
                currentPlace.placeFavUsersId.add(user.uid)
                db.collection("places").document(currentPlace.placeId!!)
                    .update("placeLikes", FieldValue.increment(1))
                db.collection("places").document(currentPlace.placeId!!)
                    .update("placeFavUsersId", FieldValue.arrayUnion(user.uid))
            }
            if (favCounter != null) {
                updatePlaceLikesCounter(currentPlace, favCounter)
            }
        }
    }

    /**
     * Finds out whether the trip is added to favourites by the current user.
     */
    fun isTripFav(currentTrip: Trip): Boolean {
        return user?.uid in currentTrip.tripFavUsersId
    }

    /**
     * Updates trip's like counter and the list of users that liked it.
     */
    fun updateTripIsFav(currentTrip: Trip) {
        if (user != null) {
            if (isTripFav(currentTrip)) {
                currentTrip.tripFavUsersId.remove(user.uid)
                db.collection("trips").document(currentTrip.tripId!!)
                    .update("tripLikes", FieldValue.increment(-1))
                db.collection("trips").document(currentTrip.tripId!!)
                    .update("tripFavUsersId", FieldValue.arrayRemove(user.uid))
            } else {
                currentTrip.tripFavUsersId.remove(user.uid)
                db.collection("trips").document(currentTrip.tripId!!)
                    .update("tripLikes", FieldValue.increment(1))
                db.collection("trips").document(currentTrip.tripId!!)
                    .update("tripFavUsersId", FieldValue.arrayUnion(user.uid))
            }
        }
    }

    /**
     * Updated a UI element that displays place's likes in a non-firestore adapter.
     */
    private fun updatePlaceLikesCounter(currentPlace: Place, favCounter: TextView) {
        db.collection("places").document(currentPlace.placeId!!)
            .get()
            .addOnSuccessListener {
                val likes = it["placeLikes"] as Long
                favCounter.text = likes.toString()
            }
    }

    /**
     * Gets a particular place from the database.
     */
    fun getPlace(placeId: String): Task<DocumentSnapshot> {
        val query = db.collection("places").document(placeId)
        return query.get()
    }

    /**
     * Changes the user's username that is displayed on his places and trips.
     */
    fun changeUsername(newUserName: String) {
        if (user != null) {
            db.collection("users").document(user.uid)
                .update("userName", newUserName)
            db.collection("places")
                .whereEqualTo("placeOwnerId", user.uid)
                .get()
                .addOnSuccessListener {
                    for (place in it) {
                        place.reference.update("placeOwnerName", newUserName)
                    }
                }
        }
    }

    /**
     * Fills a required editText with the current user's username.
     */
    fun fillUsername(editText: EditText) {
        if (user != null) {
            db.collection("users").document(user.uid)
                .get()
                .addOnSuccessListener {
                    editText.setText(it["userName"] as String)
                }
        }
    }

    /**
     * Logs out the user.
     */
    fun userLogout() = Firebase.auth.signOut()


    /**
     * Creates the recycler options for user's favourite places and trips.
     */
    fun getFavouritesRecyclerOptions(activity: FragmentActivity): Pair<FirestoreRecyclerOptions<Place>, FirestoreRecyclerOptions<Trip>>? {
        var options: Pair<FirestoreRecyclerOptions<Place>, FirestoreRecyclerOptions<Trip>>? = null
        if (user != null) {
            val favPlacesQuery = db.collection("places")
                .whereArrayContains("placeFavUsersId", user.uid)
                .whereEqualTo("placeIsPrivate", false)
                .orderBy("placeLikes")
                .orderBy("placeName")
            val favPlacesOptions = FirestoreRecyclerOptions.Builder<Place>()
                .setQuery(favPlacesQuery, Place::class.java)
                .setLifecycleOwner(activity)
                .build()

            val favTripsQuery = db.collection("trips")
                .whereArrayContains("tripFavUsersId", user.uid)
                .orderBy("tripLikes")
                .orderBy("tripName")
            val favTripsOptions = FirestoreRecyclerOptions.Builder<Trip>()
                .setQuery(favTripsQuery, Trip::class.java)
                .setLifecycleOwner(activity)
                .build()

            options = Pair(favPlacesOptions, favTripsOptions)
        }
        return options
    }

    /**
     * Creates the recycler options for user's own places and trips.
     */
    fun getUsersPlacesTripsRecyclerOptions(activity: FragmentActivity): Pair<FirestoreRecyclerOptions<Place>, FirestoreRecyclerOptions<Trip>>? {
        var options: Pair<FirestoreRecyclerOptions<Place>, FirestoreRecyclerOptions<Trip>>? = null
        if (user != null) {
            val favPlacesQuery = db.collection("places")
                .whereEqualTo("placeOwnerId", user.uid)
                .orderBy("placeLikes")
                .orderBy("placeName")
            val favPlacesOptions = FirestoreRecyclerOptions.Builder<Place>()
                .setQuery(favPlacesQuery, Place::class.java)
                .setLifecycleOwner(activity)
                .build()

            val favTripsQuery = db.collection("trips")
                .whereEqualTo("tripOwnerId", user.uid)
                .orderBy("tripLikes")
                .orderBy("tripName")
            val favTripsOptions = FirestoreRecyclerOptions.Builder<Trip>()
                .setQuery(favTripsQuery, Trip::class.java)
                .setLifecycleOwner(activity)
                .build()

            options = Pair(favPlacesOptions, favTripsOptions)
        }
        return options
    }

    /**
     * Creates the recycler options for all trips.
     */
    fun getAllTripsOptions(activity: FragmentActivity): FirestoreRecyclerOptions<Trip>? {
        var options: FirestoreRecyclerOptions<Trip>? = null
        if (user != null) {
            val userTripsQuery = db.collection("trips")
                .whereEqualTo("tripIsPrivate", false)
                .orderBy("tripLikes", Query.Direction.DESCENDING)
                .limit(100)
                .orderBy("tripName")
            options = FirestoreRecyclerOptions.Builder<Trip>()
                .setQuery(userTripsQuery, Trip::class.java)
                .setLifecycleOwner(activity)
                .build()
        }
        return options
    }

    /**
     * Creates the recycler options for user's trips.
     */
    fun getUserTripOptions(activity: FragmentActivity): FirestoreRecyclerOptions<Trip>? {
        var options: FirestoreRecyclerOptions<Trip>? = null
        if (user != null) {
            val userTripsQuery = db.collection("trips")
                .whereEqualTo("tripOwnerId", user.uid)
                .orderBy("tripLikes")
                .orderBy("tripName")
            options = FirestoreRecyclerOptions.Builder<Trip>()
                .setQuery(userTripsQuery, Trip::class.java)
                .setLifecycleOwner(activity)
                .build()
        }
        return options
    }

    /**
     * Sets category chips' isChecked attribute according to edited place's categories.
     */
    fun editPlaceSetCategoryChips(currentPlace: Place, dialogChips: ChipGroup) {
        db.collection("places").document(currentPlace.placeId!!)
            .get()
            .addOnSuccessListener {
                val place = it.toObject(Place::class.java)!!
                for (chip in dialogChips) {
                    if ((chip as Chip).text in place.placeCategories) {
                        chip.isChecked = true
                    }
                }
            }
    }

    /**
     * Updates a place according to changes done by the user.
     */
    fun editPlace(currentPlace: Place) {
        currentPlace.placeCategories = currentPlace.placeCategories.distinct() as ArrayList<String>
        db.collection("places").document(currentPlace.placeId!!)
            .set(currentPlace)
    }

    /**
     * Adds a trip to the Firestore database.
     */
    fun addTripToFirestore(currentPlaceId: String, newTrip: Trip) {
        db.collection("users").whereEqualTo("userId", user?.uid)
            .get()
            .addOnSuccessListener {
                for (user in it) {
                    newTrip.tripOwnerId = user["userId"] as String
                    newTrip.tripOwnerName = user["userName"] as String
                    newTrip.tripPlaceIdList.add(currentPlaceId)
                    db.collection("trips")
                        .add(newTrip)
                        .addOnSuccessListener { trip ->
                            db.collection("trips").document(trip.id)
                                .update("tripId", trip.id)
                        }

                }
            }
    }

    /**
     * Adds a place to a trip.
     */
    fun addPlaceToTrip(currentPlace: Place, currentTrip: Trip, context: Context) {
        if (currentTrip.tripPlaceIdList.size == 10) {
            Toast.makeText(context, "A trip can only have up to 10 places", Toast.LENGTH_SHORT).show()
            return
        }
        if (currentPlace.placeId !in currentTrip.tripPlaceIdList) {
            db.collection("trips").document(currentTrip.tripId!!)
                .update("tripPlaceIdList", FieldValue.arrayUnion(currentPlace.placeId!!))
        }
    }

    /**
     * Removes a place from a trip.
     */
    fun removePlaceFromTrip(currentPlace: Place, currentTrip: Trip) {
        if (currentPlace.placeId in currentTrip.tripPlaceIdList) {
            db.collection("trips").document(currentTrip.tripId!!)
                .update("tripPlaceIdList", FieldValue.arrayRemove(currentPlace.placeId!!))
            currentTrip.tripPlaceIdList.remove(currentPlace.placeId)
        }
    }

    /**
     * Creates recycler options for a list of trip's places.
     */
    fun getTripPlacesOptions(
        activity: FragmentActivity,
        currentTrip: Trip
    ): FirestoreRecyclerOptions<Place>? {
        var options: FirestoreRecyclerOptions<Place>? = null
        if (user != null) {
            if (currentTrip.tripPlaceIdList.isNotEmpty()) {
                val tripPlaceQuery = db.collection("places")
                    .whereIn("placeId", currentTrip.tripPlaceIdList)
                options = FirestoreRecyclerOptions.Builder<Place>()
                    .setQuery(tripPlaceQuery, Place::class.java)
                    .setLifecycleOwner(activity)
                    .build()
            }
        }
        return options
    }

    /**
     * Updates a trip according to changes done by the user.
     */
    fun editTrip(currentTrip: Trip) {
        db.collection("trips").document(currentTrip.tripId!!).set(currentTrip)
    }

    /**
     * Deletes a place from the database.
     */
    fun deletePlace(currentPlace: Place) {
        db.collection("trips")
            .get()
            .addOnSuccessListener {
                for (trip in it) {
                    trip.reference.update(
                        "tripPlaceIdList",
                        FieldValue.arrayRemove(currentPlace.placeId)
                    )
                }
            }
        db.collection("places").document(currentPlace.placeId!!)
            .delete()
    }

    /**
     * Deletes a trip from the database.
     */
    fun deleteTrip(currentTrip: Trip) {
        db.collection("trips").document(currentTrip.tripId!!)
            .delete()
    }

    /**
     * Creates a task list containing all places in a trip.
     */
    fun createShowTripPlacesTask(tripToShow: Trip): ArrayList<Task<QuerySnapshot>> {
        val tasks = arrayListOf<Task<QuerySnapshot>>()
        for (placeId in tripToShow.tripPlaceIdList) {
            val query = db.collection("places")
                .whereEqualTo("placeId", placeId)
            tasks.add(query.get())
        }
        return tasks
    }
}