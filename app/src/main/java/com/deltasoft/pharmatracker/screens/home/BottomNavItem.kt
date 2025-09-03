package com.deltasoft.pharmatracker.screens.home

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.ui.graphics.vector.ImageVector

sealed class BottomNavItem(val route: String, val icon: ImageVector, val title: String) {
    object Home : BottomNavItem("home", Icons.Default.Home, "Home")
    object Location : BottomNavItem("location", Icons.Default.LocationOn, "Location")
    object Profile : BottomNavItem("profile", Icons.Default.Person, "Profile")
}