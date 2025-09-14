package com.deltasoft.pharmatracker.screens.home.route

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel

@Composable
fun RouteQueueScreen(
    dispatchQueueViewModel: DispatchQueueViewModel = viewModel()
) {

    // Collect the state from the ViewModel
    val apiState by dispatchQueueViewModel.dispatchQueueState.collectAsState()

    // Call the API when the screen is first displayed
    LaunchedEffect(Unit) {
        dispatchQueueViewModel.getDispatchQueueList()
    }

    // Display UI based on the current state
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        when (apiState) {
            is DispatchQueueState.Idle -> {
                CircularProgressIndicator()
            }
            is DispatchQueueState.Loading -> {
                CircularProgressIndicator()
            }
            is DispatchQueueState.Success -> {
//                val items = (apiState as DispatchQueueState.Success).items
//                LazyColumn {
//                    items(items) { item ->
//                        Text(text = item)
//                    }
//                }
            }
            is DispatchQueueState.Error -> {
                val message = (apiState as DispatchQueueState.Error).message
                Text(text = message)
            }
        }
    }
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text("Route queue", style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.onSurfaceVariant )
    }
}