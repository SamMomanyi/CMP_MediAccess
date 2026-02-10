package org.sammomanyi.mediaccess.features.identity.presentation.home

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.koin.compose.viewmodel.koinViewModel
import org.sammomanyi.mediaccess.core.presentation.theme.MediAccessColors

@Composable
fun HomeScreen(
    padding: PaddingValues,
    onNavigateToHospitals: () -> Unit,
    viewModel: HomeViewModel = koinViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(padding)
            .background(MediAccessColors.Background)
    ) {
        // Header with greeting
        item {
            HomeHeader(userName = state.user?.firstName ?: "User")
        }

        // My Covers Section
        item {
            MyCoversSection()
        }

        // Quick Actions Button
        item {
            QuickActionsButton()
        }

        // Wellness Section
        item {
            WellnessSection()
        }

        // Care Section
        item {
            CareSection(
                onNavigateToHospitals = onNavigateToHospitals
            )
        }

        // Articles Section
        item {
            ArticlesSection()
        }

        item {
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun HomeHeader(userName: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White)
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Logo
        Row(verticalAlignment = Alignment.CenterVertically) {
            Surface(
                modifier = Modifier.size(40.dp),
                shape = CircleShape,
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
                fontWeight = FontWeight.Bold,
                lineHeight = MaterialTheme.typography.titleSmall.lineHeight
            )
        }

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            IconButton(onClick = { /* Notifications */ }) {
                Icon(
                    Icons.Default.Notifications,
                    contentDescription = "Notifications",
                    tint = MediAccessColors.TextSecondary
                )
            }
            IconButton(onClick = { /* Profile */ }) {
                Surface(
                    modifier = Modifier.size(32.dp),
                    shape = CircleShape,
                    color = MediAccessColors.SurfaceVariant
                ) {
                    Icon(
                        Icons.Default.Person,
                        contentDescription = "Profile",
                        tint = MediAccessColors.TextSecondary,
                        modifier = Modifier.padding(6.dp)
                    )
                }
            }
        }
    }

    // Greeting
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        shape = RoundedCornerShape(12.dp),
        color = MediAccessColors.PrimaryLight.copy(alpha = 0.1f)
    ) {
        Text(
            text = "Hi $userName",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.padding(12.dp)
        )
    }
}

@Composable
private fun MyCoversSection() {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Default.Favorite,
                    contentDescription = null,
                    modifier = Modifier.size(24.dp),
                    tint = MediAccessColors.Primary
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "My Covers",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Illustration placeholder
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = "Convenience",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "at your fingertips",
                        style = MaterialTheme.typography.bodySmall,
                        color = MediAccessColors.TextSecondary
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Illustration placeholder
                    Box(
                        modifier = Modifier
                            .size(100.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(MediAccessColors.PrimaryLight.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.Chair,
                            contentDescription = null,
                            modifier = Modifier.size(48.dp),
                            tint = MediAccessColors.Primary
                        )
                    }
                }

                Spacer(modifier = Modifier.width(16.dp))

                // Cover card
                Column(
                    modifier = Modifier.weight(1f),
                    horizontalAlignment = Alignment.End
                ) {
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(80.dp),
                        shape = RoundedCornerShape(12.dp),
                        color = MediAccessColors.TextPrimary
                    ) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier.padding(8.dp)
                        ) {
                            Text(
                                text = "Cover Card",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.White
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Button(
                        onClick = { /* Link Cover */ },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MediAccessColors.Secondary
                        ),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Link Cover", style = MaterialTheme.typography.labelMedium)
                    }
                }
            }
        }
    }
}

