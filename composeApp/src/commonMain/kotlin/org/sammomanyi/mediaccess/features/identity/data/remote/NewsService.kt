package org.sammomanyi.mediaccess.features.identity.data.remote

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsText
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import org.sammomanyi.mediaccess.features.identity.domain.model.Article
import org.sammomanyi.mediaccess.features.identity.domain.model.HealthNewsResponse

class NewsService(private val client: HttpClient) {

    suspend fun fetchHealthNews(): List<Article> {
        return try {
            println("🔵 Fetching health news...")

            val response: HealthNewsResponse = client.get(
                "https://health.gov/myhealthfinder/api/v3/topicsearch.json?lang=en"
            ).body()

            val resources = response.result.resources.resource
            println("🟢 Parsed ${resources.size} articles")

            if (resources.isEmpty()) {
                println("⚠️ Empty resources, using fallback")
                return getFallbackArticles()
            }

            // Filter to only articles that have images, take first 10
            val articlesWithImages = resources
                .filter { it.imageUrl.isNotBlank() && it.imageUrl.startsWith("http") }
                .take(10)
                .mapIndexed { index, resource ->
                    Article(
                        id = resource.id.ifBlank { "article_$index" },
                        title = resource.title.ifBlank { "Health Article" },
                        imageUrl = resource.imageUrl,
                        date = getCurrentDate(),
                        contentUrl = resource.accessibleVersion.ifBlank {
                            "https://health.gov/myhealthfinder"
                        }
                    )
                }

            println("🟢 Articles with images: ${articlesWithImages.size}")

            articlesWithImages.ifEmpty {
                println("⚠️ No articles had images, using fallback")
                getFallbackArticles()
            }

        } catch (e: Exception) {
            println("🔴 News fetch error: ${e::class.simpleName} - ${e.message}")
            getFallbackArticles()
        }
    }

    private fun getFallbackArticles(): List<Article> {
        println("📰 Loading fallback articles with Unsplash images")
        return listOf(
            Article(
                id = "f1",
                title = "Why Do I Have Acne And My Siblings Don't?",
                imageUrl = "https://images.unsplash.com/photo-1616391182219-e080b4d1042a?w=600&q=80",
                date = getCurrentDate(),
                contentUrl = "https://health.gov/myhealthfinder/health-conditions/skin-health/keep-your-skin-healthy"
            ),
            Article(
                id = "f2",
                title = "This Microneedling Technique Can Improve Your Skin",
                imageUrl = "https://images.unsplash.com/photo-1512290923902-8a9f81dc236c?w=600&q=80",
                date = getCurrentDate(),
                contentUrl = "https://health.gov/myhealthfinder"
            ),
            Article(
                id = "f3",
                title = "12 Natural Remedies For Dry Skin in Winter",
                imageUrl = "https://images.unsplash.com/photo-1556228453-efd6c1ff04f6?w=600&q=80",
                date = getCurrentDate(),
                contentUrl = "https://health.gov/myhealthfinder"
            ),
            Article(
                id = "f4",
                title = "How To Maintain A Healthy Diet Throughout The Year",
                imageUrl = "https://images.unsplash.com/photo-1490645935967-10de6ba17061?w=600&q=80",
                date = getCurrentDate(),
                contentUrl = "https://health.gov/myhealthfinder/health-conditions/diabetes/eat-healthy"
            ),
            Article(
                id = "f5",
                title = "The Importance of Regular Exercise For Heart Health",
                imageUrl = "https://images.unsplash.com/photo-1538805060514-97d9cc17730c?w=600&q=80",
                date = getCurrentDate(),
                contentUrl = "https://health.gov/myhealthfinder/health-conditions/heart-health/get-active"
            ),
            Article(
                id = "f6",
                title = "Understanding Mental Health and Wellness",
                imageUrl = "https://images.unsplash.com/photo-1544367567-0f2fcb009e0b?w=600&q=80",
                date = getCurrentDate(),
                contentUrl = "https://health.gov/myhealthfinder/health-conditions/mental-health-and-relationships/mental-health"
            )
        )
    }

    private fun getCurrentDate(): String {
        val now = Clock.System.now()
        val date = now.toLocalDateTime(TimeZone.currentSystemDefault())
        return "${date.year}-${date.monthNumber}-${date.dayOfMonth}"
    }
}