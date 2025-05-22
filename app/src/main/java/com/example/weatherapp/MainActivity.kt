package com.example.weatherapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.clickable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject

data class WeatherInfo(
    val description: String,
    val temp: Double,
    val humidity: Int,
    val wind: Double,
    val iconCode: String?
)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            Surface(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0xFFD2B893)),
                color = Color(0xFFD2B893)
            ) {
                WeatherScreen()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WeatherScreen() {
    val allCities = listOf(
        "Islamabad", "Lahore", "Karachi", "Multan", "Rawalpindi", "Peshawar",
        "Quetta", "Faisalabad", "Bahawalpur", "Sialkot", "Gujranwala", "Sukkur",
        "Hyderabad", "Sargodha", "Dera Ghazi Khan", "Abbottabad", "Kabul", "Dhaka",
        "Colombo", "Kathmandu", "Yangon", "Manila", "Hanoi", "Kuala Lumpur", "Jakarta",
        "Singapore", "Tashkent", "Bishkek", "Bangkok", "Beijing", "Tokyo", "Seoul",
        "Mumbai", "Delhi", "Chennai", "Bangalore", "Shanghai", "Addis Ababa", "Auckland",
        "Berlin", "Bogotá", "Brisbane", "Buenos Aires", "Cairo", "Chicago", "London",
        "Los Angeles", "Madrid", "Melbourne", "Mexico City", "Nairobi", "New York City",
        "Paris", "Rome", "Santiago", "São Paulo", "Sydney", "Toronto", "Vihari", "Wellington"
    )
    val popularCities = listOf("Islamabad", "Lahore", "Karachi", "London", "New York City")

    var searchQuery by remember { mutableStateOf("") }
    var city by remember { mutableStateOf("") }
    var filteredCities by remember { mutableStateOf(allCities) }
    var isFocused by remember { mutableStateOf(false) }
    var weatherInfo by remember { mutableStateOf<WeatherInfo?>(null) }
    var isLoading by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(searchQuery) {
        filteredCities = if (searchQuery.isEmpty()) {
            popularCities
        } else {
            allCities.filter {
                it.contains(searchQuery, ignoreCase = true)
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFD2B893))
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Weather App",
            style = MaterialTheme.typography.headlineMedium,
            color = Color.Black,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        OutlinedTextField(
            value = searchQuery,
            onValueChange = {
                searchQuery = it
                isFocused = true
            },
            label = { Text("Enter location", color = Color.Black) },
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = Color.Black,
                unfocusedTextColor = Color.Black,
                focusedLabelColor = Color.Black,
                unfocusedLabelColor = Color.Black,
                cursorColor = Color.Black,
                focusedBorderColor = Color.Black,
                unfocusedBorderColor = Color.Black,
                focusedContainerColor = Color(0xFFEADFC8),
                unfocusedContainerColor = Color(0xFFEADFC8)
            ),
            singleLine = true
        )

        if (isFocused) {
            if (filteredCities.isNotEmpty()) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFFEADFC8))
                        .padding(8.dp)
                ) {
                    filteredCities.forEach { cityName ->
                        Text(
                            text = cityName,
                            color = Color.Black,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(8.dp)
                                .clickable {
                                    city = cityName
                                    searchQuery = cityName
                                    isFocused = false
                                }
                        )
                    }
                }
            } else {
                Text(
                    "Could not find ❗",
                    color = Color.Red,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
        }

        Button(
            onClick = {
                if (searchQuery.isNotEmpty()) {
                    city = searchQuery
                    isLoading = true
                    weatherInfo = null
                    scope.launch {
                        weatherInfo = getWeatherForCity(city)
                        isLoading = false
                    }
                }
            },
            enabled = searchQuery.isNotEmpty(),
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp)
                .height(40.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF9E8B6A))
        ) {
            Text("Get Weather", color = Color.White)
        }

        if (isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.padding(top = 16.dp),
                color = Color.Black
            )
        }

        weatherInfo?.let { info ->
            Text(
                "Today",
                style = MaterialTheme.typography.titleLarge,
                color = Color.Black,
                modifier = Modifier
                    .align(Alignment.Start)
                    .padding(top = 16.dp, bottom = 8.dp)
            )
            WeatherCard(info)
        }
    }
}


@Composable
fun WeatherCard(info: WeatherInfo) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFEADFC8)),
        elevation = CardDefaults.cardElevation(12.dp),
        shape = RoundedCornerShape(24.dp)
    ) {
        Column(
            modifier = Modifier
                .padding(24.dp)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Image(
                painter = painterResource(id = getWeatherIconRes(info.iconCode)),
                contentDescription = "Weather Icon",
                modifier = Modifier.size(64.dp)
            )
            Spacer(modifier = Modifier.height(12.dp))

            Text(buildAnnotatedString {
                withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) { append("Weather: ") }
                append(info.description.replaceFirstChar { it.uppercase() })
            }, color = Color.Black, fontSize = 16.sp)

            Text(buildAnnotatedString {
                withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) { append("Temperature: ") }
                append(String.format("%.2f°C", info.temp))
            }, color = Color.Black, fontSize = 16.sp)

            Text(buildAnnotatedString {
                withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) { append("Humidity: ") }
                append("${info.humidity}%")
            }, color = Color.Black, fontSize = 16.sp)

            Text(buildAnnotatedString {
                withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) { append("Wind Speed: ") }
                append("${info.wind} m/s")
            }, color = Color.Black, fontSize = 16.sp)
        }
    }
}

suspend fun getWeatherForCity(city: String): WeatherInfo? {
    return try {
        val apiKey = "02003f0753dbf93a6d1aef4bf872ed31" // Replace with your actual key
        val url = "https://api.openweathermap.org/data/2.5/weather?q=$city&appid=$apiKey&units=metric"
        val client = OkHttpClient()
        val request = Request.Builder().url(url).build()
        val response = withContext(Dispatchers.IO) { client.newCall(request).execute() }

        if (!response.isSuccessful) return null
        val json = JSONObject(response.body?.string() ?: "")
        val weatherJson = json.getJSONArray("weather").getJSONObject(0)
        val description = weatherJson.getString("description")
        val iconCode = weatherJson.getString("icon")
        val temp = json.getJSONObject("main").getDouble("temp")
        val humidity = json.getJSONObject("main").getInt("humidity")
        val wind = json.getJSONObject("wind").getDouble("speed")

        WeatherInfo(description, temp, humidity, wind, iconCode)
    } catch (e: Exception) {
        null
    }
}

@DrawableRes
fun getWeatherIconRes(iconCode: String?): Int {
    return when (iconCode) {
        "01d" -> R.drawable.ic_01d
        "01n" -> R.drawable.ic_01n
        "02d" -> R.drawable.ic_02d
        "02n" -> R.drawable.ic_02n
        "03d" -> R.drawable.ic_03d
        "03n" -> R.drawable.ic_03n
        "04d" -> R.drawable.ic_04d
        "04n" -> R.drawable.ic_04n
        "09d" -> R.drawable.ic_09d
        "09n" -> R.drawable.ic_09n
        "10d" -> R.drawable.ic_10d
        "10n" -> R.drawable.ic_10n
        "11d" -> R.drawable.ic_11d
        "11n" -> R.drawable.ic_11n
        "13d" -> R.drawable.ic_13d
        "13n" -> R.drawable.ic_13n
        "50d" -> R.drawable.ic_50d
        "50n" -> R.drawable.ic_50n
        else -> R.drawable.default_weather
    }
}
