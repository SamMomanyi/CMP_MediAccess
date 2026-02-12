package org.sammomanyi.mediaccess.features.identity.presentation.link_cover

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.koin.compose.viewmodel.koinViewModel
import org.sammomanyi.mediaccess.core.presentation.theme.AppColors
import org.sammomanyi.mediaccess.core.presentation.theme.MediAccessColors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LinkCoverScreen(
    onBackClick: () -> Unit,
    onLinkSuccess: () -> Unit,
    viewModel: LinkCoverViewModel = koinViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val scrollState = rememberScrollState()

    // ── Navigate to success when done ──
    LaunchedEffect(state.isSuccess) {
        if (state.isSuccess) onLinkSuccess()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { },
                navigationIcon = {
                    TextButton(onClick = onBackClick) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = MaterialTheme.colorScheme.onBackground
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            "Back",
                            color = MaterialTheme.colorScheme.onBackground,
                            style = MaterialTheme.typography.titleMedium
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(horizontal = 24.dp)
                    .verticalScroll(scrollState)
            ) {
                Text(
                    text = "Setup Account",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )

                Spacer(modifier = Modifier.height(32.dp))

                // ── Stepper ──
                ThemeStepper(
                    activeStep = state.activeTab,
                    onStepClick = { viewModel.onAction(LinkCoverAction.ChangeTab(it)) }
                )

                Spacer(modifier = Modifier.height(32.dp))

                // ── Country (shared) ──
                CountryDropdown(
                    selected = state.selectedCountry,
                    onSelected = { viewModel.onAction(LinkCoverAction.SelectCountry(it)) }
                )

                Spacer(modifier = Modifier.height(16.dp))

                if (state.activeTab == 1) {
                    // ════════ STEP 1: AUTOMATIC ════════
                    LabelText("Email :")
                    Spacer(modifier = Modifier.height(8.dp))
                    ReadOnlyEmailField(email = state.userEmail)

                    Spacer(modifier = Modifier.height(40.dp))

                    if (state.isLoading) {
                        // "Please Wait..." — matches screenshot exactly
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(28.dp),
                                    color = MaterialTheme.colorScheme.secondary,
                                    strokeWidth = 3.dp
                                )
                                Text(
                                    text = state.loadingMessage,
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontWeight = FontWeight.Medium,
                                    color = MaterialTheme.colorScheme.onBackground
                                )
                            }
                        }
                    } else {
                        PrimaryButton(
                            text = "Link Account",
                            onClick = { viewModel.onAction(LinkCoverAction.Submit) },
                            color = MaterialTheme.colorScheme.secondary
                        )
                    }

                } else {
                    // ════════ STEP 2: MANUAL ════════
                    Text(
                        "Make a request to the Cover Provider manually for verification.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(24.dp))

                    // Insurance searchable dropdown
                    SearchableInsuranceDropdown(
                        selected = state.selectedInsurance,
                        insurances = state.availableInsurances,
                        hasError = state.error != null && state.selectedInsurance.isEmpty(),
                        onSelected = { viewModel.onAction(LinkCoverAction.SelectInsurance(it)) }
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    LabelText("Email :")
                    Spacer(modifier = Modifier.height(8.dp))
                    ReadOnlyEmailField(email = state.userEmail)

                    Spacer(modifier = Modifier.height(16.dp))

                    LabelText("Member No : *")
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = state.memberNumber,
                        onValueChange = { viewModel.onAction(LinkCoverAction.EnterMemberNumber(it)) },
                        leadingIcon = {
                            Icon(
                                Icons.Default.CreditCard,
                                null,
                                tint = MaterialTheme.colorScheme.secondary
                            )
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                            focusedTextColor = MaterialTheme.colorScheme.onBackground,
                            unfocusedTextColor = MaterialTheme.colorScheme.onBackground
                        )
                    )

                    Spacer(modifier = Modifier.height(40.dp))

                    if (state.isLoading) {
                        Box(
                            modifier = Modifier.fillMaxWidth(),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(
                                color = MaterialTheme.colorScheme.secondary
                            )
                        }
                    } else {
                        PrimaryButton(
                            text = "Submit",
                            onClick = { viewModel.onAction(LinkCoverAction.Submit) },
                            color = if (state.selectedInsurance.isNotEmpty() && state.memberNumber.isNotEmpty())
                                MaterialTheme.colorScheme.secondary
                            else MaterialTheme.colorScheme.outline
                        )
                    }
                }

                // Error message
                state.error?.let { errorMsg ->
                    Spacer(modifier = Modifier.height(16.dp))
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.errorContainer
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.ErrorOutline,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.error,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = errorMsg,
                                color = MaterialTheme.colorScheme.onErrorContainer,
                                style = MaterialTheme.typography.bodySmall,
                                modifier = Modifier.weight(1f)
                            )
                            IconButton(
                                onClick = { viewModel.onAction(LinkCoverAction.DismissError) },
                                modifier = Modifier.size(24.dp)
                            ) {
                                Icon(
                                    Icons.Default.Close,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp),
                                    tint = MaterialTheme.colorScheme.onErrorContainer
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(50.dp))
            }
        }
    }
}

