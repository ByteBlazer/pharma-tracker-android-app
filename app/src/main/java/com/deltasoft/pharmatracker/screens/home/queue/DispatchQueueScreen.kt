package com.deltasoft.pharmatracker.screens.home.queue

import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.deltasoft.pharmatracker.R
import com.deltasoft.pharmatracker.navigation.Screen
import com.deltasoft.pharmatracker.screens.AppConfirmationDialog
import com.deltasoft.pharmatracker.screens.App_CommonTopBar
import com.deltasoft.pharmatracker.screens.CustomSearchField
import com.deltasoft.pharmatracker.screens.home.HomeViewModel
import com.deltasoft.pharmatracker.screens.home.MyTrips.singletripdetails.entity.Doc
import com.deltasoft.pharmatracker.screens.home.queue.entity.RouteSummaryList
import com.deltasoft.pharmatracker.screens.home.queue.entity.UserSummaryList
import com.deltasoft.pharmatracker.screens.home.scan.ScanDocState
import com.deltasoft.pharmatracker.screens.home.scan.getColorFromCode
import com.deltasoft.pharmatracker.ui.theme.AppPrimary
import com.deltasoft.pharmatracker.ui.theme.getIconButtonColors
import com.deltasoft.pharmatracker.utils.AppUtils
import com.deltasoft.pharmatracker.utils.AppUtils.showToastMessage
import com.deltasoft.pharmatracker.utils.AppVibratorManager
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.ArrayList

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DispatchQueueScreen(
    navController: NavHostController,
    homeViewModel: HomeViewModel,
    dispatchQueueViewModel: DispatchQueueViewModel = viewModel()
) {

    val context = LocalContext.current

    val apiState by dispatchQueueViewModel.dispatchQueueState.collectAsState()

    var showDocIdListDialog by remember { mutableStateOf<UserSummaryList?>(null) }
    var showAlertDialog by remember { mutableStateOf<String?>(null) }


//    LaunchedEffect(Unit) {
//        dispatchQueueViewModel.getDispatchQueueList()
//    }

    val refreshClickEvent by homeViewModel.dispatchQueueClickEvent.collectAsState()

    // LaunchedEffect triggers whenever clickEvent changes
    LaunchedEffect(refreshClickEvent) {
        dispatchQueueViewModel.getDispatchQueueList()
    }

    val dispatchQueueList by dispatchQueueViewModel.dispatchQueueList.collectAsState()

    // Use a derived state to calculate if any item is selected
    val anyItemSelected by remember {
        derivedStateOf {
            dispatchQueueList.any { routeSummary ->
                routeSummary.userSummaryList.any { userSummary ->
                    userSummary.isChecked.value
                }
            }
        }
    }

    val sheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = true // Ensures it goes straight to expanded or hidden
    )

    val scope = rememberCoroutineScope()

    val scanState by dispatchQueueViewModel.scanDocState.collectAsState()

    LaunchedEffect(scanState) {
        when (scanState) {
            is ScanDocState.Idle -> {

            }

            is ScanDocState.Loading -> {
            }

            is ScanDocState.Success -> {
                dispatchQueueViewModel.getDispatchQueueList()
                val message = (scanState as ScanDocState.Success).message
                val code = (scanState as ScanDocState.Success).code
                message.showToastMessage(context)
                dispatchQueueViewModel.clearScanDocState()
            }

            is ScanDocState.Error -> {
                val message = (scanState as ScanDocState.Error).message
                val code = (scanState as ScanDocState.Error).code
                message.showToastMessage(context)
                dispatchQueueViewModel.clearScanDocState()
            }
        }
    }

    Scaffold(
        floatingActionButton = {
                ExtendedFloatingActionButton(
                    onClick = {
                        if (anyItemSelected) {
                        if (dispatchQueueViewModel.getSelectedRouteCount() > 1) {
                            Toast.makeText(context,"You cannot mix routes. Please select from only a single route.",Toast.LENGTH_SHORT).show()
                        } else {
                            val route = dispatchQueueViewModel.getSelectedRoute()
                            val userListJson = dispatchQueueViewModel.getSelectedUsersDetsils()
                            navController.navigate(
                                Screen.ScheduleNewTrip.createRoute(
                                    route = route,
                                    userList = userListJson
                                )
                            )
                        }}else{
                            Toast.makeText(context,"Please select a route",Toast.LENGTH_SHORT).show()
                        }
                    },
                    icon = {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Add"
                        )
                    },
                    text = {
                        Text(text = "Schedule Trip")
                    },
                    containerColor = AppPrimary,
                    contentColor = Color.White
                )
        }
    ) { paddingValues ->
        val modifier = Modifier.padding(paddingValues)
        Column(
            modifier = Modifier
                .fillMaxSize()
//                .padding(vertical = paddingValues.calculateBottomPadding())
                .padding(horizontal = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top
        ) {
            Box(modifier = Modifier
                .fillMaxSize(), contentAlignment = Alignment.Center) {
                if (scanState is ScanDocState.Loading){
                    CircularProgressIndicator()
                }else {
                    when (apiState) {
                        is DispatchQueueState.Idle -> {
                            CircularProgressIndicator()
                        }

                        is DispatchQueueState.Loading -> {
                            CircularProgressIndicator()
                        }

                        is DispatchQueueState.Success -> {
                            val dispatchQueueResponse =
                                (apiState as DispatchQueueState.Success).dispatchQueueResponse
                            dispatchQueueViewModel.updateDispatchQueueList(
                                dispatchQueueResponse?.dispatchQueueList?.routeSummaryList
                                    ?: arrayListOf()
                            )
                            DispatchQueueListCompose(
                                dispatchQueueViewModel,
                                dispatchQueueResponse?.message,
                                infoButtonClick = { docIdList ->
                                    showDocIdListDialog = docIdList
                                })
                        }

                        is DispatchQueueState.Error -> {
                            val message = (apiState as DispatchQueueState.Error).message
                            Text(text = message)
                        }
                    }
                }
            }
        }
    }

//    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
//        Text("Route queue", style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.onSurfaceVariant )
//    }

    if (showDocIdListDialog != null) {
        ModalBottomSheet(
            // When the user swipes down or taps the scrim
            onDismissRequest = {
                showDocIdListDialog = null
            },
            sheetState = sheetState,
            // Optional: A default drag handle is provided by Material 3
            dragHandle = { BottomSheetDefaults.DragHandle() }
        ) {
            val listItems = showDocIdListDialog?.docIdList?: arrayListOf()
            var searchQuery by remember { mutableStateOf("") }
            val filteredItems = remember(searchQuery) {
                if (searchQuery.isBlank()) {
                    listItems
                } else {
                    listItems.filter {
                        it.contains(searchQuery.trim(), ignoreCase = true)
                    }
                }
            }
            // CONTENT: The list of items inside the bottom sheet
            Column(modifier = Modifier.padding(bottom = 32.dp)) {
                Text(
                    text = "Documents Scanned",
                    style = MaterialTheme.typography.headlineSmall,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )
                CustomSearchField(
                    query = searchQuery,
                    onQueryChange = { searchQuery = it }
                )

                // Use a LazyColumn for a performance-efficient list
                if (filteredItems.isNotEmpty()) {
                    LazyColumn(
                        // To prevent the bottom sheet from taking up the entire screen,
                        // you can apply a max height or use default content padding.
                        contentPadding = PaddingValues(vertical = 8.dp)
                    ) {
                        items(filteredItems.size) { index ->
                            val item = filteredItems[index]
                            ListItem(
                                headlineContent = { Text(item) },
                                modifier = Modifier.clickable {
                                    // Action when an item is clicked
                                    println("Clicked on: $item")

                                    // Hide the sheet on item selection
                                    scope.launch {
                                        sheetState.hide()
                                    }.invokeOnCompletion {
                                        // IMPORTANT: Once hidden, remove it from composition
                                        if (!sheetState.isVisible) {
                                            showDocIdListDialog = null
                                        }
                                    }
                                },
                                trailingContent = {
                                    IconButton(
                                        onClick = {
                                            showAlertDialog = item
                                        },
                                        modifier = Modifier,
                                        colors = getIconButtonColors()
                                    ) {
                                        Icon(
                                            painter = painterResource(id = R.drawable.ic_delete_24),
                                            contentDescription = "delete",
                                            modifier = Modifier.size(24.dp),
                                        )
                                    }
                                }
                            )
//                            Divider(Modifier.padding(horizontal = 16.dp))
                            HorizontalDivider(Modifier.padding(horizontal = 16.dp))
                        }
                    }
                }else{
                    ListItem(
                        headlineContent = {  Text("No data found", color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center) }
                    )

                }
            }
        }
    }
    if (showAlertDialog != null){
        AppConfirmationDialog(
            showDialog = showAlertDialog != null,
            onConfirm = {
                val barcode = showAlertDialog
                dispatchQueueViewModel.scanDoc(barcode = barcode?:"",unscan = true)
                showAlertDialog = null
                showDocIdListDialog = null
            },
            onDismiss = {
                showAlertDialog = null
            },
            title = "Confirm Deletion",
            message = "Are you sure you want to remove this Doc ID ${showAlertDialog} from the queue? This action cannot be undone.",
            confirmButtonText = "Delete",
            dismissButtonText = "Cancel"
        )
    }
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun DispatchQueueListCompose(dispatchQueueViewModel: DispatchQueueViewModel, message: String?,
                             infoButtonClick: (docIdList: UserSummaryList) -> Unit = { a -> }) {
    val routeSummaryLists by dispatchQueueViewModel.dispatchQueueList.collectAsState()

    val dispatchQueueState by dispatchQueueViewModel.dispatchQueueState.collectAsState()
    val isRefreshing = dispatchQueueState.let { it is DispatchQueueState.Loading }

    val pullRefreshState = rememberPullRefreshState(isRefreshing, { dispatchQueueViewModel.getDispatchQueueList() })

    Box(Modifier
        .fillMaxSize()
        .pullRefresh(pullRefreshState), contentAlignment = Alignment.TopCenter) {
        if (routeSummaryLists.isEmpty()){
            val noDataMessage = message?:"No data found"
            Column(Modifier
                .fillMaxSize()
                .padding(16.dp)
                .verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally) {
                Text(noDataMessage, style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.onSurfaceVariant , textAlign = TextAlign.Center)
            }
        }else{
            Column(Modifier.fillMaxWidth()) {
                Spacer(Modifier.height(16.dp))
                Text("Select docs from any one route", color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.fillMaxWidth(), fontWeight = FontWeight.Bold, textAlign = TextAlign.Center, style = MaterialTheme.typography.titleMedium )
                Spacer(Modifier.height(16.dp))
                LazyColumn {
                    items(routeSummaryLists.size) { index ->
                        if (index in routeSummaryLists.indices) {
                            val route = routeSummaryLists[index]
                            RouteHeaderComposable(route,dispatchQueueViewModel,infoButtonClick = infoButtonClick)
                            if (routeSummaryLists.last() == route){
                                App_CommonTopBar(backButtonVisibility = false, useDefaultColor = true)
                            }else{
                                Spacer(Modifier.height(28.dp))
                            }
                        }
                    }
                }
            }
        }
        // The indicator must be a separate composable, typically aligned to the top
        PullRefreshIndicator(
            refreshing = isRefreshing,
            state = pullRefreshState,
            modifier = Modifier.align(Alignment.TopCenter)
        )
    }
}

@Composable
fun RouteItemComposable(
    item: UserSummaryList,
    route: String?,
    dispatchQueueViewModel: DispatchQueueViewModel,
    infoButtonClick: (docIdList: UserSummaryList) -> Unit = { a -> }
) {
    Card(modifier = Modifier
        .padding(vertical = 8.dp)
        .clickable {
            item.isChecked.value = !item.isChecked.value
        },
        elevation = CardDefaults.cardElevation(defaultElevation = dimensionResource(R.dimen.card_elevation))) {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            ListItem(
                headlineContent = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(("Documents Scanned: " + item.count) ?: "", color = MaterialTheme.colorScheme.onSurfaceVariant )
                        Spacer(Modifier.width(8.dp))
                        Icon(
                            painter = painterResource(id = R.drawable.ic_info_outline_24),
                            contentDescription = "info",
                            modifier = Modifier.size(24.dp).clickable {
                                infoButtonClick.invoke(item)
                            },
                            tint = AppPrimary
                        )
                    }
                },
                modifier = Modifier.weight(1f),
                overlineContent = {
                    Text(item.scannedFromLocation?:"", color = MaterialTheme.colorScheme.onSurfaceVariant )
                },
                supportingContent = {
                    Text(item.scannedByName?:"", color = MaterialTheme.colorScheme.onSurfaceVariant )
                },
                leadingContent = null,
                trailingContent = null
//                    {
//                    Checkbox(
//                        checked = item.isChecked.value,
//                        onCheckedChange = {
//                            item.isChecked.value = it
//                        }
//                    )
//                }
                ,
                colors = ListItemDefaults.colors(
                    containerColor = Color.Transparent
                )
            )
            Checkbox(
                checked = item.isChecked.value,
                onCheckedChange = {
                    item.isChecked.value = it
                }
            )
        }
    }
}

@Composable
fun RouteHeaderComposable(route: RouteSummaryList, dispatchQueueViewModel: DispatchQueueViewModel,
                          infoButtonClick: (docIdList: UserSummaryList) -> Unit = { a -> }) {
    Text(route.route?:"", style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.onSurfaceVariant )
    RouteItemsListComposable(innerItems = route.userSummaryList, route = route.route,dispatchQueueViewModel,infoButtonClick = infoButtonClick)
}

@Composable
fun RouteItemsListComposable(
    innerItems: ArrayList<UserSummaryList>,
    route: String?,
    dispatchQueueViewModel: DispatchQueueViewModel,
    infoButtonClick: (docIdList: UserSummaryList) -> Unit = { a -> }
) {
    Column(
        modifier = Modifier.padding(start = 16.dp)
    ) {
        innerItems.forEach { innerItem ->
            RouteItemComposable(innerItem,route,dispatchQueueViewModel,infoButtonClick = infoButtonClick)
        }
    }
}
