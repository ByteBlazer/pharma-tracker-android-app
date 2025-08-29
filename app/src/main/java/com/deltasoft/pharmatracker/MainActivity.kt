package com.deltasoft.pharmatracker

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.deltasoft.pharmatracker.ui.theme.PharmaTrackerTheme
import com.deltasoft.pharmatracker.BuildConfig


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            PharmaTrackerTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Greeting(
                        name = "Android",
                        apiUrl = BuildConfig.BASE_API_URL,
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }
}

@Composable
fun Greeting(name: String, apiUrl: String, modifier: Modifier = Modifier) {
    Column(modifier = modifier) {
        Text(text = "Hello Dear $name!")
        Text(text = "API URL: $apiUrl")
    }
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    PharmaTrackerTheme {
        Greeting(name = "Android", apiUrl = "https://preview-url.com/")
    }
}
