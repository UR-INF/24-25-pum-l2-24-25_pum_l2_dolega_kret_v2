package com.example.appcar

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

class VehicleActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener  {
    private lateinit var database: DatabaseReference
    private lateinit var auth: FirebaseAuth
    private lateinit var drawerLayout: DrawerLayout

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_car)

        drawerLayout = findViewById(R.id.drawer_layout)
        val navigationView: NavigationView = findViewById(R.id.navigation_view)
        navigationView.setNavigationItemSelectedListener(this)

        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance("https://appcar-4092a-default-rtdb.europe-west1.firebasedatabase.app").reference

        val openDrawerButton = findViewById<Button>(R.id.open_drawer_button)
        openDrawerButton.setOnClickListener {
            drawerLayout.openDrawer(GravityCompat.START)
        }

        val brandEditText = findViewById<EditText>(R.id.brand)
        val modelEditText = findViewById<EditText>(R.id.model)
        val yearEditText = findViewById<EditText>(R.id.year)
        val engineCapacityEditText = findViewById<EditText>(R.id.engine_capacity)
        val fuelTypeEditText = findViewById<EditText>(R.id.fuel_type)
        val fuelConsumptionEditText = findViewById<EditText>(R.id.fuel_consumption)
        val saveButton = findViewById<Button>(R.id.save_button)

        saveButton.setOnClickListener {
            Log.d("VehicleActivity", "Save button clicked")

            val brand = brandEditText.text.toString()
            val model = modelEditText.text.toString()
            val year = yearEditText.text.toString().toIntOrNull()
            val engineCapacity = engineCapacityEditText.text.toString().toDoubleOrNull()
            val fuelType = fuelTypeEditText.text.toString()
            val fuelConsumption = fuelConsumptionEditText.text.toString().toDoubleOrNull()

            if (brand.isNotEmpty() && model.isNotEmpty() && year != null && engineCapacity != null && fuelType.isNotEmpty() && fuelConsumption != null) {
                val userId = auth.currentUser?.uid
                if (userId != null) {
                    val vehicleId = database.child("vehicles").push().key
                    if (vehicleId != null) {
                        val vehicle = Vehicle(vehicleId, brand, model, year, engineCapacity, fuelType, fuelConsumption, userId)
                        database.child("vehicles").child(vehicleId).setValue(vehicle)
                            .addOnSuccessListener {
                                Log.d("VehicleActivity", "Vehicle saved successfully")
                                Toast.makeText(this, "Vehicle saved", Toast.LENGTH_SHORT).show()
                                val intent = Intent(this, ProfileActivity::class.java)
                                startActivity(intent)
                                finish()
                            }
                            .addOnFailureListener { exception ->
                                Log.e("VehicleActivity", "Failed to save vehicle", exception)
                                Toast.makeText(this, "Failed to save vehicle", Toast.LENGTH_SHORT).show()
                            }
                    } else {
                        Log.e("VehicleActivity", "Failed to generate vehicle ID")
                    }
                } else {
                    Log.e("VehicleActivity", "User not authenticated")
                }
            } else {
                Log.e("VehicleActivity", "Please fill in all fields")
                Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.nav_main -> {
                val intent = Intent(this, MainActivity::class.java)
                startActivity(intent)
            }
            R.id.nav_vehicles -> {
                val intent = Intent(this, ProfileActivity::class.java)
                startActivity(intent)
            }
            R.id.nav_history -> {
                val intent = Intent(this, HistoryActivity::class.java)
                startActivity(intent)
            }
            R.id.nav_logout -> {
                auth.signOut()
                val intent = Intent(this, LoginActivity::class.java)
                startActivity(intent)
                finish()
            }
        }
        drawerLayout.closeDrawer(GravityCompat.START)
        return true
    }
}