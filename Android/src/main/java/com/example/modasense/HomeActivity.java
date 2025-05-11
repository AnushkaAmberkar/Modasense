package com.example.modasense;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.FirebaseApp;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import android.widget.LinearLayout;
import androidx.viewpager2.widget.ViewPager2;
import com.example.modasense.BannerAdapter;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import android.util.Log;

public class HomeActivity extends AppCompatActivity {

    private static final int LOCATION_PERMISSION_REQUEST_CODE = 100;
    private FusedLocationProviderClient fusedLocationClient;

    private Retrofit retrofit;
    private WeatherApiService weatherApiService;

    private FirebaseFirestore db;
    private AutoCompleteTextView autoCompleteWeather;
    private AutoCompleteTextView autoCompleteOccasion;

    private Handler handler;
    private Runnable runnable;

    private ViewPager2 bannerViewPager;
    private BannerAdapter bannerAdapter;
    private Handler bannerHandler = new Handler();

    private String currentSeason = "Spring";  // default value

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        bannerViewPager = findViewById(R.id.bannerViewPager);
        int[] bannerImages = {
                R.drawable.banner1,
                R.drawable.banner2,
                R.drawable.banner3,
                R.drawable.banner4
        };
        bannerAdapter = new BannerAdapter(this, bannerImages);
        bannerViewPager.setAdapter(bannerAdapter);
        autoSlideBanner();

        // ✅ Initialize Firebase
        FirebaseApp.initializeApp(this);
        db = FirebaseFirestore.getInstance();

        // ✅ Init Dropdowns
        autoCompleteWeather = findViewById(R.id.autoComplete_weather);
        autoCompleteOccasion = findViewById(R.id.autoComplete_occasion);

        // ✅ Updated dropdown options
        String[] weatherOptions = {"Auto Detect", "Winter", "Fall", "Summer", "Spring"};
        String[] occasionOptions = {"Casual", "Formal", "Party", "Sportswear"};

        autoCompleteWeather.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, weatherOptions));
        autoCompleteWeather.setText("Auto Detect", false);

        autoCompleteOccasion.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, occasionOptions));
        autoCompleteOccasion.setText("Casual", false);

        // ✅ Category Buttons
        LinearLayout btnMen = findViewById(R.id.btn_men);
        LinearLayout btnWomen = findViewById(R.id.btn_women);
        LinearLayout btnBoy = findViewById(R.id.btn_boy);
        LinearLayout btnGirl = findViewById(R.id.btn_girl);

        btnMen.setOnClickListener(v -> openCategory("Men"));
        btnWomen.setOnClickListener(v -> openCategory("Women"));
        btnBoy.setOnClickListener(v -> openCategory("Boys"));
        btnGirl.setOnClickListener(v -> openCategory("Girls"));

        // ✅ Init Location Client + Retrofit
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        retrofit = new Retrofit.Builder()
                .baseUrl("https://api.openweathermap.org/data/2.5/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        weatherApiService = retrofit.create(WeatherApiService.class);

        requestLocationPermission();
    }

    private void openCategory(String category) {
        String selectedWeather = autoCompleteWeather.getText().toString();
        String selectedOccasion = autoCompleteOccasion.getText().toString();

        if (selectedWeather == null || selectedWeather.isEmpty()) {
            selectedWeather = currentSeason; // ✅ fallback to detected season
        }

        if (selectedOccasion == null || selectedOccasion.isEmpty()) {
            selectedOccasion = "Casual";
        }

        // ✅ Log season value being passed
        Log.d("ModaSense", "Passed to API - Season: " + selectedWeather);

        // Show toast confirmation
        String toastMessage = "Showing " + category + " fashion for " + selectedWeather + " + " + selectedOccasion;
        Toast.makeText(this, toastMessage, Toast.LENGTH_SHORT).show();

        // 🔥 Log to Firebase
        Map<String, Object> selection = new HashMap<>();
        selection.put("category", category);
        selection.put("weather", selectedWeather);
        selection.put("occasion", selectedOccasion);
        selection.put("timestamp", com.google.firebase.firestore.FieldValue.serverTimestamp());

        db.collection("userSelections")
                .add(selection)
                .addOnSuccessListener(documentReference ->
                        Toast.makeText(this, "Logged selection!", Toast.LENGTH_SHORT).show())
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Log failed: " + e.getMessage(), Toast.LENGTH_SHORT).show());

        // Launch product listing
        Intent intent = new Intent(this, ProductListingActivity.class);
        intent.putExtra("category", category);
        intent.putExtra("weather", selectedWeather);
        intent.putExtra("occasion", selectedOccasion);
        startActivity(intent);
    }

    private void requestLocationPermission() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    LOCATION_PERMISSION_REQUEST_CODE);
        } else {
            getCurrentLocation();
        }
    }

    private void getCurrentLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            fusedLocationClient.getLastLocation()
                    .addOnSuccessListener(this, new OnSuccessListener<Location>() {
                        @Override
                        public void onSuccess(Location location) {
                            if (location != null) {
                                double latitude = location.getLatitude();
                                double longitude = location.getLongitude();
                                callWeatherApi(latitude, longitude);
                            } else {
                                Toast.makeText(HomeActivity.this, "Unable to detect location!", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
        }
    }

    private void callWeatherApi(double lat, double lon) {
        weatherApiService.getWeatherByLocation(lat, lon, "30ac57a3537442af6a25dd7a9767b1ea", "metric")
                .enqueue(new Callback<WeatherResponse>() {
                    @Override
                    public void onResponse(Call<WeatherResponse> call, Response<WeatherResponse> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            double temperature = response.body().main.temp;
                            currentSeason = detectSeasonFromTemperature(temperature); // 🔥 store clean season

                            Toast.makeText(HomeActivity.this, "Detected: " + currentSeason + " (" + temperature + "°C)", Toast.LENGTH_LONG).show();

                            // ✅ Set only season in the dropdown
                            autoCompleteWeather.setText(currentSeason, false);
                        } else {
                            Toast.makeText(HomeActivity.this, "Failed to get weather!", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<WeatherResponse> call, Throwable t) {
                        Toast.makeText(HomeActivity.this, "API Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    // 🔥 Helper function to map Temperature → Season
    private String detectSeasonFromTemperature(double temperature) {
        if (temperature <= 10) {
            return "Winter";
        } else if (temperature <= 20) {
            return "Fall";
        } else if (temperature <= 25) {
            return "Spring";
        } else {
            return "Summer";
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (bannerHandler != null) {
            bannerHandler.removeCallbacksAndMessages(null);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getCurrentLocation();
            } else {
                Toast.makeText(this, "Location permission denied", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void autoSlideBanner() {
        bannerHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                int current = bannerViewPager.getCurrentItem();
                int total = bannerAdapter.getItemCount();
                bannerViewPager.setCurrentItem((current + 1) % total, true);
                bannerHandler.postDelayed(this, 3000);
            }
        }, 3000);
    }
}
