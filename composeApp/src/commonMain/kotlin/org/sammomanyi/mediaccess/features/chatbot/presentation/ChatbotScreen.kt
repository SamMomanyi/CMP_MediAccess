package org.sammomanyi.mediaccess.features.chatbot.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import org.koin.compose.viewmodel.koinViewModel

// ── Specialty model ───────────────────────────────────────────────────────────

data class MedicalSpecialty(
    val label: String,
    val displayName: String,
    val systemPrompt: String
)

val specialties = listOf(
    MedicalSpecialty(
        label = "General",
        displayName = "General Health",
        systemPrompt = "You are 'MediBot', a helpful, empathetic general health assistant inside the MediAccess app. Answer health questions clearly and briefly. Always remind the user you are an AI and they should consult a doctor."
    ),
    MedicalSpecialty(
        label = "Cardiology",
        displayName = "Cardiology",
        systemPrompt = "You are 'MediBot', a cardiology-focused health assistant inside the MediAccess app. You specialise in heart health, cardiovascular conditions, and related lifestyle advice. Always remind the user you are an AI and they should consult a cardiologist for diagnosis."
    ),
    MedicalSpecialty(
        label = "Pediatrics",
        displayName = "Pediatrics",
        systemPrompt = "You are 'MediBot', a pediatrics-focused health assistant inside the MediAccess app. You specialise in child health, development milestones, and common childhood illnesses. Always remind the user you are an AI and they should consult a pediatrician."
    ),
    MedicalSpecialty(
        label = "Mental Health",
        displayName = "Mental Health",
        systemPrompt = "You are 'MediBot', a mental health-focused assistant inside the MediAccess app. You provide supportive, empathetic guidance on stress, anxiety, depression and wellbeing. Always remind the user you are an AI and encourage them to seek professional mental health support."
    ),
    MedicalSpecialty(
        label = "Nutrition",
        displayName = "Nutrition",
        systemPrompt = "You are 'MediBot', a nutrition and diet-focused health assistant inside the MediAccess app. You specialise in healthy eating, dietary advice, and nutritional guidance. Always remind the user you are an AI and they should consult a registered dietitian."
    ),
    MedicalSpecialty(
        label = "Dermatology",
        displayName = "Dermatology",
        systemPrompt = "You are 'MediBot', a dermatology-focused health assistant inside the MediAccess app. You specialise in skin health, common skin conditions, and skincare advice. Always remind the user you are an AI and they should consult a dermatologist for diagnosis."
    )
)

// ── Colors ────────────────────────────────────────────────────────────────────

private val GreenPrimary = Color(0xFF1D9E75)
private val GreenDark = Color(0xFF0F6E56)
private val GreenMid = Color(0xFF9FE1CB)
private val GreenLight = Color(0xFFE1F5EE)

// Light mode
private val LightBg = Color(0xFFF0F4F3)
private val LightSurface = Color.White
private val LightBotBubble = Color.White
private val LightTextPrimary = Color(0xFF1A1A1A)
private val LightTextSecondary = Color(0xFF666666)
private val LightInputBg = Color(0xFFF8F8F8)
private val LightBorder = Color(0xFFDDDDDD)

// Dark mode
private val DarkBg = Color(0xFF0D1F18)
private val DarkSurface = Color(0xFF0A2E22)
private val DarkBotBubble = Color(0xFF1A3528)
private val DarkTextPrimary = Color(0xFFD4EDE3)
private val DarkTextSecondary = Color(0xFF5DCAA5)
private val DarkInputBg = Color(0xFF0A2E22)
private val DarkBorder = Color(0xFF2A5040)
private val DarkHeader = Color(0xFF0A2E22)

// ── Screen ────────────────────────────────────────────────────────────────────

