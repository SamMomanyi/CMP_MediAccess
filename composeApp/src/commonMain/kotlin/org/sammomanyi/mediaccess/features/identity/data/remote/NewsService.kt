package org.sammomanyi.mediaccess.features.identity.data.remote

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.get
import io.ktor.serialization.kotlinx.json.json
import kotlinx.datetime.toLocalDateTime
import kotlinx.serialization.json.Json
import org.sammomanyi.mediaccess.features.identity.domain.model.Article
import org.sammomanyi.mediaccess.features.identity.domain.model.HealthNewsResponse

class NewsService(private val client: HttpClient) {

    suspend fun fetchHealthNews(): List<Article> {
        return try {
            println("🔵 Fetching health news...")
            val response: HealthNewsResponse = client.get(
                "https://health.gov/myhealthfinder/api/v3/topicsearch.json?lang=en"
            ).body()

            println("🟢 Got ${response.Result.Resources.size} articles")

            response.Result.Resources.take(5).map { resource ->
                Article(
                    id = resource.Id,
                    title = resource.Title,
                    imageUrl = resource.ImageUrl.ifBlank {
                        "https://via.placeholder.com/400x200/00838F/FFFFFF?text=Health+Article"
                    },
                    date = getCurrentDate(),
                    contentUrl = resource.AccessibleVersion
                )
            }
        } catch (e: Exception) {
            println("🔴 Error fetching news: ${e.message}")
            e.printStackTrace()
            getFallbackArticles()
        }
    }

    private fun getFallbackArticles(): List<Article> {
        return listOf(
            Article(
                id = "1",
                title = "Why Do I Have Acne And My Siblings Don't?",
                imageUrl = "https://via.placeholder.com/400x200/00838F/FFFFFF?text=Skin+Care",
                date = getCurrentDate(),
                contentUrl = ""
            ),
            Article(
                id = "2",
                title = "This new Microneedling technique made me look 5 years younger",
                imageUrl = "https://via.placeholder.com/400x200/FF6B6B/FFFFFF?text=Beauty+Tips",
                date = getCurrentDate(),
                contentUrl = ""
            ),
            Article(
                id = "3",
                title = "12 Natural Remedies For Dry Skin in Winter",
                imageUrl = "https://via.placeholder.com/400x200/00838F/FFFFFF?text=Winter+Care",
                date = getCurrentDate(),
                contentUrl = ""
            )
        )
    }

    private fun getCurrentDate(): String {
        val now = kotlinx.datetime.Clock.System.now()
        val date = now.toLocalDateTime(kotlinx.datetime.TimeZone.currentSystemDefault())
        return "${date.year}-${date.monthNumber}-${date.dayOfMonth}"
    }
}