package com.deltasoft.pharmatracker.screens.home


import android.content.Context
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.deltasoft.pharmatracker.R
import com.deltasoft.pharmatracker.navigation.NavConstants
import com.deltasoft.pharmatracker.navigation.Screen
import com.deltasoft.pharmatracker.screens.home.MyTrips.MyTripsScreen
import com.deltasoft.pharmatracker.screens.home.location.LocationScreen
import com.deltasoft.pharmatracker.screens.home.route.DispatchQueueScreen
import com.deltasoft.pharmatracker.screens.home.scan.BarCodeScanner
import com.deltasoft.pharmatracker.screens.home.schedule.ScheduledTripsScreen
import com.deltasoft.pharmatracker.utils.sharedpreferences.PrefsKey
import com.deltasoft.pharmatracker.utils.sharedpreferences.SharedPreferencesUtil
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    navController: NavHostController, context:Context,
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
                            icon = { Icon(painterResource(item.icon) , contentDescription = item.title) },
                            label = { Text(item.title,
                                modifier = Modifier,
                                textAlign = TextAlign.Center,
                                maxLines = 2 ) },
                            selected = pagerState.currentPage == index,
                            onClick = {
                                coroutineScope.launch {
                                    pagerState.animateScrollToPage(index)
                                }
                            }
                        )
                    }
                }
            },
            topBar = {
                val currentPageTitle = bottomNavItems[pagerState.currentPage].title
                CenterAlignedTopAppBar(
                    title = {
                        Text(
                            text = currentPageTitle,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    },
                    navigationIcon = {
                        if (false) {
                            IconButton(onClick = {
                                navController.navigate(Screen.Profile.route)
                            }) {
                                Icon(
                                    painter = painterResource(id = R.drawable.ic_outline_person),
                                    contentDescription = "profile"
                                )
                            }
                        }
                    },
                    actions = {
                        val isNeedToShowReloadButton = bottomNavItems[pagerState.currentPage].route != NavConstants.ROUTE_SCAN_SCREEN
                        if (true) {
                            if (isNeedToShowReloadButton) {
                                IconButton(onClick = {
                                    when(bottomNavItems[pagerState.currentPage].route ){
                                        NavConstants.ROUTE_ROUTE_SCREEN->{
                                            homeViewModel?.onDispatchQueueReloadButtonClick()
                                        }
                                        NavConstants.ROUTE_SCHEDULED_TRIPS_SCREEN->{
                                            homeViewModel?.onScheduledReloadButtonClick()
                                        }
                                        NavConstants.ROUTE_MY_TRIPS_SCREEN->{
                                            homeViewModel?.onMyTripsReloadButtonClick()
                                        }
                                    }
                                }) {
                                    Icon(
                                        painter = painterResource(id = R.drawable.ic_refresh),
                                        contentDescription = "dispatch queue reload"
                                    )
                                }
                            }
                            IconButton(onClick = {
                                navController.navigate(Screen.Profile.route)
                            }) {
                                Icon(
                                    painter = painterResource(id = R.drawable.ic_outline_person),
                                    contentDescription = "profile"
                                )
                            }
                        }
                    }
                )
            }
        ) { paddingValues ->
            HorizontalPager(
                state = pagerState,
                modifier = Modifier.padding(paddingValues)
            ) { page ->
                when (bottomNavItems[page].route) {
                    NavConstants.ROUTE_SCAN_SCREEN -> BarCodeScanner()
                    NavConstants.ROUTE_ROUTE_SCREEN -> DispatchQueueScreen(navController = navController, homeViewModel = homeViewModel)
                    NavConstants.ROUTE_SCHEDULED_TRIPS_SCREEN -> ScheduledTripsScreen(homeViewModel = homeViewModel)
                    NavConstants.ROUTE_MY_TRIPS_SCREEN -> MyTripsScreen(homeViewModel = homeViewModel)
                }
            }
        }
    }
}