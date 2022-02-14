package com.baqterya.wroclawtripplanner.viewmodel

import androidx.core.view.contains
import androidx.core.view.get
import androidx.core.view.isVisible
import androidx.core.view.iterator
import com.baqterya.wroclawtripplanner.model.Place
import com.baqterya.wroclawtripplanner.model.Tag
import com.firebase.geofire.GeoQueryBounds
import com.google.android.gms.tasks.Task
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.QuerySnapshot
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import java.lang.Exception

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
}