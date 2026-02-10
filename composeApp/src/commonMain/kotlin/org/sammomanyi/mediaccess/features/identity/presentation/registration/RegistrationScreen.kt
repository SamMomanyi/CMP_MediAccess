package org.sammomanyi.mediaccess.features.identity.presentation.registration

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.Key.Companion.R
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import mediaccess.composeapp.generated.resources.Res
import mediaccess.composeapp.generated.resources.agree_to_terms
import mediaccess.composeapp.generated.resources.and_text
import mediaccess.composeapp.generated.resources.back
import mediaccess.composeapp.generated.resources.confirm_password_label
import mediaccess.composeapp.generated.resources.email_label
import mediaccess.composeapp.generated.resources.email_placeholder
import mediaccess.composeapp.generated.resources.email_recommendation
import mediaccess.composeapp.generated.resources.first_name_hint
import mediaccess.composeapp.generated.resources.last_name_hint
import mediaccess.composeapp.generated.resources.logo
import mediaccess.composeapp.generated.resources.password_case_sensitive
import mediaccess.composeapp.generated.resources.password_hint
import mediaccess.composeapp.generated.resources.password_label
import mediaccess.composeapp.generated.resources.password_strength
import mediaccess.composeapp.generated.resources.phone_label
import mediaccess.composeapp.generated.resources.phone_placeholder
import mediaccess.composeapp.generated.resources.phone_recommendation
import mediaccess.composeapp.generated.resources.powered_by
import mediaccess.composeapp.generated.resources.privacy_policy
import mediaccess.composeapp.generated.resources.register_button
import mediaccess.composeapp.generated.resources.registration_subtitle
import mediaccess.composeapp.generated.resources.registration_title
import mediaccess.composeapp.generated.resources.terms_of_use
import mediaccess.composeapp.generated.resources.verify
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import org.sammomanyi.mediaccess.core.presentation.asString
import org.sammomanyi.mediaccess.core.presentation.theme.MediAccessColors

