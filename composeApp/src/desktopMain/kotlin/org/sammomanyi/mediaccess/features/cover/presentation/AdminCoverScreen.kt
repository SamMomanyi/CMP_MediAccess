package org.sammomanyi.mediaccess.features.cover.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import org.koin.compose.viewmodel.koinViewModel
import org.sammomanyi.mediaccess.features.cover.domain.model.CoverLinkRequest
import org.sammomanyi.mediaccess.features.cover.domain.model.CoverStatus

@Composable
fun AdminCoverScreen() {
    val viewModel: AdminCoverViewModel = koinViewModel()
    val state by viewModel.state.collectAsState()

    var showRejectDialog by remember { mutableStateOf(false) }
    var rejectNote by remember { mutableStateOf("") }

    val snackbarHostState = remember { SnackbarHostState() }
    LaunchedEffect(state.actionSuccess, state.actionError) {
        val msg = state.actionSuccess ?: state.actionError
        if (msg != null) {
            snackbarHostState.showSnackbar(msg)
            viewModel.dismissFeedback()
        }
    }

    Scaffold(snackbarHost = { SnackbarHost(snackbarHostState) }) { padding ->
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // ── Left panel: list ──────────────────────────────
            Column(
                modifier = Modifier
                    .width(340.dp)
                    .fillMaxHeight()
                    .background(MaterialTheme.colorScheme.surfaceContainer)
            ) {
                // Header + refresh button
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Cover Requests",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                        if (state.lastRefreshedAt != null) {
                            Text(
                                text = "Updated ${formatTimestamp(state.lastRefreshedAt!!)}",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    IconButton(
                        onClick = { viewModel.refresh() },
                        enabled = !state.isLoading
                    ) {
                        if (state.isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(18.dp),
                                strokeWidth = 2.dp
                            )
                        } else {
                            Icon(
                                Icons.Default.Refresh,
                                contentDescription = "Refresh",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }

                // Error banner
                if (state.loadError != null) {
                    Surface(
                        color = MaterialTheme.colorScheme.errorContainer,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 4.dp),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = state.loadError!!,
                            color = MaterialTheme.colorScheme.onErrorContainer,
                            style = MaterialTheme.typography.labelSmall,
                            modifier = Modifier.padding(8.dp)
                        )
                    }
                }

                // Filter chips
                Row(
                    modifier = Modifier
                        .horizontalScroll(rememberScrollState())
                        .padding(horizontal = 12.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    CoverRequestFilter.entries.forEach { filter ->
                        val count = when (filter) {
                            CoverRequestFilter.ALL -> state.requests.size
                            CoverRequestFilter.PENDING -> state.requests.count { it.status == CoverStatus.PENDING }
                            CoverRequestFilter.APPROVED -> state.requests.count { it.status == CoverStatus.APPROVED }
                            CoverRequestFilter.REJECTED -> state.requests.count { it.status == CoverStatus.REJECTED }
                        }
                        FilterChip(
                            selected = state.activeFilter == filter,
                            onClick = { viewModel.setFilter(filter) },
                            label = {
                                Text("${filter.name.lowercase().replaceFirstChar { it.uppercase() }} ($count)")
                            }
                        )
                    }
                }

                HorizontalDivider(modifier = Modifier.padding(top = 8.dp))

                when {
                    state.isLoading && state.requests.isEmpty() -> {
                        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                CircularProgressIndicator()
                                Spacer(Modifier.height(8.dp))
                                Text("Loading requests...",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }
                    state.filteredRequests.isEmpty() -> {
                        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    "No ${state.activeFilter.name.lowercase()} requests",
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                if (state.lastRefreshedAt == null) {
                                    Spacer(Modifier.height(8.dp))
                                    TextButton(onClick = { viewModel.refresh() }) {
                                        Text("Tap refresh to load")
                                    }
                                }
                            }
                        }
                    }
                    else -> {
                        LazyColumn(
                            contentPadding = PaddingValues(vertical = 8.dp),
                            verticalArrangement = Arrangement.spacedBy(2.dp)
                        ) {
                            items(state.filteredRequests, key = { it.id }) { request ->
                                RequestListItem(
                                    request = request,
                                    isSelected = state.selectedRequest?.id == request.id,
                                    onClick = { viewModel.selectRequest(request) }
                                )
                            }
                        }
                    }
                }
            }

            VerticalDivider()

            // ── Right panel: detail ───────────────────────────
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .background(MaterialTheme.colorScheme.surface)
            ) {
                if (state.selectedRequest == null) {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(
                            "Select a request to review",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                } else {
                    RequestDetailPanel(
                        request = state.selectedRequest!!,
                        isLoading = state.actionInProgress,
                        onApprove = { viewModel.approveRequest(state.selectedRequest!!.id) },
                        onReject = {
                            rejectNote = ""
                            showRejectDialog = true
                        }
                    )
                }
            }
        }
    }

    // ── Reject dialog ─────────────────────────────────────────
    if (showRejectDialog) {
        AlertDialog(
            onDismissRequest = { showRejectDialog = false },
            title = { Text("Reject Request") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        "Please provide a reason. This will be visible to the patient.",
                        style = MaterialTheme.typography.bodySmall
                    )
                    OutlinedTextField(
                        value = rejectNote,
                        onValueChange = { rejectNote = it },
                        label = { Text("Rejection reason") },
                        minLines = 3,
                        maxLines = 5,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.rejectRequest(state.selectedRequest!!.id, rejectNote)
                        showRejectDialog = false
                    },
                    enabled = rejectNote.isNotBlank(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error
                    )
                ) { Text("Reject") }
            },
            dismissButton = {
                OutlinedButton(onClick = { showRejectDialog = false }) { Text("Cancel") }
            }
        )
    }
}

