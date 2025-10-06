package com.deltasoft.pharmatracker.screens.home.MyTrips.singletripdetails

import android.util.Log
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.deltasoft.pharmatracker.R
import com.deltasoft.pharmatracker.screens.AppConfirmationDialog
import com.deltasoft.pharmatracker.screens.App_CommonTopBar
import com.deltasoft.pharmatracker.screens.home.MyTrips.AppCommonApiState
import com.deltasoft.pharmatracker.screens.home.MyTrips.singletripdetails.entity.Doc
import com.deltasoft.pharmatracker.screens.home.MyTrips.singletripdetails.entity.DocGroup
import com.deltasoft.pharmatracker.screens.home.MyTrips.singletripdetails.entity.SingleTripDetailsResponse
import com.deltasoft.pharmatracker.utils.AppUtils
import com.deltasoft.pharmatracker.utils.AppUtils.isNotNullOrEmpty

private const val TAG = "SingleTripDetailsScreen"
@Composable
fun SingleTripDetailsScreen(
    navController: NavHostController,
    selectedScheduledTripId: String,
    singleTripDetailsViewModel: SingleTripDetailsViewModel = viewModel()
) {
    val context = LocalContext.current

    LaunchedEffect(selectedScheduledTripId) {
        singleTripDetailsViewModel.selectedScheduledTripId = selectedScheduledTripId
        singleTripDetailsViewModel?.getSingleTripDetails()
    }

    val singleTripDetailsState by singleTripDetailsViewModel.singleTripDetailsState.collectAsState()
    val dropOffTripState by singleTripDetailsViewModel.dropOffTripState.collectAsState()
    val endTripState by singleTripDetailsViewModel.endTripState.collectAsState()


    var dropOffTripId by remember { mutableStateOf("") }
    var dropOffHeading by remember { mutableStateOf("") }

    LaunchedEffect(dropOffTripState) {
        when (dropOffTripState) {
            is AppCommonApiState.Idle -> {
                Log.d(TAG, "State: Idle")
            }
            is AppCommonApiState.Loading -> {
                Log.d(TAG, "State: Loading")
            }
            is AppCommonApiState.Success -> {
                val message = (dropOffTripState as AppCommonApiState.Success).message
                Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                Log.d(TAG, "State: Success - Message: $message")
                singleTripDetailsViewModel?.getSingleTripDetails()
            }
            is AppCommonApiState.Error -> {
                val message = (dropOffTripState as AppCommonApiState.Error).message
                Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                Log.e(TAG, "State: Error - Message: $message")
            }
        }
    }

    LaunchedEffect(endTripState) {
        when (endTripState) {
            is AppCommonApiState.Idle -> {
                Log.d(TAG, "State: Idle")
            }
            is AppCommonApiState.Loading -> {
                Log.d(TAG, "State: Loading")
            }
            is AppCommonApiState.Success -> {
                val message = (endTripState as AppCommonApiState.Success).message
                Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                singleTripDetailsViewModel.stopService(context)
                navController.popBackStack()
                Log.d(TAG, "State: Success - Message: $message")

            }
            is AppCommonApiState.Error -> {
                val message = (endTripState as AppCommonApiState.Error).message
                Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                Log.e(TAG, "State: Error - Message: $message")
            }
        }
    }

    Scaffold(modifier = Modifier.fillMaxSize(),
        topBar = {   App_CommonTopBar(title = stringResource(R.string.single_trip_details_heading), onBackClick = {  navController.popBackStack() }) },
        bottomBar = {
            Column(Modifier
                .fillMaxWidth()
                .windowInsetsPadding(WindowInsets.navigationBars)) {
                Button(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp), onClick = {
                            singleTripDetailsViewModel.endTrip(selectedScheduledTripId)
                    }
                ) {
                    Text(stringResource(R.string.end_trip_btn_txt))
                }
            }
        }) { paddingValues ->
        val modifier = Modifier.padding(paddingValues)
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize(), contentAlignment = Alignment.Center
            ) {
                if (dropOffTripState is AppCommonApiState.Loading || endTripState is AppCommonApiState.Loading){
                    CircularProgressIndicator()
                }else {
                    when (singleTripDetailsState) {
                        is SingleTripDetailsState.Idle -> {
                            CircularProgressIndicator()
                        }

                        is SingleTripDetailsState.Loading -> {
                            CircularProgressIndicator()
                        }

                        is SingleTripDetailsState.Success -> {
                            val singleTripDetailsResponse =
                                (singleTripDetailsState as SingleTripDetailsState.Success).singleTripDetailsResponse
                            singleTripDetailsResponse?.let {
                                SingleTripDetailsCompose(it, singleTripDetailsViewModel, dropOffOnClick = { tripId, heading ->
                                    dropOffTripId = tripId
                                    dropOffHeading = heading
                                })
                            }
                        }

                        is SingleTripDetailsState.Error -> {
                            val message =
                                (singleTripDetailsState as SingleTripDetailsState.Error).message
                            Text(text = message)
                        }
                    }
                }
            }
        }
    }
    AppConfirmationDialog(
        showDialog = dropOffTripId.isNotNullOrEmpty() && dropOffHeading.isNotNullOrEmpty(),
        onConfirm = {
            singleTripDetailsViewModel.dropOffTrip(
                selectedScheduledTripId = dropOffTripId,
                heading = dropOffHeading
            )
            dropOffTripId = ""
            dropOffHeading = ""
        },
        onDismiss = {
            dropOffTripId = ""
            dropOffHeading = ""
        },
        title = stringResource(R.string.drop_off_confirm_title),
        message = stringResource(R.string.drop_off_confirm_message)
    )
}

