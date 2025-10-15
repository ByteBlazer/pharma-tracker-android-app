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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.compose.ui.res.dimensionResource
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
import com.deltasoft.pharmatracker.ui.theme.getButtonColors
import com.deltasoft.pharmatracker.ui.theme.getIconButtonColors
import com.deltasoft.pharmatracker.ui.theme.getTextButtonColors
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
    val markAsUnDeliveredState by singleTripDetailsViewModel.markAsUnDeliveredState.collectAsState()
    val markAsDeliveredState by singleTripDetailsViewModel.markAsDeliveredState.collectAsState()


    var dropOffTripId by remember { mutableStateOf("") }
    var dropOffHeading by remember { mutableStateOf("") }


    var showDeliverySuccesDocId by remember { mutableStateOf("") }
    var showDeliveryFailedDocId by remember { mutableStateOf("") }

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

    LaunchedEffect(markAsDeliveredState) {
        when (markAsDeliveredState) {
            is AppCommonApiState.Idle -> {
                Log.d(TAG, "State: Idle")
            }
            is AppCommonApiState.Loading -> {
                Log.d(TAG, "State: Loading")
            }
            is AppCommonApiState.Success -> {
                val message = (markAsDeliveredState as AppCommonApiState.Success).message
                Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                singleTripDetailsViewModel?.getSingleTripDetails()
                Log.d(TAG, "State: Success - Message: $message")

            }
            is AppCommonApiState.Error -> {
                val message = (markAsDeliveredState as AppCommonApiState.Error).message
                Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                Log.e(TAG, "State: Error - Message: $message")
            }
        }
    }


    LaunchedEffect(markAsUnDeliveredState) {
        when (markAsUnDeliveredState) {
            is AppCommonApiState.Idle -> {
                Log.d(TAG, "State: Idle")
            }
            is AppCommonApiState.Loading -> {
                Log.d(TAG, "State: Loading")
            }
            is AppCommonApiState.Success -> {
                val message = (markAsUnDeliveredState as AppCommonApiState.Success).message
                Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                singleTripDetailsViewModel?.getSingleTripDetails()
                Log.d(TAG, "State: Success - Message: $message")

            }
            is AppCommonApiState.Error -> {
                val message = (markAsUnDeliveredState as AppCommonApiState.Error).message
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
                        .fillMaxWidth().padding(horizontal = 16.dp).padding(bottom = 16.dp), onClick = {
                            singleTripDetailsViewModel.endTrip(selectedScheduledTripId)
                    },
                    colors = getButtonColors()
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
                if (dropOffTripState is AppCommonApiState.Loading || endTripState is AppCommonApiState.Loading || markAsDeliveredState is AppCommonApiState.Loading || markAsUnDeliveredState is AppCommonApiState.Loading){
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
                                }, deliverySuccessOnClick = {docId ->
                                    showDeliverySuccesDocId = docId
                                }, deliveryFailedOnClick = { docId ->
                                    showDeliveryFailedDocId = docId
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

    DeliverySuccessConfirmationDialogCustom(
        showDialog = showDeliverySuccesDocId.isNotNullOrEmpty(),
        onConfirm = { comment,signature,isChecked ->
            singleTripDetailsViewModel.markAsDelivered(
                docId = showDeliverySuccesDocId,
                signatureEncodedString = signature,
                deliveryComment = comment,
                isChecked = isChecked
            )
            showDeliverySuccesDocId = ""
        },
        onDismiss = {
            showDeliverySuccesDocId = ""
        },
        title = stringResource(R.string.delivery_success_confirm_title),
        message = stringResource(R.string.delivery_success_confirm_message)
    )
    DeliveryFailedConfirmationDialogCustom(
        showDialog = showDeliveryFailedDocId.isNotNullOrEmpty(),
        onConfirm = { comment ->
            singleTripDetailsViewModel.markAsUnDelivered(
                docId = showDeliveryFailedDocId,
                comment = comment
            )
            showDeliveryFailedDocId = ""
        },
        onDismiss = {
            showDeliveryFailedDocId = ""
        },
        title = stringResource(R.string.delivery_failed_confirm_title),
        message = stringResource(R.string.delivery_failed_confirm_message)
    )
}





@Composable
fun SingleTripDetailsCompose(
    singleTripDetailsResponse: SingleTripDetailsResponse,
    singleTripDetailsViewModel: SingleTripDetailsViewModel,
    dropOffOnClick: (tripId: String, heading: String) -> Unit = { a, b -> },
    deliverySuccessOnClick: (docId: String) -> Unit = { a -> },
    deliveryFailedOnClick: (docId: String) -> Unit = { a -> }
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
                dropOffOnClick = dropOffOnClick,
                deliverySuccessOnClick = deliverySuccessOnClick,
                deliveryFailedOnClick = deliveryFailedOnClick
            )
        }
    }
}