// ── List item ─────────────────────────────────────────────────

@Composable
private fun RequestListItem(
    request: CoverLinkRequest,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val bgColor = if (isSelected)
        MaterialTheme.colorScheme.primaryContainer
    else
        MaterialTheme.colorScheme.surfaceContainer

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .background(bgColor)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(RoundedCornerShape(20.dp))
                .background(statusColor(request.status).copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                Icons.Default.Person, contentDescription = null,
                tint = statusColor(request.status),
                modifier = Modifier.size(22.dp)
            )
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = request.userEmail,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                maxLines = 1
            )
            Text(
                text = "${request.insuranceName} · ${request.country}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1
            )
        }
        StatusBadge(request.status)
    }
}

// ── Detail panel ──────────────────────────────────────────────

@Composable
private fun RequestDetailPanel(
    request: CoverLinkRequest,
    isLoading: Boolean,
    onApprove: () -> Unit,
    onReject: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(32.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(RoundedCornerShape(28.dp))
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.Person, contentDescription = null,
                    modifier = Modifier.size(30.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
            }
            Column {
                Text(
                    text = request.userEmail,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                StatusBadge(request.status)
            }
        }

        HorizontalDivider()

        Text("Insurance Details",
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.SemiBold)

        DetailGrid(listOf(
            "Insurance Provider" to request.insuranceName,
            "Member Number" to request.memberNumber,
            "Country" to request.country,
            "Submitted" to formatTimestamp(request.submittedAt),
            "User ID" to request.userId
        ))

        if (request.reviewedAt != null || request.reviewNote.isNotEmpty()) {
            HorizontalDivider()
            Text("Review Details",
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.SemiBold)
            DetailGrid(listOfNotNull(
                request.reviewedAt?.let { "Reviewed At" to formatTimestamp(it) },
                if (request.reviewNote.isNotEmpty()) "Review Note" to request.reviewNote else null
            ))
        }

        if (request.status == CoverStatus.PENDING) {
            HorizontalDivider()
            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Button(
                    onClick = onApprove,
                    enabled = !isLoading,
                    modifier = Modifier.weight(1f).height(50.dp)
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(18.dp),
                            color = MaterialTheme.colorScheme.onPrimary,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Icon(Icons.Default.Check, contentDescription = null,
                            modifier = Modifier.padding(end = 8.dp))
                        Text("Approve")
                    }
                }
                OutlinedButton(
                    onClick = onReject,
                    enabled = !isLoading,
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    ),
                    modifier = Modifier.weight(1f).height(50.dp)
                ) {
                    Icon(Icons.Default.Close, contentDescription = null,
                        modifier = Modifier.padding(end = 8.dp))
                    Text("Reject")
                }
            }
        }
    }
}

// ── Helpers ───────────────────────────────────────────────────

@Composable
private fun DetailGrid(items: List<Pair<String, String>>) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        items.forEach { (label, value) ->
            Row(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = label,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.width(160.dp)
                )
                Text(
                    text = value,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

@Composable
private fun StatusBadge(status: CoverStatus) {
    val color = statusColor(status)
    Surface(color = color.copy(alpha = 0.12f), shape = RoundedCornerShape(12.dp)) {
        Text(
            text = status.name,
            color = color,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
        )
    }
}

@Composable
private fun statusColor(status: CoverStatus) = when (status) {
    CoverStatus.PENDING -> MaterialTheme.colorScheme.tertiary
    CoverStatus.APPROVED -> MaterialTheme.colorScheme.primary
    CoverStatus.REJECTED -> MaterialTheme.colorScheme.error
}

private fun formatTimestamp(epochMillis: Long): String {
    val instant = Instant.fromEpochMilliseconds(epochMillis)
    val local = instant.toLocalDateTime(TimeZone.currentSystemDefault())
    return "%d %s %d, %02d:%02d".format(
        local.dayOfMonth,
        local.month.name.lowercase().replaceFirstChar { it.uppercase() }.take(3),
        local.year,
        local.hour,
        local.minute
    )
}