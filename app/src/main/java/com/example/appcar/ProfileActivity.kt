package com.example.appcar


import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class ProfileActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {
    private lateinit var auth: FirebaseAuth
    private lateinit var database: DatabaseReference
    private lateinit var recyclerView: RecyclerView
    private lateinit var vehicleAdapter: VehicleAdapter
    private val vehicleList = mutableListOf<Vehicle>()
    private lateinit var drawerLayout: DrawerLayout

    companion object {
        const val REQUEST_ADD_CAR = 1
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)
        drawerLayout = findViewById(R.id.drawer_layout)
        val navigationView: NavigationView = findViewById(R.id.navigation_view)
        navigationView.setNavigationItemSelectedListener(this)

        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance("https://appcar-4092a-default-rtdb.europe-west1.firebasedatabase.app").reference

        val openDrawerButton = findViewById<Button>(R.id.open_drawer_button)
        openDrawerButton.setOnClickListener {
            drawerLayout.openDrawer(GravityCompat.START)
        }

        recyclerView = findViewById(R.id.recycler_view)
        recyclerView.layoutManager = LinearLayoutManager(this)

        // Przekazanie funkcji odświeżania i usuwania do adaptera
        vehicleAdapter = VehicleAdapter(vehicleList, { vehicle ->
            refreshSingleVehicle(vehicle)
        }, { vehicle ->
            deleteVehicle(vehicle)
        })
        recyclerView.adapter = vehicleAdapter

        loadUserVehicles() // Załaduj dane użytkownika przy starcie

        // Przycisk dodawania pojazdu
        val addCarButton = findViewById<Button>(R.id.add_car_button)
        addCarButton.setOnClickListener {
            val intent = Intent(this, VehicleActivity::class.java)
            startActivityForResult(intent, REQUEST_ADD_CAR)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_ADD_CAR && resultCode == RESULT_OK) {
            loadUserVehicles() // Ponownie załaduj listę pojazdów po dodaniu nowego
        }
    }
    private fun deleteVehicle(vehicle: Vehicle) {
        val vehicleId = vehicle.id ?: return
        database.child("vehicles").child(vehicleId).removeValue().addOnCompleteListener { task ->
            if (task.isSuccessful) {
                vehicleList.remove(vehicle)
                vehicleAdapter.notifyDataSetChanged()
                Log.d("ProfileActivity", "Vehicle deleted successfully")
            } else {
                Log.e("ProfileActivity", "Failed to delete vehicle: ${task.exception?.message}")
            }
        }
    }

    private fun loadUserVehicles() {
        val userId = auth.currentUser?.uid ?: return
        database.child("vehicles").orderByChild("userId").equalTo(userId)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    vehicleList.clear()
                    for (vehicleSnapshot in snapshot.children) {
                        val vehicle = vehicleSnapshot.getValue(Vehicle::class.java)
                        if (vehicle != null) {
                            vehicleList.add(vehicle)
                        }
                    }
                    vehicleAdapter.notifyDataSetChanged()
                    Log.d("ProfileActivity", "Vehicles loaded: ${vehicleList.size}")
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e("ProfileActivity", "Failed to load vehicles", error.toException())
                }
            })
    }

    private fun refreshSingleVehicle(vehicle: Vehicle) {
        database.child("vehicles").child(vehicle.id ?: "").get()
            .addOnSuccessListener { snapshot ->
                val updatedVehicle = snapshot.getValue(Vehicle::class.java)
                if (updatedVehicle != null) {
                    val index = vehicleList.indexOfFirst { it.id == vehicle.id }
                    if (index != -1) {
                        vehicleList[index] = updatedVehicle
                        vehicleAdapter.notifyItemChanged(index)
                        Log.d("ProfileActivity", "Dane pojazdu odświeżone dla: ${updatedVehicle.brand}")
                    }
                }
            }
            .addOnFailureListener { error ->
                Log.e("ProfileActivity", "Błąd odświeżania pojazdu: ${error.message}")
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