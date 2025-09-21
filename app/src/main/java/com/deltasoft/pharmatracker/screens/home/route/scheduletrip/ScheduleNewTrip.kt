package com.deltasoft.pharmatracker.screens.home.route.scheduletrip

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.BottomAppBar
import androidx.compose.material.Button
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.deltasoft.pharmatracker.screens.home.route.entity.UserDetailsList
import com.deltasoft.pharmatracker.screens.home.route.scheduletrip.entity.Driver
import com.deltasoft.pharmatracker.utils.AppUtils.isNotNullOrEmpty
import com.google.gson.Gson

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScheduleNewTrip(
    navController: NavHostController,
    route: String,
    userListJson: String,
    scheduleNewTripViewModel: ScheduleNewTripViewModel = viewModel()
){
    val routeWithUsers = Gson().fromJson(userListJson, UserDetailsList::class.java)
    
    val driverListApiState by scheduleNewTripViewModel.driverListState.collectAsState()

    val selectedDriverId by scheduleNewTripViewModel.selectedDriver.collectAsState()

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = route?:"",
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            )
        },
        bottomBar = {
            if (selectedDriverId.isNotNullOrEmpty()) {
                BottomAppBar {
                    Button(modifier = Modifier.fillMaxWidth(), onClick = {}) {
                        Text("Schedule Trip")
                    }
                }
            }
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
            LazyColumn {
                items(driverList.size) { index ->
                    if (index in driverList.indices) {
                        val driver = driverList[index]
                        DriverListItem(driver,scheduleNewTripViewModel,selectedDriverId.equals(driver.userId))
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
    ListItem(
        headlineContent = {
            Text(driver.baseLocationName?:"", color = MaterialTheme.colorScheme.onSurfaceVariant )
        },
        modifier = Modifier.clickable {
            scheduleNewTripViewModel.updateSelectedDriver(driver.userId?:"",clear = selected)
        },
        overlineContent = {
            Text(driver.userId?:"", color = MaterialTheme.colorScheme.onSurfaceVariant )
        },
        supportingContent = {
            Text((driver.vehicleNumber) ?: "", color = MaterialTheme.colorScheme.onSurfaceVariant )
        },
        leadingContent = {

        },
        trailingContent = {
            Checkbox(
                checked = selected,
                onCheckedChange = {
                    scheduleNewTripViewModel.updateSelectedDriver(driver.userId?:"",clear = selected)
                }
            )
        },
        colors = ListItemDefaults.colors(
            containerColor = Color.Transparent
        )
    )
}
