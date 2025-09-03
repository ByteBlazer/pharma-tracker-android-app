package com.deltasoft.pharmatracker.api

data class ApiResponse(
    var message:String? = null,
    var error:String?= null,
    var statusCode:Int? = null
)