@Composable
fun SingleTripDetailsCompose(
    singleTripDetailsResponse: SingleTripDetailsResponse,
    singleTripDetailsViewModel: SingleTripDetailsViewModel,
    dropOffOnClick: (tripId: String, heading: String) -> Unit = { a, b -> }
) {
    Column(
        Modifier
            .fillMaxWidth(),
        verticalArrangement = Arrangement.Center
    ) {
        TripBasicDetailsCompose(singleTripDetailsResponse)
        Spacer(Modifier.height(16.dp))
        for (docGroup in singleTripDetailsResponse.docGroups?:arrayListOf()) {
            DocGroupCompose(
                singleTripDetailsViewModel,
                docGroup,
                dropOffOnClick = dropOffOnClick
            )
        }
    }
}

@Composable
fun TripBasicDetailsCompose(singleTripDetailsResponse: SingleTripDetailsResponse) {
    OutlinedCard(
        modifier = Modifier
            .padding(vertical = 8.dp)
    ) {
        Column(Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            SingleMyTripRowItem(
                key = stringResource(R.string.row_item_title_route),
                value = singleTripDetailsResponse.route ?: ""+" : On Trip",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            SingleMyTripRowItem(
                key = stringResource(R.string.row_item_title_trip_id),
                value = singleTripDetailsResponse.tripId.toString(),
                style = MaterialTheme.typography.titleSmall
            )
           SingleMyTripRowItem(
                key = stringResource(R.string.row_item_title_created_by),
                value = singleTripDetailsResponse.createdBy ?: "",
                style = MaterialTheme.typography.titleSmall
            )
            SingleMyTripRowItem(
                key = stringResource(R.string.row_item_title_created_at),
                value = singleTripDetailsResponse.createdAtFormatted ?: "",
                style = MaterialTheme.typography.titleSmall
            )
            SingleMyTripRowItem(
                key = stringResource(R.string.row_item_title_driver_name),
                value = singleTripDetailsResponse.driverName ?: "",
                style = MaterialTheme.typography.titleMedium
            )
            SingleMyTripRowItem(
                key = stringResource(R.string.row_item_title_vehicle_number),
                value = singleTripDetailsResponse.vehicleNumber ?: "",
                style = MaterialTheme.typography.titleMedium
            )
        }

    }
}
@Composable
private fun SingleMyTripRowItem(key: String, value: String, style: TextStyle, color: Color = MaterialTheme.colorScheme.onSurfaceVariant, fontWeight: FontWeight = FontWeight.Normal) {
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Text(
            text = key,
            style = style,
            color = color,
            fontWeight = fontWeight,
            modifier = Modifier.weight(0.6f)
        )
        Text(
            text = ":",
            style = style,
            color = color,
            fontWeight = FontWeight.Normal,
            modifier = Modifier.weight(0.10f)
        )
        Text(
            text = value,
            style = style,
            color = color,
            modifier = Modifier.weight(1f),
            textAlign = TextAlign.Start
        )
    }
}

@Composable
fun DocGroupCompose(singleTripDetailsViewModel: SingleTripDetailsViewModel, docGroup: DocGroup,
                    dropOffOnClick: (tripId: String, heading: String) -> Unit = { a, b -> }) {
    var isExpanded by rememberSaveable { mutableStateOf(docGroup.expandGroupByDefault && !docGroup.dropOffCompleted) }
    Card(
        modifier = Modifier
            .padding(vertical = 8.dp)
    ) {
        Column(Modifier.fillMaxWidth()) {
            SingleDocGroup(
                onClick = {
                    if (!docGroup.dropOffCompleted) {
                        isExpanded = !isExpanded
                    }
                },
                isExpanded,
                docGroup,
                singleTripDetailsViewModel,
                dropOffOnClick = dropOffOnClick
            )
            AnimatedVisibility(visible = isExpanded) {
                ExpandedDocGroup(
                    singleTripDetailsViewModel,
                    docGroup
                )
            }

        }
    }
}

