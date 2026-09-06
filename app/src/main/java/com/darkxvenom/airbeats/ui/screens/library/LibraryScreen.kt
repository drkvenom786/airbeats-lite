package com.darkxvenom.airbeats.ui.screens.library

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.darkxvenom.airbeats.R
import com.darkxvenom.airbeats.constants.LibraryFilter
import com.darkxvenom.airbeats.ui.component.ChipsRow

@Composable
fun LibraryScreen(navController: NavController) {
    var filterType by rememberSaveable { mutableStateOf(LibraryFilter.PLAYLISTS) }

    val filterContent: @Composable () -> Unit = {
        Row(
            modifier = Modifier
                .windowInsetsPadding(WindowInsets.systemBars.only(WindowInsetsSides.Horizontal))
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 6.dp)
        ) {
            ChipsRow(
                chips = listOf(
                    LibraryFilter.PLAYLISTS to stringResource(R.string.filter_playlists),
                    LibraryFilter.SONGS to stringResource(R.string.filter_songs),
                    LibraryFilter.ALBUMS to stringResource(R.string.filter_albums),
                    LibraryFilter.ARTISTS to stringResource(R.string.filter_artists),
                    LibraryFilter.LOCAL to stringResource(R.string.filter_local),
                ),
                currentValue = filterType,
                onValueUpdate = {
                    filterType = if (filterType == it) LibraryFilter.PLAYLISTS else it
                },
                containerColor = MaterialTheme.colorScheme.surfaceContainer
            )
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        when (filterType) {
            LibraryFilter.PLAYLISTS ->
                LibraryPlaylistsScreen(
                    navController = navController,
                    filterContent = filterContent,
                    onLocalClick = { filterType = LibraryFilter.LOCAL }
                )

            LibraryFilter.SONGS ->
                LibrarySongsScreen(
                    navController = navController,
                    onDeselect = { filterType = LibraryFilter.PLAYLISTS }
                )

            LibraryFilter.ALBUMS ->
                LibraryAlbumsScreen(
                    navController = navController,
                    onDeselect = { filterType = LibraryFilter.PLAYLISTS }
                )

            LibraryFilter.ARTISTS ->
                LibraryArtistsScreen(
                    navController = navController,
                    onDeselect = { filterType = LibraryFilter.PLAYLISTS }
                )

            LibraryFilter.LOCAL ->
                LocalSongsScreen(
                    navController = navController
                )

            else ->
                LibraryPlaylistsScreen(
                    navController = navController,
                    filterContent = filterContent,
                    onLocalClick = { filterType = LibraryFilter.LOCAL }
                )
        }
    }
}
