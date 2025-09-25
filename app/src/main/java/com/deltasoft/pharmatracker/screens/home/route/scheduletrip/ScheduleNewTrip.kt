package com.deltasoft.pharmatracker.screens.home.route.scheduletrip

import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Button
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.material3.Card
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.deltasoft.pharmatracker.R
import com.deltasoft.pharmatracker.screens.App_CommonTopBar
import com.deltasoft.pharmatracker.screens.home.route.entity.UserDetailsList
import com.deltasoft.pharmatracker.screens.home.route.scheduletrip.entity.Driver
import com.deltasoft.pharmatracker.screens.home.scan.ScanDocState
import com.deltasoft.pharmatracker.screens.home.scan.getColorFromCode
import com.deltasoft.pharmatracker.utils.AppUtils
import com.deltasoft.pharmatracker.utils.AppUtils.isNotNullOrEmpty
import com.deltasoft.pharmatracker.utils.AppVibratorManager
import com.google.gson.Gson
import kotlinx.coroutines.delay


private const val TAG = "ScheduleNewTrip"
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScheduleNewTrip(
    navController: NavHostController,
    route: String,
    userListJson: String,
    scheduleNewTripViewModel: ScheduleNewTripViewModel = viewModel()
){
    val context = LocalContext.current
    val routeWithUsers = Gson().fromJson(userListJson, UserDetailsList::class.java)

    val userIds = routeWithUsers.userDetailsList?.map { it.scannedByUserId?:"" }?.toTypedArray()
    
    val driverListApiState by scheduleNewTripViewModel.driverListState.collectAsState()
    val scheduleNewTripState by scheduleNewTripViewModel.scheduleNewTripState.collectAsState()

    val selectedDriverId by scheduleNewTripViewModel.selectedDriver.collectAsState()

    val selectedDriver = driverListApiState.let {it.let { it as? DriverListState.Success }?.driverListResponse?.drivers?.find { it.userId == selectedDriverId } }


    var vehicleNumber by remember { mutableStateOf(selectedDriver?.vehicleNumber?:"") }
    var driverId by remember { mutableStateOf(selectedDriver?.userId?:"") }
    LaunchedEffect(selectedDriver) {
        vehicleNumber = selectedDriver?.vehicleNumber?:""
        driverId = selectedDriver?.userId?:""
    }


    LaunchedEffect(scheduleNewTripState) {
        when (scheduleNewTripState) {
            is ScheduleNewTripState.Idle -> {
                Log.d(TAG, "State: Idle")
            }
            is ScheduleNewTripState.Loading -> {
                Log.d(TAG, "State: Loading")
            }
            is ScheduleNewTripState.Success -> {
                val message = (scheduleNewTripState as ScheduleNewTripState.Success).scheduleNewTripResponse.message
                Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                Log.d(TAG, "State: Success - Message: $message")
                navController.popBackStack()
            }
            is ScheduleNewTripState.Error -> {
                val message = (scheduleNewTripState as ScheduleNewTripState.Error).message
                Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                Log.e(TAG, "State: Error - Message: $message")
                scheduleNewTripViewModel.clearScheduleNewTripState()
            }
        }
    }

    Scaffold(
        modifier = Modifier.imePadding(),
        topBar = {
            App_CommonTopBar(title = (route?:"")+" : New Trip", onBackClick = {  navController.popBackStack() })
//            CenterAlignedTopAppBar(
//                title = {
//                    Text(
//                        text = route?:"",
//                        maxLines = 1,
//                        overflow = TextOverflow.Ellipsis
//                    )
//                }
//            )
        },
        bottomBar = {
//            if (selectedDriverId.isNotNullOrEmpty()) {
//                BottomAppBar {
            Column(Modifier.fillMaxWidth() .windowInsetsPadding(WindowInsets.navigationBars)) {
                if (selectedDriver != null) {
                    Card(
                        modifier = Modifier,
                        shape = RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp)
                    ) {
                        Column(Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 16.dp)) {
                            OutlinedTextField(
                                value = vehicleNumber,
                                onValueChange = { newText ->
                                    vehicleNumber = newText
                                },
                                label = {
                                    Text(
                                        if (vehicleNumber.isNotNullOrEmpty()) stringResource(
                                            R.string.schedule_new_trip_text_field_placeholder
                                        ) else stringResource(
                                            R.string.schedule_new_trip_text_field_placeholder2
                                        )
                                    )
                                },
                                modifier = Modifier.fillMaxWidth(),
                                keyboardOptions = KeyboardOptions(
                                    keyboardType = KeyboardType.Text,
                                    imeAction = ImeAction.Done
                                ),
                                maxLines = 1,
                                enabled = scheduleNewTripState !is ScheduleNewTripState.Loading
                            )
                        }
                    }
                }
                Button(modifier = Modifier
                    .fillMaxWidth()
//                    .navigationBarsPadding()
                    , onClick = {
                        scheduleNewTripViewModel.scheduleNewTrip(route,userIds?: arrayOf(),vehicleNumber,driverId,context)

                }, enabled = scheduleNewTripState !is ScheduleNewTripState.Loading) {
                    Text("Schedule Trip")
                }
            }
//                }
//            }
        }
    ) { paddingValues ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top
        ) {
            Box(modifier = Modifier
                .fillMaxSize(), contentAlignment = Alignment.Center) {
                when (driverListApiState) {
                    is DriverListState.Idle -> {
                        CircularProgressIndicator()
                    }
                    is DriverListState.Loading -> {
                        CircularProgressIndicator()
                    }
                    is DriverListState.Success -> {
                        val driverListResponse = (driverListApiState as DriverListState.Success).driverListResponse
                        scheduleNewTripViewModel.updateDriverList(driverListResponse?.drivers?: arrayListOf())
                        DriverListCompose(scheduleNewTripViewModel,driverListResponse?.message?:"")
                    }
                    is DriverListState.Error -> {
                        val message = (driverListApiState as DriverListState.Error).message
                        Text(text = message)
                    }
                }
            }
        }
    }

}

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun DriverListCompose(scheduleNewTripViewModel: ScheduleNewTripViewModel, message: String?) {
    val driverList by scheduleNewTripViewModel.driverList.collectAsState()
    val selectedDriverId by scheduleNewTripViewModel.selectedDriver.collectAsState()

    val dispatchQueueState by scheduleNewTripViewModel.driverListState.collectAsState()
    val isRefreshing = dispatchQueueState.let { it is DriverListState.Loading }

    val pullRefreshState = rememberPullRefreshState(isRefreshing, {
//        scheduleNewTripViewModel.getDriverList()
    })

    Box(Modifier
        .fillMaxSize()
        .pullRefresh(pullRefreshState), contentAlignment = Alignment.TopCenter) {
        if (driverList.isEmpty()){
            val noDataMessage = message?:"No data found"
            Column(Modifier
                .fillMaxSize()
                .padding(16.dp)
                .verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally) {
                Text(noDataMessage, style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.onSurfaceVariant , textAlign = TextAlign.Center)
            }
        }else{
            Column(Modifier.fillMaxWidth()) {
                Text("Select a driver for this trip", color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.fillMaxWidth(), fontWeight = FontWeight.Bold, textAlign = TextAlign.Center, style = MaterialTheme.typography.titleMedium )
                Spacer(Modifier.height(16.dp))
                LazyColumn {
                    items(driverList.size) { index ->
                        if (index in driverList.indices) {
                            val driver = driverList[index]
                            DriverListItem(driver,scheduleNewTripViewModel,selectedDriverId.equals(driver.userId))
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
fun DriverListItem(
    driver: Driver,
    scheduleNewTripViewModel: ScheduleNewTripViewModel,
    selected: Boolean
) {
    Card(modifier = Modifier
        .padding(vertical = 8.dp)
        .clickable {
            scheduleNewTripViewModel.updateSelectedDriver(driver.userId ?: "", clear = selected)
        }) {
        ListItem(
            headlineContent = {
                Text(
                    driver.driverName ?: "",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = if (driver.sameLocation == true)FontWeight.Bold else FontWeight.Normal
                )
            },
            modifier = Modifier,
            overlineContent = {
                Text(driver.baseLocationName ?: "", color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = if (driver.sameLocation == true)FontWeight.Bold else FontWeight.Normal)
            },
            supportingContent = {
                Text(
                    (driver.vehicleNumber) ?: "",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = if (driver.sameLocation == true)FontWeight.Bold else FontWeight.Normal
                )
            },
//            leadingContent = {
//
//            },
            trailingContent = {
                RadioButton(
                    selected = selected,
                    onClick = {
                        scheduleNewTripViewModel.updateSelectedDriver(
                            driver.userId ?: "",
                            clear = selected
                        )
                    },
                )
//                Checkbox(
//                    checked = selected,
//                    onCheckedChange = {
//                        scheduleNewTripViewModel.updateSelectedDriver(
//                            driver.userId ?: "",
//                            clear = selected
//                        )
//                    }
//                )
            },
            colors = ListItemDefaults.colors(
                containerColor = Color.Transparent
            )
        )
    }
}
