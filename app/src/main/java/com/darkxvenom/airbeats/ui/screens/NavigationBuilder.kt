package com.darkxvenom.airbeats.ui.screens

import android.annotation.SuppressLint
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.FastOutLinearInEasing
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.navigation.NavBackStackEntry
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import com.darkxvenom.airbeats.ui.component.BottomSheetState
import androidx.compose.ui.window.DialogProperties
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.compose.dialog
import androidx.navigation.navArgument
import com.darkxvenom.airbeats.BuildConfig
import com.darkxvenom.airbeats.constants.HomeScreenStyle
import com.darkxvenom.airbeats.constants.HomeScreenStyleKey
import com.darkxvenom.airbeats.utils.rememberEnumPreference
import com.darkxvenom.airbeats.ui.screens.artist.ArtistItemsScreen
import com.darkxvenom.airbeats.ui.screens.artist.ArtistScreen
import com.darkxvenom.airbeats.ui.screens.artist.ArtistSongsScreen
import com.darkxvenom.airbeats.ui.screens.library.CachePlaylistScreen
import com.darkxvenom.airbeats.ui.screens.library.LibraryScreen
import com.darkxvenom.airbeats.ui.screens.library.PlayfulLibraryScreen
import com.darkxvenom.airbeats.ui.screens.playlist.AutoPlaylistScreen
import com.darkxvenom.airbeats.ui.screens.playlist.LocalPlaylistScreen
import com.darkxvenom.airbeats.ui.screens.playlist.OnlinePlaylistScreen
import com.darkxvenom.airbeats.ui.screens.playlist.TopPlaylistScreen
import com.darkxvenom.airbeats.ui.screens.search.OnlineSearchResult
import com.darkxvenom.airbeats.ui.screens.settings.AboutScreen
import com.darkxvenom.airbeats.ui.screens.settings.AccountSettings
import com.darkxvenom.airbeats.ui.screens.settings.AODSettings
import com.darkxvenom.airbeats.ui.screens.settings.AppearanceSettings
import com.darkxvenom.airbeats.ui.screens.settings.BackupAndRestore
import com.darkxvenom.airbeats.ui.screens.settings.ContentSettings
import com.darkxvenom.airbeats.ui.screens.settings.DiscordLoginScreen
import com.darkxvenom.airbeats.ui.screens.settings.DiscordSettings
import com.darkxvenom.airbeats.ui.screens.settings.PlayerSettings
import com.darkxvenom.airbeats.ui.screens.settings.PrivacySettings
import com.darkxvenom.airbeats.ui.screens.settings.SettingsScreen
import com.darkxvenom.airbeats.ui.screens.settings.StorageSettings

private val slideEnterTransition: (AnimatedContentTransitionScope<NavBackStackEntry>.() -> EnterTransition) = {
    fadeIn(tween(300, easing = LinearOutSlowInEasing)) +
        slideInHorizontally(
            initialOffsetX = { (it * 0.28f).toInt() },
            animationSpec = tween(320, easing = FastOutSlowInEasing)
        )
}

private val slideExitTransition: (AnimatedContentTransitionScope<NavBackStackEntry>.() -> ExitTransition) = {
    fadeOut(tween(260, easing = FastOutLinearInEasing)) +
        slideOutHorizontally(
            targetOffsetX = { (-it * 0.20f).toInt() },
            animationSpec = tween(300, easing = FastOutSlowInEasing)
        )
}

private val slidePopEnterTransition: (AnimatedContentTransitionScope<NavBackStackEntry>.() -> EnterTransition) = {
    fadeIn(tween(300, easing = LinearOutSlowInEasing)) +
        slideInHorizontally(
            initialOffsetX = { (-it * 0.20f).toInt() },
            animationSpec = tween(300, easing = FastOutSlowInEasing)
        )
}

private val slidePopExitTransition: (AnimatedContentTransitionScope<NavBackStackEntry>.() -> ExitTransition) = {
    fadeOut(tween(260, easing = FastOutLinearInEasing)) +
        slideOutHorizontally(
            targetOffsetX = { (it * 0.28f).toInt() },
            animationSpec = tween(320, easing = FastOutSlowInEasing)
        )
}

