package com.baqterya.wroclawtripplanner.viewmodel

import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import androidx.core.view.get
import androidx.core.view.isVisible
import androidx.core.view.iterator
import androidx.fragment.app.FragmentActivity
import com.baqterya.wroclawtripplanner.R
import com.baqterya.wroclawtripplanner.model.Place
import com.baqterya.wroclawtripplanner.model.Tag
import com.baqterya.wroclawtripplanner.model.Trip
import com.firebase.geofire.GeoQueryBounds
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.google.android.gms.tasks.Task
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.QuerySnapshot
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class FirestoreViewModel {
    private val db = Firebase.firestore
    private val user = Firebase.auth.currentUser

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

    fun updatePlaceTags(place: Place, tagsToAdd: ArrayList<Tag>, tagsToRemove: ArrayList<Tag>, chipGroup: ChipGroup
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

    fun createFindPlacesTask(bounds: List<GeoQueryBounds>, category: String? = null) : ArrayList<Task<QuerySnapshot>> {
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

    fun isPlaceFav(currentPlace: Place, favButton: ImageButton) {
        if (user != null) {
            if (user.uid in currentPlace.placeFavUsersId) {
                favButton.setImageResource(R.drawable.ic_favourite)
                favButton.scaleX = 1.1F
                favButton.scaleY = 1.1F
            }
        }
    }

    fun updatePlaceIsFav(currentPlace: Place, favButton: ImageButton, favCounter: TextView) {
        if (user != null) {
            favButton.scaleX = 1.1F
            favButton.scaleY = 1.1F
            if (user.uid in currentPlace.placeFavUsersId) {
                currentPlace.placeFavUsersId.remove(user.uid)
                db.collection("places").document(currentPlace.placeId!!)
                    .update("placeLikes", FieldValue.increment(-1))
                db.collection("places").document(currentPlace.placeId!!)
                    .update("placeFavUsersId", FieldValue.arrayRemove(user.uid))
                    .addOnSuccessListener {
                        updateLikesCounter(currentPlace, favCounter)
                    }
                favButton.setImageResource(R.drawable.ic_favourite_border)
            } else {
                currentPlace.placeFavUsersId.add(user.uid)
                db.collection("places").document(currentPlace.placeId!!)
                .update("placeLikes", FieldValue.increment(1))
                db.collection("places").document(currentPlace.placeId!!)
                    .update("placeFavUsersId", FieldValue.arrayUnion(user.uid))
                    .addOnSuccessListener {
                        updateLikesCounter(currentPlace, favCounter)
                    }
                favButton.setImageResource(R.drawable.ic_favourite)
            }
        }
    }

    fun updateLikesCounter(currentPlace: Place, favCounter: TextView) {
        db.collection("places").document(currentPlace.placeId!!)
            .get()
            .addOnSuccessListener {
                val likes = it["placeLikes"] as Long
                favCounter.text = likes.toString()
            }
    }

    fun createFindPlacesByTagTask(bounds: List<GeoQueryBounds>) : ArrayList<Task<QuerySnapshot>> {
        val tasks = arrayListOf<Task<QuerySnapshot>>()
        for (bound in bounds) {
            val query = db.collection("places")
                .orderBy("placeGeoHash")
                .startAt(bound.startHash)
                .endAt(bound.endHash)
            tasks.add(query.get())
        }
        return tasks
    }

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

    fun fillUsername(editText: EditText) {
        if (user != null) {
            db.collection("users").document(user.uid)
                .get()
                .addOnSuccessListener {
                    editText.setText(it["userName"] as String)
                }
        }
    }

    fun userLogout() {
        Firebase.auth.signOut()
    }

    fun getFavouritesRecyclerOptions(activity: FragmentActivity) : Pair<FirestoreRecyclerOptions<Place>, FirestoreRecyclerOptions<Trip>>?  {
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

    fun editPlace(currentPlace: Place) {
        db.collection("places").document(currentPlace.placeId!!)
            .set(currentPlace)
    }

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

}