package org.sammomanyi.mediaccess.features.chatbot.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.sammomanyi.mediaccess.features.chatbot.data.GeminiRepository
import kotlin.collections.plus

data class ChatMessage(val text: String, val isUser: Boolean)

class ChatbotViewModel(private val repository: GeminiRepository) : ViewModel() {
    private val _messages = MutableStateFlow<List<ChatMessage>>(
        listOf(ChatMessage("Hi there! I'm your MediAccess Health Assistant. How can I help you today?", isUser = false))
    )
    val messages = _messages.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    fun sendMessage(text: String, systemPrompt: String = specialties.first().systemPrompt) {
        if (text.isBlank()) return
        _messages.update { it + ChatMessage(text, isUser = true) }
        _isLoading.value = true
        viewModelScope.launch {
            val response = repository.sendMessage(text, systemPrompt)
            _messages.update { it + ChatMessage(response, isUser = false) }
            _isLoading.value = false
        }
    }
}