package com.example.appcar

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class TripAdapter(private val onDeleteClick: (Trip) -> Unit) : RecyclerView.Adapter<TripAdapter.TripViewHolder>() {
    private var trips: List<Trip> = listOf()

    fun setTrips(trips: List<Trip>) {
        this.trips = trips
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TripViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_trip, parent, false)
        return TripViewHolder(view, onDeleteClick)
    }

    override fun onBindViewHolder(holder: TripViewHolder, position: Int) {
        val trip = trips[position]
        holder.bind(trip)
    }

    override fun getItemCount(): Int = trips.size

    class TripViewHolder(itemView: View, private val onDeleteClick: (Trip) -> Unit) : RecyclerView.ViewHolder(itemView) {
        private val startLocation: TextView = itemView.findViewById(R.id.start_location)
        private val destination: TextView = itemView.findViewById(R.id.destination)
        private val distance: TextView = itemView.findViewById(R.id.distance)
        private val fuelUsed: TextView = itemView.findViewById(R.id.fuel_used)
        private val date: TextView = itemView.findViewById(R.id.date)
        private val carName: TextView = itemView.findViewById(R.id.car_name)
        private val deleteButton: ImageButton = itemView.findViewById(R.id.delete_button)

        fun bind(trip: Trip) {
            startLocation.text = "From: ${trip.startLocation}"
            destination.text = "To: ${trip.destination}"
            distance.text = "Distance: %.2f km".format(trip.distance)
            fuelUsed.text = "Fuel used: %.2f L".format(trip.fuelUsed)
            date.text = "Date: ${trip.date}"
            carName.text = "Car Name: ${trip.carName}"

            deleteButton.setOnClickListener {
                onDeleteClick(trip)
            }
        }
    }
}