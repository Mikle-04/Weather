package com.example.weaterapplication.ui

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.weaterapplication.R
import com.example.weaterapplication.data.location.ForecastLocation

class LocationViewHolder(parent: ViewGroup) :
    RecyclerView.ViewHolder(
        LayoutInflater.from(parent.context)
        .inflate(R.layout.view_list_item, parent, false)) {

    var name: TextView = itemView.findViewById(R.id.locationName)

    fun bind(location: ForecastLocation) {
        name.text = "${location.name} (${location.country})"
    }
}