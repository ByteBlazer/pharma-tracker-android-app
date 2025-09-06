package com.deltasoft.pharmatracker.screens.home

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBox
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.ui.graphics.vector.ImageVector

sealed class BottomNavItem(
    val route: String, val icon: ImageVector, val title: String,
    val visibleFor: Set<UserType>
) {
    object Scan : BottomNavItem(
        "scan", Icons.Default.Home, "Scan",
        setOf(
            UserType.WEB_ACCESS,
            UserType.APP_SCANNER,
            UserType.APP_TRIP_CREATOR,
            UserType.APP_ADMIN,
            UserType.APP_TRIP_DRIVER
        )
    )

    object RouteQueue : BottomNavItem(
        "route_queue", Icons.Default.AccountBox, "Route Queue",
        setOf(UserType.APP_TRIP_CREATOR, UserType.APP_ADMIN)
    )

    object ScheduledTrips : BottomNavItem(
        "scheduled_trips", Icons.Default.AddCircle, "Scheduled Trips",
        setOf(UserType.APP_SCANNER, UserType.APP_ADMIN, UserType.APP_TRIP_DRIVER)
    )

    object Drive : BottomNavItem(
        "drive", Icons.Default.LocationOn, "Drive",
        setOf(UserType.APP_SCANNER, UserType.APP_ADMIN, UserType.APP_TRIP_DRIVER)
    )

    object Profile : BottomNavItem(
        "profile", Icons.Default.Person, "Profile",
        setOf(UserType.APP_SCANNER, UserType.APP_ADMIN, UserType.APP_TRIP_DRIVER)
    )
}