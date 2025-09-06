package com.deltasoft.pharmatracker.screens.home

import androidx.lifecycle.ViewModel
import com.deltasoft.pharmatracker.utils.AppUtils.isNotNullOrEmpty
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class HomeViewModel : ViewModel() {
    private val _userRoles = MutableStateFlow<Set<UserType>>(emptySet())
    val userRoles = _userRoles.asStateFlow()

    fun setUserRoles(rolesAsString: String) {
        val rolesList = if (rolesAsString.isNotNullOrEmpty()) {
            rolesAsString.split(",")
        } else {
            listOf()
        }
        val mappedRoles = rolesList.mapNotNull { mapStringToUserType(it) }.toSet()
        _userRoles.value = mappedRoles
    }

    private fun mapStringToUserType(roleString: String): UserType? {
        return when (roleString.lowercase()) {
            "web-access" -> UserType.WEB_ACCESS
            "app-scanner" -> UserType.APP_SCANNER
            "app-trip-creator" -> UserType.APP_TRIP_CREATOR
            "app-admin" -> UserType.APP_ADMIN
            "app-trip-driver" -> UserType.APP_TRIP_DRIVER
            else -> null
        }
    }
}