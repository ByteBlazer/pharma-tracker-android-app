package com.deltasoft.pharmatracker.screens.home.scan

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.deltasoft.pharmatracker.screens.home.BarCodeScanner

@Composable
fun ScanScreen() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        BarCodeScanner()
    }
}