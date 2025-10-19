package com.deltasoft.pharmatracker.screens.home.queue.entity

data class RouteSummaryList(
    var route: String? = null,
    var userSummaryList: ArrayList<UserSummaryList> = arrayListOf()
)
