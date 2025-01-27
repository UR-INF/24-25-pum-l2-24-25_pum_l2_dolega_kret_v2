package com.example.appcar

import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class VehicleAdapter(
    private val vehicleList: MutableList<Vehicle>,
    private val onRefresh: (Vehicle) -> Unit, // Funkcja wywoływana przy kliknięciu Refresh
    private val onDelete: (Vehicle) -> Unit
) : RecyclerView.Adapter<VehicleAdapter.VehicleViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VehicleViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_vehicle, parent, false)
        return VehicleViewHolder(view)
    }

    override fun onBindViewHolder(holder: VehicleViewHolder, position: Int) {
        val vehicle = vehicleList[position]
        holder.bind(vehicle, onRefresh, onDelete)
    }

    override fun getItemCount(): Int = vehicleList.size


    class VehicleViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val deleteButton: ImageButton = itemView.findViewById(R.id.delete_button3)
        private val brandTextView: TextView = itemView.findViewById(R.id.brand_text)
        private val startButton: View = itemView.findViewById(R.id.start_button)
        private val editButton: ImageButton = itemView.findViewById(R.id.edit_button)
        private val infoButton: ImageButton = itemView.findViewById(R.id.info_button)
        private val refreshButton: ImageButton = itemView.findViewById(R.id.refresh_button)

        fun bind(vehicle: Vehicle, onRefresh: (Vehicle) -> Unit, onDelete: (Vehicle) -> Unit) {
            val carName = "${vehicle.brand} ${vehicle.model}"
            brandTextView.text = carName

            // Obsługa przycisku Start
            startButton.setOnClickListener {
                Log.d("VehicleAdapter", "Kliknięto Start dla pojazdu: $carName")
                val intent = Intent(itemView.context, FuelConsumptionActivity::class.java).apply {
                    putExtra("fuelConsumption", vehicle.fuelConsumption)
                    putExtra("carName", carName)
                    putExtra("vehicleId", vehicle.id) // Dodanie vehicleId
                }
                itemView.context.startActivity(intent)
            }


            // Obsługa przycisku Edytuj
            editButton.setOnClickListener {
                Log.d("VehicleAdapter", "Kliknięto Edytuj dla pojazdu: $carName")
                val intent = Intent(itemView.context, EditVehicleActivity::class.java).apply {
                    putExtra("vehicleId", vehicle.id)
                }
                itemView.context.startActivity(intent)
            }

            // Obsługa przycisku Info
            infoButton.setOnClickListener {
                val intent = Intent(itemView.context, VehicleInfoActivity::class.java).apply {
                    putExtra("vehicleId", vehicle.id)
                    putExtra("brand", vehicle.brand)
                    putExtra("model", vehicle.model)
                    putExtra("fuelConsumption", vehicle.fuelConsumption)
                    putExtra("year", vehicle.year)
                    putExtra("fuelType", vehicle.fuelType)
                    putExtra("engineCapacity", vehicle.engineCapacity)
                }
                itemView.context.startActivity(intent)
            }



            // Obsługa przycisku Refresh
            refreshButton.setOnClickListener {
                Log.d("VehicleAdapter", "Kliknięto Refresh dla pojazdu: $carName")
                onRefresh(vehicle) // Wywołanie funkcji przekazanej z MainActivity lub ProfileActivity
            }
            deleteButton.setOnClickListener {
                onDelete(vehicle)
            }
        }
    }
}
