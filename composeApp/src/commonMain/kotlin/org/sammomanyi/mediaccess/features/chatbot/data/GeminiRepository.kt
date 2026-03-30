package org.sammomanyi.mediaccess.features.chatbot.data

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import kotlinx.serialization.Serializable
import org.sammomanyi.mediaccess.BuildKonfig

// Lightweight data models for the API
@Serializable data class GeminiRequest(val contents: List<GeminiContent>)
@Serializable data class GeminiContent(val role: String = "user", val parts: List<GeminiPart>)
@Serializable data class GeminiPart(val text: String)
@Serializable data class GeminiResponse(val candidates: List<GeminiCandidate>? = null)
@Serializable data class GeminiCandidate(val content: GeminiContent? = null)

class GeminiRepository(private val httpClient: HttpClient) {
    suspend fun sendMessage(userMessage: String): String {
        return try {
            val apiKey = BuildKonfig.GEMINI_API_KEY
            println("🔵 GeminiRepository: Sending message: \"$userMessage\"")
            println("🔵 GeminiRepository: API key present = ${apiKey.isNotBlank()}")

            val url = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.0-flash:generateContent?key=$apiKey"

            val systemPrompt = "You are 'MediBot', a helpful, empathetic, and professional health assistant inside the MediAccess app. Answer the user's health question briefly. Always include a short disclaimer that you are an AI and they should consult their doctor. User says: $userMessage"

            val requestBody = GeminiRequest(
                contents = listOf(GeminiContent(parts = listOf(GeminiPart(text = systemPrompt))))
            )

            val httpResponse = httpClient.post(url) {
                contentType(ContentType.Application.Json)
                setBody(requestBody)
            }

            println("🔵 GeminiRepository: HTTP status = ${httpResponse.status}")

            val response: GeminiResponse = httpResponse.body()

            println("🔵 GeminiRepository: candidates count = ${response.candidates?.size}")
            println("🔵 GeminiRepository: first candidate content = ${response.candidates?.firstOrNull()?.content}")

            val text = response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
            println("🔵 GeminiRepository: extracted text = $text")

            text ?: "I'm sorry, I couldn't process that right now."

        } catch (e: Exception) {
            println("🔴 GeminiRepository: Exception type = ${e::class.simpleName}")
            println("🔴 GeminiRepository: Exception message = ${e.message}")
            "Network error connecting to MediBot. Please check your connection."
        }
    }
}