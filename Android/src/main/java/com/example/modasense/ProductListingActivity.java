package com.example.modasense;

import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.gson.Gson;

import java.util.List;
import java.util.ArrayList;
import android.util.Log;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class ProductListingActivity extends AppCompatActivity {

    private ProgressBar progressBar;
    private RecyclerView recyclerView;
    private RecommendationAdapter adapter;

    private TextView categoryTextView, weatherTextView, occasionTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product_listing);

        // Initialize Views
        categoryTextView = findViewById(R.id.tv_category);
        weatherTextView = findViewById(R.id.tv_weather);
        occasionTextView = findViewById(R.id.tv_occasion);
        progressBar = findViewById(R.id.progressBar);
        recyclerView = findViewById(R.id.recyclerRecommendations);
        recyclerView.setLayoutManager(new GridLayoutManager(this, 2));

        // Get intent data
        String gender = getIntent().getStringExtra("category");
        String weatherString = getIntent().getStringExtra("weather");
        String usage = getIntent().getStringExtra("occasion");

        if (gender == null) gender = "All";
        if (weatherString == null) weatherString = "25";
        if (usage == null) usage = "Casual";

        String season = weatherString.contains(",") ? weatherString.split(",")[0].trim() : weatherString.trim();

        categoryTextView.setText("Gender: " + gender);
        weatherTextView.setText("Season: " + season);
        occasionTextView.setText("Usage: " + usage);

        // Retrofit setup
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("http://192.168.1.79:8000/") // 🔁 Update if needed
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        ApiService apiService = retrofit.create(ApiService.class);
        progressBar.setVisibility(View.VISIBLE);

        Call<RecommendationResponse> call = apiService.getRecommendation(season, usage, gender);

        call.enqueue(new Callback<RecommendationResponse>() {
            @Override
            public void onResponse(Call<RecommendationResponse> call, Response<RecommendationResponse> response) {
                progressBar.setVisibility(View.GONE);

                Log.d("ModaSense", "🟨 Entered onResponse()");
                Log.d("ModaSense", "📡 Response Code: " + response.code());

                if (response.body() == null) {
                    Log.e("ModaSense", "❌ response.body() is null");
                    Toast.makeText(ProductListingActivity.this, "No data received from server", Toast.LENGTH_LONG).show();
                    return;
                }

                Log.d("ModaSense", "✅ response.body() is not null");
                Log.d("ModaSense", "📦 Raw JSON: " + new Gson().toJson(response.body()));

                List<String> imageUrls = response.body().image_urls;

                if (imageUrls == null || imageUrls.isEmpty()) {
                    Log.w("ModaSense", "⚠️ No image URLs received.");
                    Toast.makeText(ProductListingActivity.this, "No image URLs from server", Toast.LENGTH_SHORT).show();
                    return;
                }

                Log.d("ModaSense", "🔥 Total images received: " + imageUrls.size());
                for (String url : imageUrls) {
                    Log.d("ModaSense", "📷 URL: " + url);
                }

                adapter = new RecommendationAdapter(ProductListingActivity.this, imageUrls);
                recyclerView.setAdapter(adapter);

                Toast.makeText(ProductListingActivity.this, "Images: " + imageUrls.size(), Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onFailure(Call<RecommendationResponse> call, Throwable t) {
                progressBar.setVisibility(View.GONE);
                Log.e("ModaSense", "❌ API call failed: " + t.getMessage());
                Toast.makeText(ProductListingActivity.this, "API Error: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    // Unused, kept for reference
    private String detectSeasonFromTemperature(double temperature) {
        if (temperature <= 10) return "Winter";
        else if (temperature <= 20) return "Fall";
        else if (temperature <= 25) return "Spring";
        else return "Summer";
    }
}
