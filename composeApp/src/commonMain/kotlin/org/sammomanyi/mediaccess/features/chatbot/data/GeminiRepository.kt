package org.sammomanyi.mediaccess.features.chatbot.data

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.contentType
import kotlinx.coroutines.delay
import kotlinx.serialization.Serializable
import org.sammomanyi.mediaccess.BuildKonfig

@Serializable data class GeminiRequest(val contents: List<GeminiContent>)
@Serializable data class GeminiContent(val role: String = "user", val parts: List<GeminiPart>)
@Serializable data class GeminiPart(val text: String)
@Serializable data class GeminiResponse(val candidates: List<GeminiCandidate>? = null)
@Serializable data class GeminiCandidate(val content: GeminiContent? = null)

class GeminiRepository(private val httpClient: HttpClient) {

    suspend fun sendMessage(userMessage: String, systemPrompt: String): String {
        return sendWithRetry(userMessage, systemPrompt, attempt = 0)
    }

    private suspend fun sendWithRetry(userMessage: String, systemPrompt: String, attempt: Int): String {
        return try {
            val apiKey = BuildKonfig.GEMINI_API_KEY
            println("🔵 GeminiRepository: Attempt ${attempt + 1}, message = \"$userMessage\"")
            println("🔵 GeminiRepository: API key present = ${apiKey.isNotBlank()}")

            val url = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash:generateContent?key=$apiKey"
            val systemPrompt = "You are 'MediBot', a helpful, empathetic, and professional health assistant inside the MediAccess app. Answer the user's health question briefly. Always include a short disclaimer that you are an AI and they should consult their doctor. User says: $userMessage"

            val httpResponse = httpClient.post(url) {
                contentType(ContentType.Application.Json)
                setBody(GeminiRequest(contents = listOf(GeminiContent(parts = listOf(GeminiPart(text = systemPrompt))))))
            }

            println("🔵 GeminiRepository: HTTP status = ${httpResponse.status.value}")

            when (httpResponse.status.value) {
                429 -> {
                    if (attempt < 2) {
                        val waitMs = (attempt + 1) * 15000L  // 15s, then 30s — more breathing room
                        println("🟡 GeminiRepository: Rate limited, retrying in ${waitMs}ms (attempt ${attempt + 1})...")
                        delay(waitMs)
                        sendWithRetry(
                            userMessage, attempt + 1,
                            attempt = TODO()
                        )
                    } else {
                        println("🔴 GeminiRepository: Rate limit exceeded after ${attempt + 1} attempts")
                        "MediBot is a bit overwhelmed right now. Please try again in a few minutes."
                    }
                }
                200 -> {
                    val response: GeminiResponse = httpResponse.body()
                    println("🔵 GeminiRepository: Candidates count = ${response.candidates?.size}")
                    val text = response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
                    println("🔵 GeminiRepository: Extracted text = $text")
                    text ?: "I couldn't generate a response. Please try again."
                }
                else -> {
                    val errorBody = httpResponse.bodyAsText()
                    println("🔴 GeminiRepository: Error ${httpResponse.status.value} — $errorBody")
                    when (httpResponse.status.value) {
                        401, 403 -> "API key issue. Please contact support."
                        404 -> "Model not found. Please contact support."
                        else -> "Something went wrong (${httpResponse.status.value}). Please try again."
                    }
                }
            }

        } catch (e: Exception) {
            println("🔴 GeminiRepository: Exception = ${e::class.simpleName}: ${e.message}")
            "Network error. Please check your connection and try again."
        }
    }
}