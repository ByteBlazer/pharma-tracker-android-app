package com.deltasoft.pharmatracker.screens.home

import androidx.lifecycle.ViewModel
import com.deltasoft.pharmatracker.utils.AppUtils.isNotNullOrEmpty
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.UUID

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

    private val _dispatchQueueClickEvent = MutableStateFlow(UUID.randomUUID())
    val dispatchQueueClickEvent: StateFlow<UUID> = _dispatchQueueClickEvent.asStateFlow()

    fun onDispatchQueueReloadButtonClick() {
        _dispatchQueueClickEvent.value = UUID.randomUUID()
    }



    private val _scheduledListRefreshClickEvent = MutableStateFlow(UUID.randomUUID())
    val scheduledListRefreshClickEvent: StateFlow<UUID> = _scheduledListRefreshClickEvent.asStateFlow()

    fun onScheduledReloadButtonClick() {
        _scheduledListRefreshClickEvent.value = UUID.randomUUID()
    }



    private val _myTripsListRefreshClickEvent = MutableStateFlow(UUID.randomUUID())
    val myTripsListRefreshClickEvent: StateFlow<UUID> = _myTripsListRefreshClickEvent.asStateFlow()

    fun onMyTripsReloadButtonClick() {
        _myTripsListRefreshClickEvent.value = UUID.randomUUID()
    }
}