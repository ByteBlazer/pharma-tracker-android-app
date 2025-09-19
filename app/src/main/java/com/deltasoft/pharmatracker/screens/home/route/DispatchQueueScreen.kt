package com.deltasoft.pharmatracker.screens.home.route

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.material3.Card
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.deltasoft.pharmatracker.screens.home.HomeViewModel
import com.deltasoft.pharmatracker.screens.home.route.entity.RouteSummaryList
import com.deltasoft.pharmatracker.screens.home.route.entity.UserSummaryList
import java.util.ArrayList

@Composable
fun DispatchQueueScreen(
    homeViewModel: HomeViewModel,
    dispatchQueueViewModel: DispatchQueueViewModel = viewModel()
) {
    val apiState by dispatchQueueViewModel.dispatchQueueState.collectAsState()


//    LaunchedEffect(Unit) {
//        dispatchQueueViewModel.getDispatchQueueList()
//    }

    val refreshClickEvent by homeViewModel.dispatchQueueClickEvent.collectAsState()

    // LaunchedEffect triggers whenever clickEvent changes
    LaunchedEffect(refreshClickEvent) {
        dispatchQueueViewModel.getDispatchQueueList()
    }

    Box(modifier = Modifier
        .fillMaxSize()
        .padding(horizontal = 16.dp), contentAlignment = Alignment.Center) {
        when (apiState) {
            is DispatchQueueState.Idle -> {
                CircularProgressIndicator()
            }
            is DispatchQueueState.Loading -> {
                CircularProgressIndicator()
            }
            is DispatchQueueState.Success -> {
                val dispatchQueueResponse = (apiState as DispatchQueueState.Success).dispatchQueueResponse
                dispatchQueueViewModel.updateDispatchQueueList(dispatchQueueResponse?.dispatchQueueList?.routeSummaryList?: arrayListOf())
                DispatchQueueListCompose(dispatchQueueViewModel,dispatchQueueResponse?.message)
            }
            is DispatchQueueState.Error -> {
                val message = (apiState as DispatchQueueState.Error).message
                Text(text = message)
            }
        }
    }
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text("Route queue", style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.onSurfaceVariant )
    }
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun DispatchQueueListCompose(dispatchQueueViewModel: DispatchQueueViewModel, message: String?) {
    val routeSummaryLists by dispatchQueueViewModel.dispatchQueueList.collectAsState()

    val dispatchQueueState by dispatchQueueViewModel.dispatchQueueState.collectAsState()
    val isRefreshing = dispatchQueueState.let { it is DispatchQueueState.Loading }

    val pullRefreshState = rememberPullRefreshState(isRefreshing, { dispatchQueueViewModel.getDispatchQueueList() })

    Box(Modifier.fillMaxSize()
        .pullRefresh(pullRefreshState), contentAlignment = Alignment.TopCenter) {
        if (routeSummaryLists.isEmpty()){
            val noDataMessage = message?:"No data found"
            Column(Modifier.fillMaxSize().padding(16.dp)
                .verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally) {
                Text(noDataMessage, style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.onSurfaceVariant , textAlign = TextAlign.Center)
            }
        }else{
            LazyColumn {
                items(routeSummaryLists.size) { index ->
                    if (index in routeSummaryLists.indices) {
                        val route = routeSummaryLists[index]
                        RouteHeaderComposable(route,dispatchQueueViewModel)
                        Spacer(Modifier.height(28.dp))
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
    dispatchQueueViewModel: DispatchQueueViewModel
) {
    Card(modifier = Modifier
        .padding(vertical = 8.dp)
        .clickable {
            item.isChecked.value = !item.isChecked.value
        }) {
        ListItem(
            headlineContent = {
                Text(item.scannedByName?:"", color = MaterialTheme.colorScheme.onSurfaceVariant )
            },
            modifier = Modifier,
            overlineContent = {
                Text(item.scannedFromLocation?:"", color = MaterialTheme.colorScheme.onSurfaceVariant )
            },
            supportingContent = {
                Text(("Count: " + item.count) ?: "", color = MaterialTheme.colorScheme.onSurfaceVariant )
            },
            leadingContent = {

            },
            trailingContent = {
                Checkbox(
                    checked = item.isChecked.value,
                    onCheckedChange = {
                        item.isChecked.value = it
                    }
                )
            },
            colors = ListItemDefaults.colors(
                containerColor = Color.Transparent
            )
        )
    }
}

@Composable
fun RouteHeaderComposable(route: RouteSummaryList, dispatchQueueViewModel: DispatchQueueViewModel) {
    Text(route.route?:"", style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.onSurfaceVariant )
    RouteItemsListComposable(innerItems = route.userSummaryList, route = route.route,dispatchQueueViewModel)
}

@Composable
fun RouteItemsListComposable(
    innerItems: ArrayList<UserSummaryList>,
    route: String?,
    dispatchQueueViewModel: DispatchQueueViewModel
) {
    Column(
        modifier = Modifier.padding(start = 16.dp)
    ) {
        innerItems.forEach { innerItem ->
            RouteItemComposable(innerItem,route,dispatchQueueViewModel)
        }
    }
}