@Composable
fun RegistrationScreen(
    onLoginClick: () -> Unit,
    onSuccess: () -> Unit,
    onBackClick: () -> Unit,
    viewModel: RegistrationViewModel = koinViewModel()
) {
    val state by viewModel.state.collectAsState()
    var passwordVisible by remember { mutableStateOf(false) }
    var confirmPasswordVisible by remember { mutableStateOf(false) }
    var agreedToTerms by remember { mutableStateOf(false) }
    var selectedCountryCode by remember { mutableStateOf("+254") }

    LaunchedEffect(state.isSuccess) {
        if (state.isSuccess) onSuccess()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        MediAccessColors.Background,
                        Color.White
                    )
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .verticalScroll(rememberScrollState())
                .padding(24.dp)
        ) {
            // Back Button
            IconButton(onClick = onBackClick) {
                Icon(
                    Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = stringResource(Res.string.back),
                    tint = MediAccessColors.TextPrimary
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Logo
            Image(
                painter = painterResource(Res.drawable.logo),
                contentDescription = null,
                modifier = Modifier
                    .size(60.dp)
                    .align(Alignment.Start)
                    .clip(RoundedCornerShape(12.dp))
            )

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = stringResource(Res.string.registration_title),
                style = MaterialTheme.typography.displayLarge,
                color = MediAccessColors.TextPrimary,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = stringResource(Res.string.registration_subtitle),
                style = MaterialTheme.typography.bodyMedium,
                color = MediAccessColors.Secondary
            )

            Spacer(modifier = Modifier.height(32.dp))

            // First Name
            OutlinedTextField(
                value = state.firstName,
                onValueChange = { viewModel.onAction(RegistrationAction.OnFirstNameChange(it)) },
                placeholder = { Text(stringResource(Res.string.first_name_hint), color = MediAccessColors.TextHint) },
                leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
                isError = state.firstNameError != null,
                supportingText = {
                    state.firstNameError?.let {
                        Text(it.asString(), color = MediAccessColors.Error)
                    }
                },
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MediAccessColors.Primary,
                    unfocusedBorderColor = MediAccessColors.SurfaceVariant,
                    // FIX 2: Visible Text
                    focusedTextColor = MediAccessColors.TextPrimary,
                    unfocusedTextColor = MediAccessColors.TextPrimary,
                    cursorColor = MediAccessColors.Primary
                ),
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Last Name
            OutlinedTextField(
                value = state.lastName,
                onValueChange = { viewModel.onAction(RegistrationAction.OnLastNameChange(it)) },
                placeholder = { Text(stringResource(Res.string.last_name_hint), color = MediAccessColors.TextHint) },
                leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
                isError = state.lastNameError != null,
                supportingText = {
                    state.lastNameError?.let {
                        Text(it.asString(), color = MediAccessColors.Error)
                    }
                },
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MediAccessColors.Primary,
                    unfocusedBorderColor = MediAccessColors.SurfaceVariant,
                    // FIX 2: Visible Text
                    focusedTextColor = MediAccessColors.TextPrimary,
                    unfocusedTextColor = MediAccessColors.TextPrimary,
                    cursorColor = MediAccessColors.Primary
                ),
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Phone Number
            Column {
                Text(
                    text = stringResource(Res.string.phone_label),
                    style = MaterialTheme.typography.labelMedium,
                    color = MediAccessColors.TextPrimary
                )
                Text(
                    text = stringResource(Res.string.phone_recommendation),
                    style = MaterialTheme.typography.bodySmall,
                    color = MediAccessColors.TextSecondary
                )
                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = selectedCountryCode,
                        onValueChange = { },
                        readOnly = true,
                        leadingIcon = { Text("🇰🇪") },
                        modifier = Modifier.width(120.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MediAccessColors.Primary,
                            unfocusedBorderColor = MediAccessColors.SurfaceVariant,
                            // FIX 2: Visible Text
                            focusedTextColor = MediAccessColors.TextPrimary,
                            unfocusedTextColor = MediAccessColors.TextPrimary,
                            cursorColor = MediAccessColors.Primary
                        ),
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = state.phoneNumber,
                        onValueChange = { viewModel.onAction(RegistrationAction.OnPhoneNumberChange(it)) },
                        placeholder = { Text(stringResource(Res.string.phone_placeholder), color = MediAccessColors.TextHint) },
                        isError = state.phoneError != null,
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Phone,
                            imeAction = ImeAction.Next
                        ),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MediAccessColors.Primary,
                            unfocusedBorderColor = MediAccessColors.SurfaceVariant,
                            // FIX 2: Visible Text
                            focusedTextColor = MediAccessColors.TextPrimary,
                            unfocusedTextColor = MediAccessColors.TextPrimary,
                            cursorColor = MediAccessColors.Primary
                        ),
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        trailingIcon = {
                            TextButton(onClick = { /* TODO */ }) {
                                Text(
                                    stringResource(Res.string.verify),
                                    color = Color.White,
                                    style = MaterialTheme.typography.labelMedium
                                )
                            }
                        }
                    )
                }

                state.phoneError?.let {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = it.asString(),
                        color = MediAccessColors.Error,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Email
            Column {
                Text(
                    text = stringResource(Res.string.email_label),
                    style = MaterialTheme.typography.labelMedium,
                    color = MediAccessColors.TextPrimary
                )
                Text(
                    text = stringResource(Res.string.email_recommendation),
                    style = MaterialTheme.typography.bodySmall,
                    color = MediAccessColors.TextSecondary
                )
                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = state.email,
                    onValueChange = { viewModel.onAction(RegistrationAction.OnEmailChange(it)) },
                    placeholder = { Text(stringResource(Res.string.email_placeholder), color = MediAccessColors.TextHint) },
                    leadingIcon = { Icon(Icons.Default.Email, contentDescription = null) },
                    isError = state.emailError != null,
                    supportingText = {
                        state.emailError?.let {
                            Text(it.asString(), color = MediAccessColors.Error)
                        }
                    },
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Email,
                        imeAction = ImeAction.Next
                    ),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MediAccessColors.Primary,
                        unfocusedBorderColor = MediAccessColors.SurfaceVariant,
                        // FIX 2: Visible Text
                        focusedTextColor = MediAccessColors.TextPrimary,
                        unfocusedTextColor = MediAccessColors.TextPrimary,
                        cursorColor = MediAccessColors.Primary
                    ),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    trailingIcon = {
                        TextButton(onClick = { /* TODO */ }) {
                            Text(
                                stringResource(Res.string.verify),
                                color = MediAccessColors.TextSecondary,
                                style = MaterialTheme.typography.labelMedium
                            )
                        }
                    }
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Password
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = stringResource(Res.string.password_label),
                        style = MaterialTheme.typography.labelMedium,
                        color = MediAccessColors.TextPrimary
                    )
                    Icon(
                        Icons.Default.Info,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = MediAccessColors.TextSecondary
                    )
                }
                Text(
                    text = stringResource(Res.string.password_case_sensitive),
                    style = MaterialTheme.typography.bodySmall,
                    color = MediAccessColors.TextSecondary
                )
                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = state.password,
                    onValueChange = { viewModel.onAction(RegistrationAction.OnPasswordChange(it)) },
                    placeholder = { Text(stringResource(Res.string.password_hint), color = MediAccessColors.TextHint) },
                    leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) },
                    trailingIcon = {
                        IconButton(onClick = { passwordVisible = !passwordVisible }) {
                            Icon(
                                if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                contentDescription = null
                            )
                        }
                    },
                    visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    isError = state.passwordError != null,
                    supportingText = {
                        state.passwordError?.let {
                            Text(it.asString(), color = MediAccessColors.Error)
                        }
                    },
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Password,
                        imeAction = ImeAction.Next
                    ),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MediAccessColors.Primary,
                        unfocusedBorderColor = MediAccessColors.SurfaceVariant,
                        // FIX 2: Visible Text
                        focusedTextColor = MediAccessColors.TextPrimary,
                        unfocusedTextColor = MediAccessColors.TextPrimary,
                        cursorColor = MediAccessColors.Primary
                    ),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                if (state.password.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    val strength = calculatePasswordStrength(state.password)
                    LinearProgressIndicator(
                        progress = { strength / 100f },
                        modifier = Modifier.fillMaxWidth().height(4.dp),
                        color = when {
                            strength < 30 -> MediAccessColors.Error
                            strength < 70 -> MediAccessColors.Warning
                            else -> MediAccessColors.Success
                        },
                    )
                    Text(
                        text = stringResource(Res.string.password_strength),
                        style = MaterialTheme.typography.bodySmall,
                        color = MediAccessColors.TextSecondary,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Confirm Password
            Column {
                Text(
                    text = stringResource(Res.string.confirm_password_label),
                    style = MaterialTheme.typography.labelMedium,
                    color = MediAccessColors.TextPrimary
                )
                Text(
                    text = stringResource(Res.string.password_case_sensitive),
                    style = MaterialTheme.typography.bodySmall,
                    color = MediAccessColors.TextSecondary
                )
                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = state.confirmPassword,
                    onValueChange = { viewModel.onAction(RegistrationAction.OnConfirmPasswordChange(it)) },
                    placeholder = { Text(stringResource(Res.string.confirm_password_label), color = MediAccessColors.TextHint) },
                    leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) },
                    trailingIcon = {
                        IconButton(onClick = { confirmPasswordVisible = !confirmPasswordVisible }) {
                            Icon(
                                if (confirmPasswordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                contentDescription = null
                            )
                        }
                    },
                    visualTransformation = if (confirmPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Password,
                        imeAction = ImeAction.Done
                    ),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MediAccessColors.Primary,
                        unfocusedBorderColor = MediAccessColors.SurfaceVariant,
                        // FIX 2: Visible Text
                        focusedTextColor = MediAccessColors.TextPrimary,
                        unfocusedTextColor = MediAccessColors.TextPrimary,
                        cursorColor = MediAccessColors.Primary
                    ),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Terms and Conditions
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Checkbox(
                    checked = agreedToTerms,
                    onCheckedChange = { agreedToTerms = it },
                    colors = CheckboxDefaults.colors(checkedColor = MediAccessColors.Primary)
                )
                Row {
                    Text(
                        text = stringResource(Res.string.agree_to_terms),
                        style = MaterialTheme.typography.bodySmall,
                        color = MediAccessColors.TextPrimary
                    )
                    Text(
                        text = stringResource(Res.string.privacy_policy),
                        style = MaterialTheme.typography.bodySmall,
                        color = MediAccessColors.Primary,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.clickable { /* TODO */ }
                    )
                    Text(
                        text = stringResource(Res.string.and_text),
                        style = MaterialTheme.typography.bodySmall,
                        color = MediAccessColors.TextPrimary
                    )
                    Text(
                        text = stringResource(Res.string.terms_of_use),
                        style = MaterialTheme.typography.bodySmall,
                        color = MediAccessColors.Primary,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.clickable { /* TODO */ }
                    )
                    Text(text = " .", style = MaterialTheme.typography.bodySmall)
                }
            }

            state.errorMessage?.let { error ->
                Spacer(modifier = Modifier.height(16.dp))
                Surface(
                    color = MediAccessColors.Error.copy(alpha = 0.1f),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = error.asString(),
                        color = MediAccessColors.Error,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(12.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Register Button
            Button(
                onClick = { viewModel.onAction(RegistrationAction.OnRegisterClick) },
                enabled = !state.isLoading && agreedToTerms,
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MediAccessColors.Secondary,
                    disabledContainerColor = MediAccessColors.SurfaceVariant
                )
            ) {
                if (state.isLoading) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Color.White)
                } else {
                    Text(
                        text = stringResource(Res.string.register_button),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Footer
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(Res.string.powered_by),
                    style = MaterialTheme.typography.bodySmall,
                    color = MediAccessColors.TextSecondary
                )
                Text(
                    text = "MediAccess",
                    style = MaterialTheme.typography.bodySmall,
                    color = MediAccessColors.Primary,
                    fontWeight = FontWeight.SemiBold
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}
private fun calculatePasswordStrength(password: String): Int {
    var strength = 0
    if (password.length >= 8) strength += 25
    if (password.any { it.isUpperCase() }) strength += 25
    if (password.any { it.isLowerCase() }) strength += 25
    if (password.any { it.isDigit() }) strength += 25
    return strength
}
