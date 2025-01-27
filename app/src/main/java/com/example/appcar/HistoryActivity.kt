package com.example.appcar

import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import java.text.SimpleDateFormat
import java.util.*

class HistoryActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {
    private lateinit var tripAdapter: TripAdapter
    private lateinit var database: DatabaseReference
    private lateinit var totalDistanceTextView: TextView
    private lateinit var totalFuelUsedTextView: TextView
    private lateinit var filterMonthButton: Button
    private lateinit var filterYearButton: Button
    private lateinit var filterAllButton: Button
    private lateinit var drawerLayout: DrawerLayout
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_history)

        drawerLayout = findViewById(R.id.drawer_layout)
        val navigationView: NavigationView = findViewById(R.id.navigation_view)
        navigationView.setNavigationItemSelectedListener(this)

        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance("https://appcar-4092a-default-rtdb.europe-west1.firebasedatabase.app").reference.child("trips")

        val openDrawerButton = findViewById<Button>(R.id.open_drawer_button)
        openDrawerButton.setOnClickListener {
            drawerLayout.openDrawer(GravityCompat.START)
        }

        val recyclerView = findViewById<RecyclerView>(R.id.recycler_view)
        recyclerView.layoutManager = LinearLayoutManager(this)
        tripAdapter = TripAdapter { trip -> deleteTrip(trip) }
        recyclerView.adapter = tripAdapter

        totalDistanceTextView = findViewById(R.id.total_distance)
        totalFuelUsedTextView = findViewById(R.id.total_fuel_used)
        filterMonthButton = findViewById(R.id.filter_month_button)
        filterYearButton = findViewById(R.id.filter_year_button)
        filterAllButton = findViewById(R.id.filter_all_button)

        filterMonthButton.setOnClickListener { loadTrips(filterBy = "month") }
        filterYearButton.setOnClickListener { loadTrips(filterBy = "year") }
        filterAllButton.setOnClickListener { loadTrips() }

        loadTrips()
    }

    private fun loadTrips(filterBy: String = "") {
        val currentUser = auth.currentUser
        if (currentUser != null) {
            database.orderByChild("userId").equalTo(currentUser.uid).addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val trips = mutableListOf<Trip>()
                    var totalDistance = 0.0
                    var totalFuelUsed = 0.0
                    val currentDate = Date()
                    val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

                    for (tripSnapshot in snapshot.children) {
                        val trip = tripSnapshot.getValue(Trip::class.java)
                        if (trip != null) {
                            val tripDate = dateFormat.parse(trip.date)
                            if (tripDate != null) {
                                when (filterBy) {
                                    "month" -> {
                                        if (isSameMonth(tripDate, currentDate)) {
                                            trips.add(trip)
                                            totalDistance += trip.distance
                                            totalFuelUsed += trip.fuelUsed
                                        }
                                    }
                                    "year" -> {
                                        if (isSameYear(tripDate, currentDate)) {
                                            trips.add(trip)
                                            totalDistance += trip.distance
                                            totalFuelUsed += trip.fuelUsed
                                        }
                                    }
                                    else -> {
                                        trips.add(trip)
                                        totalDistance += trip.distance
                                        totalFuelUsed += trip.fuelUsed
                                    }
                                }
                            }
                        }
                    }
                    tripAdapter.setTrips(trips)
                    totalDistanceTextView.text = "Total Distance: %.2f km".format(totalDistance)
                    totalFuelUsedTextView.text = "Total Fuel Used: %.2f L".format(totalFuelUsed)
                }

                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(this@HistoryActivity, "Failed to load trips: ${error.message}", Toast.LENGTH_SHORT).show()
                }
            })
        }
    }

    private fun deleteTrip(trip: Trip) {
        database.child(trip.id).removeValue().addOnCompleteListener { task ->
            if (task.isSuccessful) {
                Toast.makeText(this, "Trip deleted successfully", Toast.LENGTH_SHORT).show()
                loadTrips()
            } else {
                Toast.makeText(this, "Failed to delete trip: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }
    private fun isSameMonth(date1: Date, date2: Date): Boolean {
        val cal1 = Calendar.getInstance()
        val cal2 = Calendar.getInstance()
        cal1.time = date1
        cal2.time = date2
        return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) && cal1.get(Calendar.MONTH) == cal2.get(Calendar.MONTH)
    }

    private fun isSameYear(date1: Date, date2: Date): Boolean {
        val cal1 = Calendar.getInstance()
        val cal2 = Calendar.getInstance()
        cal1.time = date1
        cal2.time = date2
        return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR)
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