@Composable
fun ChatbotScreen(
    onBack: () -> Unit,
    viewModel: ChatbotViewModel = koinViewModel()
) {
    val messages by viewModel.messages.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    var inputText by remember { mutableStateOf("") }
    var isDark by remember { mutableStateOf(false) }
    var selectedSpecialty by remember { mutableStateOf(specialties.first()) }
    val listState = rememberLazyListState()
    val scope = rememberCoroutineScope()

    // Derived colors based on mode
    val bg = if (isDark) DarkBg else LightBg
    val surface = if (isDark) DarkSurface else LightSurface
    val textPrimary = if (isDark) DarkTextPrimary else LightTextPrimary
    val textSecondary = if (isDark) DarkTextSecondary else LightTextSecondary
    val inputBg = if (isDark) DarkInputBg else LightInputBg
    val border = if (isDark) DarkBorder else LightBorder
    val bannerBg = if (isDark) Color(0xFF0D2B1E) else GreenLight
    val bannerText = if (isDark) GreenMid else GreenDark
    val chipBg = if (isDark) Color(0xFF0D2B1E) else GreenLight
    val chipText = if (isDark) GreenMid else Color(0xFF085041)
    val chipBorder = if (isDark) GreenDark else GreenMid

    LaunchedEffect(messages.size, isLoading) {
        if (messages.isNotEmpty()) listState.animateScrollToItem(messages.size - 1)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .imePadding()
            .background(bg)
    ) {

        // ── Header ────────────────────────────────────────────────────────────
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    if (isDark) SolidColor(DarkHeader) // ✨ FIX: Wrap in SolidColor()
                    else Brush.linearGradient(listOf(GreenPrimary, GreenDark))
                )
                .statusBarsPadding()
                .padding(horizontal = 12.dp, vertical = 12.dp)
        ) {
            Column {
                // Top row
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    // Back
                    IconButton(onClick = onBack, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White, modifier = Modifier.size(18.dp))
                    }

                    Spacer(Modifier.width(6.dp))

                    // Avatar
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(if (isDark) GreenPrimary else Color.White.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.SmartToy, contentDescription = null, tint = Color.White, modifier = Modifier.size(22.dp))
                    }

                    Spacer(Modifier.width(10.dp))

                    // Name + status
                    Column(modifier = Modifier.weight(1f)) {
                        Text("MediBot", fontWeight = FontWeight.SemiBold, color = Color.White, fontSize = 15.sp)
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(modifier = Modifier.size(5.dp).clip(CircleShape).background(GreenMid))
                            Spacer(Modifier.width(4.dp))
                            Text(
                                "${selectedSpecialty.displayName} · Online",
                                color = Color.White.copy(alpha = 0.75f),
                                fontSize = 10.sp
                            )
                        }
                    }

                    // Dark mode toggle
                    IconButton(
                        onClick = { isDark = !isDark },
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.15f))
                    ) {
                        Icon(
                            if (isDark) Icons.Default.LightMode else Icons.Default.DarkMode,
                            contentDescription = "Toggle theme",
                            tint = Color.White,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }

                Spacer(Modifier.height(10.dp))

                // Specialty chips row
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    specialties.forEach { specialty ->
                        val isSelected = specialty == selectedSpecialty
                        Surface(
                            onClick = {
                                selectedSpecialty = specialty
                                println("🔵 ChatbotScreen: Specialty changed to ${specialty.displayName}")
                            },
                            shape = RoundedCornerShape(20.dp),
                            color = if (isSelected) Color.White else Color.White.copy(alpha = 0.12f),
                            border = ButtonDefaults.outlinedButtonBorder.copy(
                                width = 0.5.dp
                            )
                        ) {
                            Text(
                                text = specialty.label,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                fontSize = 10.sp,
                                fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                                color = if (isSelected) GreenDark else Color.White.copy(alpha = 0.8f)
                            )
                        }
                    }
                }
            }
        }

        // ── Disclaimer banner ─────────────────────────────────────────────────
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(bannerBg)
                .padding(horizontal = 14.dp, vertical = 7.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Outlined.Warning, contentDescription = null, tint = bannerText, modifier = Modifier.size(12.dp))
            Spacer(Modifier.width(6.dp))
            Text(
                "AI assistant — always consult your doctor for medical decisions.",
                fontSize = 10.sp, color = bannerText, lineHeight = 14.sp
            )
        }

        HorizontalDivider(color = if (isDark) GreenDark else GreenMid, thickness = 0.5.dp)

        // ── Messages ──────────────────────────────────────────────────────────
        LazyColumn(
            state = listState,
            modifier = Modifier.weight(1f).fillMaxWidth(),
            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(messages) { msg ->
                ChatBubble(
                    message = msg,
                    isDark = isDark,
                    surface = if (isDark) DarkBotBubble else LightBotBubble,
                    textPrimary = textPrimary,
                    border = border
                )
            }

            if (isLoading) {
                item {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        BotAvatarSmall()
                        Spacer(Modifier.width(8.dp))
                        TypingIndicator(isDark = isDark, surface = if (isDark) DarkBotBubble else LightBotBubble)
                    }
                }
            }
        }

        // ── Suggestion chips ──────────────────────────────────────────────────
        if (messages.size <= 1) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(bg)
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf("What medications help?", "How to prevent this?", "Is this serious?", "Find a doctor")
                    .forEach { chip ->
                        Surface(
                            onClick = {
                                println("🔵 ChatbotScreen: Chip tapped: \"$chip\"")
                                viewModel.sendMessage(chip, selectedSpecialty.systemPrompt)
                            },
                            shape = RoundedCornerShape(20.dp),
                            color = chipBg,
                            border = ButtonDefaults.outlinedButtonBorder
                        ) {
                            Text(
                                text = chip,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                fontSize = 11.sp, color = chipText, fontWeight = FontWeight.Medium
                            )
                        }
                    }
            }
        }

        // ── Input area ────────────────────────────────────────────────────────
        Surface(color = surface, shadowElevation = 8.dp, tonalElevation = 0.dp) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 10.dp)
                    .navigationBarsPadding(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = inputText,
                    onValueChange = { inputText = it },
                    modifier = Modifier.weight(1f),
                    placeholder = { Text("Ask a health question...", fontSize = 13.sp) },
                    shape = RoundedCornerShape(24.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = textPrimary,
                        unfocusedTextColor = textPrimary,
                        focusedBorderColor = GreenPrimary,
                        unfocusedBorderColor = border,
                        focusedContainerColor = inputBg,
                        unfocusedContainerColor = inputBg,
                        focusedPlaceholderColor = textSecondary,
                        unfocusedPlaceholderColor = textSecondary
                    ),
                    singleLine = false,
                    maxLines = 4,
                    textStyle = LocalTextStyle.current.copy(fontSize = 13.sp)
                )

                Spacer(Modifier.width(8.dp))

                val canSend = inputText.isNotBlank() && !isLoading
                IconButton(
                    onClick = {
                        if (canSend) {
                            println("🔵 ChatbotScreen: Send tapped, message = \"$inputText\", specialty = ${selectedSpecialty.displayName}")
                            val msg = inputText
                            scope.launch {
                                viewModel.sendMessage(msg, selectedSpecialty.systemPrompt)
                                inputText = ""
                            }
                        }
                    },
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(if (canSend) GreenPrimary else Color(0xFFCCCCCC))
                ) {
                    Icon(Icons.Default.Send, contentDescription = "Send", tint = Color.White, modifier = Modifier.size(18.dp))
                }
            }
        }
    }
}

