package com.example.weatherapp2;

import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class MainActivity extends AppCompatActivity {
    private TextView cityText, tempText, humidityText, descText, windText;
    private ImageView weatherIcon;
    private Button refreshButton;
    private EditText cityInput;
    private static final String API_KEY = "e8cc7f0316764c143a2082673a7c079a";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        cityText = findViewById(R.id.city_name);
        tempText = findViewById(R.id.temprature_text);
        humidityText = findViewById(R.id.humidityText);
        windText = findViewById(R.id.windText);
        descText = findViewById(R.id.descriptonText);
        weatherIcon = findViewById(R.id.weatherIcon);
        refreshButton = findViewById(R.id.edit_button);
        cityInput = findViewById(R.id.edit_text);

        FetchWeatherData("Istanbul");

        refreshButton.setOnClickListener(v -> {
            String cityName = cityInput.getText().toString().trim();
            if (!cityName.isEmpty()) {
                FetchWeatherData(cityName);
            } else {
                Toast.makeText(this, "Lütfen bir şehir adı girin!", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void FetchWeatherData(String cityName) {
        String url = "https://api.openweathermap.org/data/2.5/weather?q=" + cityName + "&appid=" + API_KEY + "&units=metric";

        new Thread(() -> {
            try {
                URL apiUrl = new URL(url);
                HttpURLConnection connection = (HttpURLConnection) apiUrl.openConnection();
                connection.setRequestMethod("GET");
                connection.connect();

                int responseCode = connection.getResponseCode();
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                    StringBuilder response = new StringBuilder();
                    String line;

                    while ((line = reader.readLine()) != null) {
                        response.append(line);
                    }
                    reader.close();

                    parseWeatherData(response.toString());
                } else {
                    runOnUiThread(() -> Toast.makeText(this, "Şehir bulunamadı!", Toast.LENGTH_SHORT).show());
                }

            } catch (Exception e) {
                Log.e("WeatherApp", "Error fetching weather data", e);
                runOnUiThread(() -> Toast.makeText(this, "Bir hata oluştu. Lütfen tekrar deneyin!", Toast.LENGTH_SHORT).show());
            }
        }).start();
    }

    private void parseWeatherData(String jsonData) {
        try {
            JSONObject jsonObject = new JSONObject(jsonData);
            JSONObject main = jsonObject.getJSONObject("main");
            double temperature = main.getDouble("temp");
            double humidity = main.getDouble("humidity");

            JSONObject wind = jsonObject.getJSONObject("wind");
            double windSpeed = wind.getDouble("speed");

            JSONArray weatherArray = jsonObject.getJSONArray("weather");
            JSONObject weather = weatherArray.getJSONObject(0);
            String description = weather.getString("description");
            String iconCode = weather.getString("icon");

            runOnUiThread(() -> {
                try {
                    cityText.setText(jsonObject.getString("name"));
                } catch (JSONException e) {
                    throw new RuntimeException(e);
                }
                tempText.setText(String.format("%.1f°C", temperature));
                humidityText.setText(String.format("%.0f%%", humidity));
                windText.setText(String.format("%.1f km/h", windSpeed));
                descText.setText(description);

                int resId = getResources().getIdentifier("ic_" + iconCode, "drawable", getPackageName());
                weatherIcon.setImageResource(resId);
            });

        } catch (JSONException e) {
            Log.e("WeatherApp", "Error parsing JSON data", e);
            runOnUiThread(() -> Toast.makeText(this, "Veri işlenirken bir hata oluştu!", Toast.LENGTH_SHORT).show());
        }
    }
}
