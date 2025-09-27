package com.deltasoft.pharmatracker.screens.home.MyTrips

import android.Manifest
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.deltasoft.pharmatracker.screens.home.HomeViewModel
import com.deltasoft.pharmatracker.screens.home.schedule.ScheduledTripsState
import com.deltasoft.pharmatracker.screens.home.schedule.entity.ScheduledTrip
import com.deltasoft.pharmatracker.utils.AppUtils
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.google.accompanist.permissions.shouldShowRationale

private const val TAG = "MyTripsScreen"
@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun MyTripsScreen(
    homeViewModel: HomeViewModel, myTripsViewModel: MyTripsViewModel = viewModel()
) {
    val context = LocalContext.current

    val locationPermissionState = rememberPermissionState(
        Manifest.permission.ACCESS_FINE_LOCATION
    )

    val isRunning by myTripsViewModel.isServiceRunning.collectAsState()

    val latitude by myTripsViewModel.latitude.collectAsState()
    val longitude by myTripsViewModel.longitude.collectAsState()

    DisposableEffect(myTripsViewModel) {
        myTripsViewModel.registerReceiver(context)
        onDispose {
            myTripsViewModel.unregisterReceiver(context)
        }
    }


    var isPermissionCheckedOnce by remember { mutableStateOf(false) }
    var isLocationPermissionClicked by remember { mutableStateOf(false) }

    val apiState by myTripsViewModel.scheduledTripsState.collectAsState()
    val refreshClickEvent by homeViewModel.myTripsListRefreshClickEvent.collectAsState()

    LaunchedEffect(apiState) {
        when (apiState) {
            is ScheduledTripsState.Idle -> {
                Log.d(TAG, "State: Idle")
            }
            is ScheduledTripsState.Loading -> {
                Log.d(TAG, "State: Loading")
            }
            is ScheduledTripsState.Success -> {
                val message = (apiState as ScheduledTripsState.Success).scheduledTripsResponse.message
                Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                Log.d(TAG, "State: Success - Message: $message")
                myTripsViewModel.startMyService(context)
            }
            is ScheduledTripsState.Error -> {
                val message = (apiState as ScheduledTripsState.Error).message
                Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                Log.e(TAG, "State: Error - Message: $message")
                myTripsViewModel.clearState()
            }
        }
    }

    // NEW LaunchedEffect to react to permission status changes
    LaunchedEffect(locationPermissionState.status) {
        if (locationPermissionState.status.isGranted && isLocationPermissionClicked) {
            myTripsViewModel.startTrip()
            isLocationPermissionClicked = false
        } else {
            Log.d(TAG, "Location permission DENIED or not yet requested.")
        }
    }

    LaunchedEffect(refreshClickEvent) {
        myTripsViewModel.getMyTripsList()
    }

    Scaffold(modifier = Modifier.fillMaxSize()) { paddingValues ->
        val modifier = Modifier.padding(paddingValues)
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top
        ) {
            Box(modifier = Modifier
                .fillMaxSize(), contentAlignment = Alignment.Center) {
                when (apiState) {
                    is ScheduledTripsState.Idle -> {
                        CircularProgressIndicator()
                    }
                    is ScheduledTripsState.Loading -> {
                        CircularProgressIndicator()
                    }
                    is ScheduledTripsState.Success -> {
                        val scheduledTripsResponse = (apiState as ScheduledTripsState.Success).scheduledTripsResponse
                        myTripsViewModel.updateScheduledList(scheduledTripsResponse?.trips?: arrayListOf())
                        MyTripListCompose(myTripsViewModel,scheduledTripsResponse?.message, onItemClick = { schduledTrip ->
                            if (schduledTrip?.status.equals("SCHEDULED")) {
                                // Start Trip
                                myTripsViewModel.currentTrip = schduledTrip
                                when {
                                    // 2. If the permission is granted
                                    locationPermissionState.status.isGranted -> {
                                        myTripsViewModel.startTrip()
                                        isLocationPermissionClicked = false
                                    }

                                    // 3. If the user has denied the permission, show a rationale
                                    //    or guide them to settings.
                                    locationPermissionState.status.shouldShowRationale -> {
                                        isLocationPermissionClicked = true
                                        locationPermissionState.launchPermissionRequest()
                                        isPermissionCheckedOnce = true
                                    }

                                    // 4. If it's the first time or they've denied permanently,
                                    //    show a button to request permission.
                                    else -> {
                                        isLocationPermissionClicked = true
                                        if (!locationPermissionState.status.isGranted && !locationPermissionState.status.shouldShowRationale && isPermissionCheckedOnce) {
                                            AppUtils.openAppSettings(context)
                                        } else {
                                            locationPermissionState.launchPermissionRequest()
                                            isPermissionCheckedOnce = true
                                        }
                                    }
                                }
                            }else{
                                //Resume Trip

                            }
                        })
                    }
                    is ScheduledTripsState.Error -> {
                        val message = (apiState as ScheduledTripsState.Error).message
                        Text(text = message)
                    }
                }
            }
        }
    }


}


