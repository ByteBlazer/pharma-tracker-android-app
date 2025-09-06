package com.deltasoft.pharmatracker.screens.home


import android.content.Context
import android.util.Log
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import com.deltasoft.pharmatracker.screens.home.location.LocationScreen
import com.deltasoft.pharmatracker.screens.home.location.LocationViewModel
import com.deltasoft.pharmatracker.utils.sharedpreferences.PrefsKey
import com.deltasoft.pharmatracker.utils.sharedpreferences.SharedPreferencesUtil
import kotlinx.coroutines.launch

@Composable
fun HomeScreen(context:Context,
               homeViewModel: HomeViewModel = viewModel()) {
    val sharedPrefsUtil = SharedPreferencesUtil(context)
    val token = sharedPrefsUtil.getString(PrefsKey.USER_ACCESS_TOKEN)
    val roles = sharedPrefsUtil.getString(PrefsKey.ROLES)

    homeViewModel.setUserRoles(roles)

    val userRoles by homeViewModel.userRoles.collectAsState()

    if (userRoles.size == 0){
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("No navigation tabs are visible as your user id does not have the required access roles. Please contact admin to provide the necessary access roles for your user id", style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.onSurfaceVariant )
        }
    }else {

        val bottomNavItems = listOf(
            BottomNavItem.Scan,
            BottomNavItem.RouteQueue,
            BottomNavItem.ScheduledTrips,
            BottomNavItem.Drive,
//        BottomNavItem.Profile
        ).filter {
            it.visibleFor.any { role -> userRoles.contains(role) }
        }


        val pagerState = rememberPagerState(initialPage = 0) {
            bottomNavItems.size
        }
        val coroutineScope = rememberCoroutineScope()

        Scaffold(
            bottomBar = {
                NavigationBar {
                    bottomNavItems.forEachIndexed { index, item ->
                        NavigationBarItem(
                            icon = { Icon(item.icon, contentDescription = item.title) },
                            label = { Text(item.title) },
                            selected = pagerState.currentPage == index,
                            onClick = {
                                coroutineScope.launch {
                                    pagerState.animateScrollToPage(index)
                                }
                            }
                        )
                    }
                }
            }
        ) { paddingValues ->
            HorizontalPager(
                state = pagerState,
                modifier = Modifier.padding(paddingValues)
            ) { page ->
                when (bottomNavItems[page].route) {
                    "scan" -> HomeScreenContent()
                    "route_queue" -> RouteQueueScreen()
                    "scheduled_trips" -> ScheduledTripsScreen()
                    "drive" -> LocationScreenContent()
                }
            }
        }
    }
}


@Composable
fun HomeScreenContent() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        BarCodeScanner()
    }
}

@Composable
fun LocationScreenContent(
    locationViewModel: LocationViewModel = viewModel()
) {
    val context = LocalContext.current
    val latitude by locationViewModel.latitude.collectAsState()
    val longitude by locationViewModel.longitude.collectAsState()

    Log.d("TAG", "LocationScreenContent: latitude "+latitude)
    Log.d("TAG", "LocationScreenContent: longitude "+longitude)

    // Register and unregister the receiver with the composable's lifecycle
    DisposableEffect(locationViewModel) {
        locationViewModel.registerReceiver(context)
        onDispose {
            locationViewModel.unregisterReceiver(context)
        }
    }

    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        LocationScreen(longitude = longitude, latitude = latitude)
    }
}

@Composable
fun RouteQueueScreen() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text("Route queue", style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.onSurfaceVariant )
    }
}

@Composable
fun ScheduledTripsScreen() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text("Scheduled Trips", style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.onSurfaceVariant )
    }
}


//@Preview(showBackground = true)
//@Composable
//fun HomeScreenPreview() {
//    HomeScreen()
//}