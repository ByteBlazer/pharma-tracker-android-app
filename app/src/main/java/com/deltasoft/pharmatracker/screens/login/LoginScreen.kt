package com.deltasoft.pharmatracker.screens.login

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.deltasoft.pharmatracker.R
import com.deltasoft.pharmatracker.screens.App_CommonTopBar
import com.deltasoft.pharmatracker.ui.theme.getButtonColors

private const val MPC_PHARMA_PACKAGE_ID = "com.mpc.pharma.mpc_pharma"

@Composable
fun LoginScreen(
    navController: NavHostController,
    prefillPhoneNumber: String? = "",
) {
    val context = LocalContext.current
    val playStoreWebUrl = stringResource(R.string.mpc_pharma_play_store_url)

    fun openMpcPharmaDownload() {
        val marketIntent = Intent(
            Intent.ACTION_VIEW,
            Uri.parse("market://details?id=$MPC_PHARMA_PACKAGE_ID")
        ).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        try {
            context.startActivity(marketIntent)
        } catch (_: Exception) {
            context.startActivity(
                Intent(Intent.ACTION_VIEW, Uri.parse(playStoreWebUrl)).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
            )
        }
    }

    Scaffold(
        topBar = {
            App_CommonTopBar(backButtonVisibility = false, useDefaultColor = true)
        },
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top
        ) {
            App_CommonTopBar(backButtonVisibility = false, useDefaultColor = true)
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    elevation = CardDefaults.cardElevation(
                        defaultElevation = dimensionResource(R.dimen.card_elevation)
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = stringResource(R.string.migration_banner_title),
                            style = MaterialTheme.typography.headlineLarge,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(24.dp))
                        Text(
                            text = stringResource(R.string.migration_banner_body),
                            style = MaterialTheme.typography.bodyLarge,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(32.dp))
                        Button(
                            onClick = { openMpcPharmaDownload() },
                            modifier = Modifier.fillMaxWidth(),
                            colors = getButtonColors()
                        ) {
                            Text(text = stringResource(R.string.migration_download_button))
                        }
                        Spacer(modifier = Modifier.height(24.dp))
                        Text(
                            text = stringResource(R.string.migration_uninstall_note),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }
    }
}