// ── Sub-composables ───────────────────────────────────────────────────────────

@Composable
private fun ChatBubble(
    message: ChatMessage,
    isDark: Boolean,
    surface: Color,
    textPrimary: Color,
    border: Color
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (message.isUser) Arrangement.End else Arrangement.Start,
        verticalAlignment = Alignment.Bottom
    ) {
        if (!message.isUser) {
            BotAvatarSmall()
            Spacer(Modifier.width(8.dp))
        }

        Column(horizontalAlignment = if (message.isUser) Alignment.End else Alignment.Start) {
            if (message.isUser) {
                Box(
                    modifier = Modifier
                        .widthIn(max = 260.dp)
                        .clip(RoundedCornerShape(topStart = 18.dp, topEnd = 18.dp, bottomStart = 18.dp, bottomEnd = 4.dp))
                        .background(Brush.linearGradient(listOf(GreenPrimary, GreenDark)))
                        .padding(horizontal = 13.dp, vertical = 10.dp)
                ) {
                    Text(text = message.text, color = Color.White, fontSize = 13.sp, lineHeight = 19.sp)
                }
            } else {
                Surface(
                    shape = RoundedCornerShape(topStart = 18.dp, topEnd = 18.dp, bottomStart = 4.dp, bottomEnd = 18.dp),
                    color = surface,
                    shadowElevation = if (isDark) 0.dp else 1.dp,
                    border = ButtonDefaults.outlinedButtonBorder
                ) {
                    Text(
                        text = message.text,
                        modifier = Modifier.widthIn(max = 260.dp).padding(horizontal = 13.dp, vertical = 10.dp),
                        color = textPrimary,
                        fontSize = 13.sp,
                        lineHeight = 19.sp
                    )
                }
                Spacer(Modifier.height(2.dp))
                Text(
                    "⚠ AI only — see a real doctor for diagnosis.",
                    fontSize = 9.sp,
                    color = if (isDark) Color(0xFF5DCAA5) else GreenDark,
                    fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                    modifier = Modifier.padding(start = 4.dp)
                )
            }
        }

        if (message.isUser) Spacer(Modifier.width(4.dp))
    }
}

@Composable
private fun BotAvatarSmall() {
    Box(
        modifier = Modifier.size(28.dp).clip(CircleShape).background(GreenPrimary),
        contentAlignment = Alignment.Center
    ) {
        Icon(Icons.Default.SmartToy, contentDescription = null, tint = Color.White, modifier = Modifier.size(14.dp))
    }
}

@Composable
private fun TypingIndicator(isDark: Boolean, surface: Color) {
    Surface(
        shape = RoundedCornerShape(topStart = 18.dp, topEnd = 18.dp, bottomStart = 4.dp, bottomEnd = 18.dp),
        color = surface,
        shadowElevation = if (isDark) 0.dp else 1.dp
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            repeat(3) {
                CircularProgressIndicator(modifier = Modifier.size(6.dp), color = GreenPrimary, strokeWidth = 1.5.dp)
            }
        }
    }
}