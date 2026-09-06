package com.darkxvenom.airbeats.ui.component

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.rankDataStore by preferencesDataStore("rank_badge_prefs")

@Singleton
class RankPreferenceManager @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    companion object {
        private val DISPLAYED_RANK_KEY = stringPreferencesKey("displayed_rank")
        private val LAST_SEEN_RANK_KEY = stringPreferencesKey("last_seen_rank")
    }

    /** The badge the user chose to display (null = auto = real rank). */
    val displayedRank: Flow<AirBeatsRank?> = context.rankDataStore.data.map { prefs ->
        prefs[DISPLAYED_RANK_KEY]?.let { runCatching { AirBeatsRank.valueOf(it) }.getOrNull() }
    }

    /** The highest rank the user has been notified about (used for rank-up popup). */
    val lastSeenRank: Flow<AirBeatsRank?> = context.rankDataStore.data.map { prefs ->
        prefs[LAST_SEEN_RANK_KEY]?.let { runCatching { AirBeatsRank.valueOf(it) }.getOrNull() }
    }

    suspend fun saveDisplayedRank(rank: AirBeatsRank?) {
        context.rankDataStore.edit { prefs ->
            if (rank != null) prefs[DISPLAYED_RANK_KEY] = rank.name
            else prefs.remove(DISPLAYED_RANK_KEY)
        }
    }

    suspend fun saveLastSeenRank(rank: AirBeatsRank) {
        context.rankDataStore.edit { prefs ->
            prefs[LAST_SEEN_RANK_KEY] = rank.name
        }
    }
}
