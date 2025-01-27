package com.example.appcar

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.widget.Autocomplete
import com.google.android.libraries.places.widget.AutocompleteActivity
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode
import com.google.firebase.auth.FirebaseAuth
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import android.Manifest
import android.content.pm.PackageManager
import android.location.Geocoder
import android.view.MenuItem
import android.widget.ImageButton
import androidx.core.app.ActivityCompat
import androidx.core.view.GravityCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.model.LatLng
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class FuelConsumptionActivity : AppCompatActivity() {

    private lateinit var database: DatabaseReference
    private lateinit var startLocationInput: EditText
    private lateinit var destinationInput: EditText
    private lateinit var distanceResult: TextView
    private lateinit var fuelUsageResult: TextView
    private lateinit var rangeResult: TextView
    private lateinit var fuelInput: EditText
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private val AUTOCOMPLETE_REQUEST_CODE_START = 1
    private val AUTOCOMPLETE_REQUEST_CODE_DESTINATION = 2
    companion object {
        private const val REQUEST_LOCATION_PERMISSION = 1
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_fuel_consumption)

        database = FirebaseDatabase.getInstance("https://appcar-4092a-default-rtdb.europe-west1.firebasedatabase.app").reference
        startLocationInput = findViewById(R.id.start_location_input)
        destinationInput = findViewById(R.id.destination_input)
        distanceResult = findViewById(R.id.distance_result)
        fuelUsageResult = findViewById(R.id.fuel_usage_result)
        rangeResult = findViewById(R.id.range_result)
        fuelInput = findViewById(R.id.fuel_input)
        val openMapsButton: Button = findViewById(R.id.open_maps_button)
        val calculateFuelUsageButton: Button = findViewById(R.id.calculate_fuel_usage_button)
        val calculateButton: Button = findViewById(R.id.calculate_button)
        val findGasStationButton: Button = findViewById(R.id.find_gas_station_button)
        val sendRouteSmsButton: Button = findViewById(R.id.send_route_sms_button)

        val fuelConsumption = intent.getDoubleExtra("fuelConsumption", 0.0)
        val carName = intent.getStringExtra("carName") ?: "Unknown Car"
        val vehicleId = intent.getStringExtra("vehicleId") ?: "Unknown Vehicle"

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        // Inicjalizacja Google Places API
        if (!Places.isInitialized()) {
            Places.initialize(applicationContext, "AIzaSyDgOgamFFaS1gkMUMaG0_F0yqH_bOStTZs")
        }

        // Obsługa pola miejsca początkowego
        startLocationInput.setOnClickListener {
            val intent = Autocomplete.IntentBuilder(AutocompleteActivityMode.OVERLAY, listOf(Place.Field.NAME, Place.Field.LAT_LNG))
                .build(this)
            startActivityForResult(intent, AUTOCOMPLETE_REQUEST_CODE_START)
        }


        startLocationInput = findViewById(R.id.start_location_input)
        val setCurrentLocationButton: ImageButton = findViewById(R.id.gps_button)
        setCurrentLocationButton.setOnClickListener {
            getCurrentLocation()
        }

        // Obsługa pola miejsca docelowego
        destinationInput.setOnClickListener {
            val intent = Autocomplete.IntentBuilder(AutocompleteActivityMode.OVERLAY, listOf(Place.Field.NAME, Place.Field.LAT_LNG))
                .build(this)
            startActivityForResult(intent, AUTOCOMPLETE_REQUEST_CODE_DESTINATION)
        }

        // Otwieranie Google Maps
        openMapsButton.setOnClickListener {
            val startLocation = startLocationInput.text.toString().trim()
            val destination = destinationInput.text.toString().trim()

            if (startLocation.isNotEmpty() && destination.isNotEmpty()) {
                Thread {
                    val (distance, duration) = calculateDistance(startLocation, destination)
                    runOnUiThread {
                        if (distance > 0) {
                            openGoogleMaps(startLocation, destination)
                            val fuelUsed = (distance / 100) * fuelConsumption
                            saveTripToFirebase(startLocation, destination, distance, fuelUsed, carName,vehicleId)
                        } else {
                            Toast.makeText(this, "Nie można obliczyć dystansu", Toast.LENGTH_SHORT).show()
                        }
                    }
                }.start()
            } else {
                Toast.makeText(this, "Proszę wypełnić oba pola", Toast.LENGTH_SHORT).show()
            }
        }

        // Obliczanie zużycia paliwa i dystansu
        calculateFuelUsageButton.setOnClickListener {
            val startLocation = startLocationInput.text.toString().trim()
            val destination = destinationInput.text.toString().trim()

            if (startLocation.isNotEmpty() && destination.isNotEmpty()) {
                Thread {
                    val (distance, duration) = calculateDistance(startLocation, destination)
                    runOnUiThread {
                        if (distance > 0) {
                            distanceResult.text = "Dystans: %.2f km".format(distance)

                            if (fuelConsumption > 0) {
                                val fuelUsed = (distance / 100) * fuelConsumption
                                fuelUsageResult.text = "Zużycie paliwa: %.2f L".format(fuelUsed)
                            } else {
                                Toast.makeText(this, "Brak danych o spalaniu", Toast.LENGTH_SHORT).show()
                            }
                        } else {
                            Toast.makeText(this, "Nie można obliczyć dystansu", Toast.LENGTH_SHORT).show()
                        }
                    }
                }.start()
            } else {
                Toast.makeText(this, "Proszę wypełnić oba pola", Toast.LENGTH_SHORT).show()
            }
        }

        // Obliczanie zasięgu na podstawie paliwa
        calculateButton.setOnClickListener {
            val fuel = fuelInput.text.toString().toDoubleOrNull()
            if (fuel != null && fuel > 0) {
                if (fuelConsumption > 0) {
                    val range = (fuel / fuelConsumption) * 100
                    rangeResult.text = "Zasięg: %.2f km".format(range)
                } else {
                    Toast.makeText(this, "Brak danych o spalaniu", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(this, "Wprowadź poprawną ilość paliwa", Toast.LENGTH_SHORT).show()
            }
        }
        // Znajdowanie najbliższej stacji benzynowej
        findGasStationButton.setOnClickListener {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), 1)
                return@setOnClickListener
            }
            fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                if (location != null) {
                    val currentLocation = LatLng(location.latitude, location.longitude)
                    findNearestGasStation(currentLocation, fuelConsumption, carName,vehicleId)
                } else {
                    Toast.makeText(this, "Nie można uzyskać aktualnej lokalizacji", Toast.LENGTH_SHORT).show()
                }
            }
        }
        sendRouteSmsButton.setOnClickListener {
            val startLocation = startLocationInput.text.toString().trim()
            val destination = destinationInput.text.toString().trim()

            if (startLocation.isNotEmpty() && destination.isNotEmpty()) {
                Thread {
                    val (distance, duration) = calculateDistance(startLocation, destination)
                    runOnUiThread {
                        if (distance > 0) {
                            distanceResult.text = "Dystans: %.2f km".format(distance)

                            if (fuelConsumption > 0) {
                                val fuelUsed = (distance / 100) * fuelConsumption
                                fuelUsageResult.text = "Zużycie paliwa: %.2f L".format(fuelUsed)
                                val message = "Trasa: $startLocation do $destination\nDystans: %.2f km\nCzas przejazdu: $duration\nZużycie paliwa: %.2f L".format(distance, fuelUsed)

                                val smsIntent = Intent(Intent.ACTION_SENDTO).apply {
                                    data = Uri.parse("smsto:") // This ensures only SMS apps handle this
                                    putExtra("sms_body", message)
                                }

                                if (smsIntent.resolveActivity(packageManager) != null) {
                                    startActivity(smsIntent)
                                } else {
                                    Toast.makeText(this, "Brak aplikacji do wysyłania SMS", Toast.LENGTH_SHORT).show()
                                }
                            } else {
                                Toast.makeText(this, "Brak danych o spalaniu", Toast.LENGTH_SHORT).show()
                            }
                        } else {
                            Toast.makeText(this, "Nie można obliczyć dystansu", Toast.LENGTH_SHORT).show()
                        }
                    }
                }.start()
            } else {
                Toast.makeText(this, "Proszę wypełnić oba pola", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (resultCode == RESULT_OK && data != null) {
            val place = Autocomplete.getPlaceFromIntent(data)
            when (requestCode) {
                AUTOCOMPLETE_REQUEST_CODE_START -> startLocationInput.setText(place.name)
                AUTOCOMPLETE_REQUEST_CODE_DESTINATION -> destinationInput.setText(place.name)
            }
        } else if (resultCode == AutocompleteActivity.RESULT_ERROR) {
            val status = Autocomplete.getStatusFromIntent(data)
            Toast.makeText(this, "Błąd: ${status.statusMessage}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun openGoogleMaps(startLocation: String, destination: String) {
        val uri = Uri.parse("https://www.google.com/maps/dir/?api=1&origin=$startLocation&destination=$destination&travelmode=driving")
        val intent = Intent(Intent.ACTION_VIEW, uri)
        intent.setPackage("com.google.android.apps.maps")

        if (intent.resolveActivity(packageManager) != null) {
            startActivity(intent)
        } else {
            Toast.makeText(this, "Google Maps nie jest zainstalowane", Toast.LENGTH_SHORT).show()
        }
    }
    private fun findNearestGasStation(currentLocation: LatLng, fuelConsumption: Double, carName: String,vehicleId: String) {
        val apiKey = "AIzaSyDgOgamFFaS1gkMUMaG0_F0yqH_bOStTZs"
        val url = "https://maps.googleapis.com/maps/api/place/nearbysearch/json?location=${currentLocation.latitude},${currentLocation.longitude}&radius=5000&type=gas_station&key=$apiKey"

        val client = OkHttpClient()
        val request = Request.Builder()
            .url(url)
            .build()

        Thread {
            try {
                val response = client.newCall(request).execute()
                if (response.isSuccessful) {
                    val jsonData = response.body?.string()
                    val jsonObject = JSONObject(jsonData ?: "")
                    val results = jsonObject.getJSONArray("results")
                    if (results.length() > 0) {
                        val nearestGasStation = results.getJSONObject(0)
                        val location = nearestGasStation.getJSONObject("geometry").getJSONObject("location")
                        val lat = location.getDouble("lat")
                        val lng = location.getDouble("lng")
                        val destination = "$lat,$lng"
                        val destinationName = nearestGasStation.getString("name")

                        val (distance, _) = calculateDistance("${currentLocation.latitude},${currentLocation.longitude}", destination)
                        val fuelUsed = (distance / 100) * fuelConsumption

                        runOnUiThread {
                            val uri = Uri.parse("https://www.google.com/maps/dir/?api=1&origin=${currentLocation.latitude},${currentLocation.longitude}&destination=$destination&travelmode=driving")
                            val intent = Intent(Intent.ACTION_VIEW, uri)
                            intent.setPackage("com.google.android.apps.maps")

                            if (intent.resolveActivity(packageManager) != null) {
                                startActivity(intent)
                                saveTripToFirebase("${currentLocation.latitude},${currentLocation.longitude}", destinationName, distance, fuelUsed, carName,vehicleId)
                            } else {
                                Toast.makeText(this, "Google Maps nie jest zainstalowane", Toast.LENGTH_SHORT).show()
                            }
                        }
                    } else {
                        runOnUiThread {
                            Toast.makeText(this, "Nie znaleziono stacji benzynowych w pobliżu", Toast.LENGTH_SHORT).show()
                        }
                    }
                } else {
                    runOnUiThread {
                        Toast.makeText(this, "Nie udało się pobrać stacji benzynowych", Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                runOnUiThread {
                    Toast.makeText(this, "Błąd: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }.start()
    }

    private fun calculateDistance(startLocation: String, destination: String): Pair<Double, String> {
        val apiKey = "AIzaSyDgOgamFFaS1gkMUMaG0_F0yqH_bOStTZs"
        val url = "https://maps.googleapis.com/maps/api/distancematrix/json?origins=$startLocation&destinations=$destination&key=$apiKey"

        val client = OkHttpClient()
        val request = Request.Builder()
            .url(url)
            .build()

        return try {
            val response = client.newCall(request).execute()
            if (response.isSuccessful) {
                val jsonData = response.body?.string()
                val jsonObject = JSONObject(jsonData ?: "")
                val rows = jsonObject.getJSONArray("rows")
                val elements = rows.getJSONObject(0).getJSONArray("elements")
                val distance = elements.getJSONObject(0).getJSONObject("distance").getDouble("value")
                val duration = elements.getJSONObject(0).getJSONObject("duration").getString("text")

                // Return distance in kilometers and duration as a string
                Pair(distance / 1000, duration)
            } else {
                Pair(0.0, "N/A")
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Pair(0.0, "N/A")
        }
    }
    private fun saveTripToFirebase(
        startLocation: String,
        destination: String,
        distance: Double,
        fuelUsed: Double,
        carName: String,
        vehicleId: String // Dodanie vehicleId jako parametru
    ) {
        val user = FirebaseAuth.getInstance().currentUser
        if (user != null) {
            val tripId = database.child("trips").push().key
            if (tripId != null) {
                val date = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(Date())
                val trip = Trip(
                    id = tripId,
                    startLocation = startLocation,
                    destination = destination,
                    distance = distance,
                    fuelUsed = fuelUsed,
                    date = date,
                    carName = carName,
                    userId = user.uid,
                    vehicleId = vehicleId // Ustawienie vehicleId
                )
                database.child("trips").child(tripId).setValue(trip)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            Toast.makeText(this, "Trip saved successfully", Toast.LENGTH_SHORT).show()
                        } else {
                            Toast.makeText(this, "Failed to save trip: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                        }
                    }
            } else {
                Toast.makeText(this, "Failed to generate trip ID", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun getCurrentLocation() {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                REQUEST_LOCATION_PERMISSION
            )
        } else {
            fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                if (location != null) {
                    val geocoder = Geocoder(this, Locale.getDefault())
                    val addresses = geocoder.getFromLocation(location.latitude, location.longitude, 1)
                    if (!addresses.isNullOrEmpty()) {
                        val address = addresses[0].getAddressLine(0) // Pełny adres
                        startLocationInput.setText(address) // Ustaw adres w polu tekstowym
                        Toast.makeText(this, "Lokalizacja ustawiona: $address", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(this, "Nie znaleziono adresu", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(this, "Nie można pobrać lokalizacji", Toast.LENGTH_SHORT).show()
                }
            }.addOnFailureListener { e ->
                Toast.makeText(this, "Błąd podczas pobierania lokalizacji: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_LOCATION_PERMISSION) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getCurrentLocation()
            } else {
                Toast.makeText(this, "Uprawnienia lokalizacji są wymagane", Toast.LENGTH_SHORT).show()
            }
        }
    }

}