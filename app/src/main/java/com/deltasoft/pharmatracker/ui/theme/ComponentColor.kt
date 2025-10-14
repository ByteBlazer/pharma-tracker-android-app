package com.deltasoft.pharmatracker.ui.theme

import androidx.compose.material3.ButtonColors
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.IconButtonColors
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.NavigationBarItemColors
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.RadioButtonColors
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.TopAppBarColors
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.deltasoft.pharmatracker.utils.AppUtils

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun getCenterAlignedTopAppBarColors(): TopAppBarColors {
    return TopAppBarDefaults.centerAlignedTopAppBarColors(
        containerColor = AppPrimary,
        titleContentColor = AppUtils.getTextColorBasedOnColortype(AppPrimary),
        navigationIconContentColor = AppUtils.getTextColorBasedOnColortype(AppPrimary),
        actionIconContentColor = AppUtils.getTextColorBasedOnColortype(AppPrimary)
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun getButtonColors(): ButtonColors {
    return ButtonDefaults.buttonColors(
        containerColor = AppPrimary,
        contentColor = AppUtils.getTextColorBasedOnColortype(AppPrimary)
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun getTextButtonColors(): ButtonColors {
    return ButtonDefaults.textButtonColors(
        contentColor = AppPrimary
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun getIconButtonColors(contentColor: Color? = null): IconButtonColors {
    return IconButtonDefaults.iconButtonColors(
        contentColor = contentColor ?: AppPrimary
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun getRadioButtonColors(): RadioButtonColors {
    return RadioButtonDefaults.colors(
        selectedColor = AppPrimary
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun getNavigationBarItemColors(): NavigationBarItemColors {
    return NavigationBarItemDefaults.colors(
        selectedIconColor = AppPrimary,
    selectedTextColor = AppPrimary,
    indicatorColor = AppTertiary
    )
}