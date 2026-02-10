package org.sammomanyi.mediaccess.features.identity.data.remote

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import org.sammomanyi.mediaccess.features.identity.domain.model.Article
import org.sammomanyi.mediaccess.features.identity.domain.model.HealthNewsResponse

class NewsService(private val client: HttpClient) {
    suspend fun fetchHealthNews(): List<Article> {
        return try {
            val response: HealthNewsResponse = client.get("https://health.gov/myhealthfinder/api/v3/topicsearch.json?lang=en").body()
            response.Result.Resources.map {
                Article(
                    id = it.Id,
                    title = it.Title,
                    imageUrl = it.ImageUrl,
                    date = "Feb 2026", // API date varies, hardcoding month for UI
                    contentUrl = it.AccessibleVersion
                )
            }
        } catch (e: Exception) {
            emptyList()
        }
    }
}