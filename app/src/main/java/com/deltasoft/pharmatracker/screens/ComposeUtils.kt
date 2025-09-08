package com.deltasoft.pharmatracker.screens

import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import com.deltasoft.pharmatracker.R
import com.deltasoft.pharmatracker.utils.AppUtils.isNotNullOrEmpty

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun App_CommonTopBar(title : String="", onBackClick: () -> Unit ={},backButtonVisibility : Boolean = true,btnTxt:String? = null,onBtnTxtClick: () -> Unit ={}){
    CenterAlignedTopAppBar(
        title = {
            Text(
                text = title,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        },
        navigationIcon = {
            if (backButtonVisibility) {
                IconButton(onClick = {
                    onBackClick.invoke()
                }) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_arrow_back),
                        contentDescription = "Back"
                    )
                }
            }
        }
    )
}