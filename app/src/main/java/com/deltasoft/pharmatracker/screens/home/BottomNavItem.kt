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

sealed class BottomNavItem(
    val route: String, val icon: Int, val title: String,
    val visibleFor: Set<UserType>
) {
    object Scan : BottomNavItem(
        "scan", R.drawable.ic_barcode_scanner, "Scan",
        setOf(
            UserType.APP_ADMIN,
            UserType.APP_TRIP_CREATOR,
            UserType.APP_SCANNER
        )
    )

    object RouteQueue : BottomNavItem(
        "route_queue", R.drawable.ic_format_list_bulleted_add, "Dispatch Queue",
        setOf(
            UserType.APP_ADMIN,
            UserType.APP_TRIP_CREATOR
        )
    )

    object ScheduledTrips : BottomNavItem(
        "scheduled_trips",  R.drawable.ic_calendar_clock, "Scheduled Trips",
        setOf(
            UserType.APP_ADMIN,
            UserType.APP_TRIP_CREATOR
        )
    )

    object Drive : BottomNavItem(
        "drive",  R.drawable.ic_local_shipping, "Drive",
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