package com.deltasoft.pharmatracker.screens.home.route

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.deltasoft.pharmatracker.screens.home.route.entity.RouteSummaryList
import com.deltasoft.pharmatracker.screens.home.route.entity.UserSummaryList
import java.util.ArrayList

@Composable
fun RouteQueueScreen(
    dispatchQueueViewModel: DispatchQueueViewModel = viewModel()
) {
    val apiState by dispatchQueueViewModel.dispatchQueueState.collectAsState()


    LaunchedEffect(Unit) {
        dispatchQueueViewModel.getDispatchQueueList()
    }

    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        when (apiState) {
            is DispatchQueueState.Idle -> {
                CircularProgressIndicator()
            }
            is DispatchQueueState.Loading -> {
                CircularProgressIndicator()
            }
            is DispatchQueueState.Success -> {
                val dispatchQueueResponse = (apiState as DispatchQueueState.Success).dispatchQueueResponse
                DispatchQueueListCompose(dispatchQueueResponse?.dispatchQueueList?.routeSummaryList?: arrayListOf())
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

@Composable
fun DispatchQueueListCompose(routeSummaryLists: ArrayList<RouteSummaryList>) {
    if (routeSummaryLists.isEmpty()){

    }else{
        LazyColumn {
            items(routeSummaryLists.size) { index ->
                if (index in routeSummaryLists.indices) {
                    val route = routeSummaryLists[index]
                    RouteHeaderComposable(route)
                }
            }
        }
    }
}

@Composable
fun RouteItemComposable(item: UserSummaryList) {
    ListItem(
        headlineContent = {
            Text(item.scannedByName?:"", color = MaterialTheme.colorScheme.onSurfaceVariant )
        },
        modifier = Modifier,
        overlineContent = {
            Text(item.scannedByUserId?:"", color = MaterialTheme.colorScheme.onSurfaceVariant )
        },
        supportingContent = {
            Text(item.scannedFromLocation?:"", color = MaterialTheme.colorScheme.onSurfaceVariant )
        },
        leadingContent = {

        },
        trailingContent = {

        },
        colors = ListItemDefaults.colors(
            containerColor = Color.Transparent
        )
    )
}

@Composable
fun RouteHeaderComposable(route: RouteSummaryList) {
    Text(route.route?:"", style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.onSurfaceVariant )
    RouteItemsListComposable(innerItems = route.userSummaryList)
}

@Composable
fun RouteItemsListComposable(innerItems: ArrayList<UserSummaryList>) {
    Column(
        modifier = Modifier.padding(start = 16.dp)
    ) {
        innerItems.forEach { innerItem ->
            RouteItemComposable(innerItem)
        }
    }
}