// ─────────────────────────────────────────────
// Country Dropdown
// ─────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CountryDropdown(selected: String, onSelected: (String) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    Column {
        LabelText("Country : *")
        Spacer(modifier = Modifier.height(8.dp))
        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = !expanded }
        ) {
            OutlinedTextField(
                value = selected,
                onValueChange = {},
                readOnly = true,
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                modifier = Modifier
                    .fillMaxWidth()
                    .menuAnchor(),
                shape = RoundedCornerShape(8.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = MaterialTheme.colorScheme.onBackground,
                    unfocusedTextColor = MaterialTheme.colorScheme.onBackground,
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline
                )
            )
            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                COUNTRIES.forEach { country ->
                    DropdownMenuItem(
                        text = {
                            Text(
                                country,
                                color = MaterialTheme.colorScheme.onBackground
                            )
                        },
                        onClick = {
                            onSelected(country)
                            expanded = false
                        }
                    )
                }
            }
        }
    }
}

// ─────────────────────────────────────────────
// Searchable Insurance Dropdown
// ─────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SearchableInsuranceDropdown(
    selected: String,
    insurances: List<String>,
    hasError: Boolean,
    onSelected: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }
    val filtered = remember(searchQuery) {
        insurances.filter { it.contains(searchQuery, ignoreCase = true) }
    }

    Column {
        LabelText("Insurance : *")
        Spacer(modifier = Modifier.height(8.dp))
        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = !expanded }
        ) {
            OutlinedTextField(
                value = selected.ifBlank { "select an insurance" },
                onValueChange = {},
                readOnly = true,
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                modifier = Modifier
                    .fillMaxWidth()
                    .menuAnchor(),
                shape = RoundedCornerShape(8.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = MaterialTheme.colorScheme.onBackground,
                    unfocusedTextColor = if (selected.isBlank())
                        MaterialTheme.colorScheme.onSurfaceVariant
                    else MaterialTheme.colorScheme.onBackground,
                    focusedBorderColor = if (hasError)
                        MaterialTheme.colorScheme.error
                    else MaterialTheme.colorScheme.secondary,
                    unfocusedBorderColor = if (hasError)
                        MaterialTheme.colorScheme.error
                    else MaterialTheme.colorScheme.outline
                )
            )
            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = {
                    expanded = false
                    searchQuery = ""
                }
            ) {
                // Search field inside dropdown
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Search Insurance") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 4.dp),
                    shape = RoundedCornerShape(8.dp),
                    singleLine = true
                )
                filtered.forEach { insurer ->
                    DropdownMenuItem(
                        text = {
                            Text(
                                insurer,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onBackground
                            )
                        },
                        onClick = {
                            onSelected(insurer)
                            searchQuery = ""
                            expanded = false
                        }
                    )
                }
            }
        }
    }
}

// ─────────────────────────────────────────────
// Read-only email field (matches screenshot style)
// ─────────────────────────────────────────────

@Composable
private fun ReadOnlyEmailField(email: String) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.06f)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Red envelope icon box — matches screenshot
            Surface(
                modifier = Modifier.size(36.dp),
                shape = RoundedCornerShape(6.dp),
                color = MaterialTheme.colorScheme.secondary
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        Icons.Default.Email,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = email,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

// ─────────────────────────────────────────────
// Reusable shared components
// ─────────────────────────────────────────────

@Composable
fun LabelText(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant
    )
}

@Composable
fun PrimaryButton(text: String, onClick: () -> Unit, color: Color) {
    Button(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(50.dp),
        colors = ButtonDefaults.buttonColors(containerColor = color),
        shape = RoundedCornerShape(8.dp)
    ) {
        Text(text, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
    }
}

@Composable
fun ThemeStepper(activeStep: Int, onStepClick: (Int) -> Unit) {
    Box(
        modifier = Modifier.fillMaxWidth(),
        contentAlignment = Alignment.TopCenter
    ) {
        HorizontalDivider(
            color = if (activeStep == 2)
                Color(0xFF4CAF50)
            else MaterialTheme.colorScheme.outline,
            thickness = 2.dp,
            modifier = Modifier
                .padding(horizontal = 50.dp)
                .padding(top = 16.dp)
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            StepCircle(
                number = "1",
                label = "automatic linkage",
                isActive = activeStep == 1,
                isCompleted = activeStep == 2,
                onClick = { onStepClick(1) }
            )
            StepCircle(
                number = "2",
                label = "manual linkage",
                isActive = activeStep == 2,
                isCompleted = false,
                onClick = { onStepClick(2) }
            )
        }
    }
}

@Composable
fun StepCircle(
    number: String,
    label: String,
    isActive: Boolean,
    isCompleted: Boolean,
    onClick: () -> Unit
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Surface(
            modifier = Modifier
                .size(34.dp)
                .clickable(onClick = onClick),
            shape = CircleShape,
            color = when {
                isCompleted -> Color(0xFF4CAF50)
                isActive -> MaterialTheme.colorScheme.primary
                else -> MaterialTheme.colorScheme.outline
            }
        ) {
            Box(contentAlignment = Alignment.Center) {
                if (isCompleted) {
                    Icon(
                        Icons.Default.Check,
                        null,
                        tint = Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                } else {
                    Text(
                        number,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = if (isActive || isCompleted)
                MaterialTheme.colorScheme.primary
            else MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}