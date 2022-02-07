package com.baqterya.wroclawtripplanner.viewmodel

import com.baqterya.wroclawtripplanner.model.Place
import com.baqterya.wroclawtripplanner.model.Tag
import com.firebase.geofire.GeoQueryBounds
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.QuerySnapshot
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class PlaceViewModel {
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

    fun addTagsToPlace(place: Place, tagList: ArrayList<Tag>) {
        db.collection("places").document(place.placeId!!)
            .get()
            .addOnSuccessListener {
                val placeToUpdate = it.toObject(Place().javaClass)!!
                val placeTagListToUpdate = placeToUpdate.placeTagList
                for (newTag in tagList) {
                    for (oldTag in placeTagListToUpdate) {
                        if (newTag.tagName == oldTag.tagName) {
                            oldTag.tagCounter++
                            break
                        }
                        placeTagListToUpdate.add(newTag)
                    }
                }
                db.collection("places").document(place.placeId!!)
                    .update("placeTagList", placeTagListToUpdate)
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

    fun createFindPlacesByTagTask(bounds: List<GeoQueryBounds>, tagList: ArrayList<Tag>) : ArrayList<Task<QuerySnapshot>> {
        val tasks = arrayListOf<Task<QuerySnapshot>>()
        for (bound in bounds) {
            val query = db.collection("places")
                .orderBy("placeGeoHash")
                .whereArrayContainsAny("placeTagList", tagList)
                .startAt(bound.startHash)
                .endAt(bound.endHash)
            tasks.add(query.get())
        }
        return tasks
    }
}