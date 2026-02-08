package org.sammomanyi.mediaccess.features.identity.presentation.registration

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import org.koin.compose.viewmodel.koinViewModel
import org.sammomanyi.mediaccess.core.presentation.asString

@Composable
fun RegistrationScreen(
    onLoginClick: () -> Unit,
    onSuccess: () -> Unit,
    viewModel: RegistrationViewModel = koinViewModel()
) {
    val state by viewModel.state.collectAsState()
    var passwordVisible by remember { mutableStateOf(false) }

    LaunchedEffect(state.isSuccess) {
        if (state.isSuccess) onSuccess()
    }

    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Create Account",
            style = MaterialTheme.typography.headlineLarge,
            color = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.height(32.dp))

        // First Name
        OutlinedTextField(
            value = state.firstName,
            onValueChange = { viewModel.onAction(RegistrationAction.OnFirstNameChange(it)) },
            label = { Text("First Name") },
            isError = state.firstNameError != null,
            supportingText = {
                state.firstNameError?.let {
                    Text(it.asString(), color = MaterialTheme.colorScheme.error)
                }
            },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Last Name
        OutlinedTextField(
            value = state.lastName,
            onValueChange = { viewModel.onAction(RegistrationAction.OnLastNameChange(it)) },
            label = { Text("Last Name") },
            isError = state.lastNameError != null,
            supportingText = {
                state.lastNameError?.let {
                    Text(it.asString(), color = MaterialTheme.colorScheme.error)
                }
            },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Email
        OutlinedTextField(
            value = state.email,
            onValueChange = { viewModel.onAction(RegistrationAction.OnEmailChange(it)) },
            label = { Text("Email") },
            isError = state.emailError != null,
            supportingText = {
                state.emailError?.let {
                    Text(it.asString(), color = MaterialTheme.colorScheme.error)
                }
            },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Phone Number - THIS WAS MISSING!
        OutlinedTextField(
            value = state.phoneNumber,
            onValueChange = { viewModel.onAction(RegistrationAction.OnPhoneNumberChange(it)) },
            label = { Text("Phone Number") },
            isError = state.phoneError != null,
            supportingText = {
                state.phoneError?.let {
                    Text(it.asString(), color = MaterialTheme.colorScheme.error)
                }
            },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Password
        OutlinedTextField(
            value = state.password,
            onValueChange = { viewModel.onAction(RegistrationAction.OnPasswordChange(it)) },
            label = { Text("Password") },
            isError = state.passwordError != null,
            supportingText = {
                state.passwordError?.let {
                    Text(it.asString(), color = MaterialTheme.colorScheme.error)
                }
            },
            visualTransformation = if (passwordVisible) {
                VisualTransformation.None
            } else {
                PasswordVisualTransformation()
            },
            trailingIcon = {
                IconButton(onClick = { passwordVisible = !passwordVisible }) {
                    Icon(
                        imageVector = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                        contentDescription = if (passwordVisible) "Hide password" else "Show password"
                    )
                }
            },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        // General Error Message
        state.errorMessage?.let { error ->
            Spacer(modifier = Modifier.height(8.dp))
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = error.asString(),
                    color = MaterialTheme.colorScheme.onErrorContainer,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(12.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Register Button
        Button(
            onClick = { viewModel.onAction(RegistrationAction.OnRegisterClick) },
            enabled = !state.isLoading,
            modifier = Modifier.fillMaxWidth().height(50.dp)
        ) {
            if (state.isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    color = MaterialTheme.colorScheme.onPrimary
                )
            } else {
                Text("Sign Up")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Login Link
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("Already have an account? ", style = MaterialTheme.typography.bodyMedium)
            TextButton(onClick = onLoginClick) {
                Text("Sign In")
            }
        }
    }
}