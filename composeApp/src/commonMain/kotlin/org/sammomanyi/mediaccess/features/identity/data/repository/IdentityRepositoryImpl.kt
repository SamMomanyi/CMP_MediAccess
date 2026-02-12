package org.sammomanyi.mediaccess.features.identity.data.repository

import com.google.android.gms.common.util.CollectionUtils.mapOf
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
// ADD THESE EXPLICIT IMPORTS:
import kotlinx.datetime.Clock as kotlinxClock
import kotlinx.datetime.Instant as kotlinxInstant // Use alias to avoid conflicts
import org.sammomanyi.mediaccess.core.domain.util.DataError
import org.sammomanyi.mediaccess.core.domain.util.Result
import org.sammomanyi.mediaccess.features.identity.data.local.UserDao
import org.sammomanyi.mediaccess.features.identity.data.mappers.toEntity
import org.sammomanyi.mediaccess.features.identity.data.mappers.toUser
import org.sammomanyi.mediaccess.features.identity.domain.model.User
import org.sammomanyi.mediaccess.features.identity.domain.model.VisitCode
import org.sammomanyi.mediaccess.features.identity.domain.model.VisitPurpose
import org.sammomanyi.mediaccess.features.identity.domain.repository.IdentityRepository
import dev.gitlive.firebase.auth.FirebaseAuth
import dev.gitlive.firebase.auth.FirebaseAuthException
import dev.gitlive.firebase.firestore.FirebaseFirestore
import dev.gitlive.firebase.firestore.FirebaseFirestoreException
import jdk.internal.net.http.common.Utils.close
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.flow
import org.sammomanyi.mediaccess.features.identity.domain.model.CoverLinkRequest
import kotlin.collections.mapOf
import kotlin.random.Random
import kotlin.time.ExperimentalTime

