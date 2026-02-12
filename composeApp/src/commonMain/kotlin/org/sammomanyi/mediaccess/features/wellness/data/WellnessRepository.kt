package org.sammomanyi.mediaccess.features.wellness.data

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.sammomanyi.mediaccess.features.wellness.domain.model.Habit
import org.sammomanyi.mediaccess.features.wellness.domain.model.WellnessData
import org.sammomanyi.mediaccess.features.wellness.domain.model.defaultHabits

class WellnessRepository(
    private val dataStore: DataStore<Preferences>
) {
    companion object {
        val WELLNESS_ONBOARDED = booleanPreferencesKey("wellness_onboarded")
        val WELLNESS_DATE = stringPreferencesKey("wellness_date")
        val WELLNESS_STEPS = intPreferencesKey("wellness_steps")
        val WELLNESS_HYDRATION = intPreferencesKey("wellness_hydration")
        val WELLNESS_HABITS = stringPreferencesKey("wellness_habits")
        val STEP_GOAL = intPreferencesKey("step_goal")
        val HYDRATION_GOAL = intPreferencesKey("hydration_goal")
    }

    private val json = Json { ignoreUnknownKeys = true }

    val isOnboarded: Flow<Boolean> = dataStore.data
        .catch { emit(androidx.datastore.preferences.core.emptyPreferences()) }
        .map { it[WELLNESS_ONBOARDED] ?: false }

    val wellnessData: Flow<WellnessData> = dataStore.data
        .catch { emit(androidx.datastore.preferences.core.emptyPreferences()) }
        .map { prefs ->
            val today = getCurrentDate()
            val savedDate = prefs[WELLNESS_DATE] ?: ""

            // Reset daily stats if date changed
            val steps = if (savedDate == today) prefs[WELLNESS_STEPS] ?: 0 else 0
            val hydration = if (savedDate == today) prefs[WELLNESS_HYDRATION] ?: 0 else 0
            val habitsJson = if (savedDate == today) prefs[WELLNESS_HABITS] else null
            val habits = habitsJson?.let {
                runCatching { json.decodeFromString<List<Habit>>(it) }.getOrNull()
            } ?: defaultHabits()

            WellnessData(
                date = today,
                steps = steps,
                stepGoal = prefs[STEP_GOAL] ?: 10000,
                hydrationGlasses = hydration,
                hydrationGoal = prefs[HYDRATION_GOAL] ?: 8,
                habits = habits,
                caloriesBurned = (steps * 0.04).toInt(),
                distanceKm = steps * 0.000762
            )
        }

    suspend fun setOnboarded(value: Boolean) {
        dataStore.edit { it[WELLNESS_ONBOARDED] = value }
    }

    suspend fun updateSteps(steps: Int) {
        dataStore.edit {
            it[WELLNESS_DATE] = getCurrentDate()
            it[WELLNESS_STEPS] = steps
        }
    }

    suspend fun addHydrationGlass() {
        dataStore.edit { prefs ->
            val current = prefs[WELLNESS_HYDRATION] ?: 0
            val goal = prefs[HYDRATION_GOAL] ?: 8
            if (current < goal + 4) { // Allow slightly over goal
                prefs[WELLNESS_HYDRATION] = current + 1
                prefs[WELLNESS_DATE] = getCurrentDate()
            }
        }
    }

    suspend fun removeHydrationGlass() {
        dataStore.edit { prefs ->
            val current = prefs[WELLNESS_HYDRATION] ?: 0
            if (current > 0) {
                prefs[WELLNESS_HYDRATION] = current - 1
            }
        }
    }

    suspend fun toggleHabit(habitId: String, currentHabits: List<Habit>) {
        val updated = currentHabits.map {
            if (it.id == habitId) it.copy(isCompleted = !it.isCompleted) else it
        }
        dataStore.edit { prefs ->
            prefs[WELLNESS_HABITS] = json.encodeToString(updated)
            prefs[WELLNESS_DATE] = getCurrentDate()
        }
    }

    suspend fun updateGoals(stepGoal: Int, hydrationGoal: Int) {
        dataStore.edit {
            it[STEP_GOAL] = stepGoal
            it[HYDRATION_GOAL] = hydrationGoal
        }
    }

    private fun getCurrentDate(): String {
        val now = Clock.System.now()
        val date = now.toLocalDateTime(TimeZone.currentSystemDefault())
        return "${date.year}-${date.monthNumber}-${date.dayOfMonth}"
    }
}