@Composable
fun TripBasicDetailsCompose(singleTripDetailsResponse: SingleTripDetailsResponse) {
    OutlinedCard(
        modifier = Modifier
            .padding(vertical = 8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = dimensionResource(R.dimen.card_elevation))
    ) {
        Column(Modifier
            .fillMaxWidth()
            .padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
//            SingleMyTripRowItem(
//                key = stringResource(R.string.row_item_title_route),
//                value = (singleTripDetailsResponse.route ?: "")+" : On Trip",
//                style = MaterialTheme.typography.bodyLarge,
//                fontWeight = FontWeight.Bold
//            )
//            SingleMyTripRowItem(
//                key = stringResource(R.string.row_item_title_trip_id),
//                value = singleTripDetailsResponse.tripId.toString(),
//                style = MaterialTheme.typography.titleSmall
//            )
//           SingleMyTripRowItem(
//                key = stringResource(R.string.row_item_title_created_by),
//                value = singleTripDetailsResponse.createdBy ?: "",
//                style = MaterialTheme.typography.titleSmall
//            )
//            SingleMyTripRowItem(
//                key = stringResource(R.string.row_item_title_created_at),
//                value = singleTripDetailsResponse.createdAtFormatted ?: "",
//                style = MaterialTheme.typography.titleSmall
//            )
//            SingleMyTripRowItem(
//                key = stringResource(R.string.row_item_title_driver_name),
//                value = singleTripDetailsResponse.driverName ?: "",
//                style = MaterialTheme.typography.titleMedium
//            )
//            SingleMyTripRowItem(
//                key = stringResource(R.string.row_item_title_vehicle_number),
//                value = singleTripDetailsResponse.vehicleNumber ?: "",
//                style = MaterialTheme.typography.titleMedium
//            )

            SingleMyTripRowItem(
                icon = R.drawable.ic_hash,
                value = singleTripDetailsResponse.tripId.toString(),
                style = MaterialTheme.typography.titleSmall
            )
            SingleMyTripRowItem(
                icon = R.drawable.ic_route,
                value = (singleTripDetailsResponse.route ?: "")+" : On Trip",
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Bold
            )
           SingleMyTripRowItem(
                icon = R.drawable.ic_outline_person,
                value = singleTripDetailsResponse.createdBy ?: "",
                style = MaterialTheme.typography.titleSmall
            )
            SingleMyTripRowItem(
                icon = R.drawable.ic_calendar_clock,
                value = singleTripDetailsResponse.createdAtFormatted ?: "",
                style = MaterialTheme.typography.titleSmall
            )
            SingleMyTripRowItem(
                icon = R.drawable.ic_steering_wheel,
                value = singleTripDetailsResponse.driverName ?: "",
                style = MaterialTheme.typography.titleMedium
            )
            SingleMyTripRowItem(
                icon = R.drawable.ic_local_shipping,
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
private fun SingleMyTripRowItem(icon: Int, value: String, style: TextStyle, color: Color = MaterialTheme.colorScheme.onSurfaceVariant, fontWeight: FontWeight = FontWeight.Normal) {
//    ListItem(
//        modifier = Modifier.fillMaxWidth(),
//        headlineContent = {
//            Text(
//                text = value,
//                style = style,
//                color = color,
//                textAlign = TextAlign.Start
//            )
//        },
//        leadingContent = {
//            Icon(
//                painter = painterResource(icon),
//                contentDescription = "Icon",
//                modifier = Modifier.size(24.dp)
//            )
//        },
//        colors = getListItemColors(),
//        supportingContent = null
//    )
    Row(Modifier.fillMaxWidth().padding(vertical = 2.dp), verticalAlignment = Alignment.CenterVertically) {
        Icon(
            painter = painterResource(icon),
            contentDescription = "Icon",
            modifier = Modifier.size(24.dp)
        )
        Spacer(Modifier.width(4.dp))
        Text(
            text = value,
            style = style,
            color = color,
            textAlign = TextAlign.Start
        )
    }
}

@Composable
fun DocGroupCompose(singleTripDetailsViewModel: SingleTripDetailsViewModel, docGroup: DocGroup,
                    deliverySuccessOnClick: (docId: String) -> Unit = { a -> },
                    dropOffOnClick: (tripId: String, heading: String) -> Unit = { a, b -> },
    deliveryFailedOnClick: (docId: String) -> Unit = { a -> }) {
    var isExpanded by rememberSaveable { mutableStateOf(docGroup.expandGroupByDefault && !docGroup.dropOffCompleted) }
    Card(
        modifier = Modifier
            .padding(vertical = 8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = dimensionResource(R.dimen.card_elevation))
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
                    docGroup,
                    deliverySuccessOnClick = deliverySuccessOnClick,
                    deliveryFailedOnClick = deliveryFailedOnClick
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
    val isAnyItemNeedToDeliver = docGroup.docs?.any { it.status == DeliveryStatusConstants.ON_TRIP } == true

    ListItem(
        modifier = Modifier.clickable { onClick.invoke() },
        headlineContent = { Text(text = docGroup.heading?:"") },
        leadingContent = null,
        trailingContent = {
            Row {
                if (isAnyItemNeedToDeliver) {
                    if (docGroup.showDropOffButton) {
                        Button(onClick = {
//                        singleTripDetailsViewModel.dropOffTrip(
//                            selectedScheduledTripId = singleTripDetailsViewModel.selectedScheduledTripId,
//                            heading = docGroup.heading ?: ""
//                        )
                            dropOffOnClick.invoke(
                                singleTripDetailsViewModel.selectedScheduledTripId ?: "",
                                docGroup.heading ?: ""
                            )
                        },
                            colors = getButtonColors()
                        ) {
                            Text(text = stringResource(R.string.drop_off_at_hub_btn_txt))
                        }
                    } else if (docGroup.droppable) {
                        TextButton(onClick = {
                            onClick.invoke()
                        },
                            colors = getTextButtonColors()
                        ) {
                            Text(text = stringResource(R.string.dropped_off_at_hub_txt))
                        }
                    } else {
                        null
                    }
                }
                IconButton(onClick = {onClick.invoke() }, colors = getIconButtonColors()) {
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
fun ExpandedDocGroup(singleTripDetailsViewModel: SingleTripDetailsViewModel, docGroup: DocGroup,deliverySuccessOnClick: (docId: String) -> Unit = { a -> },deliveryFailedOnClick: (docId: String) -> Unit = { a -> }) {

    Column(
        Modifier
            .fillMaxWidth()
            .padding(start = 8.dp)
    ) {
        for (doc in docGroup.docs?: arrayListOf()) {
            Column(Modifier.fillMaxWidth()) {
                SingleDoc(
                    singleTripDetailsViewModel,
                    doc,
                    deliverySuccessOnClick = deliverySuccessOnClick,
                    deliveryFailedOnClick = deliveryFailedOnClick
                )
            }
        }
    }
}

@Composable
fun SingleDoc(singleTripDetailsViewModel: SingleTripDetailsViewModel, doc: Doc,deliverySuccessOnClick: (docId: String) -> Unit = { a -> },deliveryFailedOnClick: (docId: String) -> Unit = { a -> }) {
    val context = LocalContext.current
    Card(
        modifier = Modifier
            .padding(vertical = 8.dp)
    ) {
        Column(Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
//            SingleDocRowItem(
//                key = stringResource(R.string.row_item_title_firm_name),
//                value = doc.customerFirmName ?: "",
//                style = MaterialTheme.typography.bodyLarge,
//                fontWeight = FontWeight.Bold
//            )
//            SingleDocRowItem(
//                key = stringResource(R.string.row_item_title_amount),
//                value = doc.docAmount.toString(),
//                style = MaterialTheme.typography.titleSmall
//            )
//            SingleDocRowItem(
//                key = stringResource(R.string.row_item_title_address),
//                value = doc.customerAddress ?: "",
//                style = MaterialTheme.typography.titleSmall
//            )
//            SingleDocRowItem(
//                key = stringResource(R.string.row_item_title_city),
//                value = doc.customerCity ?: "",
//                style = MaterialTheme.typography.titleSmall
//            )
//            SingleDocRowItem(
//                key = stringResource(R.string.row_item_title_pin_code),
//                value = doc.customerPincode ?: "",
//                style = MaterialTheme.typography.titleMedium
//            )
//            SingleDocRowItem(
//                key = stringResource(R.string.row_item_title_phone),
//                value = doc.customerPhone ?: "",
//                style = MaterialTheme.typography.titleMedium
//            )
//            SingleDocRowItem(
//                key = stringResource(R.string.row_item_title_status),
//                value = if (doc.status?.equals(DeliveryStatusConstants.DELIVERED,ignoreCase = true) == true) "Delivered"
//                else if(doc?.status?.equals(DeliveryStatusConstants.UNDELIVERED) == true) "Not Delivered" else "On Trip",
//                style = MaterialTheme.typography.titleMedium
//            )

            Row(Modifier.fillMaxWidth()) {
                Column(Modifier.fillMaxWidth().weight(1f).padding(top = 10.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    SingleDocRowItem(
                        icon = R.drawable.ic_store,
                        value = doc.customerFirmName ?: "",
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Bold
                    )
                    SingleDocRowItem(
                        icon = R.drawable.ic_receipt,
                        value = "₹"+ doc.docAmount.toString(),
                        style = MaterialTheme.typography.titleSmall
                    )
                    if (doc.customerAddress.isNotNullOrEmpty()) {
                        SingleDocRowItem(
                            icon = R.drawable.ic_business,
                            value = doc.customerAddress ?: "",
                            style = MaterialTheme.typography.titleSmall
                        )
                    }
                    if (doc.customerCity.isNotNullOrEmpty()) {
                        SingleDocRowItem(
                            icon = R.drawable.ic_location_city,
                            value = doc.customerCity ?: "",
                            style = MaterialTheme.typography.titleSmall
                        )
                    }
                    if (doc.customerPincode.isNotNullOrEmpty()) {
                        SingleDocRowItem(
                            icon = R.drawable.ic_markunread_mailbox,
                            value = doc.customerPincode ?: "",
                            style = MaterialTheme.typography.titleMedium
                        )
                    }
                    if (doc.customerPhone.isNotNullOrEmpty()) {
                        SingleDocRowItem(
                            icon = R.drawable.ic_mobile,
                            value = doc.customerPhone ?: "",
                            style = MaterialTheme.typography.titleMedium
                        )
                    }
                    val deliveryIcon =  if (doc.status?.equals(DeliveryStatusConstants.DELIVERED,ignoreCase = true) == true) R.drawable.ic__check_circle
                    else if(doc?.status?.equals(DeliveryStatusConstants.UNDELIVERED) == true) R.drawable.ic_error_24 else R.drawable.ic_delivery_truck_speed
                    SingleDocRowItem(
                        icon = deliveryIcon,
                        value = if (doc.status?.equals(DeliveryStatusConstants.DELIVERED,ignoreCase = true) == true) "Delivered"
                        else if(doc?.status?.equals(DeliveryStatusConstants.UNDELIVERED) == true) "Not Delivered" else "On Trip",
                        style = MaterialTheme.typography.titleMedium
                    )
                }
                IconButton(
                    onClick = {
                        AppUtils.startGoogleMapsDirections(
                            context = context,
                            latitude = doc.customerGeoLatitude?:"",
                            longitude = doc.customerGeoLongitude?:"",
                            destinationName = doc.customerFirmName?:""
                        )
                    },
                    modifier = Modifier,
                    colors = getIconButtonColors()
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_location),
                        contentDescription = "location",
                        modifier = Modifier.size(24.dp),
                    )
                }

            }

            Column (Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(0.dp), horizontalAlignment = Alignment.End) {
                if (doc.status == DeliveryStatusConstants.ON_TRIP) {
                    Button(onClick = {
                        deliverySuccessOnClick.invoke(doc.id ?: "")
                    },
                        colors = getButtonColors()
                    ) {
                        Text(text = stringResource(R.string.mark_as_delivered_btn_txt))
                    }
                    Button(onClick = {
                        deliveryFailedOnClick.invoke(doc.id ?: "")
                    }, colors = getButtonColors()) {
                        Text(text = stringResource(R.string.mark_as_un_delivered_btn_txt))
                    }
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



@Composable
private fun SingleDocRowItem(icon: Int, value: String, style: TextStyle, color: Color = MaterialTheme.colorScheme.onSurfaceVariant, fontWeight: FontWeight = FontWeight.Normal) {
//    ListItem(
//        modifier = Modifier.fillMaxWidth(),
//        headlineContent = {
//            Text(
//                text = value,
//                style = style,
//                color = color,
//                textAlign = TextAlign.Start
//            )
//        },
//        leadingContent = {
//            Icon(
//                painter = painterResource(icon),
//                contentDescription = "Icon",
//                modifier = Modifier.size(24.dp)
//            )
//        },
//        colors = getListItemColors(),
//        supportingContent = null
//    )
    Row(Modifier.fillMaxWidth().padding(vertical = 2.dp), verticalAlignment = Alignment.CenterVertically) {
        Icon(
            painter = painterResource(icon),
            contentDescription = "Icon",
            modifier = Modifier.size(24.dp)
        )
        Spacer(Modifier.width(4.dp))
        Text(
            text = value,
            style = style,
            color = color,
            textAlign = TextAlign.Start
        )
    }
}
