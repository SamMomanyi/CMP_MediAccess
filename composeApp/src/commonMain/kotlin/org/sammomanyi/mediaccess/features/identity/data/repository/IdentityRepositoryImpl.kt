package org.sammomanyi.mediaccess.features.identity.data.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.flow
import org.sammomanyi.mediaccess.core.domain.util.DataError
import org.sammomanyi.mediaccess.core.domain.util.Result
import org.sammomanyi.mediaccess.features.identity.data.local.UserDao
import org.sammomanyi.mediaccess.features.identity.data.mappers.toEntity
import org.sammomanyi.mediaccess.features.identity.data.mappers.toUser
import org.sammomanyi.mediaccess.features.identity.domain.model.User
import org.sammomanyi.mediaccess.features.identity.domain.model.VisitCode
import org.sammomanyi.mediaccess.features.identity.domain.model.VisitPurpose
import org.sammomanyi.mediaccess.features.identity.domain.model.CoverLinkRequest
import dev.gitlive.firebase.auth.FirebaseAuth
import dev.gitlive.firebase.auth.FirebaseAuthException
import dev.gitlive.firebase.firestore.FirebaseFirestore
import dev.gitlive.firebase.firestore.FirebaseFirestoreException
import org.sammomanyi.mediaccess.features.identity.domain.repository.IdentityRepository
import kotlin.random.Random

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
                val localUser = userDao.getUser().firstOrNull()
                if (localUser != null) {
                    return Result.Success(localUser.toUser())
                }
                Result.Success(null)
            } else {
                try {
                    val userDoc = firestore.collection("users").document(currentUser.uid).get()
                    if (userDoc.exists) {
                        val user = userDoc.data<User>()
                        userDao.clearUser()
                        userDao.upsertUser(user.toEntity())
                        Result.Success(user)
                    } else {
                        Result.Error(DataError.Auth.USER_NOT_FOUND)
                    }
                } catch (e: Exception) {
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
            val authResult = firebaseAuth.createUserWithEmailAndPassword(user.email, password)
            val uid = authResult.user?.uid ?: return Result.Error(DataError.Network.SERVER_ERROR)

            val userWithId = user.copy(
                id = uid,
                createdAt = System.currentTimeMillis()
            )

            firestore.collection("users").document(uid).set(userWithId)
            userDao.clearUser()
            userDao.upsertUser(userWithId.toEntity())

            Result.Success(Unit)
        } catch (e: FirebaseAuthException) {
            val error = when (e.errorCode.toString()) {
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
        println("🕵️‍♂️ LOGIN ATTEMPT: $email")

        return try {
            firebaseAuth.signOut()

            val authResult = firebaseAuth.signInWithEmailAndPassword(email, password)
            val uid = authResult.user?.uid ?: return Result.Error(DataError.Auth.INVALID_CREDENTIALS)

            val userSnapshot = firestore.collection("users").document(uid).get()
            if (!userSnapshot.exists) {
                return Result.Error(DataError.Auth.USER_NOT_FOUND)
            }

            val user = userSnapshot.data<User>()

            println("🔄 Clearing old user data and inserting ${user.email}")
            userDao.clearUser()
            userDao.upsertUser(user.toEntity())

            Result.Success(Unit)
        } catch (e: FirebaseAuthException) {
            Result.Error(DataError.Auth.INVALID_CREDENTIALS)
        } catch (e: Exception) {
            println("🔴 Login error: ${e.message}")
            Result.Error(DataError.Network.UNAUTHORIZED)
        }
    }

    override suspend fun logout(): Result<Unit, DataError> {
        return try {
            println("🚪 LOGGING OUT - Clearing all data")
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
            println("🔵 Starting Google Sign-In")
            val uid = firebaseAuth.currentUser?.uid ?: return Result.Error(DataError.Auth.INVALID_CREDENTIALS)

            val userSnapshot = firestore.collection("users").document(uid).get()

            if (userSnapshot.exists) {
                val user = userSnapshot.data<User>()
                userDao.clearUser()
                userDao.upsertUser(user.toEntity())
            } else {
                val names = displayName.split(" ", limit = 2)
                val newUser = User(
                    id = uid,
                    firstName = names.getOrNull(0) ?: "User",
                    lastName = names.getOrNull(1) ?: "",
                    email = email,
                    password = "",
                    phoneNumber = "",
                    dateOfBirth = "2000-01-01",
                    gender = "PREFER_NOT_TO_SAY",
                    role = "PATIENT",
                    medicalId = generateMedicalId(),
                    profileImageUrl = photoUrl,
                    isEmailVerified = true,
                    createdAt = System.currentTimeMillis()
                )

                firestore.collection("users").document(uid).set(newUser)
                userDao.clearUser()
                userDao.upsertUser(newUser.toEntity())
            }

            Result.Success(Unit)
        } catch (e: Exception) {
            println("🔴 Google Sign-In error: ${e.message}")
            Result.Error(DataError.Network.SERVER_ERROR)
        }
    }

    private fun generateMedicalId(): String {
        val timestamp = System.currentTimeMillis().toString().takeLast(6)
        return "MED-$timestamp"
    }

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

            firestore.collection("visit_codes").document(visitCode.code).set(visitData)
            Result.Success(visitCode)
        } catch (e: Exception) {
            Result.Error(DataError.Network.SERVER_ERROR)
        }
    }

    override suspend fun verifyVisitCode(code: String): Result<User, DataError> {
        return try {
            val snapshot = firestore.collection("visit_codes").document(code).get()
            if (!snapshot.exists) return Result.Error(DataError.Validation.INVALID_VISIT_CODE)

            val userId = snapshot.get<String>("userId")
            val expiresAt = snapshot.get<Long>("expiresAt")
            val isActive = snapshot.get<Boolean>("isActive") ?: true
            val usedAt = snapshot.get<Long?>("usedAt")

            if (!isActive || usedAt != null) {
                return Result.Error(DataError.Validation.VISIT_CODE_USED)
            }

            if (System.currentTimeMillis() > expiresAt) {
                return Result.Error(DataError.Validation.VISIT_CODE_EXPIRED)
            }

            val userSnapshot = firestore.collection("users").document(userId).get()
            if (!userSnapshot.exists) return Result.Error(DataError.Auth.USER_NOT_FOUND)

            val user = userSnapshot.data<User>()
            firestore.collection("visit_codes").document(code).update(
                mapOf("usedAt" to System.currentTimeMillis())
            )

            Result.Success(user)
        } catch (e: Exception) {
            Result.Error(DataError.Network.SERVER_ERROR)
        }
    }

    override suspend fun getActiveVisitCode(userId: String): Result<VisitCode?, DataError> {
        return try {
            val snapshot = firestore.collection("visit_codes")
                .where { "userId" equalTo userId }
                .where { "isActive" equalTo true }
                .get()

            val now = System.currentTimeMillis()
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
                    generatedAt = kotlinx.datetime.Instant.fromEpochMilliseconds(activeCode.get<Long>("generatedAt")),
                    expiresAt = kotlinx.datetime.Instant.fromEpochMilliseconds(activeCode.get<Long>("expiresAt")),
                    purpose = VisitPurpose.valueOf(activeCode.get<String>("purpose")),
                    usedAt = activeCode.get<Long?>("usedAt")?.let { kotlinx.datetime.Instant.fromEpochMilliseconds(it) },
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
            firestore.collection("visit_codes").document(code).update(mapOf("isActive" to false))
            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error(DataError.Network.SERVER_ERROR)
        }
    }

    override suspend fun markVisitCodeAsUsed(code: String): Result<Unit, DataError> {
        return try {
            firestore.collection("visit_codes").document(code).update(
                mapOf("usedAt" to System.currentTimeMillis())
            )
            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error(DataError.Network.SERVER_ERROR)
        }
    }

    private fun generateRandomId(): String {
        val chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789"
        return (1..20).map { chars[Random.nextInt(chars.length)] }.joinToString("")
    }

    override suspend fun submitCoverLinkRequest(request: CoverLinkRequest): Flow<Result<Unit, DataError>> = flow {
        try {
            val newId = generateRandomId()
            val finalRequest = request.copy(
                id = newId,
                timestamp = System.currentTimeMillis()
            )

            firestore.collection("cover_link_requests").document(newId).set(finalRequest)
            emit(Result.Success(Unit))
        } catch (e: Exception) {
            emit(Result.Error(DataError.Network.SERVER_ERROR))
        }
    }
}