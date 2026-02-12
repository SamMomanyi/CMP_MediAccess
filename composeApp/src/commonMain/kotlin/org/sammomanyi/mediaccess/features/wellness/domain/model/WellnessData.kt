package org.sammomanyi.mediaccess.features.wellness.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class WellnessData(
    val date: String = "",
    val steps: Int = 0,
    val stepGoal: Int = 10000,
    val hydrationGlasses: Int = 0,
    val hydrationGoal: Int = 8,
    val habits: List<Habit> = defaultHabits(),
    val caloriesBurned: Int = 0,
    val distanceKm: Double = 0.0
)

@Serializable
data class Habit(
    val id: String,
    val title: String,
    val emoji: String,
    val isCompleted: Boolean = false,
    val frequency: String = "Daily"
)

fun defaultHabits() = listOf(
    Habit("h1", "Morning Stretch", "🧘", false),
    Habit("h2", "Take Vitamins", "💊", false),
    Habit("h3", "Meditate", "🧠", false),
    Habit("h4", "Sleep 8hrs", "😴", false),
    Habit("h5", "Avoid Junk Food", "🥗", false),
    Habit("h6", "Exercise", "🏃", false)
)