@Composable
fun SingleDocGroup(
    onClick: () -> Unit,
    isExpanded: Boolean,
    docGroup: DocGroup,
    singleTripDetailsViewModel: SingleTripDetailsViewModel,
    dropOffOnClick: (tripId: String, heading: String) -> Unit = { a, b -> }
) {
    var expandIcon =  if (isExpanded) {
        painterResource(id = R.drawable.ic_expand_circle_down)
    } else {
        painterResource(id = R.drawable.ic_expand_circle_right)
    }
    ListItem(
        modifier = Modifier.clickable { onClick.invoke() },
        headlineContent = { Text(text = docGroup.heading?:"") },
        leadingContent = null,
        trailingContent = {
            Row {
                if (docGroup.showDropOffButton) {
                    Button(onClick = {
//                        singleTripDetailsViewModel.dropOffTrip(
//                            selectedScheduledTripId = singleTripDetailsViewModel.selectedScheduledTripId,
//                            heading = docGroup.heading ?: ""
//                        )
                        dropOffOnClick.invoke(singleTripDetailsViewModel.selectedScheduledTripId?:"", docGroup.heading ?: "")
                    }) {
                        Text(text = stringResource(R.string.drop_off_at_hub_btn_txt))
                    }
                }else if (docGroup.droppable) {
                    TextButton(onClick = {
                        onClick.invoke()
                    }) {
                        Text(text = stringResource(R.string.dropped_off_at_hub_txt))
                    }
                }else{
                    null
                }
                IconButton(onClick = {onClick.invoke() }) {
                    Icon(
                        painter = expandIcon,
                        contentDescription = "expandable button",
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        },
        colors = ListItemDefaults.colors(containerColor = Color.Transparent)
    )
}
@Composable
fun ExpandedDocGroup(singleTripDetailsViewModel: SingleTripDetailsViewModel, docGroup: DocGroup) {

    Column(
        Modifier
            .fillMaxWidth()
            .padding(start = 8.dp)
    ) {
        for (doc in docGroup.docs?: arrayListOf()) {
            Column(Modifier.fillMaxWidth()) {
                SingleDoc(
                    singleTripDetailsViewModel,
                    doc
                )
            }
        }
    }
}

@Composable
fun SingleDoc(singleTripDetailsViewModel: SingleTripDetailsViewModel, doc: Doc) {
    val context = LocalContext.current
    Card(
        modifier = Modifier
            .padding(vertical = 8.dp)
    ) {
        Column(Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            SingleDocRowItem(
                key = stringResource(R.string.row_item_title_firm_name),
                value = doc.customerFirmName ?: "",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            SingleDocRowItem(
                key = stringResource(R.string.row_item_title_amount),
                value = doc.docAmount.toString(),
                style = MaterialTheme.typography.titleSmall
            )
            SingleDocRowItem(
                key = stringResource(R.string.row_item_title_address),
                value = doc.customerAddress ?: "",
                style = MaterialTheme.typography.titleSmall
            )
            SingleDocRowItem(
                key = stringResource(R.string.row_item_title_city),
                value = doc.customerCity ?: "",
                style = MaterialTheme.typography.titleSmall
            )
            SingleDocRowItem(
                key = stringResource(R.string.row_item_title_pin_code),
                value = doc.customerPincode ?: "",
                style = MaterialTheme.typography.titleMedium
            )
            SingleDocRowItem(
                key = stringResource(R.string.row_item_title_phone),
                value = doc.customerPhone ?: "",
                style = MaterialTheme.typography.titleMedium
            )
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                IconButton(
                    onClick = {
                        AppUtils.startGoogleMapsNavigation(
                            context = context,
                            latitude = doc.customerGeoLatitude?:"",
                            longitude = doc.customerGeoLongitude?:"",
                            destinationName = doc.customerFirmName?:""
                        )
                    },
                    modifier = Modifier
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_location),
                        contentDescription = "location",
                        modifier = Modifier.size(24.dp),
                    )
                }
            }
        }

    }
}

@Composable
private fun SingleDocRowItem(key: String, value: String, style: TextStyle, color: Color = MaterialTheme.colorScheme.onSurfaceVariant, fontWeight: FontWeight = FontWeight.Normal) {
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Text(
            text = key,
            style = style,
            color = color,
            fontWeight = fontWeight,
            modifier = Modifier.weight(0.6f)
        )
        Text(
            text = ":",
            style = style,
            color = color,
            fontWeight = FontWeight.Normal,
            modifier = Modifier.weight(0.10f)
        )
        Text(
            text = value,
            style = style,
            color = color,
            modifier = Modifier.weight(1f),
            textAlign = TextAlign.Start
        )
    }
}
