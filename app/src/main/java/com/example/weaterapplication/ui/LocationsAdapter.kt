package com.example.weaterapplication.ui

import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.weaterapplication.data.location.ForecastLocation

class LocationsAdapter(val clickListener: LocationClickListener) : RecyclerView.Adapter<LocationViewHolder>() {

    var locations = ArrayList<ForecastLocation>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LocationViewHolder = LocationViewHolder(parent)

    override fun onBindViewHolder(holder: LocationViewHolder, position: Int) {
        holder.bind(locations[position])
        holder.itemView.setOnClickListener { clickListener.onLocationClick(locations[position]) }
    }

    override fun getItemCount(): Int = locations.size

    fun interface LocationClickListener {
        fun onLocationClick(location: ForecastLocation)
    }
}