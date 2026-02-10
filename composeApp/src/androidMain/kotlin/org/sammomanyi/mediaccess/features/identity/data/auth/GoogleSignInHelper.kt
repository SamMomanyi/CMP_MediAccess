package org.sammomanyi.mediaccess.features.identity.data.auth

import GoogleSignInProvider
import android.content.Context
import android.content.Intent
import android.content.IntentSender
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.IntentSenderRequest
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.GetCredentialResponse
import com.google.android.gms.auth.api.identity.BeginSignInRequest
import com.google.android.gms.auth.api.identity.Identity
import com.google.android.gms.auth.api.identity.SignInClient
import com.google.android.gms.common.api.ApiException
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.libraries.identity.googleid.GoogleIdTokenParsingException
import dev.gitlive.firebase.auth.FirebaseAuth
import dev.gitlive.firebase.auth.GoogleAuthProvider
import kotlinx.coroutines.tasks.await
import org.sammomanyi.mediaccess.BuildKonfig
import org.sammomanyi.mediaccess.features.identity.domain.auth.GoogleSignInResult
import java.security.MessageDigest
import java.util.UUID

class GoogleSignInHelper(
    private val context: Context,
    private val firebaseAuth: FirebaseAuth
) : GoogleSignInProvider{
    private val credentialManager = CredentialManager.create(context)

    /**
     * Modern approach using Credential Manager (Recommended for Android 14+)
     */
    suspend fun signInWithGoogle(): GoogleSignInResult {
        return try {
            val googleIdOption = GetGoogleIdOption.Builder()
                .setFilterByAuthorizedAccounts(false)
                .setServerClientId(BuildKonfig.GOOGLE_WEB_CLIENT_ID)
                .setAutoSelectEnabled(true)
                .setNonce(generateNonce())
                .build()

            val request = GetCredentialRequest.Builder()
                .addCredentialOption(googleIdOption)
                .build()

            val result = credentialManager.getCredential(
                request = request,
                context = context
            )

            handleSignInResult(result)
        } catch (e: Exception) {
            println("🔴 Google Sign-In Error: ${e.message}")
            e.printStackTrace()
            GoogleSignInResult.Error(e.message ?: "Unknown error occurred")
        }
    }

    private suspend fun handleSignInResult(result: GetCredentialResponse): GoogleSignInResult {
        return try {
            when (val credential = result.credential) {
                is CustomCredential -> {
                    if (credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                        val googleIdTokenCredential = GoogleIdTokenCredential
                            .createFrom(credential.data)

                        // Get the ID token
                        val idToken = googleIdTokenCredential.idToken

                        // Sign in to Firebase with the ID token
                        val authCredential = GoogleAuthProvider.credential(idToken, null)
                        val authResult = firebaseAuth.signInWithCredential(authCredential)

                        val user = authResult.user

                        if (user != null) {
                            GoogleSignInResult.Success(
                                idToken = idToken,
                                email = user.email ?: "",
                                displayName = user.displayName ?: "",
                                photoUrl = user.photoURL,
                                userId = user.uid
                            )
                        } else {
                            GoogleSignInResult.Error("Failed to get user information")
                        }
                    } else {
                        GoogleSignInResult.Error("Unexpected credential type")
                    }
                }
                else -> {
                    GoogleSignInResult.Error("Unexpected credential type")
                }
            }
        } catch (e: GoogleIdTokenParsingException) {
            println("🔴 Google ID Token Parsing Error: ${e.message}")
            GoogleSignInResult.Error("Failed to parse Google ID token")
        } catch (e: Exception) {
            println("🔴 Firebase Sign-In Error: ${e.message}")
            e.printStackTrace()
            GoogleSignInResult.Error(e.message ?: "Failed to sign in with Firebase")
        }
    }

    /**
     * Legacy approach using Sign-In Client (Fallback for older devices)
     */
    private val oneTapClient: SignInClient by lazy {
        Identity.getSignInClient(context)
    }

    suspend fun beginSignIn(): IntentSenderRequest? {
        return try {
            val signInRequest = BeginSignInRequest.builder()
                .setGoogleIdTokenRequestOptions(
                    BeginSignInRequest.GoogleIdTokenRequestOptions.builder()
                        .setSupported(true)
                        .setServerClientId(BuildKonfig.GOOGLE_WEB_CLIENT_ID)
                        .setFilterByAuthorizedAccounts(false)
                        .build()
                )
                .setAutoSelectEnabled(true)
                .build()

            val result = oneTapClient.beginSignIn(signInRequest).await()

            IntentSenderRequest.Builder(result.pendingIntent.intentSender).build()
        } catch (e: Exception) {
            println("🔴 Begin Sign-In Error: ${e.message}")
            null
        }
    }

    suspend fun handleOneTapSignInResult(intent: Intent): GoogleSignInResult {
        return try {
            val credential = oneTapClient.getSignInCredentialFromIntent(intent)
            val idToken = credential.googleIdToken

            if (idToken == null) {
                return GoogleSignInResult.Error("No ID token found")
            }

            // Sign in to Firebase
            val authCredential = GoogleAuthProvider.credential(idToken, null)
            val authResult = firebaseAuth.signInWithCredential(authCredential)

            val user = authResult.user

            if (user != null) {
                GoogleSignInResult.Success(
                    idToken = idToken,
                    email = user.email ?: "",
                    displayName = user.displayName ?: "",
                    photoUrl = user.photoURL,
                    userId = user.uid
                )
            } else {
                GoogleSignInResult.Error("Failed to get user information")
            }
        } catch (e: ApiException) {
            println("🔴 API Exception: ${e.message}")
            GoogleSignInResult.Error("Google Sign-In failed: ${e.message}")
        } catch (e: Exception) {
            println("🔴 Sign-In Error: ${e.message}")
            GoogleSignInResult.Error(e.message ?: "Unknown error")
        }
    }

    private fun generateNonce(): String {
        val bytes = UUID.randomUUID().toString().toByteArray()
        val md = MessageDigest.getInstance("SHA-256")
        val digest = md.digest(bytes)
        return digest.fold("") { str, it -> str + "%02x".format(it) }
    }

    override suspend fun signIn(): GoogleSignInResult {
        return signInWithGoogle()
    }
}

