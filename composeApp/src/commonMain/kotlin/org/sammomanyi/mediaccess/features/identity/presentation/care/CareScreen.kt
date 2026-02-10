package org.sammomanyi.mediaccess.features.identity.presentation.care

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.koin.compose.viewmodel.koinViewModel
import org.sammomanyi.mediaccess.core.presentation.theme.MediAccessColors
import org.sammomanyi.mediaccess.core.util.rememberLocationState

@Composable
fun CareScreen(
    padding: PaddingValues,
    viewModel: CareViewModel = koinViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val locationState = rememberLocationState()

    var selectedTab by remember { mutableStateOf(CareTab.LOCATE_PROVIDER) }

    // Request location permission on first launch
    LaunchedEffect(Unit) {
        if (!locationState.checkPermission()) {
            locationState.requestPermission()
        }
    }

    // Get location when permission is granted
    LaunchedEffect(locationState.state.permissionGranted) {
        if (locationState.state.permissionGranted) {
            locationState.getCurrentLocation()
        }
    }

    // Update ViewModel with location
    LaunchedEffect(locationState.state.latitude, locationState.state.longitude) {
        val lat = locationState.state.latitude
        val lon = locationState.state.longitude
        if (lat != null && lon != null) {
            viewModel.onAction(CareAction.OnLocationUpdated(lat, lon))
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MediAccessColors.Background)
            .padding(padding)
    ) {
        // Header
        CareHeader()

        // Tabs
        TabRow(
            selectedTabIndex = selectedTab.ordinal,
            containerColor = Color.White,
            contentColor = MediAccessColors.Primary
        ) {
            Tab(
                selected = selectedTab == CareTab.LOCATE_PROVIDER,
                onClick = { selectedTab = CareTab.LOCATE_PROVIDER },
                text = {
                    Text(
                        "Locate Provider",
                        fontWeight = if (selectedTab == CareTab.LOCATE_PROVIDER) FontWeight.Bold else FontWeight.Normal
                    )
                }
            )
            Tab(
                selected = selectedTab == CareTab.MY_VISITS,
                onClick = { selectedTab = CareTab.MY_VISITS },
                text = {
                    Text(
                        "My Visits",
                        fontWeight = if (selectedTab == CareTab.MY_VISITS) FontWeight.Bold else FontWeight.Normal
                    )
                }
            )
        }

        when (selectedTab) {
            CareTab.LOCATE_PROVIDER -> LocateProviderContent(
                state = state,
                locationState = locationState.state,
                onAction = viewModel::onAction
            )
            CareTab.MY_VISITS -> MyVisitsContent(
                state = state,
                onAction = viewModel::onAction
            )
        }
    }
}

