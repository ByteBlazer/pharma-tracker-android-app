package com.deltasoft.pharmatracker.screens.home.MyTrips.singletripdetails

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
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
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.deltasoft.pharmatracker.R
import com.deltasoft.pharmatracker.screens.App_CommonTopBar
import com.deltasoft.pharmatracker.screens.home.MyTrips.singletripdetails.entity.Doc
import com.deltasoft.pharmatracker.screens.home.MyTrips.singletripdetails.entity.DocGroup
import com.deltasoft.pharmatracker.screens.home.MyTrips.singletripdetails.entity.SingleTripDetailsResponse
import com.deltasoft.pharmatracker.screens.home.route.scheduletrip.ScheduleNewTripState
import com.deltasoft.pharmatracker.utils.AppUtils
import com.deltasoft.pharmatracker.utils.AppUtils.isNotNullOrEmpty

@Composable
fun SingleTripDetailsScreen(
    navController: NavHostController,
    selectedScheduledTripId: String,
    singleTripDetailsViewModel: SingleTripDetailsViewModel = viewModel()
) {
    val context = LocalContext.current

    LaunchedEffect(selectedScheduledTripId) {
        singleTripDetailsViewModel?.getSingleTripDetails(selectedScheduledTripId)
    }

    val singleTripDetailsState by singleTripDetailsViewModel.singleTripDetailsState.collectAsState()

    Scaffold(modifier = Modifier.fillMaxSize(),
        topBar = {   App_CommonTopBar(title = "My Trip Details", onBackClick = {  navController.popBackStack() }) },
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
                    Text("End Trip")
                }
            }
        }) { paddingValues ->
        val modifier = Modifier.padding(paddingValues)
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize(), contentAlignment = Alignment.Center
            ) {
                when (singleTripDetailsState) {
                    is SingleTripDetailsState.Idle -> {
                        CircularProgressIndicator()
                    }

                    is SingleTripDetailsState.Loading -> {
                        CircularProgressIndicator()
                    }

                    is SingleTripDetailsState.Success -> {
                        val singleTripDetailsResponse  =
                            (singleTripDetailsState as SingleTripDetailsState.Success).singleTripDetailsResponse
                        singleTripDetailsResponse?.let {
                            SingleTripDetailsCompose(it,singleTripDetailsViewModel)
                        }
                    }

                    is SingleTripDetailsState.Error -> {
                        val message = (singleTripDetailsState as SingleTripDetailsState.Error).message
                        Text(text = message)
                    }
                }
            }
        }
    }
}

@Composable
fun SingleTripDetailsCompose(
    singleTripDetailsResponse: SingleTripDetailsResponse,
    singleTripDetailsViewModel: SingleTripDetailsViewModel
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
                docGroup
            )
        }
    }
}

@Composable
fun TripBasicDetailsCompose(singleTripDetailsResponse: SingleTripDetailsResponse) {
    Card(
        modifier = Modifier
            .padding(vertical = 8.dp)
    ) {
        Column(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            SingleMyTripRowItem(
                key = "Route",
                value = singleTripDetailsResponse.route ?: ""+" : On Trip",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            SingleMyTripRowItem(
                key = "Trip ID",
                value = singleTripDetailsResponse.tripId.toString(),
                style = MaterialTheme.typography.titleSmall
            )
           SingleMyTripRowItem(
                key = "Created By",
                value = singleTripDetailsResponse.createdBy ?: "",
                style = MaterialTheme.typography.titleSmall
            )
            SingleMyTripRowItem(
                key = "Created At",
                value = singleTripDetailsResponse.createdAtFormatted ?: "",
                style = MaterialTheme.typography.titleSmall
            )
            SingleMyTripRowItem(
                key = "Driver Name",
                value = singleTripDetailsResponse.driverName ?: "",
                style = MaterialTheme.typography.titleMedium
            )
            SingleMyTripRowItem(
                key = "Vehicle Number",
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
fun DocGroupCompose(singleTripDetailsViewModel: SingleTripDetailsViewModel, docGroup: DocGroup) {
    var isExpanded by rememberSaveable { mutableStateOf(docGroup.expandGroupByDefault && !docGroup.dropOffCompleted) }

    Column(Modifier.fillMaxWidth()) {
        SingleDocGroup(
            onClick = {
                if(!docGroup.dropOffCompleted){
                    isExpanded = !isExpanded
                }
            },
            isExpanded,
            docGroup
        )
        AnimatedVisibility(visible = isExpanded) {
            ExpandedDocGroup(
                singleTripDetailsViewModel,
                docGroup
            )
        }

    }
}

@Composable
fun SingleDocGroup(
    onClick: () -> Unit,
    isExpanded: Boolean,
    docGroup: DocGroup
) {
    var expandIcon =  if (isExpanded) {
        painterResource(id = R.drawable.ic_expand_circle_right)
    } else {
        painterResource(id = R.drawable.ic_expand_circle_down)
    }
    ListItem(
        modifier = Modifier.clickable { onClick.invoke() },
        headlineContent = { Text(text = docGroup.heading?:"") },
        leadingContent = null,
        trailingContent = {
            Row {
                if (docGroup.showDropOffButton) {
                    TextButton(onClick = {  }) {
                        Text(text = "DDrop Off At Hub")
                    }
                }else if (docGroup.droppable) {
                    TextButton(onClick = {  }) {
                        Text(text = "Dropped Off At Hub")
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
        }
    )
}
@Composable
fun ExpandedDocGroup(singleTripDetailsViewModel: SingleTripDetailsViewModel, docGroup: DocGroup) {

    Column(
        Modifier
            .fillMaxWidth()
            .padding(start = 32.dp)
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
        Column(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            SingleDocRowItem(
                key = "Firm Name",
                value = doc.customerFirmName ?: "",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            SingleDocRowItem(
                key = "Amount",
                value = doc.docAmount.toString(),
                style = MaterialTheme.typography.titleSmall
            )
            SingleDocRowItem(
                key = "Address",
                value = doc.customerAddress ?: "",
                style = MaterialTheme.typography.titleSmall
            )
            SingleDocRowItem(
                key = "City",
                value = doc.customerCity ?: "",
                style = MaterialTheme.typography.titleSmall
            )
            SingleDocRowItem(
                key = "Pin Code",
                value = doc.customerPincode ?: "",
                style = MaterialTheme.typography.titleMedium
            )
            SingleDocRowItem(
                key = "Phone",
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
