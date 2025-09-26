package com.deltasoft.pharmatracker.screens.home.schedule

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
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
import com.deltasoft.pharmatracker.screens.home.schedule.entity.ScheduledTrip
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun ScheduledTripsScreen(
    homeViewModel: HomeViewModel, scheduledTripsViewModel: ScheduledTripsViewModel = viewModel()
) {
    val context = LocalContext.current

    val apiState by scheduledTripsViewModel.scheduledTripsState.collectAsState()

    val refreshClickEvent by homeViewModel.scheduledListRefreshClickEvent.collectAsState()


    val cancelScheduleApiState by scheduledTripsViewModel.cancelScheduleState.collectAsState()

    var isDialogOpen by remember { mutableStateOf(false) }
    var cancelApiResponseDialogMessage by remember { mutableStateOf("") }

    // CoroutineScope to launch the timed action
    val scope = rememberCoroutineScope()

    LaunchedEffect(cancelScheduleApiState) {
        when (cancelScheduleApiState) {
            is CancelScheduleState.Idle -> {
            }
            is CancelScheduleState.Loading -> {
            }
            is CancelScheduleState.Success -> {
                val message = (cancelScheduleApiState as CancelScheduleState.Success).message
                cancelApiResponseDialogMessage = message
                // 1. Show the dialog
                isDialogOpen = true

                delay(2000L)
                isDialogOpen = false

                cancelApiResponseDialogMessage = ""
                scheduledTripsViewModel.clearCancelScheduleState()
                scheduledTripsViewModel.getScheduledTripsList()
            }
            is CancelScheduleState.Error -> {
                val message = (cancelScheduleApiState as CancelScheduleState.Error).message
                cancelApiResponseDialogMessage = message
                isDialogOpen = true


                delay(2000L)
                isDialogOpen = false

                cancelApiResponseDialogMessage = ""
                scheduledTripsViewModel.clearCancelScheduleState()
            }
        }
    }

    if (isDialogOpen) {
        AlertDialog(
            onDismissRequest = {
                isDialogOpen = false
            },
            text = {
                Text(cancelApiResponseDialogMessage, color = MaterialTheme.colorScheme.onSurface)
            },
            confirmButton = {}
        )
    }


    // LaunchedEffect triggers whenever clickEvent changes
    LaunchedEffect(refreshClickEvent) {
        scheduledTripsViewModel.getScheduledTripsList()
    }

    var showDialog by remember { mutableStateOf(false) }
    var scheduleCancelNeededItem by remember { mutableStateOf(ScheduledTrip()) }
    ScheduleCancelConfirmationDialog(
        showDialog = showDialog,
        onConfirm = {
            showDialog = false // Hide the dialog after action
            scheduledTripsViewModel.cancelScheduledTrip(scheduleCancelNeededItem.tripId.toString(),context)
            scheduleCancelNeededItem = ScheduledTrip()
        },
        onDismiss = {
            showDialog = false
            scheduleCancelNeededItem = ScheduledTrip()
        },scheduledTripsViewModel,scheduleCancelNeededItem
    )

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
                        scheduledTripsViewModel.updateScheduledList(scheduledTripsResponse?.trips?: arrayListOf())
                        ScheduledTripListCompose(scheduledTripsViewModel,scheduledTripsResponse?.message, onItemClick = { schduledTrip ->
                            scheduleCancelNeededItem = schduledTrip
                            showDialog = true
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

fun showSnackbar(
    scope: CoroutineScope,
    snackbarHostState: SnackbarHostState,
    message: String
) {
    scope.launch {
        snackbarHostState.showSnackbar(
            message = message,
            duration = SnackbarDuration.Long // Use Long for more reading time
        )
    }
}
@Composable
private fun ScheduleCancelConfirmationDialog(
    showDialog: Boolean,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
    scheduledTripsViewModel: ScheduledTripsViewModel,
    scheduleCancelNeededItem: ScheduledTrip
) {
    if (showDialog) {
        AlertDialog(
            onDismissRequest = onDismiss,
            title = {
                androidx.compose.material.Text(
                    text = "Cancel Scheduled Item", style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold
                )
            },
            text = {
                androidx.compose.material.Text(
                    text = "Are you sure you want to cancel item Id : ${scheduleCancelNeededItem.tripId} for the ${scheduleCancelNeededItem.route} route?",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
            },
            confirmButton = {
                TextButton(onClick = onConfirm) {
                    androidx.compose.material.Text(
                        "Confirm",
                        color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = onDismiss) {
                    androidx.compose.material.Text(
                        "Cancel",
                        color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold
                    )
                }
            }
        )
    }
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
private fun ScheduledTripListCompose(scheduledTripsViewModel: ScheduledTripsViewModel, message: String?,onItemClick: (scheduledTrip: ScheduledTrip) -> Unit = { a->}) {
    val scheduledTripList by scheduledTripsViewModel.scheduledTripList.collectAsState()

    val scheduledTripsState by scheduledTripsViewModel.scheduledTripsState.collectAsState()
    val isRefreshing = scheduledTripsState.let { it is ScheduledTripsState.Loading }

    val pullRefreshState = rememberPullRefreshState(isRefreshing, { scheduledTripsViewModel.getScheduledTripsList() })

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
            LazyColumn {
                items(scheduledTripList.size) { index ->
                    if (index in scheduledTripList.indices) {
                        val scheduledTrip = scheduledTripList[index]
                        SingleScheduledTripCompose(scheduledTrip,onItemClick)
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
private fun SingleScheduledRowItem(key: String, value: String, style: TextStyle, color: Color = MaterialTheme.colorScheme.onSurfaceVariant, fontWeight: FontWeight = FontWeight.Normal) {
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
private fun SingleScheduledTripCompose(scheduledTrip: ScheduledTrip,onItemClick: (scheduledTrip: ScheduledTrip) -> Unit = { a->}) {
    Card(
        modifier = Modifier
            .padding(vertical = 8.dp)
    ) {
        Row(Modifier
            .fillMaxWidth()
            .padding(16.dp)) {
            Column(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                SingleScheduledRowItem(
                    key = "Trip ID",
                    value = scheduledTrip.tripId.toString(),
                    style = MaterialTheme.typography.titleSmall
                )
                SingleScheduledRowItem(
                    key = "Route",
                    value = scheduledTrip.route?:"",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold)
                SingleScheduledRowItem(
                    key = "Created By",
                    value = scheduledTrip.createdBy?:"",
                    style = MaterialTheme.typography.titleSmall)
                SingleScheduledRowItem(
                    key = "Created At",
                    value = scheduledTrip.createdAtFormatted?:"",
                    style = MaterialTheme.typography.titleSmall)
                SingleScheduledRowItem(
                    key = "Driver Name",
                    value = scheduledTrip.driverName ?: "",
                    style = MaterialTheme.typography.titleMedium)
                SingleScheduledRowItem(
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
                        Text("Cancel")
                    }
                }
            }
        }
    }
}