@Composable
private fun CareHeader() {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = Color.White
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    modifier = Modifier.size(40.dp),
                    shape = RoundedCornerShape(8.dp),
                    color = MediAccessColors.Primary
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = "SA",
                            style = MaterialTheme.typography.titleSmall,
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Smart\nACCESS",
                    style = MaterialTheme.typography.titleSmall,
                    color = MediAccessColors.Secondary,
                    fontWeight = FontWeight.Bold
                )
            }

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                IconButton(onClick = { }) {
                    Icon(Icons.Default.Notifications, contentDescription = "Notifications")
                }
                IconButton(onClick = { }) {
                    Surface(
                        modifier = Modifier.size(32.dp),
                        shape = androidx.compose.foundation.shape.CircleShape,
                        color = MediAccessColors.SurfaceVariant
                    ) {
                        Icon(
                            Icons.Default.Person,
                            contentDescription = "Profile",
                            modifier = Modifier.padding(6.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun LocateProviderContent(
    state: CareState,
    locationState: org.sammomanyi.mediaccess.core.util.LocationState,
    onAction: (CareAction) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        // Initiate Visit Button
        Button(
            onClick = { onAction(CareAction.OnInitiateVisit) },
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .height(50.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MediAccessColors.Secondary
            ),
            shape = RoundedCornerShape(12.dp)
        ) {
            Icon(Icons.Default.CalendarToday, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                "Initiate a visit",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
        }

        // Search and Filter
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedTextField(
                value = state.searchQuery,
                onValueChange = { onAction(CareAction.OnSearchQueryChange(it)) },
                placeholder = { Text("Search Healthcare...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedBorderColor = MediAccessColors.SurfaceVariant
                ),
                singleLine = true
            )

            Button(
                onClick = { onAction(CareAction.OnFilterClick) },
                colors = ButtonDefaults.buttonColors(
                    containerColor = MediAccessColors.Secondary
                ),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.height(56.dp)
            ) {
                Icon(Icons.Default.FilterList, contentDescription = "Filter")
                Spacer(modifier = Modifier.width(4.dp))
                Text("Filter")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Results Count and Location Toggle
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "${state.hospitals.size} Results",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            Row(verticalAlignment = Alignment.CenterVertically) {
                Switch(
                    checked = state.showNearbyOnly,
                    onCheckedChange = { onAction(CareAction.OnToggleNearby) },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = MediAccessColors.Primary,
                        checkedTrackColor = MediAccessColors.PrimaryLight
                    )
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    "Providers Near Me",
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Hospitals List
        if (state.isLoading) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else if (state.hospitals.isEmpty()) {
            EmptyHospitalsView()
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
            ) {
                items(state.hospitals) { hospital ->
                    HospitalListItem(
                        hospital = hospital,
                        userLat = locationState.latitude,
                        userLon = locationState.longitude,
                        onClick = { onAction(CareAction.OnHospitalClick(hospital)) }
                    )
                }
            }
        }
    }
}

@Composable
private fun HospitalListItem(
    hospital: org.sammomanyi.mediaccess.features.identity.domain.model.Hospital,
    userLat: Double?,
    userLon: Double?,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = hospital.name.uppercase(),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = hospital.address,
                    style = MaterialTheme.typography.bodySmall,
                    color = MediAccessColors.TextSecondary
                )
            }

            if (userLat != null && userLon != null) {
                Surface(
                    shape = androidx.compose.foundation.shape.CircleShape,
                    color = MediAccessColors.SurfaceVariant
                ) {
                    Text(
                        text = hospital.formattedDistance(userLat, userLon),
                        style = MaterialTheme.typography.labelSmall,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun MyVisitsContent(
    state: CareState,
    onAction: (CareAction) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        // Initiate Visit Button
        Button(
            onClick = { onAction(CareAction.OnInitiateVisit) },
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .height(50.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MediAccessColors.Secondary
            ),
            shape = RoundedCornerShape(12.dp)
        ) {
            Icon(Icons.Default.CalendarToday, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                "Initiate a visit",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
        }

        // Empty State
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Illustration placeholder
                Box(
                    modifier = Modifier
                        .size(200.dp)
                        .background(
                            color = MediAccessColors.Secondary.copy(alpha = 0.2f),
                            shape = RoundedCornerShape(16.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.FolderOpen,
                        contentDescription = null,
                        modifier = Modifier.size(80.dp),
                        tint = MediAccessColors.Secondary
                    )
                    // Add "No data found" text overlay
                    Text(
                        text = "No data\nfound",
                        style = MaterialTheme.typography.titleMedium,
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.align(Alignment.Center)
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    text = "NO VISITS FOUND",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MediAccessColors.TextPrimary
                )
            }
        }
    }
}

@Composable
private fun EmptyHospitalsView() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                Icons.Default.SearchOff,
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                tint = MediAccessColors.TextSecondary
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "No hospitals found",
                style = MaterialTheme.typography.bodyLarge,
                color = MediAccessColors.TextSecondary
            )
        }
    }
}

enum class CareTab {
    LOCATE_PROVIDER,
    MY_VISITS
}