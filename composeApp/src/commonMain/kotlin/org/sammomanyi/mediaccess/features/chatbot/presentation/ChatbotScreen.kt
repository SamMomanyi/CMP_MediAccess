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
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.SmartToy
import androidx.compose.material.icons.outlined.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import org.koin.compose.viewmodel.koinViewModel

private val MediBotGreen = Color(0xFF1D9E75)
private val MediBotGreenDark = Color(0xFF0F6E56)
private val MediBotGreenLight = Color(0xFFE1F5EE)
private val MediBotGreenMid = Color(0xFF9FE1CB)

// Quick suggestion chips shown below messages
private val suggestionChips = listOf(
    "What medications help?",
    "How to prevent this?",
    "Is this serious?",
    "Find a doctor"
)

@Composable
fun ChatbotScreen(
    onBack: () -> Unit,
    viewModel: ChatbotViewModel = koinViewModel()
) {
    val messages by viewModel.messages.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    var inputText by remember { mutableStateOf("") }
    val listState = rememberLazyListState()
    val scope = rememberCoroutineScope()

    // Auto-scroll on new message
    LaunchedEffect(messages.size, isLoading) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .imePadding()
            .background(Color(0xFFF5F7FA))
    ) {

        // ── Header ──────────────────────────────────────────────
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(MediBotGreen)
                .statusBarsPadding()
                .padding(horizontal = 12.dp, vertical = 12.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                IconButton(onClick = onBack) {
                    Icon(
                        Icons.Default.ArrowBack,
                        contentDescription = "Back",
                        tint = Color.White
                    )
                }

                // Bot avatar circle
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(MediBotGreenDark),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.SmartToy, contentDescription = null, tint = Color.White, modifier = Modifier.size(22.dp))
                }

                Spacer(Modifier.width(10.dp))

                Column {
                    Text("MediBot", fontWeight = FontWeight.SemiBold, color = Color.White, fontSize = 16.sp)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(modifier = Modifier.size(6.dp).clip(CircleShape).background(MediBotGreenMid))
                        Spacer(Modifier.width(4.dp))
                        Text("Online · MediAccess Assistant", color = Color.White.copy(alpha = 0.78f), fontSize = 11.sp)
                    }
                }
            }
        }

        // ── Disclaimer banner ────────────────────────────────────
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(MediBotGreenLight)
                .padding(horizontal = 14.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Outlined.Warning, contentDescription = null, tint = MediBotGreenDark, modifier = Modifier.size(14.dp))
            Spacer(Modifier.width(6.dp))
            Text(
                text = "AI assistant — always consult your doctor for medical decisions.",
                fontSize = 11.sp,
                color = MediBotGreenDark,
                lineHeight = 15.sp
            )
        }

        HorizontalDivider(color = MediBotGreenMid, thickness = 0.5.dp)

        // ── Messages ─────────────────────────────────────────────
        LazyColumn(
            state = listState,
            modifier = Modifier.weight(1f).fillMaxWidth(),
            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(messages) { msg ->
                ChatBubble(message = msg)
            }

            if (isLoading) {
                item {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        BotAvatarSmall()
                        Spacer(Modifier.width(8.dp))
                        TypingIndicator()
                    }
                }
            }
        }

        // ── Suggestion chips ──────────────────────────────────────
        if (messages.size <= 1) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                suggestionChips.forEach { chip ->
                    SuggestionChip(
                        label = chip,
                        onClick = {
                            println("🔵 ChatbotScreen: Chip tapped: \"$chip\"")
                            viewModel.sendMessage(chip)
                        }
                    )
                }
            }
        }

        // ── Input area ────────────────────────────────────────────
        Surface(
            color = Color.White,
            shadowElevation = 8.dp,
            tonalElevation = 0.dp
        ) {
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
                        focusedBorderColor = MediBotGreen,
                        unfocusedBorderColor = Color(0xFFDDDDDD),
                        focusedContainerColor = Color(0xFFF8F8F8),
                        unfocusedContainerColor = Color(0xFFF8F8F8)
                    ),
                    singleLine = false,
                    maxLines = 4,
                    textStyle = LocalTextStyle.current.copy(fontSize = 13.sp)
                )

                Spacer(Modifier.width(8.dp))

                // Send button
                val canSend = inputText.isNotBlank() && !isLoading
                IconButton(
                    onClick = {
                        if (canSend) {
                            println("🔵 ChatbotScreen: Send tapped, message = \"$inputText\"")
                            scope.launch {
                                viewModel.sendMessage(inputText)
                                inputText = ""
                            }
                        }
                    },
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(if (canSend) MediBotGreen else Color(0xFFCCCCCC))
                ) {
                    Icon(Icons.Default.Send, contentDescription = "Send", tint = Color.White, modifier = Modifier.size(18.dp))
                }
            }
        }
    }
}

// ── Sub-composables ──────────────────────────────────────────────────────────

@Composable
private fun ChatBubble(message: ChatMessage) {
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
            Surface(
                shape = RoundedCornerShape(
                    topStart = 18.dp, topEnd = 18.dp,
                    bottomStart = if (message.isUser) 18.dp else 4.dp,
                    bottomEnd = if (message.isUser) 4.dp else 18.dp
                ),
                color = if (message.isUser) MediBotGreen else Color.White,
                shadowElevation = if (message.isUser) 0.dp else 1.dp,
                border = if (!message.isUser) ButtonDefaults.outlinedButtonBorder else null
            ) {
                Text(
                    text = message.text,
                    modifier = Modifier
                        .widthIn(max = 260.dp)
                        .padding(horizontal = 13.dp, vertical = 10.dp),
                    color = if (message.isUser) Color.White else Color(0xFF1A1A1A),
                    fontSize = 13.sp,
                    lineHeight = 19.sp
                )
            }
        }

        if (message.isUser) Spacer(Modifier.width(4.dp))
    }
}

@Composable
private fun BotAvatarSmall() {
    Box(
        modifier = Modifier.size(28.dp).clip(CircleShape).background(MediBotGreen),
        contentAlignment = Alignment.Center
    ) {
        Icon(Icons.Default.SmartToy, contentDescription = null, tint = Color.White, modifier = Modifier.size(14.dp))
    }
}

@Composable
private fun TypingIndicator() {
    Surface(
        shape = RoundedCornerShape(topStart = 18.dp, topEnd = 18.dp, bottomStart = 4.dp, bottomEnd = 18.dp),
        color = Color.White,
        shadowElevation = 1.dp
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            repeat(3) {
                CircularProgressIndicator(
                    modifier = Modifier.size(6.dp),
                    color = MediBotGreen,
                    strokeWidth = 1.5.dp
                )
            }
        }
    }
}

@Composable
private fun SuggestionChip(label: String, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(20.dp),
        color = MediBotGreenLight,
        border = ButtonDefaults.outlinedButtonBorder
    ) {
        Text(
            text = label,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            fontSize = 11.sp,
            color = MediBotGreenDark,
            fontWeight = FontWeight.Medium
        )
    }
}