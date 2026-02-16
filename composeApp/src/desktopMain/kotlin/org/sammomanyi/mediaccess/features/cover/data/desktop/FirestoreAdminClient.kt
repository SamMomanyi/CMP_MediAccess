package org.sammomanyi.mediaccess.features.cover.data.desktop

import com.google.auth.oauth2.GoogleCredentials
import com.google.cloud.firestore.DocumentSnapshot
import com.google.cloud.firestore.EventListener
import com.google.cloud.firestore.Firestore
import com.google.cloud.firestore.FirestoreException
import com.google.cloud.firestore.QuerySnapshot
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import com.google.firebase.cloud.FirestoreClient
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import java.io.File

class FirestoreAdminClient {

    private val db: Firestore

    init {
        // Load service account from composeApp/secrets/service-account.json
        // This file is gitignored and never committed
        val serviceAccountPath = resolveServiceAccountPath()
        val serviceAccount = File(serviceAccountPath).inputStream()

        val options = FirebaseOptions.builder()
            .setCredentials(GoogleCredentials.fromStream(serviceAccount))
            .build()

        // Only initialize if not already initialized (prevents duplicate app error on hot reload)
        if (FirebaseApp.getApps().isEmpty()) {
            FirebaseApp.initializeApp(options)
        }

        db = FirestoreClient.getFirestore()
    }

    private fun resolveServiceAccountPath(): String {
        // 1. Check environment variable first (useful for CI or custom setups)
        System.getenv("MEDIACCESS_SERVICE_ACCOUNT")?.let { return it }

        // 2. Look relative to working directory (composeApp/secrets/)
        val relativePath = "secrets/service-account.json"
        if (File(relativePath).exists()) return relativePath

        // 3. Look in composeApp/secrets/ from project root
        val fromRoot = "composeApp/secrets/service-account.json"
        if (File(fromRoot).exists()) return fromRoot

        throw IllegalStateException(
            "Firebase service account not found.\n" +
                    "Place your service-account.json at: composeApp/secrets/service-account.json\n" +
                    "Or set the MEDIACCESS_SERVICE_ACCOUNT environment variable to the full path."
        )
    }

    // ── Real-time collection listener as a Flow ───────────────
    fun collectionSnapshots(collectionPath: String): Flow<List<Map<String, Any?>>> =
        callbackFlow {
            val listener = db.collection(collectionPath)
                .addSnapshotListener(object : EventListener<QuerySnapshot> {
                    override fun onEvent(snapshot: QuerySnapshot?, error: FirestoreException?) {
                        if (error != null) {
                            close(error)
                            return
                        }
                        val docs = snapshot?.documents?.map { it.data ?: emptyMap() }
                            ?: emptyList()
                        trySend(docs)
                    }
                })

            awaitClose { listener.remove() }
        }

    // ── One-shot writes ───────────────────────────────────────
    suspend fun setDocument(
        collection: String,
        documentId: String,
        data: Map<String, Any?>
    ) {
        db.collection(collection).document(documentId).set(data).get()
    }

    suspend fun updateDocument(
        collection: String,
        documentId: String,
        fields: Map<String, Any?>
    ) {
        db.collection(collection).document(documentId).update(fields).get()
    }
}