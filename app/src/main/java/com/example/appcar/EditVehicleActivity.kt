package com.example.appcar

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.database.FirebaseDatabase

class EditVehicleActivity : AppCompatActivity() {

    private lateinit var database: FirebaseDatabase

    private lateinit var brandEditText: EditText
    private lateinit var modelEditText: EditText
    private lateinit var yearEditText: EditText
    private lateinit var engineCapacityEditText: EditText
    private lateinit var fuelTypeEditText: EditText
    private lateinit var fuelConsumptionEditText: EditText
    private lateinit var saveButton: Button

    private lateinit var vehicleId: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_vehicle)

        database = FirebaseDatabase.getInstance("https://appcar-4092a-default-rtdb.europe-west1.firebasedatabase.app")

        // Pobieranie ID pojazdu z Intent
        vehicleId = intent.getStringExtra("vehicleId") ?: run {
            Toast.makeText(this, "Nie przekazano ID pojazdu", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        // Powiązanie widoków z XML
        brandEditText = findViewById(R.id.edit_brand)
        modelEditText = findViewById(R.id.edit_model)
        yearEditText = findViewById(R.id.edit_year)
        engineCapacityEditText = findViewById(R.id.edit_engine_capacity)
        fuelTypeEditText = findViewById(R.id.edit_fuel_type)
        fuelConsumptionEditText = findViewById(R.id.edit_fuel_consumption)
        saveButton = findViewById(R.id.save_button)

        // Załaduj dane pojazdu
        loadVehicleData()

        // Obsługa zapisu zmian
        saveButton.setOnClickListener {
            saveChanges()
        }
    }

    private fun loadVehicleData() {
        database.reference.child("vehicles").child(vehicleId).get()
            .addOnSuccessListener { snapshot ->
                val vehicle = snapshot.getValue(Vehicle::class.java)
                if (vehicle != null) {
                    brandEditText.setText(vehicle.brand)
                    modelEditText.setText(vehicle.model)
                    yearEditText.setText(vehicle.year.toString())
                    engineCapacityEditText.setText(vehicle.engineCapacity.toString())
                    fuelTypeEditText.setText(vehicle.fuelType)
                    fuelConsumptionEditText.setText(vehicle.fuelConsumption.toString())
                } else {
                    Toast.makeText(this, "Nie znaleziono pojazdu.", Toast.LENGTH_SHORT).show()
                    finish()
                }
            }
            .addOnFailureListener { exception ->
                Log.e("EditVehicleActivity", "Błąd wczytywania danych: ${exception.message}")
                Toast.makeText(this, "Błąd wczytywania danych.", Toast.LENGTH_SHORT).show()
            }
    }

    private fun saveChanges() {
        val updatedBrand = brandEditText.text.toString()
        val updatedModel = modelEditText.text.toString()
        val updatedYear = yearEditText.text.toString().toIntOrNull()
        val updatedEngineCapacity = engineCapacityEditText.text.toString().toDoubleOrNull()
        val updatedFuelType = fuelTypeEditText.text.toString()
        val updatedFuelConsumption = fuelConsumptionEditText.text.toString().toDoubleOrNull()

        if (updatedBrand.isEmpty() || updatedModel.isEmpty() || updatedYear == null ||
            updatedEngineCapacity == null || updatedFuelType.isEmpty() || updatedFuelConsumption == null
        ) {
            Toast.makeText(this, "Wypełnij wszystkie pola poprawnie.", Toast.LENGTH_SHORT).show()
            return
        }

        val updates = mapOf(
            "brand" to updatedBrand,
            "model" to updatedModel,
            "year" to updatedYear,
            "engineCapacity" to updatedEngineCapacity,
            "fuelType" to updatedFuelType,
            "fuelConsumption" to updatedFuelConsumption
        )

        database.reference.child("vehicles").child(vehicleId).updateChildren(updates)
            .addOnSuccessListener {
                Log.d("EditVehicleActivity", "Zmiany zapisane pomyślnie.")
                Toast.makeText(this, "Zmiany zapisane pomyślnie.", Toast.LENGTH_SHORT).show()
                finish() // Zamknij aktywność
            }
            .addOnFailureListener { exception ->
                Log.e("EditVehicleActivity", "Błąd zapisywania danych: ${exception.message}")
                Toast.makeText(this, "Błąd zapisywania danych.", Toast.LENGTH_SHORT).show()
            }
    }

}