@Composable
private fun QuickActionsButton() {
    Button(
        onClick = { /* Quick Actions */ },
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .height(50.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = MediAccessColors.Secondary
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Text(
            text = "Quick Actions",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
private fun WellnessSection() {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Default.FitnessCenter,
                    contentDescription = null,
                    modifier = Modifier.size(24.dp),
                    tint = MediAccessColors.Primary
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Wellness",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Lets do it",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "one step at a\ntime",
                        style = MaterialTheme.typography.bodySmall,
                        color = MediAccessColors.TextSecondary
                    )
                }

                // Exercise illustration placeholder
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(MediAccessColors.PrimaryLight.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.DirectionsRun,
                        contentDescription = null,
                        modifier = Modifier.size(40.dp),
                        tint = MediAccessColors.Primary
                    )
                }

                OutlinedButton(
                    onClick = { /* Wellness action */ },
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = MediAccessColors.Secondary
                    )
                ) {
                    Text("LETS DO THIS", style = MaterialTheme.typography.labelMedium)
                }
            }
        }
    }
}

@Composable
private fun CareSection(onNavigateToHospitals: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Default.MedicalServices,
                    contentDescription = null,
                    modifier = Modifier.size(24.dp),
                    tint = MediAccessColors.Primary
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Care",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Access Healthcare\nseamlessly",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Care Actions Grid
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                CareActionCard(
                    icon = Icons.Default.LocalHospital,
                    label = "VISIT",
                    modifier = Modifier.weight(1f),
                    onClick = { }
                )
                CareActionCard(
                    icon = Icons.Default.CardGiftcard,
                    label = "BENEFITS",
                    modifier = Modifier.weight(1f),
                    onClick = { }
                )
                CareActionCard(
                    icon = Icons.Default.Payment,
                    label = "SPENT",
                    modifier = Modifier.weight(1f),
                    onClick = { }
                )
                CareActionCard(
                    icon = Icons.Default.Business,
                    label = "HOSPITAL",
                    modifier = Modifier.weight(1f),
                    onClick = onNavigateToHospitals
                )
            }
        }
    }
}

@Composable
private fun CareActionCard(
    icon: ImageVector,
    label: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        modifier = modifier.aspectRatio(1f),
        shape = RoundedCornerShape(12.dp),
        color = MediAccessColors.SurfaceVariant
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Surface(
                modifier = Modifier.size(48.dp),
                shape = CircleShape,
                color = Color.White
            ) {
                Icon(
                    icon,
                    contentDescription = label,
                    modifier = Modifier.padding(12.dp),
                    tint = MediAccessColors.Primary
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = MediAccessColors.TextPrimary
            )
        }
    }
}

@Composable
private fun ArticlesSection() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(bottom = 12.dp)
        ) {
            Icon(
                Icons.Default.Article,
                contentDescription = null,
                modifier = Modifier.size(24.dp),
                tint = MediAccessColors.Primary
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Articles",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        }

        // Topic Filter
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = MediAccessColors.Secondary,
            modifier = Modifier.padding(bottom = 12.dp)
        ) {
            Text(
                text = "skin",
                style = MaterialTheme.typography.labelMedium,
                color = Color.White,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )
        }

        // Article Cards
        ArticleCard(
            title = "This new Microneedling technique made me look 5 years young...",
            date = "2023-1-12"
        )

        Spacer(modifier = Modifier.height(12.dp))

        ArticleCard(
            title = "12 Natural Remedies For Dry Skin in Winter",
            date = "2023-1-13"
        )
    }
}

@Composable
private fun ArticleCard(title: String, date: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column {
            // Image placeholder
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp)
                    .background(MediAccessColors.SurfaceVariant),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.Image,
                    contentDescription = null,
                    modifier = Modifier.size(48.dp),
                    tint = MediAccessColors.TextSecondary
                )
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.weight(1f),
                    maxLines = 2
                )
                Column(horizontalAlignment = Alignment.End) {
                    Icon(
                        Icons.Default.RssFeed,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp),
                        tint = MediAccessColors.Secondary
                    )
                    Text(
                        text = date,
                        style = MaterialTheme.typography.labelSmall,
                        color = MediAccessColors.TextSecondary
                    )
                }
            }
        }
    }
}