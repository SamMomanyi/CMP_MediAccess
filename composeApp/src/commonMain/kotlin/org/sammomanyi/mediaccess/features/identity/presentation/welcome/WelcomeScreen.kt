package org.sammomanyi.mediaccess.features.identity.presentation.welcome

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import mediaccess.composeapp.generated.resources.Res
import mediaccess.composeapp.generated.resources.hero_health
import mediaccess.composeapp.generated.resources.logo
import mediaccess.composeapp.generated.resources.welcome_logo_content_description
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import org.sammomanyi.mediaccess.core.presentation.theme.MediAccessColors

@Composable
fun WelcomeScreen(
    onLoginClick: () -> Unit,
    onRegisterClick: () -> Unit
) {
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
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(60.dp))

            // Logo
            // TODO: Add your logo here
            Image(
                painter = painterResource(Res.drawable.logo),
                contentDescription = stringResource(Res.string.welcome_logo_content_description),
                modifier = Modifier
                    .size(100.dp)
                    .clip(RoundedCornerShape(20.dp)),
                contentScale = ContentScale.Fit
            )


            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "MediAccess",
                style = MaterialTheme.typography.displayLarge,
                color = MediAccessColors.TextPrimary,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Manage Your Health,\nThe Smart Way",
                style = MaterialTheme.typography.titleLarge,
                color = MediAccessColors.Primary,
                textAlign = TextAlign.Center,
                fontWeight = FontWeight.Medium
            )

            Spacer(modifier = Modifier.height(40.dp))

            Image(
                painter = painterResource(Res.drawable.hero_health),
                contentDescription = null,
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f) // <--- CHANGED: This makes the image fill the empty space
                    .clip(RoundedCornerShape(16.dp)),
                contentScale = ContentScale.Crop // Ensures the image fills the area without distortion
            )


            Spacer(modifier = Modifier.height(32.dp)) // Add a small fixed spacer for breathing room

            Text(
                text = "No more waiting",
                style = MaterialTheme.typography.headlineMedium,
                color = MediAccessColors.Secondary,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Login Button
            Button(
                onClick = onLoginClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MediAccessColors.Primary
                )
            ) {
                Text(
                    text = "Log In",
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.White,
                    fontWeight = FontWeight.SemiBold
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Register Button
            OutlinedButton(
                onClick = onRegisterClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = MediAccessColors.Primary
                )
            ) {
                Text(
                    text = "Register",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Footer
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "Powered by ",
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