package com.deltasoft.pharmatracker.screens.home

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBox
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.ui.graphics.vector.ImageVector
import com.deltasoft.pharmatracker.R
import com.deltasoft.pharmatracker.navigation.NavConstants

sealed class BottomNavItem(
    val route: String, val icon: Int, val title: String,
    val visibleFor: Set<UserType>
) {
    object Scan : BottomNavItem(
        NavConstants.ROUTE_SCAN_SCREEN, R.drawable.ic_barcode_scanner, "Scan",
        setOf(
            UserType.APP_ADMIN,
            UserType.APP_TRIP_CREATOR,
            UserType.APP_SCANNER
        )
    )

    object RouteQueue : BottomNavItem(
        NavConstants.ROUTE_ROUTE_SCREEN, R.drawable.ic_format_list_bulleted_add, "Queue",
        setOf(
            UserType.APP_ADMIN,
            UserType.APP_TRIP_CREATOR
        )
    )

    object ScheduledTrips : BottomNavItem(
        NavConstants.ROUTE_SCHEDULED_TRIPS_SCREEN,  R.drawable.ic_calendar_clock, "Trips",
        setOf(
            UserType.APP_ADMIN,
            UserType.APP_TRIP_CREATOR
        )
    )

    object Drive : BottomNavItem(
        NavConstants.ROUTE_MY_TRIPS_SCREEN,  R.drawable.ic_local_shipping, "My Trips",
        setOf(
            UserType.APP_ADMIN,
            UserType.APP_TRIP_DRIVER
        )
    )

    object Profile : BottomNavItem(
        "profile",  R.drawable.ic_outline_person, "Profile",
        setOf(UserType.APP_ADMIN)
    )
}