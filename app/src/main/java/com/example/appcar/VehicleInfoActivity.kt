package com.example.appcar

import android.app.DatePickerDialog
import android.os.Bundle
import android.util.Log
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.database.*
import java.text.SimpleDateFormat
import java.util.*
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent

class VehicleInfoActivity : AppCompatActivity() {

    private lateinit var database: DatabaseReference
    private lateinit var tripAdapter: TripAdapter
    private lateinit var vehicleId: String
    private lateinit var lastServiceTextView: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_vehicle_info)

        // Pobierz vehicleId z Intent
        vehicleId = intent.getStringExtra("vehicleId") ?: run {
            Toast.makeText(this, "Brak ID pojazdu", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        // Inicjalizacja referencji Firebase
        database = FirebaseDatabase.getInstance("https://appcar-4092a-default-rtdb.europe-west1.firebasedatabase.app").reference

        // Inicjalizacja widoków
        lastServiceTextView = findViewById(R.id.last_service_text)

        // Przycisk dodania przeglądu
        val addServiceButton: ImageButton = findViewById(R.id.add_service_button)
        addServiceButton.setOnClickListener {
            showDatePicker()
        }

        // RecyclerView dla historii tras
        val recyclerView: RecyclerView = findViewById(R.id.trip_recycler_view)
        recyclerView.layoutManager = LinearLayoutManager(this)
        tripAdapter = TripAdapter { trip -> deleteTrip(trip) }
        recyclerView.adapter = tripAdapter

        // Pobierz dane pojazdu i historię tras
        loadVehicleData()
        loadTripHistory()
    }
    private fun scheduleServiceReminders(lastServiceDate: String) {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val lastService = dateFormat.parse(lastServiceDate) ?: return

        val calendarOneMonthBefore = Calendar.getInstance().apply {
            time = lastService
            add(Calendar.MONTH, 11)
        }
        scheduleAlarm(calendarOneMonthBefore.timeInMillis, "Przypomnienie o przeglądzie", "Twój pojazd wymaga przeglądu za miesiąc.")

        val calendarOneWeekBefore = Calendar.getInstance().apply {
            time = lastService
            add(Calendar.MONTH, 12)
            add(Calendar.WEEK_OF_YEAR, -1)
        }
        scheduleAlarm(calendarOneWeekBefore.timeInMillis, "Przypomnienie o przeglądzie", "Twój pojazd wymaga przeglądu za tydzień.")

        val calendarOnTheDay = Calendar.getInstance().apply {
            time = lastService
            add(Calendar.MONTH, 12)
        }
        scheduleAlarm(calendarOnTheDay.timeInMillis, "Przypomnienie o przeglądzie", "Dziś jest ostatni dzień na przegląd pojazdu.")
    }

    private fun scheduleAlarm(timeInMillis: Long, title: String, message: String) {
        val intent = Intent(this, ServiceReminderReceiver::class.java).apply {
            putExtra("title", title)
            putExtra("message", message)
        }
        val pendingIntent = PendingIntent.getBroadcast(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT)
        val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
        alarmManager.setExact(AlarmManager.RTC_WAKEUP, timeInMillis, pendingIntent)
    }

    private fun loadVehicleData() {
        database.child("vehicles").child(vehicleId).get()
            .addOnSuccessListener { snapshot ->
                val vehicle = snapshot.getValue(Vehicle::class.java)
                if (vehicle != null) {
                    val detailsTextView: TextView = findViewById(R.id.vehicle_details_text)
                    detailsTextView.text = """
                        Marka: ${vehicle.brand ?: "Brak danych"}
                        Model: ${vehicle.model ?: "Brak danych"}
                        Rok produkcji: ${vehicle.year}
                        Typ paliwa: ${vehicle.fuelType ?: "Brak danych"}
                        Pojemność silnika: ${vehicle.engineCapacity ?: "Brak danych"}L
                        Spalanie: ${vehicle.fuelConsumption ?: "Brak danych"}L/100km
                    """.trimIndent()

                    // Wyświetlenie daty ostatniego przeglądu
                    val lastService = vehicle.lastService ?: "Brak danych"
                    lastServiceTextView.text = "Ostatni przegląd: $lastService"
                } else {
                    Toast.makeText(this, "Nie znaleziono pojazdu w bazie.", Toast.LENGTH_SHORT).show()
                    finish()
                }
            }
            .addOnFailureListener { exception ->
                Log.e("VehicleInfoActivity", "Błąd pobierania danych pojazdu: ${exception.message}")
                Toast.makeText(this, "Błąd pobierania danych pojazdu.", Toast.LENGTH_SHORT).show()
            }
    }

    private fun loadTripHistory() {
        database.child("trips").orderByChild("vehicleId").equalTo(vehicleId)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val trips = mutableListOf<Trip>()
                    for (tripSnapshot in snapshot.children) {
                        val trip = tripSnapshot.getValue(Trip::class.java)
                        if (trip != null) {
                            trips.add(trip)
                        }
                    }
                    tripAdapter.setTrips(trips)
                    if (trips.isEmpty()) {
                        Toast.makeText(this@VehicleInfoActivity, "Brak tras dla tego pojazdu.", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(this@VehicleInfoActivity, "Błąd pobierania historii tras.", Toast.LENGTH_SHORT).show()
                }
            })
    }



    private fun showDatePicker() {
        val calendar = Calendar.getInstance()
        val datePicker = DatePickerDialog(
            this,
            { _, year, month, dayOfMonth ->
                val selectedDate = Calendar.getInstance()
                selectedDate.set(year, month, dayOfMonth)

                // Formatowanie daty na czytelny format
                val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                val formattedDate = dateFormat.format(selectedDate.time)

                saveLastServiceDate(formattedDate)
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        )
        datePicker.show()
    }

    private fun saveLastServiceDate(date: String) {
        val servicePath = "vehicles/$vehicleId/lastService"

        database.child(servicePath).setValue(date)
            .addOnSuccessListener {
                Toast.makeText(this, "Ostatni przegląd zapisany: $date", Toast.LENGTH_SHORT).show()
                lastServiceTextView.text = "Ostatni przegląd: $date"
                scheduleServiceReminders(date)

                val notificationHelper = NotificationHelper(this)
                notificationHelper.sendNotification("Service Reminder", "Ostatni przegląd zapisany: $date")
            }
            .addOnFailureListener { exception ->
                Log.e("VehicleInfoActivity", "Błąd zapisywania przeglądu: ${exception.message}")
                Toast.makeText(this, "Błąd zapisywania przeglądu.", Toast.LENGTH_SHORT).show()
            }
    }
    private fun deleteTrip(trip: Trip) {
        database.child("trips").child(trip.id).removeValue().addOnCompleteListener { task ->
            if (task.isSuccessful) {
                Toast.makeText(this, "Trip deleted successfully", Toast.LENGTH_SHORT).show()
                loadTripHistory()
            } else {
                Toast.makeText(this, "Failed to delete trip: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
