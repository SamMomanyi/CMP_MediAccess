

import org.sammomanyi.mediaccess.features.identity.domain.auth.GoogleSignInResult

interface GoogleSignInProvider {
    suspend fun signIn(): GoogleSignInResult
}