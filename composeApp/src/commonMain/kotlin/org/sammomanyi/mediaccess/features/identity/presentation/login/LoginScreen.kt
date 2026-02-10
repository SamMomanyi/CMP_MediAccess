package org.sammomanyi.mediaccess.features.identity.presentation.login

import GoogleSignInProvider
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import mediaccess.composeapp.generated.resources.Res
import mediaccess.composeapp.generated.resources.back
import mediaccess.composeapp.generated.resources.forgot_password
import mediaccess.composeapp.generated.resources.forgot_username
import mediaccess.composeapp.generated.resources.login_button
import mediaccess.composeapp.generated.resources.login_google
import mediaccess.composeapp.generated.resources.login_phone
import mediaccess.composeapp.generated.resources.login_subtitle
import mediaccess.composeapp.generated.resources.login_title
import mediaccess.composeapp.generated.resources.logo
import mediaccess.composeapp.generated.resources.no_account_prompt
import mediaccess.composeapp.generated.resources.or_divider
import mediaccess.composeapp.generated.resources.password_hint
import mediaccess.composeapp.generated.resources.register_now
import mediaccess.composeapp.generated.resources.remember_me
import mediaccess.composeapp.generated.resources.username_hint
import mediaccess.composeapp.generated.resources.username_label
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel
import org.sammomanyi.mediaccess.core.presentation.asString
import org.sammomanyi.mediaccess.core.presentation.theme.MediAccessColors
import org.sammomanyi.mediaccess.features.identity.domain.auth.GoogleSignInResult