@RequiresApi(Build.VERSION_CODES.TIRAMISU)
@SuppressLint("UnrememberedMutableState")
@OptIn(ExperimentalMaterial3Api::class)
fun NavGraphBuilder.navigationBuilder(
    navController: NavHostController,
    scrollBehavior: TopAppBarScrollBehavior,
    latestVersionName: String,
    playerBottomSheetState: BottomSheetState,
    onSearchClick: () -> Unit,
) {
    composable(Screens.Home.route) {
        HomeScreen(navController = navController, onSearchClick = onSearchClick)
    }

    composable(Screens.Library.route) {
        LibraryScreen(navController)
    }
    composable(Screens.Explore.route) {
        ExploreScreen(navController, scrollBehavior)
    }
    composable(
        route = Screens.Search.route,
        enterTransition = slideEnterTransition,
        exitTransition = slideExitTransition,
        popEnterTransition = slidePopEnterTransition,
        popExitTransition = slidePopExitTransition,
    ) {
        SpotifySearchScreen(navController = navController)
    }
    composable(
        route = "search/",
        enterTransition = slideEnterTransition,
        exitTransition = slideExitTransition,
        popEnterTransition = slidePopEnterTransition,
        popExitTransition = slidePopExitTransition,
    ) {
        SpotifySearchScreen(navController = navController)
    }
    composable("history") {
        HistoryScreen(navController)
    }
    composable("onboarding") {
        HomeScreen(navController = navController, onSearchClick = onSearchClick)
    }
    composable("guest_profile_setup") {
        HomeScreen(navController = navController, onSearchClick = onSearchClick)
    }
    composable("neon_search") {
        com.darkxvenom.airbeats.ui.screens.search.NeonSearchScreen(navController = navController)
    }
    composable("stats") {
        StatsScreen(navController)
    }
    composable("account") {
        AccountScreen(navController, scrollBehavior)
    }
    composable("spotify_login") {
        SpotifyLoginScreen(navController)
    }
    composable("spotify_account") {
        com.darkxvenom.airbeats.ui.screens.settings.SpotifyAccountScreen(navController)
    }
    composable(
        route = "new_release",
        enterTransition = slideEnterTransition,
        exitTransition = slideExitTransition,
        popEnterTransition = slidePopEnterTransition,
        popExitTransition = slidePopExitTransition,
    ) {
        NewReleaseScreen(navController, scrollBehavior)
    }
    composable("insight") {
        InsightScreen(navController)
    }
    composable("year_in_music") {
        YearInMusicScreen(navController)
    }
    composable("listen_together") {
        ListenTogetherScreen(navController, scrollBehavior)
    }
    composable(com.darkxvenom.airbeats.ui.screens.musicrecognition.MusicRecognitionRoute) {
        com.darkxvenom.airbeats.ui.screens.musicrecognition.MusicRecognitionScreen(navController)
    }

    composable(
        route = "search/{query}",
        arguments =
            listOf(
                navArgument("query") {
                    type = NavType.StringType
                },
            ),
        enterTransition = slideEnterTransition,
        exitTransition = slideExitTransition,
        popEnterTransition = slidePopEnterTransition,
        popExitTransition = slidePopExitTransition,
    ) {
        OnlineSearchResult(navController = navController)
    }
    composable(
        route = "album/{albumId}",
        arguments =
            listOf(
                navArgument("albumId") {
                    type = NavType.StringType
                },
            ),
    ) {
        AlbumScreen(navController, scrollBehavior)
    }
    composable(
        route = "artist/{artistId}",
        arguments =
            listOf(
                navArgument("artistId") {
                    type = NavType.StringType
                },
            ),
    ) { backStackEntry ->
        val artistId = backStackEntry.arguments?.getString("artistId")!!
        if (artistId.startsWith("LA")) {
            ArtistSongsScreen(navController, scrollBehavior)
        } else {
            ArtistScreen(navController, scrollBehavior)
        }
    }
    composable(
        route = "artist/{artistId}/songs",
        arguments =
            listOf(
                navArgument("artistId") {
                    type = NavType.StringType
                },
            ),
    ) {
        ArtistSongsScreen(navController, scrollBehavior)
    }
    composable(
        route = "artist/{artistId}/items?browseId={browseId}&params={params}",
        arguments =
            listOf(
                navArgument("artistId") {
                    type = NavType.StringType
                },
                navArgument("browseId") {
                    type = NavType.StringType
                    nullable = true
                },
                navArgument("params") {
                    type = NavType.StringType
                    nullable = true
                },
            ),
    ) {
        ArtistItemsScreen(navController, scrollBehavior)
    }
    composable(
        route = "online_playlist/{playlistId}",
        arguments =
            listOf(
                navArgument("playlistId") {
                    type = NavType.StringType
                },
            ),
    ) {
        OnlinePlaylistScreen(navController, scrollBehavior)
    }
    composable(
        route = "local_playlist/{playlistId}",
        arguments =
            listOf(
                navArgument("playlistId") {
                    type = NavType.StringType
                },
            ),
    ) {
        LocalPlaylistScreen(navController, scrollBehavior)
    }
    composable(
        route = "auto_playlist/{playlist}",
        arguments =
            listOf(
                navArgument("playlist") {
                    type = NavType.StringType
                },
            ),
    ) {
        AutoPlaylistScreen(navController, scrollBehavior)
    }
    composable(
        route = "cache_playlist/{playlist}",
        arguments =
            listOf(
                navArgument("playlist") {
                    type = NavType.StringType
                },
            ),
    ) {
        CachePlaylistScreen(navController, scrollBehavior)
    }



    composable(
        route = "top_playlist/{top}",
        arguments =
            listOf(
                navArgument("top") {
                    type = NavType.StringType
                },
            ),
    ) {
        TopPlaylistScreen(navController, scrollBehavior)
    }
    composable(
        route = "youtube_browse/{browseId}?params={params}",
        arguments =
            listOf(
                navArgument("browseId") {
                    type = NavType.StringType
                    nullable = true
                },
                navArgument("params") {
                    type = NavType.StringType
                    nullable = true
                },
            ),
    ) {
        YouTubeBrowseScreen(navController)
    }


    composable(
        route = "settings",
        enterTransition = slideEnterTransition,
        exitTransition = slideExitTransition,
        popEnterTransition = slidePopEnterTransition,
        popExitTransition = slidePopExitTransition,
    ) {
        val latestVersion by mutableLongStateOf(BuildConfig.VERSION_CODE.toLong())
        SettingsScreen(latestVersion, navController, scrollBehavior)
    }
    composable(
        route = "settings/appearance",
        enterTransition = slideEnterTransition,
        exitTransition = slideExitTransition,
        popEnterTransition = slidePopEnterTransition,
        popExitTransition = slidePopExitTransition,
    ) {
        AppearanceSettings(navController, scrollBehavior)
    }
    composable(
        route = "settings/dynamic_island",
        enterTransition = slideEnterTransition,
        exitTransition = slideExitTransition,
        popEnterTransition = slidePopEnterTransition,
        popExitTransition = slidePopExitTransition,
    ) {
        com.darkxvenom.airbeats.ui.screens.settings.DynamicIslandSettings(navController, scrollBehavior)
    }
    composable(
        route = "settings/always_on_display",
        enterTransition = slideEnterTransition,
        exitTransition = slideExitTransition,
        popEnterTransition = slidePopEnterTransition,
        popExitTransition = slidePopExitTransition,
    ) {
        AODSettings(navController, scrollBehavior)
    }
    composable(
        route = "settings/account",
        enterTransition = slideEnterTransition,
        exitTransition = slideExitTransition,
        popEnterTransition = slidePopEnterTransition,
        popExitTransition = slidePopExitTransition,
    ) {
        AccountSettings(navController, scrollBehavior)
    }
    composable(
        route = "settings/content",
        enterTransition = slideEnterTransition,
        exitTransition = slideExitTransition,
        popEnterTransition = slidePopEnterTransition,
        popExitTransition = slidePopExitTransition,
    ) {
        ContentSettings(navController, scrollBehavior)
    }
    composable(
        route = "settings/player",
        enterTransition = slideEnterTransition,
        exitTransition = slideExitTransition,
        popEnterTransition = slidePopEnterTransition,
        popExitTransition = slidePopExitTransition,
    ) {
        PlayerSettings(navController, scrollBehavior)
    }
    composable(
        route = "settings/storage",
        enterTransition = slideEnterTransition,
        exitTransition = slideExitTransition,
        popEnterTransition = slidePopEnterTransition,
        popExitTransition = slidePopExitTransition,
    ) {
        StorageSettings(navController, scrollBehavior)
    }
    composable(
        route = "settings/privacy",
        enterTransition = slideEnterTransition,
        exitTransition = slideExitTransition,
        popEnterTransition = slidePopEnterTransition,
        popExitTransition = slidePopExitTransition,
    ) {
        PrivacySettings(navController, scrollBehavior)
    }
    composable(
        route = "settings/backup_restore",
        enterTransition = slideEnterTransition,
        exitTransition = slideExitTransition,
        popEnterTransition = slidePopEnterTransition,
        popExitTransition = slidePopExitTransition,
    ) {
        BackupAndRestore(navController, scrollBehavior)
    }
    composable(
        route = "settings/discord",
        enterTransition = slideEnterTransition,
        exitTransition = slideExitTransition,
        popEnterTransition = slidePopEnterTransition,
        popExitTransition = slidePopExitTransition,
    ) {
        DiscordSettings(navController, scrollBehavior)
    }
    composable(
        route = "settings/about",
        enterTransition = slideEnterTransition,
        exitTransition = slideExitTransition,
        popEnterTransition = slidePopEnterTransition,
        popExitTransition = slidePopExitTransition,
    ) {
        AboutScreen(navController, scrollBehavior)
    }
    composable("login") {
        HomeScreen(navController = navController, onSearchClick = onSearchClick)
    }
        composable("youtube_login") {
            YouTubeLoginScreen(navController)
        }
    composable("contributor/{username}") { backStackEntry ->
        val username = backStackEntry.arguments?.getString("username") ?: return@composable
        ContributorProfileScreen(navController, username)
    }
    dialog(
        route = "always_on_display",
        dialogProperties = DialogProperties(
            usePlatformDefaultWidth = false,
            decorFitsSystemWindows = false
        )
    ) {
        AlwaysOnDisplayScreen(navController)
    }
}






