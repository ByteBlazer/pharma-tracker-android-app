package com.deltasoft.pharmatracker.screens.home.profile

import android.content.Context
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.ListItem
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.deltasoft.pharmatracker.utils.AppUtils.isNotNullOrEmpty
import com.deltasoft.pharmatracker.utils.sharedpreferences.PrefsKey
import com.deltasoft.pharmatracker.utils.sharedpreferences.SharedPreferencesUtil

@Composable
fun ProfileScreen(context: Context) {

    val sharedPrefsUtil = SharedPreferencesUtil(context)
    val userName = sharedPrefsUtil.getString(PrefsKey.USER_NAME)
    val userId = sharedPrefsUtil.getString(PrefsKey.USER_ID)
    val phone = sharedPrefsUtil.getString(PrefsKey.PHONE_NUMBER)
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(
            Modifier.fillMaxWidth(fraction = 0.75f),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Image(
                imageVector = Icons.Default.Person,
                contentDescription = "",
                modifier = Modifier.size(96.dp),
                colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.onSurface)
            )
            Spacer(Modifier.height(16.dp))
            SingleRowItem("User ID", userId)
            SingleRowItem("User name", userName)
            SingleRowItem("Phone", phone)
        }
    }
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun SingleRowItem(key: String, value: String?) {
    if (value.isNotNullOrEmpty()) {
        ListItem(
            modifier = Modifier.fillMaxWidth(),
            text = {
                Row(Modifier.fillMaxWidth()) {
                    Text(
                        text = key ?: "",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.weight(1f)
                    )
                    Text(
                        text = value ?: "",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.weight(1f),
                    )
                }
            }
        )
    }
}