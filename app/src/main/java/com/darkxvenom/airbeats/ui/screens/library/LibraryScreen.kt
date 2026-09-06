package com.darkxvenom.airbeats.ui.screens.library

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.darkxvenom.airbeats.R
import com.darkxvenom.airbeats.constants.LibraryFilter
import com.darkxvenom.airbeats.ui.component.ChipsRow
import com.darkxvenom.airbeats.ui.component.CircleIconButton

@Composable
fun LibraryScreen(navController: NavController) {
    var filterType by rememberSaveable { mutableStateOf(LibraryFilter.PLAYLISTS) }

    val filterContent: @Composable () -> Unit = {
        Column(
            modifier = Modifier
                .windowInsetsPadding(WindowInsets.systemBars.only(WindowInsetsSides.Horizontal))
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            // Material Design 3 Top Bar: Bold "Library" header + Search and Settings icons
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp, bottom = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(R.string.library),
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )

                Spacer(modifier = Modifier.weight(1f))

                Row(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    CircleIconButton(
                        icon = R.drawable.search,
                        onClick = { navController.navigate("search/") }
                    )
                    CircleIconButton(
                        icon = R.drawable.settings,
                        onClick = { navController.navigate("settings") }
                    )
                }
            }

            // Material Design 3 Filter Chips Row
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
                    filterContent = filterContent,
                    onDeselect = { filterType = LibraryFilter.PLAYLISTS }
                )

            LibraryFilter.ALBUMS ->
                LibraryAlbumsScreen(
                    navController = navController,
                    filterContent = filterContent,
                    onDeselect = { filterType = LibraryFilter.PLAYLISTS }
                )

            LibraryFilter.ARTISTS ->
                LibraryArtistsScreen(
                    navController = navController,
                    filterContent = filterContent,
                    onDeselect = { filterType = LibraryFilter.PLAYLISTS }
                )

            LibraryFilter.LOCAL ->
                LocalSongsScreen(
                    navController = navController,
                    filterContent = filterContent
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
