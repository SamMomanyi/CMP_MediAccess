package org.sammomanyi.mediaccess.features.cover.data.desktop

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.forms.submitForm
import io.ktor.http.Parameters
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import java.io.File
import java.security.KeyFactory
import java.security.Signature
import java.security.spec.PKCS8EncodedKeySpec
import java.util.Base64

@Serializable
private data class ServiceAccountJson(
    @SerialName("client_email") val clientEmail: String,
    @SerialName("private_key") val privateKey: String,
    @SerialName("project_id") val projectId: String
)

@Serializable
private data class TokenResponse(
    @SerialName("access_token") val accessToken: String,
    @SerialName("expires_in") val expiresIn: Int
)

class ServiceAccountCredentials(serviceAccountPath: String) {

    private val json = Json { ignoreUnknownKeys = true }
    private val account: ServiceAccountJson
    val projectId: String

    // Cache the token to avoid signing a new JWT on every request
    private var cachedToken: String? = null
    private var tokenExpiresAt: Long = 0L

    init {
        val raw = File(serviceAccountPath).readText()
        account = json.decodeFromString(raw)
        projectId = account.projectId
    }

    suspend fun getAccessToken(httpClient: HttpClient): String {
        val now = System.currentTimeMillis() / 1000
        // Reuse token if it's still valid with a 60s buffer
        if (cachedToken != null && now < tokenExpiresAt - 60) {
            return cachedToken!!
        }

        val jwt = buildJwt(now)
        val response = httpClient.submitForm(
            url = "https://oauth2.googleapis.com/token",
            formParameters = Parameters.build {
                append("grant_type", "urn:ietf:params:oauth:grant-type:jwt-bearer")
                append("assertion", jwt)
            }
        )
        val tokenResponse = response.body<TokenResponse>()
        cachedToken = tokenResponse.accessToken
        tokenExpiresAt = now + tokenResponse.expiresIn
        return tokenResponse.accessToken
    }

    private fun buildJwt(nowSeconds: Long): String {
        val header = """{"alg":"RS256","typ":"JWT"}"""
        val payload = """
            {
              "iss": "${account.clientEmail}",
              "scope": "https://www.googleapis.com/auth/datastore",
              "aud": "https://oauth2.googleapis.com/token",
              "iat": $nowSeconds,
              "exp": ${nowSeconds + 3600}
            }
        """.trimIndent()

        val encoder = Base64.getUrlEncoder().withoutPadding()
        val headerEncoded = encoder.encodeToString(header.toByteArray())
        val payloadEncoded = encoder.encodeToString(payload.toByteArray())
        val signingInput = "$headerEncoded.$payloadEncoded"

        // Strip PEM headers and decode the private key
        val pemBody = account.privateKey
            .replace("-----BEGIN PRIVATE KEY-----", "")
            .replace("-----END PRIVATE KEY-----", "")
            .replace("\\s".toRegex(), "")
        val keyBytes = Base64.getDecoder().decode(pemBody)
        val privateKey = KeyFactory.getInstance("RSA")
            .generatePrivate(PKCS8EncodedKeySpec(keyBytes))

        val sig = Signature.getInstance("SHA256withRSA")
        sig.initSign(privateKey)
        sig.update(signingInput.toByteArray())
        val signatureEncoded = encoder.encodeToString(sig.sign())

        return "$signingInput.$signatureEncoded"
    }

    companion object {
        fun resolve(): String {
            System.getenv("MEDIACCESS_SERVICE_ACCOUNT")?.let { return it }
            val relative = "secrets/service-account.json"
            if (File(relative).exists()) return relative
            val fromRoot = "composeApp/secrets/service-account.json"
            if (File(fromRoot).exists()) return fromRoot
            throw IllegalStateException(
                "Service account not found.\n" +
                        "Place it at: composeApp/secrets/service-account.json"
            )
        }
    }
}