class IdentityRepositoryImpl(
    private val userDao: UserDao,
    private val firebaseAuth: FirebaseAuth,
    private val firestore: FirebaseFirestore
) : IdentityRepository {



    override fun getLocalUser(): Flow<User?> {
        return userDao.getUser().map { it?.toUser() }
    }

    override suspend fun getCurrentUser(): Result<User?, DataError> {
        return try {
            val currentUser = firebaseAuth.currentUser
            if (currentUser == null) {
                // If no Firebase session, check if we have a local user cached
                // We grab the first emission from your Flow
                val localUser = userDao.getUser().firstOrNull()
                if (localUser != null) {
                    return Result.Success(localUser.toUser())
                }
                Result.Success(null)
            } else {
// Try to sync with Firestore
                try {
                    val userDoc = firestore.collection("users").document(currentUser.uid).get()
                    if (userDoc.exists) {
                        val user = userDoc.data<User>()
                        // Update cache
                        userDao.upsertUser(user.toEntity())
                        Result.Success(user)
                    } else {
                        Result.Error(DataError.Auth.USER_NOT_FOUND)
                    }
                } catch (e: Exception) {
                    // Network failed? Fallback to local data
                    val local = userDao.getUser().firstOrNull()
                    if (local != null && local.id == currentUser.uid) {
                        Result.Success(local.toUser())
                    } else {
                        Result.Error(DataError.Network.NO_INTERNET)
                    }
                }
            }
        } catch (e: FirebaseAuthException) {
            Result.Error(DataError.Auth.SESSION_EXPIRED)
        } catch (e: Exception) {
            Result.Error(DataError.Network.SERVER_ERROR)
        }
    }

    override suspend fun signUp(user: User, password: String): Result<Unit, DataError> {
        return try {
            // Step 1: Create the Auth Account
            val authResult = firebaseAuth.createUserWithEmailAndPassword(user.email, password)
            val uid = authResult.user?.uid ?: return Result.Error(DataError.Network.SERVER_ERROR)

            val userWithId = user.copy(
                id = uid,
                createdAt = kotlinxClock.System.now().toEpochMilliseconds()
            )

            // Step 2: Write to Firestore
            firestore.collection("users").document(uid).set(userWithId)

            // Step 3: Write to Local Room DB
            userDao.upsertUser(userWithId.toEntity())

            Result.Success(Unit)
        } catch (e: FirebaseAuthException) {
            val error = when (e.errorCode) {
                "ERROR_EMAIL_ALREADY_IN_USE" -> DataError.Auth.EMAIL_ALREADY_EXISTS
                "ERROR_INVALID_EMAIL" -> DataError.Validation.INVALID_EMAIL
                "ERROR_WEAK_PASSWORD" -> DataError.Validation.PASSWORD_TOO_WEAK
                "ERROR_USER_NOT_FOUND" -> DataError.Auth.USER_NOT_FOUND
                else -> DataError.Network.SERVER_ERROR
            }
            Result.Error(error)
        }

    }

    override suspend fun login(email: String, password: String): Result<Unit, DataError> {
        println("🕵️‍♂️ LOGIN ATTEMPT: Checking Local Database for $email...")
        // 1. Check Local DB First
        // Since your DAO only has getUser(), we grab the snapshot
        val localUser = userDao.getUser().firstOrNull()

        if(localUser != null) {
            println("✅ LOCAL LOGIN SUCCESS!")
            println("🔐 PASSWORD CHECK: Local '${localUser.password}' vs Input '$password'")
        // We must check if the locally stored user matches the email AND password
        // (Assuming your UserEntity stores the password)
        if (localUser != null && localUser.email == email && localUser.password == password) {
            println("🎉 OFFLINE LOGIN SUCCESS!")
            return Result.Success(Unit)
        }else {
            println("❌ PASSWORD MISMATCH")
        } } else{
            println("❌ NO LOCAL USER FOUND proceed to firebase")
        }

        // 2. If Local Failed, Try Network
        return try {
            val authResult = firebaseAuth.signInWithEmailAndPassword(email, password)
            val uid = authResult.user?.uid ?: return Result.Error(DataError.Auth.INVALID_CREDENTIALS)

            val userSnapshot = firestore.collection("users").document(uid).get()
            if (!userSnapshot.exists) {
                return Result.Error(DataError.Auth.USER_NOT_FOUND)
            }

            val user = userSnapshot.data<User>()
            userDao.upsertUser(user.toEntity())

            Result.Success(Unit)
        } catch (e: FirebaseAuthException) {
            Result.Error(DataError.Auth.INVALID_CREDENTIALS)
        } catch (e: Exception) {
            Result.Error(DataError.Network.UNAUTHORIZED)
        }
    }

    override suspend fun logout(): Result<Unit, DataError> {
        return try {
            firebaseAuth.signOut()
            userDao.clearUser()
            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error(DataError.Network.SERVER_ERROR)
        }
    }

    override suspend fun updateUserProfile(user: User): Result<Unit, DataError> {
        return try {
            val uid = user.id ?: return Result.Error(DataError.Auth.USER_NOT_FOUND)
            firestore.collection("users").document(uid).set(user)
            userDao.upsertUser(user.toEntity())
            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error(DataError.Network.SERVER_ERROR)
        }
    }

    override suspend fun resetPassword(email: String): Result<Unit, DataError> {
        return try {
            firebaseAuth.sendPasswordResetEmail(email)
            Result.Success(Unit)
        } catch (e: FirebaseAuthException) {
            Result.Error(DataError.Auth.USER_NOT_FOUND)
        } catch (e: Exception) {
            Result.Error(DataError.Network.SERVER_ERROR)
        }
    }

    override suspend fun loginWithGoogle(
        idToken: String,
        email: String,
        displayName: String,
        photoUrl: String?
    ): Result<Unit, DataError> {
        return try {
            println("🔵 Starting Google Sign-In with Firebase")

            // Firebase auth is already done in GoogleSignInHelper
            // Just get the current user
            val uid = firebaseAuth.currentUser?.uid

            if (uid == null) {
                println("🔴 No current user after Google Sign-In")
                return Result.Error(DataError.Auth.INVALID_CREDENTIALS)
            }

            println("🔵 Got UID: $uid")

            // Check if user exists in Firestore
            val userSnapshot = firestore.collection("users").document(uid).get()

            if (userSnapshot.exists) {
                println("🔵 User exists in Firestore, loading profile")
                // User exists, load their profile
                val user = userSnapshot.data<User>()
                userDao.upsertUser(user.toEntity())
            } else {
                println("🔵 New Google user, creating profile")
                // New user, create profile
                val names = displayName.split(" ", limit = 2)
                val firstName = names.getOrNull(0) ?: "User"
                val lastName = names.getOrNull(1) ?: ""

                val newUser = User(
                    id = uid,
                    firstName = firstName,
                    lastName = lastName,
                    email = email,
                    password =  "", // Google accounts don't have passwords
                    phoneNumber = "", // Can be added later
                    dateOfBirth = "2000-01-01",
                    gender = "PREFER_NOT_TO_SAY",
                    role = "PATIENT",
                    medicalId = generateMedicalId(),
                    profileImageUrl = photoUrl,
                    isEmailVerified = true, // Google accounts are pre-verified
                    createdAt = kotlinx.datetime.Clock.System.now().toEpochMilliseconds()
                )

                // Save to Firestore
                firestore.collection("users").document(uid).set(newUser)

                // Save locally
                userDao.upsertUser(newUser.toEntity())
            }

            println("🟢 Google Sign-In completed successfully")
            Result.Success(Unit)

        } catch (e: Exception) {
            println("🔴 Google Sign-In error: ${e.message}")
            e.printStackTrace()
            Result.Error(DataError.Network.SERVER_ERROR)
        }
    }

    private fun generateMedicalId(): String {
        val timestamp = System.currentTimeMillis().toString().takeLast(6)
        return "MED-$timestamp"
    }

    @OptIn(ExperimentalTime::class)
    override suspend fun generateVisitCode(
        userId: String,
        purpose: VisitPurpose
    ): Result<VisitCode, DataError> {
        return try {
            val visitCode = VisitCode.generate(userId, purpose)

            val visitData = mapOf(
                "code" to visitCode.code,
                "userId" to visitCode.userId,
                "generatedAt" to visitCode.generatedAt.toEpochMilliseconds(),
                "expiresAt" to visitCode.expiresAt.toEpochMilliseconds(),
                "purpose" to visitCode.purpose.name,
                "isActive" to visitCode.isActive,
                "usedAt" to visitCode.usedAt?.toEpochMilliseconds()
            )

            firestore.collection("visit_codes")
                .document(visitCode.code)
                .set(visitData)

            Result.Success(visitCode)
        } catch (e: FirebaseFirestoreException) {
            Result.Error(DataError.Network.SERVER_ERROR)
        } catch (e: Exception) {
            Result.Error(DataError.Network.SERVER_ERROR)
        }
    }

    override suspend fun verifyVisitCode(code: String): Result<User, DataError> {
        return try {
            val snapshot = firestore.collection("visit_codes").document(code).get()

            if (!snapshot.exists) {
                return Result.Error(DataError.Validation.INVALID_VISIT_CODE)
            }

            val userId = snapshot.get<String>("userId")
            val expiresAt = snapshot.get<Long>("expiresAt")
            val isActive = snapshot.get<Boolean>("isActive") ?: true
            val usedAt = snapshot.get<Long?>("usedAt")

            if (!isActive) {
                return Result.Error(DataError.Validation.INVALID_VISIT_CODE)
            }

            if (usedAt != null) {
                return Result.Error(DataError.Validation.VISIT_CODE_USED)
            }

            val now = kotlinxClock.System.now().toEpochMilliseconds()
            if (now > expiresAt) {
                return Result.Error(DataError.Validation.VISIT_CODE_EXPIRED)
            }

            val userSnapshot = firestore.collection("users").document(userId).get()
            if (!userSnapshot.exists) {
                return Result.Error(DataError.Auth.USER_NOT_FOUND)
            }

            val user = userSnapshot.data<User>()

            firestore.collection("visit_codes").document(code).update(
                mapOf("usedAt" to now)
            )

            Result.Success(user)
        } catch (e: FirebaseFirestoreException) {
            Result.Error(DataError.Network.SERVER_ERROR)
        } catch (e: Exception) {
            Result.Error(DataError.Network.SERVER_ERROR)
        }
    }

    @OptIn(ExperimentalTime::class)
    override suspend fun getActiveVisitCode(userId: String): Result<VisitCode?, DataError> {
        return try {
            val snapshot = firestore.collection("visit_codes")
                .where { "userId" equalTo userId }
                .where { "isActive" equalTo true }
                .get()

            val now = kotlinxClock.System.now().toEpochMilliseconds()

            val activeCode = snapshot.documents.firstOrNull { doc ->
                val expiresAt = doc.get<Long>("expiresAt")
                val usedAt = doc.get<Long?>("usedAt")
                usedAt == null && expiresAt > now
            }

            if (activeCode == null) {
                Result.Success(null)
            } else {
                val visitCode = VisitCode(
                    code = activeCode.get<String>("code"),
                    userId = activeCode.get<String>("userId"),
                    generatedAt = kotlinxInstant.fromEpochMilliseconds(activeCode.get<Long>("generatedAt")),
                    expiresAt = kotlinxInstant.fromEpochMilliseconds(activeCode.get<Long>("expiresAt")),
                    purpose = VisitPurpose.valueOf(activeCode.get<String>("purpose")),
                    usedAt = activeCode.get<Long?>("usedAt")?.let { kotlinxInstant.fromEpochMilliseconds(it) },
                    isActive = activeCode.get<Boolean>("isActive") ?: true
                )
                Result.Success(visitCode)
            }
        } catch (e: Exception) {
            Result.Error(DataError.Network.SERVER_ERROR)
        }
    }

    override suspend fun invalidateVisitCode(code: String): Result<Unit, DataError> {
        return try {
            firestore.collection("visit_codes").document(code).update(
                mapOf("isActive" to false)
            )
            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error(DataError.Network.SERVER_ERROR)
        }
    }

    override suspend fun markVisitCodeAsUsed(code: String): Result<Unit, DataError> {
        return try {
            val now = kotlinxClock.System.now().toEpochMilliseconds()
            firestore.collection("visit_codes").document(code).update(
                mapOf("usedAt" to now)
            )
            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error(DataError.Network.SERVER_ERROR)
        }
    }

    // 1. Helper Function to generate a Firestore-like ID
    private fun generateRandomId(): String {
        val chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789"
        return (1..20)
            .map { chars[Random.nextInt(chars.length)] }
            .joinToString("")
    }


    // 2. Updated Submit Function
    override suspend fun submitCoverLinkRequest(request: CoverLinkRequest): Flow<Result<Unit, DataError>> = flow {
        try {
            // FIX: Generate the ID manually first
            val newId = generateRandomId()

            // Pass the ID explicitly to document()
            val newDoc = firestore.collection("cover_link_requests").document(newId)

            val finalRequest = request.copy(
                id = newId, // Use the generated ID
                timestamp = kotlinxClock.System.now().toEpochMilliseconds()
            )

            newDoc.set(finalRequest)

            emit(Result.Success(Unit))
        } catch (e: Exception) {
            e.printStackTrace()
            emit(Result.Error(DataError.Network.SERVER_ERROR))
        }
    }


}