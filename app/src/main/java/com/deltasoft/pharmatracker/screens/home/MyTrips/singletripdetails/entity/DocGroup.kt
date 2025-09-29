package com.deltasoft.pharmatracker.screens.home.MyTrips.singletripdetails.entity

data class DocGroup(
    val heading: String? = null,
    val droppable: Boolean = false,
    val dropOffCompleted: Boolean = false,
    val showDropOffButton: Boolean = false,
    val expandGroupByDefault: Boolean = false,
    val docs: List<Doc>? = null
)
