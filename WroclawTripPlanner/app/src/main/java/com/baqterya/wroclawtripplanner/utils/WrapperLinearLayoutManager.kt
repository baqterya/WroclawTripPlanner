package com.baqterya.wroclawtripplanner.utils

import android.content.Context
import android.util.Log
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class WrapperLinearLayoutManager(context: Context) : LinearLayoutManager(context) {
    /**
     * Linear layout manager wrapper that handles an exception that occurs when returning to
     * the app with a recycler view opened.
     */
    override fun onLayoutChildren(recycler: RecyclerView.Recycler?, state: RecyclerView.State?) {
        try {
            super.onLayoutChildren(recycler, state)
        } catch (e: IndexOutOfBoundsException) {
            Log.e("qwerty", "onLayoutChildren: ", e)
        }
    }
}