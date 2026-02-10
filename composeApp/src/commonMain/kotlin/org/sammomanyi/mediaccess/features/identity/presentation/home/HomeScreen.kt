package org.sammomanyi.mediaccess.features.identity.presentation.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.pulltorefresh.*
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
import coil3.compose.AsyncImage
import org.koin.compose.viewmodel.koinViewModel
import org.sammomanyi.mediaccess.core.presentation.theme.MediAccessColors
import org.sammomanyi.mediaccess.features.identity.domain.model.Article
import org.sammomanyi.mediaccess.features.identity.presentation.home.dialogs.*


@Composable
fun HomeScreen(
    padding: PaddingValues,
    onNavigateToHospitals: () -> Unit,
    onNavigateToBenefits: () -> Unit,
    onNavigateToSpent: () -> Unit,
    onNavigateToNotifications: () -> Unit,
    onNavigateToProfile: () -> Unit,
    viewModel: HomeViewModel = koinViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    // Dialog states
    var showBenefitsDialog by remember { mutableStateOf(false) }
    var showSpentDialog by remember { mutableStateOf(false) }
    var showQuickActionsDialog by remember { mutableStateOf(false) }
    var showWellnessDialog by remember { mutableStateOf(false) }
    var showLinkCoverDialog by remember { mutableStateOf(false) }

    // Pull to refresh
    val pullToRefreshState = rememberPullToRefreshState()

    PullToRefreshBox(
        isRefreshing = state.isRefreshing,
        onRefresh = { viewModel.onAction(HomeAction.OnRefresh) },
        state = pullToRefreshState,
        modifier = Modifier.fillMaxSize()
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(Color(0xFFF5F5F5))
        ) {
            item {
                HomeHeader(
                    userName = state.user?.firstName ?: "User",
                    onNotificationsClick = onNavigateToNotifications,
                    onProfileClick = onNavigateToProfile
                )
            }

            item {
                MyCoversSection(
                    onLinkCoverClick = { showLinkCoverDialog = true }
                )
            }

            item {
                QuickActionsButton(
                    onClick = { showQuickActionsDialog = true }
                )
            }

            item {
                WellnessSection(
                    onLetsDoItClick = { showWellnessDialog = true }
                )
            }

            item {
                CareSection(
                    onVisitClick = { },
                    onBenefitsClick = { showBenefitsDialog = true },
                    onSpentClick = { showSpentDialog = true },
                    onHospitalClick = onNavigateToHospitals
                )
            }

            item {
                ArticlesSection(
                    articles = state.articles,
                    isLoading = state.isLoadingNews
                )
            }

            item {
                Spacer(modifier = Modifier.height(24.dp))
            }
        }

    }

    // All Dialogs
    if (showBenefitsDialog) {
        BenefitsDialog(onDismiss = { showBenefitsDialog = false })
    }

    if (showSpentDialog) {
        SpentDialog(onDismiss = { showSpentDialog = false })
    }

    if (showQuickActionsDialog) {
        QuickActionsDialog(
            onDismiss = { showQuickActionsDialog = false },
            onSearchClick = onNavigateToHospitals
        )
    }

    if (showWellnessDialog) {
        WellnessDialog(onDismiss = { showWellnessDialog = false })
    }

    if (showLinkCoverDialog) {
        LinkCoverDialog(
            userEmail = state.user?.email ?: "",
            onDismiss = { showLinkCoverDialog = false }
        )
    }
}

@Composable
private fun HomeHeader(
    userName: String,
    onNotificationsClick: () -> Unit,
    onProfileClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White)
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Surface(
                modifier = Modifier.size(40.dp),
                shape = CircleShape,
                color = MediAccessColors.Primary
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = userName.take(2).uppercase(),
                        style = MaterialTheme.typography.titleSmall,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "MediAccess",
                style = MaterialTheme.typography.titleMedium,
                color = MediAccessColors.Secondary,
                fontWeight = FontWeight.Bold
            )
        }

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            IconButton(onClick = onNotificationsClick) {
                Icon(
                    Icons.Default.Notifications,
                    contentDescription = "Notifications",
                    tint = MediAccessColors.TextSecondary
                )
            }
            IconButton(onClick = onProfileClick) {
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
private fun MyCoversSection(onLinkCoverClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
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
                Column(modifier = Modifier.weight(1f)) {
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

                    Box(
                        modifier = Modifier
                            .size(100.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(MediAccessColors.PrimaryLight.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.EventSeat,
                            contentDescription = null,
                            modifier = Modifier.size(48.dp),
                            tint = MediAccessColors.Primary
                        )
                    }
                }

                Spacer(modifier = Modifier.width(16.dp))

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
                        onClick = onLinkCoverClick,
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
private fun QuickActionsButton(onClick: () -> Unit) {
    Button(
        onClick = onClick,
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
private fun WellnessSection(onLetsDoItClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
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
                    onClick = onLetsDoItClick,
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
private fun CareSection(
    onVisitClick: () -> Unit,
    onBenefitsClick: () -> Unit,
    onSpentClick: () -> Unit,
    onHospitalClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
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

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                CareActionCard(
                    icon = Icons.Default.LocalHospital,
                    label = "VISIT",
                    modifier = Modifier.weight(1f),
                    onClick = onVisitClick
                )
                CareActionCard(
                    icon = Icons.Default.CardGiftcard,
                    label = "BENEFITS",
                    modifier = Modifier.weight(1f),
                    onClick = onBenefitsClick
                )
                CareActionCard(
                    icon = Icons.Default.Payment,
                    label = "SPENT",
                    modifier = Modifier.weight(1f),
                    onClick = onSpentClick
                )
                CareActionCard(
                    icon = Icons.Default.Business,
                    label = "HOSPITAL",
                    modifier = Modifier.weight(1f),
                    onClick = onHospitalClick
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
            modifier = Modifier.padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Surface(
                modifier = Modifier.size(40.dp),
                shape = CircleShape,
                color = Color.White
            ) {
                Icon(
                    icon,
                    contentDescription = label,
                    modifier = Modifier.padding(8.dp),
                    tint = MediAccessColors.Primary
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
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
private fun ArticlesSection(
    articles: List<Article>,
    isLoading: Boolean
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
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

        Spacer(modifier = Modifier.height(16.dp))

        when {
            isLoading -> {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(100.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
            articles.isEmpty() -> {
                Text(
                    text = "No articles available",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MediAccessColors.TextSecondary
                )
            }
            else -> {
                articles.forEach { article ->
                    ArticleCard(article)
                    Spacer(modifier = Modifier.height(12.dp))
                }
            }
        }
    }
}

@Composable
private fun ArticleCard(article: Article) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column {
            AsyncImage(
                model = article.imageUrl,
                contentDescription = article.title,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(160.dp),
                contentScale = ContentScale.Crop
            )

            Column(modifier = Modifier.padding(12.dp)) {
                Text(
                    text = article.title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    maxLines = 2
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.RssFeed,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = MediAccessColors.Secondary
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = article.date,
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.Gray
                        )
                    }
                }
            }
        }
    }
}