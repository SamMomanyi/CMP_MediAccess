package org.sammomanyi.mediaccess.features.identity.presentation.care

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import mediaccess.composeapp.generated.resources.Res
import mediaccess.composeapp.generated.resources.logo
import org.jetbrains.compose.resources.painterResource
import org.koin.compose.viewmodel.koinViewModel
import org.sammomanyi.mediaccess.core.presentation.theme.MediAccessColors
import org.sammomanyi.mediaccess.core.util.rememberLocationHelper
import org.sammomanyi.mediaccess.core.util.LocationState

@Composable
fun CareScreen(
    padding: PaddingValues,
    viewModel: CareViewModel = koinViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    // UI State for tracking location locally in the Composable
    var internalLocationState by remember { mutableStateOf(LocationState()) }

    // Initialize the KMP Helper
    val locationHelper = rememberLocationHelper { granted ->
        internalLocationState = internalLocationState.copy(permissionGranted = granted)
    }

    var selectedTab by remember { mutableStateOf(CareTab.LOCATE_PROVIDER) }

    // --- Logic Blocks ---

    // 1. Request permission on first launch
    LaunchedEffect(Unit) {
        if (!locationHelper.checkPermission()) {
            locationHelper.requestPermission()
        } else {
            internalLocationState = internalLocationState.copy(permissionGranted = true)
        }
    }

    // 2. Get location when permission is granted
    LaunchedEffect(internalLocationState.permissionGranted) {
        if (internalLocationState.permissionGranted) {
            internalLocationState = internalLocationState.copy(isLoading = true)
            locationHelper.getCurrentLocation { lat, lon, err ->
                internalLocationState = internalLocationState.copy(
                    latitude = lat,
                    longitude = lon,
                    error = err,
                    isLoading = false
                )
            }
        }
    }

    // 3. Update ViewModel with coordinates for filtering
    LaunchedEffect(internalLocationState.latitude, internalLocationState.longitude) {
        val lat = internalLocationState.latitude
        val lon = internalLocationState.longitude
        if (lat != null && lon != null) {
            viewModel.onAction(CareAction.OnLocationUpdated(lat, lon))
        }
    }

    // --- UI Layout ---
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF5F5F5))
            .padding(padding)
    ) {
        // Aligned Header
        CareHeader()

        // Aligned Tabs
        TabRow(
            selectedTabIndex = selectedTab.ordinal,
            containerColor = Color.White,
            contentColor = MediAccessColors.Primary,
            indicator = { tabPositions ->
                TabRowDefaults.SecondaryIndicator(
                    Modifier.tabIndicatorOffset(tabPositions[selectedTab.ordinal]),
                    color = MediAccessColors.Primary
                )
            },
            divider = {}
        ) {
            CareTab.entries.forEach { tab ->
                Tab(
                    selected = selectedTab == tab,
                    onClick = { selectedTab = tab },
                    text = {
                        Text(
                            text = if (tab == CareTab.LOCATE_PROVIDER) "Locate Provider" else "My Visits",
                            fontWeight = if (selectedTab == tab) FontWeight.Bold else FontWeight.Normal,
                            color = if (selectedTab == tab) MediAccessColors.Primary else Color.Gray
                        )
                    }
                )
            }
        }

        when (selectedTab) {
            CareTab.LOCATE_PROVIDER -> LocateProviderContent(
                state = state,
                locationState = internalLocationState,
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
        color = Color.White,
        shadowElevation = 1.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Image(
                    painter = painterResource(Res.drawable.logo),
                    contentDescription = null,
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(8.dp))
                )
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = "Medi",
                        style = MaterialTheme.typography.titleMedium,
                        color = MediAccessColors.Secondary,
                        fontWeight = FontWeight.Bold,
                        lineHeight = 16.sp
                    )
                    Text(
                        text = "ACCESS",
                        style = MaterialTheme.typography.labelSmall,
                        color = MediAccessColors.Primary,
                        fontWeight = FontWeight.ExtraBold,
                        lineHeight = 12.sp
                    )
                }
            }

            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                IconButton(onClick = { }) {
                    Icon(Icons.Default.Notifications, contentDescription = "Notifications", tint = Color.Gray)
                }
                Surface(
                    modifier = Modifier.size(32.dp),
                    shape = CircleShape,
                    color = Color.LightGray.copy(alpha = 0.4f)
                ) {
                    Icon(Icons.Default.Person, contentDescription = "Profile", modifier = Modifier.padding(6.dp), tint = Color.White)
                }
            }
        }
    }
}

