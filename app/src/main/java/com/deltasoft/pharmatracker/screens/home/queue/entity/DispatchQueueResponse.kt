package com.deltasoft.pharmatracker.screens.home.queue.entity

data class DispatchQueueResponse(
    var success: Boolean? = false,
    var message: String? = null,
    var dispatchQueueList: DispatchQueueListData? = DispatchQueueListData(),
    var totalDocs: Int? = null
)