@Composable
fun LoginScreen(
    onRegisterClick: () -> Unit,
    onLoginSuccess: () -> Unit,
    onBackClick: () -> Unit,
    viewModel: LoginViewModel = koinViewModel()
) {
    val state by viewModel.state.collectAsState()
    var passwordVisible by remember { mutableStateOf(false) }
    var rememberMe by remember { mutableStateOf(false) }
    val googleSignInProvider: GoogleSignInProvider = koinInject()
    val scope = rememberCoroutineScope()

    LaunchedEffect(state.isSuccess) {
        if (state.isSuccess) onLoginSuccess()
    }

    // Google Sign-In Helper
    fun launchGoogleSignIn() {
        scope.launch {
            when (val result = googleSignInProvider.signIn()) {
                is GoogleSignInResult.Success -> {
                    viewModel.onAction(
                        LoginAction.OnGoogleSignIn(
                            idToken = result.idToken,
                            email = result.email,
                            displayName = result.displayName,
                            photoUrl = result.photoUrl
                        )
                    )
                }
                is GoogleSignInResult.Error -> {
                    println("Google Sign-In Error: ${result.message}")
                }
                GoogleSignInResult.Cancelled -> {
                    println("Google Sign-In Cancelled")
                }
            }
        }
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
            .statusBarsPadding()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .statusBarsPadding()
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

            Spacer(modifier = Modifier.height(24.dp))

            // Logo Placeholder
            Image(
                painter = painterResource(Res.drawable.logo),
                contentDescription = null,
                modifier = Modifier
                    .size(80.dp)
                    .align(Alignment.CenterHorizontally)
                    .clip(RoundedCornerShape(16.dp))
            )

            Spacer(modifier = Modifier.height(32.dp))

            Text(
                text = stringResource(Res.string.login_title),
                style = MaterialTheme.typography.displayLarge,
                color = MediAccessColors.TextPrimary,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = stringResource(Res.string.login_subtitle),
                style = MaterialTheme.typography.bodyMedium,
                color = MediAccessColors.Secondary
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Username/Email Field
            Column {
                Text(
                    text = stringResource(Res.string.username_label),
                    style = MaterialTheme.typography.labelLarge,
                    color = MediAccessColors.TextPrimary
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = state.email,
                    onValueChange = { viewModel.onAction(LoginAction.OnEmailChange(it)) },
                    placeholder = { Text(stringResource(Res.string.username_hint), color = MediAccessColors.TextHint) },
                    leadingIcon = {
                        Icon(Icons.Default.Person, contentDescription = null)
                    },
                    isError = state.emailError != null,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Email,
                        imeAction = ImeAction.Next
                    ),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MediAccessColors.Primary,
                        unfocusedBorderColor = MediAccessColors.SurfaceVariant,
                        // FIX 2: Force text to be visible
                        focusedTextColor = MediAccessColors.TextPrimary,
                        unfocusedTextColor = MediAccessColors.TextPrimary,
                        cursorColor = MediAccessColors.Primary
                    ),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                if (state.emailError != null) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = state.emailError!!.asString(),
                        color = MediAccessColors.Error,
                        style = MaterialTheme.typography.bodySmall
                    )
                } else {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        Text(
                            text = stringResource(Res.string.forgot_username),
                            style = MaterialTheme.typography.bodySmall,
                            color = MediAccessColors.Secondary,
                            modifier = Modifier
                                .padding(top = 4.dp)
                                .clickable { /* TODO: Implement forgot username */ }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Password Field
            Column {
                Text(
                    text = stringResource(Res.string.password_hint),
                    style = MaterialTheme.typography.labelLarge,
                    color = MediAccessColors.TextPrimary
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = state.password,
                    onValueChange = { viewModel.onAction(LoginAction.OnPasswordChange(it)) },
                    placeholder = { Text(stringResource(Res.string.password_hint), color = MediAccessColors.TextHint) },
                    leadingIcon = {
                        Icon(Icons.Default.Lock, contentDescription = null)
                    },
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
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Password,
                        imeAction = ImeAction.Done
                    ),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MediAccessColors.Primary,
                        unfocusedBorderColor = MediAccessColors.SurfaceVariant,
                        // FIX 2: Force text to be visible
                        focusedTextColor = MediAccessColors.TextPrimary,
                        unfocusedTextColor = MediAccessColors.TextPrimary,
                        cursorColor = MediAccessColors.Primary
                    ),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                if (state.passwordError != null) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = state.passwordError!!.asString(),
                        color = MediAccessColors.Error,
                        style = MaterialTheme.typography.bodySmall
                    )
                } else {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        Text(
                            text = stringResource(Res.string.forgot_password),
                            style = MaterialTheme.typography.bodySmall,
                            color = MediAccessColors.Secondary,
                            modifier = Modifier
                                .padding(top = 4.dp)
                                .clickable { /* TODO: Implement forgot password */ }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Remember Me
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Checkbox(
                    checked = rememberMe,
                    onCheckedChange = { rememberMe = it },
                    colors = CheckboxDefaults.colors(
                        checkedColor = MediAccessColors.Primary
                    )
                )
                Text(
                    text = stringResource(Res.string.remember_me),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MediAccessColors.TextPrimary
                )
            }

            // General Error
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

            // Login Button
            Button(
                onClick = { viewModel.onAction(LoginAction.OnLoginClick) },
                enabled = !state.isLoading,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MediAccessColors.Secondary
                )
            ) {
                if (state.isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = Color.White
                    )
                } else {
                    Text(
                        text = stringResource(Res.string.login_button),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // OR Divider
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                HorizontalDivider(modifier = Modifier.weight(1f), color = MediAccessColors.SurfaceVariant)
                Text(
                    text = stringResource(Res.string.or_divider),
                    modifier = Modifier.padding(horizontal = 16.dp),
                    color = MediAccessColors.TextSecondary,
                    style = MaterialTheme.typography.bodyMedium
                )
                HorizontalDivider(modifier = Modifier.weight(1f), color = MediAccessColors.SurfaceVariant)
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Login with Phone
            OutlinedButton(
                onClick = { /* TODO: Phone login */ },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = MediAccessColors.TextPrimary
                )
            ) {
                Icon(Icons.Default.Phone, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = stringResource(Res.string.login_phone),
                    style = MaterialTheme.typography.titleMedium
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Google Sign-In
            OutlinedButton(
                onClick = { launchGoogleSignIn() },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = MediAccessColors.TextPrimary
                )
            ) {
                Text(
                    text = "G",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(end = 8.dp)
                )
                Text(
                    text = stringResource(Res.string.login_google),
                    style = MaterialTheme.typography.titleMedium
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Register Link
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
                Text(
                    text = stringResource(Res.string.no_account_prompt),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MediAccessColors.TextSecondary
                )
                Text(
                    text = stringResource(Res.string.register_now),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MediAccessColors.Secondary,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.clickable(onClick = onRegisterClick)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}