@Composable
private fun LocateProviderContent(
    state: CareState,
    locationState: LocationState,
    onAction: (CareAction) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        Button(
            onClick = { onAction(CareAction.OnInitiateVisit) },
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .height(48.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFB71C1C)),
            shape = RoundedCornerShape(8.dp)
        ) {
            Icon(Icons.Default.DateRange, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text("Initiate a visit", fontWeight = FontWeight.Bold)
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedTextField(
                value = state.searchQuery,
                onValueChange = { onAction(CareAction.OnSearchQueryChange(it)) },
                placeholder = { Text("Search Healthcare...", color = Color.Gray) },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = Color.Gray) },
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(8.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedBorderColor = Color.LightGray,
                    focusedTextColor = MediAccessColors.TextPrimary
                ),
                singleLine = true
            )

            Button(
                onClick = { onAction(CareAction.OnFilterClick) },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFB71C1C)),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.height(54.dp)
            ) {
                Icon(Icons.Default.Tune, contentDescription = "Filter")
                Spacer(modifier = Modifier.width(4.dp))
                Text("Filter")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "${state.hospitals.size} Results",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = Color.DarkGray
            )

            Row(verticalAlignment = Alignment.CenterVertically) {
                Switch(
                    checked = state.showNearbyOnly,
                    onCheckedChange = { onAction(CareAction.OnToggleNearby) },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color.White,
                        checkedTrackColor = MediAccessColors.Primary
                    )
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Providers Near Me", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
            }
        }

        if (state.isLoading || locationState.isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = MediAccessColors.Primary)
            }
        } else if (state.hospitals.isEmpty()) {
            EmptyHospitalsView()
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                contentPadding = PaddingValues(16.dp)
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
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF9F9F9)),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = hospital.name.uppercase(),
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = Color.DarkGray
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Circle, null, modifier = Modifier.size(8.dp), tint = Color.LightGray)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(hospital.address, style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                }
            }

            if (userLat != null && userLon != null) {
                Text(
                    text = hospital.formattedDistance(userLat, userLon),
                    style = MaterialTheme.typography.labelSmall,
                    color = MediAccessColors.Primary,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
private fun MyVisitsContent(
    state: CareState,
    onAction: (CareAction) -> Unit
) {
    Column(modifier = Modifier.fillMaxSize().background(Color.White)) {
        Button(
            onClick = { onAction(CareAction.OnInitiateVisit) },
            modifier = Modifier.fillMaxWidth().padding(16.dp).height(48.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFB71C1C)),
            shape = RoundedCornerShape(8.dp)
        ) {
            Icon(Icons.Default.DateRange, null, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text("Initiate a visit", fontWeight = FontWeight.Bold)
        }

        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(Icons.Default.Folder, null, modifier = Modifier.size(160.dp), tint = Color(0xFFF28B82))
                Text("No data\nfound", color = Color.White, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
            }
            Spacer(modifier = Modifier.height(20.dp))
            Text("NO VISITS FOUND", fontWeight = FontWeight.Bold, color = Color.Black)
        }
    }
}

@Composable
private fun EmptyHospitalsView() {
    Column(modifier = Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
        Icon(Icons.Default.SearchOff, null, modifier = Modifier.size(64.dp), tint = Color.LightGray)
        Spacer(modifier = Modifier.height(16.dp))
        Text("No hospitals found", color = Color.Gray)
    }
}

enum class CareTab { LOCATE_PROVIDER, MY_VISITS }