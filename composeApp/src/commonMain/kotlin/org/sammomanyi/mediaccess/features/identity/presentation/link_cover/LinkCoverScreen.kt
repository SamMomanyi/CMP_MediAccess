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
    var insuranceExpanded by remember { mutableStateOf(false) }

    LaunchedEffect(state.isSuccess) {
        if (state.isSuccess) {
            onLinkSuccess()
        }
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
                            tint = AppColors.textPrimary
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            "Back",
                            color = AppColors.textPrimary,
                            style = MaterialTheme.typography.titleMedium
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = AppColors.screenBackground
                )
            )
        },
        containerColor = AppColors.screenBackground
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
                    color = AppColors.textPrimary
                )

                Spacer(modifier = Modifier.height(32.dp))

                // ──────────────── Stepper ────────────────
                ThemeStepper(
                    activeStep = state.activeTab,
                    onStepClick = { viewModel.onAction(LinkCoverAction.ChangeTab(it)) }
                )

                Spacer(modifier = Modifier.height(32.dp))

                // ──────────────── Form ────────────────

                // Country (Common)
                LabelText("Country : *")
                Spacer(modifier = Modifier.height(8.dp))
                ReadOnlyTextField(value = state.selectedCountry)

                Spacer(modifier = Modifier.height(16.dp))

                if (state.activeTab == 1) {
                    // === AUTOMATIC ===
                    LabelText("Email :")
                    Spacer(modifier = Modifier.height(8.dp))
                    ReadOnlyTextField(
                        value = state.userEmail,
                        icon = Icons.Default.Email,
                        iconTint = MediAccessColors.Secondary // Red
                    )

                    Spacer(modifier = Modifier.height(40.dp))

                    PrimaryButton(
                        text = "Link Account",
                        onClick = { viewModel.onAction(LinkCoverAction.Submit) },
                        color = MediAccessColors.Secondary // Red for Action
                    )
                } else {
                    // === MANUAL ===
                    Text(
                        "Make a request to the Cover Provider manually for verification.",
                        style = MaterialTheme.typography.bodySmall,
                        color = AppColors.textSecondary
                    )
                    Spacer(modifier = Modifier.height(24.dp))

                    LabelText("Insurance : *")
                    Spacer(modifier = Modifier.height(8.dp))

                    // Insurance Dropdown
                    Box {
                        ReadOnlyTextField(
                            value = state.selectedInsurance,
                            placeholder = "select an insurance",
                            isDropdown = true,
                            onClick = { insuranceExpanded = true },
                            hasError = state.error != null && state.selectedInsurance.isEmpty()
                        )
                        DropdownMenu(
                            expanded = insuranceExpanded,
                            onDismissRequest = { insuranceExpanded = false },
                            modifier = Modifier.fillMaxWidth(0.88f).background(AppColors.cardBackground)
                        ) {
                            state.availableInsurances.forEach { provider ->
                                DropdownMenuItem(
                                    text = { Text(provider, color = AppColors.textPrimary) },
                                    onClick = {
                                        viewModel.onAction(LinkCoverAction.SelectInsurance(provider))
                                        insuranceExpanded = false
                                    }
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    LabelText("Email :")
                    Spacer(modifier = Modifier.height(8.dp))
                    ReadOnlyTextField(
                        value = state.userEmail,
                        icon = Icons.Default.Email,
                        iconTint = MediAccessColors.Secondary
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    LabelText("Member No : *")
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = state.memberNumber,
                        onValueChange = { viewModel.onAction(LinkCoverAction.EnterMemberNumber(it)) },
                        leadingIcon = {
                            Icon(Icons.Default.CreditCard, null, tint = MediAccessColors.Secondary)
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                            focusedTextColor = AppColors.textPrimary,
                            unfocusedTextColor = AppColors.textPrimary
                        )
                    )

                    Spacer(modifier = Modifier.height(40.dp))

                    PrimaryButton(
                        text = "Submit",
                        onClick = { viewModel.onAction(LinkCoverAction.Submit) },
                        color = if (state.selectedInsurance.isNotEmpty() && state.memberNumber.isNotEmpty())
                            MediAccessColors.Secondary else MaterialTheme.colorScheme.outline
                    )
                }

                // Error Message
                if (state.error != null) {
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = state.error!!,
                        color = MediAccessColors.Error,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    )
                }

                Spacer(modifier = Modifier.height(50.dp))
            }

            // Loading
            if (state.isLoading) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(AppColors.screenBackground.copy(alpha = 0.8f))
                        .clickable(enabled = false) {},
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = MediAccessColors.Secondary)
                }
            }
        }
    }
}

// ─────────────────────────────────────────────
// Themed Components
// ─────────────────────────────────────────────

@Composable
fun LabelText(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.bodyMedium,
        color = AppColors.textSecondary
    )
}

@Composable
fun ReadOnlyTextField(
    value: String,
    placeholder: String = "",
    icon: androidx.compose.ui.graphics.vector.ImageVector? = null,
    iconTint: Color = AppColors.icon,
    isDropdown: Boolean = false,
    onClick: () -> Unit = {},
    hasError: Boolean = false
) {
    OutlinedTextField(
        value = value,
        onValueChange = {},
        readOnly = true,
        placeholder = { Text(placeholder, color = AppColors.textSecondary) },
        leadingIcon = if (icon != null) {
            { Icon(icon, null, tint = iconTint) }
        } else null,
        trailingIcon = if (isDropdown) {
            { IconButton(onClick = onClick) { Icon(Icons.Default.ArrowDropDown, null, tint = AppColors.icon) } }
        } else null,
        modifier = Modifier.fillMaxWidth().clickable(enabled = isDropdown, onClick = onClick),
        enabled = false, // We handle clicks manually for dropdowns
        shape = RoundedCornerShape(8.dp),
        colors = OutlinedTextFieldDefaults.colors(
            disabledTextColor = AppColors.textPrimary,
            disabledBorderColor = if (hasError) MediAccessColors.Error else MaterialTheme.colorScheme.outline,
            disabledContainerColor = AppColors.inputBackground, // SurfaceVariant
            disabledPlaceholderColor = AppColors.textSecondary,
            disabledLabelColor = AppColors.textPrimary
        )
    )
}

@Composable
fun PrimaryButton(text: String, onClick: () -> Unit, color: Color) {
    Button(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth().height(50.dp),
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
        // Line
        Divider(
            color = if (activeStep == 2) MediAccessColors.Success else MaterialTheme.colorScheme.outline,
            thickness = 2.dp,
            modifier = Modifier.padding(horizontal = 50.dp).padding(top = 16.dp)
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
            modifier = Modifier.size(34.dp).clickable(onClick = onClick),
            shape = CircleShape,
            // THEME MAPPING:
            // Completed -> Success (Green)
            // Active -> Primary (Teal)
            // Inactive -> Outline (Gray)
            color = when {
                isCompleted -> MediAccessColors.Success
                isActive -> MediAccessColors.Primary
                else -> MaterialTheme.colorScheme.outline
            }
        ) {
            Box(contentAlignment = Alignment.Center) {
                if (isCompleted) {
                    Icon(Icons.Default.Check, null, tint = Color.White, modifier = Modifier.size(18.dp))
                } else {
                    Text(number, color = Color.White, fontWeight = FontWeight.Bold)
                }
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = if (isActive || isCompleted) MediAccessColors.Primary else AppColors.textSecondary
        )
    }
}