@OptIn(ExperimentalMaterialApi::class)
@Composable
fun MyTripListCompose(myTripsViewModel: MyTripsViewModel, message: String?, onItemClick: (scheduledTrip: ScheduledTrip) -> Unit = { a->}) {
    val scheduledTripList by myTripsViewModel.scheduledTripList.collectAsState()

    val scheduledTripsState by myTripsViewModel.scheduledTripsState.collectAsState()
    val isRefreshing = scheduledTripsState.let { it is ScheduledTripsState.Loading }

    val pullRefreshState = rememberPullRefreshState(isRefreshing, { myTripsViewModel.getMyTripsList() })

    Box(Modifier
        .fillMaxSize()
        .pullRefresh(pullRefreshState), contentAlignment = Alignment.TopCenter) {
        if (scheduledTripList.isEmpty()){
            val noDataMessage = message?:"No data found"
            Column(Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally) {
                Text(noDataMessage, style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.onSurfaceVariant , textAlign = TextAlign.Center)
            }
        }else{
            Column(Modifier.fillMaxWidth()) {
                Text("The following trips have been assigned to you", color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.fillMaxWidth(), fontWeight = FontWeight.Bold, textAlign = TextAlign.Center, style = MaterialTheme.typography.titleMedium )
                Spacer(Modifier.height(16.dp))
                LazyColumn {
                    items(scheduledTripList.size) { index ->
                        if (index in scheduledTripList.indices) {
                            val scheduledTrip = scheduledTripList[index]
                            SingleMyTripCompose(scheduledTrip,onItemClick)
                        }
                    }
                }
            }
        }
        // The indicator must be a separate composable, typically aligned to the top
        PullRefreshIndicator(
            refreshing = isRefreshing,
            state = pullRefreshState,
            modifier = Modifier.align(Alignment.TopCenter)
        )
    }
}

@Composable
fun SingleMyTripRowItem(key: String, value: String, style: TextStyle, color: Color = MaterialTheme.colorScheme.onSurfaceVariant, fontWeight: FontWeight = FontWeight.Normal) {
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Text(
            text = key,
            style = style,
            color = color,
            fontWeight = fontWeight,
            modifier = Modifier.weight(0.6f)
        )
        Text(
            text = ":",
            style = style,
            color = color,
            fontWeight = FontWeight.Normal,
            modifier = Modifier.weight(0.10f)
        )
        Text(
            text = value,
            style = style,
            color = color,
            modifier = Modifier.weight(1f),
            textAlign = TextAlign.Start
        )
    }
}

@Composable
fun SingleMyTripCompose(scheduledTrip: ScheduledTrip, onItemClick: (scheduledTrip: ScheduledTrip) -> Unit = { a->}) {
    Card(
        modifier = Modifier
            .padding(vertical = 8.dp)
    ) {
        Row(Modifier
            .fillMaxWidth()
            .padding(16.dp)) {
            Column(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                SingleMyTripRowItem(
                    key = "Trip ID",
                    value = scheduledTrip.tripId.toString(),
                    style = MaterialTheme.typography.titleSmall
                )
                SingleMyTripRowItem(
                    key = "Route",
                    value = scheduledTrip.route?:"",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold)
                SingleMyTripRowItem(
                    key = "Created By",
                    value = scheduledTrip.createdBy?:"",
                    style = MaterialTheme.typography.titleSmall)
                SingleMyTripRowItem(
                    key = "Created At",
                    value = scheduledTrip.createdAtFormatted?:"",
                    style = MaterialTheme.typography.titleSmall)
                SingleMyTripRowItem(
                    key = "Driver Name",
                    value = scheduledTrip.driverName ?: "",
                    style = MaterialTheme.typography.titleMedium)
                SingleMyTripRowItem(
                    key = "Vehicle Number",
                    value = scheduledTrip.vehicleNumber ?: "",
                    style = MaterialTheme.typography.titleMedium)
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    Button(
                        onClick = {
                            onItemClick.invoke(scheduledTrip)
                        },
                        modifier = Modifier
                    ) {
                        Text(if (scheduledTrip.status.equals("SCHEDULED")) "Start Trip" else "Resume Trip")
                    }
                }
            }
        }
    }
}
