package org.sammomanyi.mediaccess.features.cover.data.desktop

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.patch
import io.ktor.client.request.setBody
import io.ktor.client.request.url
import io.ktor.http.ContentType
import io.ktor.http.contentType
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

class FirestoreRestClient(
    private val credentials: ServiceAccountCredentials,
    private val httpClient: HttpClient
) {
    private val json = Json { ignoreUnknownKeys = true }
    private val baseUrl =
        "https://firestore.googleapis.com/v1/projects/${credentials.projectId}/databases/(default)/documents"

    // ── Fetch all documents in a collection ───────────────────
    suspend fun getCollection(collection: String): List<Map<String, Any?>> {
        val token = credentials.getAccessToken(httpClient)
        val response = httpClient.get("$baseUrl/$collection") {
            header("Authorization", "Bearer $token")
        }
        val body = response.body<String>()
        val root = json.parseToJsonElement(body).jsonObject

        // Firestore returns {"documents": [...]} or {} if empty
        val documents = root["documents"]?.jsonArray ?: return emptyList()

        return documents.mapNotNull { doc ->
            doc.jsonObject["fields"]?.jsonObject?.let { parseFields(it) }
        }
    }

    // ── Update specific fields in a document ──────────────────
    suspend fun updateDocument(
        collection: String,
        documentId: String,
        fields: Map<String, Any?>
    ) {
        val token = credentials.getAccessToken(httpClient)

        // Build updateMask query params so we only overwrite specific fields
        val fieldMask = fields.keys.joinToString("&") {
            "updateMask.fieldPaths=$it"
        }

        val firestoreFields = fields.mapValues { (_, v) -> toFirestoreValue(v) }
        val bodyJson = """{"fields": ${buildFieldsJson(firestoreFields)}}"""

        httpClient.patch {
            url("$baseUrl/$collection/$documentId?$fieldMask")
            header("Authorization", "Bearer $token")
            contentType(ContentType.Application.Json)
            setBody(bodyJson)
        }
    }

    // ── Parse Firestore typed fields → plain Map ──────────────
    private fun parseFields(fields: JsonObject): Map<String, Any?> {
        return fields.mapValues { (_, value) -> parseValue(value.jsonObject) }
    }

    private fun parseValue(valueObj: JsonObject): Any? {
        return when {
            valueObj.containsKey("stringValue") ->
                valueObj["stringValue"]!!.jsonPrimitive.content

            valueObj.containsKey("integerValue") ->
                valueObj["integerValue"]!!.jsonPrimitive.content.toLong()

            valueObj.containsKey("doubleValue") ->
                valueObj["doubleValue"]!!.jsonPrimitive.content.toDouble()

            valueObj.containsKey("booleanValue") ->
                valueObj["booleanValue"]!!.jsonPrimitive.content.toBoolean()

            valueObj.containsKey("nullValue") ->
                null

            valueObj.containsKey("timestampValue") ->
                valueObj["timestampValue"]!!.jsonPrimitive.content

            valueObj.containsKey("mapValue") ->
                valueObj["mapValue"]!!.jsonObject["fields"]
                    ?.jsonObject?.let { parseFields(it) }

            valueObj.containsKey("arrayValue") ->
                valueObj["arrayValue"]!!.jsonObject["values"]
                    ?.jsonArray?.map { parseValue(it.jsonObject) }
                    ?: emptyList<Any?>()

            else -> null
        }
    }

    // ── Serialize plain values → Firestore typed JSON ─────────
    private fun toFirestoreValue(value: Any?): String = when (value) {
        null -> """{"nullValue": null}"""
        is String -> """{"stringValue": "$value"}"""
        is Long -> """{"integerValue": "$value"}"""
        is Int -> """{"integerValue": "$value"}"""
        is Boolean -> """{"booleanValue": $value}"""
        is Double -> """{"doubleValue": $value}"""
        else -> """{"stringValue": "${value.toString()}"}"""
    }

    private fun buildFieldsJson(fields: Map<String, String>): String {
        val entries = fields.entries.joinToString(", ") { (k, v) -> """"$k": $v""" }
        return "{$entries}"
    }
}