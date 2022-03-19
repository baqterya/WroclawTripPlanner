package com.baqterya.wroclawtripplanner.view.fragment.wrappers

import android.content.Context
import android.util.Log
import android.view.Window
import android.view.WindowManager
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.baqterya.wroclawtripplanner.R
import com.baqterya.wroclawtripplanner.model.Place
import com.baqterya.wroclawtripplanner.utils.RecyclerViewAdapter.PlaceOrderRecyclerViewAdapter
import com.baqterya.wroclawtripplanner.view.fragment.MapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.button.MaterialButton
import java.util.*
import kotlin.collections.ArrayList

class TripPlacesBottomSheetWrapper(
    private val context: Context,
    private val parent: MapFragment,
    val places: ArrayList<Place>,
) {
    lateinit var tripOrderBottomSheet: BottomSheetDialog
    var travelMode = "walking"

    fun createTripOrderBottomSheet() {
        tripOrderBottomSheet = BottomSheetDialog(context)
        tripOrderBottomSheet.setContentView(R.layout.fragment_trip_places_bottom_sheet)
        tripOrderBottomSheet.behavior.peekHeight = 600

        val adapter = PlaceOrderRecyclerViewAdapter(places)
        val recyclerView = tripOrderBottomSheet.findViewById<RecyclerView>(R.id.recycler_view_trip_places_order)!!

        tripOrderBottomSheet.findViewById<MaterialButton>(R.id.button_on_foot)?.setOnClickListener {
            travelMode = "walking"
            parent.drawTripPath(getPositions(), travelMode)
        }
        tripOrderBottomSheet.findViewById<MaterialButton>(R.id.button_by_car)?.setOnClickListener {
            travelMode = "driving"
            parent.drawTripPath(getPositions(), travelMode)
        }

        val touchHelper = ItemTouchHelper(object : ItemTouchHelper.SimpleCallback(ItemTouchHelper.UP or ItemTouchHelper.DOWN, 0) {
            override fun onMove(
                recyclerView: RecyclerView,
                source: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean {
                val sourcePosition = source.bindingAdapterPosition
                val targetPosition = target.bindingAdapterPosition
                Collections.swap(places, targetPosition, sourcePosition)
                adapter.notifyItemMoved(sourcePosition, targetPosition)

                parent.drawTripPath(getPositions(), travelMode)
                return true
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {}
        })

        recyclerView.adapter = adapter
        recyclerView.layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
        touchHelper.attachToRecyclerView(recyclerView)
        tripOrderBottomSheet.window?.clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND)
        tripOrderBottomSheet.show()
    }

    private fun getPositions(): ArrayList<LatLng> {
        val positions = arrayListOf<LatLng>()
        for (place in places) {
            positions.add(LatLng(place.placeLatitude!!, place.placeLongitude!!))
        }
        return positions
    }
}