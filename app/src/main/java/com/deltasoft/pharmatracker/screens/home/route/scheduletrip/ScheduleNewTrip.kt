package com.deltasoft.pharmatracker.screens.home.route.scheduletrip

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.deltasoft.pharmatracker.screens.home.route.DispatchQueueViewModel
import com.deltasoft.pharmatracker.screens.home.route.entity.RouteSummaryList
import com.deltasoft.pharmatracker.screens.home.route.entity.UserDetailsList
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
            Box(
                modifier = Modifier
                    .fillMaxSize(), contentAlignment = Alignment.Center
            ) {

            }
        }
    }

}