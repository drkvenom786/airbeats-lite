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
import com.darkxvenom.airbeats.ui.screens.playlist.AutoPlaylistScreen
import com.darkxvenom.airbeats.ui.screens.playlist.LocalPlaylistScreen
import com.darkxvenom.airbeats.ui.screens.playlist.OnlinePlaylistScreen
import com.darkxvenom.airbeats.ui.screens.playlist.TopPlaylistScreen
import com.darkxvenom.airbeats.ui.screens.search.OnlineSearchResult
import com.darkxvenom.airbeats.ui.screens.settings.AboutScreen

import com.darkxvenom.airbeats.ui.screens.settings.AccountSettings
import com.darkxvenom.airbeats.ui.screens.settings.AppearanceSettings
import com.darkxvenom.airbeats.ui.screens.settings.BackupAndRestore
import com.darkxvenom.airbeats.ui.screens.settings.ContentSettings
import com.darkxvenom.airbeats.ui.screens.settings.DiscordLoginScreen
import com.darkxvenom.airbeats.ui.screens.settings.DiscordSettings
import com.darkxvenom.airbeats.ui.screens.settings.PlayerSettings
import com.darkxvenom.airbeats.ui.screens.settings.PrivacySettings
import com.darkxvenom.airbeats.ui.screens.settings.SettingsScreen
import com.darkxvenom.airbeats.ui.screens.onboarding.GuestProfileSetupScreen
import com.darkxvenom.airbeats.ui.screens.settings.StorageSettings

private val slideEnterTransition: (AnimatedContentTransitionScope<NavBackStackEntry>.() -> EnterTransition) = {
    fadeIn(tween(220, easing = LinearOutSlowInEasing)) +
        slideInHorizontally(
            initialOffsetX = { (it * 0.12f).toInt() },
            animationSpec = tween(220, easing = FastOutSlowInEasing)
        )
}

private val slideExitTransition: (AnimatedContentTransitionScope<NavBackStackEntry>.() -> ExitTransition) = {
    fadeOut(tween(180, easing = FastOutLinearInEasing)) +
        slideOutHorizontally(
            targetOffsetX = { (-it * 0.08f).toInt() },
            animationSpec = tween(200, easing = FastOutSlowInEasing)
        )
}

private val slidePopEnterTransition: (AnimatedContentTransitionScope<NavBackStackEntry>.() -> EnterTransition) = {
    fadeIn(tween(220, easing = LinearOutSlowInEasing)) +
        slideInHorizontally(
            initialOffsetX = { (-it * 0.08f).toInt() },
            animationSpec = tween(220, easing = FastOutSlowInEasing)
        )
}

private val slidePopExitTransition: (AnimatedContentTransitionScope<NavBackStackEntry>.() -> ExitTransition) = {
    fadeOut(tween(180, easing = FastOutLinearInEasing)) +
        slideOutHorizontally(
            targetOffsetX = { (it * 0.12f).toInt() },
            animationSpec = tween(200, easing = FastOutSlowInEasing)
        )
}

private val fadeEnterTransition: (AnimatedContentTransitionScope<NavBackStackEntry>.() -> EnterTransition) = {
    fadeIn(animationSpec = tween(durationMillis = 220, easing = FastOutSlowInEasing))
}

private val fadeExitTransition: (AnimatedContentTransitionScope<NavBackStackEntry>.() -> ExitTransition) = {
    fadeOut(animationSpec = tween(durationMillis = 180, easing = FastOutSlowInEasing))
}

private val fadePopEnterTransition: (AnimatedContentTransitionScope<NavBackStackEntry>.() -> EnterTransition) = {
    fadeIn(animationSpec = tween(durationMillis = 220, easing = FastOutSlowInEasing))
}

private val fadePopExitTransition: (AnimatedContentTransitionScope<NavBackStackEntry>.() -> ExitTransition) = {
    fadeOut(animationSpec = tween(durationMillis = 180, easing = FastOutSlowInEasing))
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
    composable(
        route = Screens.Home.route,
        enterTransition = fadeEnterTransition,
        exitTransition = fadeExitTransition,
        popEnterTransition = fadePopEnterTransition,
        popExitTransition = fadePopExitTransition,
    ) {
        HomeScreen(navController = navController, onSearchClick = onSearchClick)
    }

    composable(
        route = Screens.Library.route,
        enterTransition = fadeEnterTransition,
        exitTransition = fadeExitTransition,
        popEnterTransition = fadePopEnterTransition,
        popExitTransition = fadePopExitTransition,
    ) {
        LibraryScreen(navController)
    }

    composable(
        route = Screens.Explore.route,
        enterTransition = fadeEnterTransition,
        exitTransition = fadeExitTransition,
        popEnterTransition = fadePopEnterTransition,
        popExitTransition = fadePopExitTransition,
    ) {
        ExploreScreen(navController, scrollBehavior)
    }

    composable(
        route = Screens.Search.route,
        enterTransition = fadeEnterTransition,
        exitTransition = fadeExitTransition,
        popEnterTransition = fadePopEnterTransition,
        popExitTransition = fadePopExitTransition,
    ) {
        SpotifySearchScreen(navController = navController)
    }

    composable(
        route = "search/",
        enterTransition = fadeEnterTransition,
        exitTransition = fadeExitTransition,
        popEnterTransition = fadePopEnterTransition,
        popExitTransition = fadePopExitTransition,
    ) {
        SpotifySearchScreen(navController = navController)
    }

    composable(
        route = "history",
        enterTransition = slideEnterTransition,
        exitTransition = slideExitTransition,
        popEnterTransition = slidePopEnterTransition,
        popExitTransition = slidePopExitTransition,
    ) {
        HistoryScreen(navController)
    }

    composable(
        route = "onboarding",
        enterTransition = slideEnterTransition,
        exitTransition = slideExitTransition,
        popEnterTransition = slidePopEnterTransition,
        popExitTransition = slidePopExitTransition,
    ) {
        GuestProfileSetupScreen(navController = navController)
    }

    composable(
        route = "guest_profile_setup",
        enterTransition = slideEnterTransition,
        exitTransition = slideExitTransition,
        popEnterTransition = slidePopEnterTransition,
        popExitTransition = slidePopExitTransition,
    ) {
        GuestProfileSetupScreen(navController = navController)
    }

    composable(
        route = "neon_search",
        enterTransition = slideEnterTransition,
        exitTransition = slideExitTransition,
        popEnterTransition = slidePopEnterTransition,
        popExitTransition = slidePopExitTransition,
    ) {
        com.darkxvenom.airbeats.ui.screens.search.NeonSearchScreen(navController = navController)
    }

    composable(
        route = "stats",
        enterTransition = slideEnterTransition,
        exitTransition = slideExitTransition,
        popEnterTransition = slidePopEnterTransition,
        popExitTransition = slidePopExitTransition,
    ) {
        StatsScreen(navController)
    }

    composable(
        route = "account",
        enterTransition = slideEnterTransition,
        exitTransition = slideExitTransition,
        popEnterTransition = slidePopEnterTransition,
        popExitTransition = slidePopExitTransition,
    ) {
        AccountScreen(navController, scrollBehavior)
    }

    composable(
        route = "spotify_login",
        enterTransition = slideEnterTransition,
        exitTransition = slideExitTransition,
        popEnterTransition = slidePopEnterTransition,
        popExitTransition = slidePopExitTransition,
    ) {
        SpotifyLoginScreen(navController)
    }

    composable(
        route = "spotify_account",
        enterTransition = slideEnterTransition,
        exitTransition = slideExitTransition,
        popEnterTransition = slidePopEnterTransition,
        popExitTransition = slidePopExitTransition,
    ) {
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

    composable(
        route = "insight",
        enterTransition = slideEnterTransition,
        exitTransition = slideExitTransition,
        popEnterTransition = slidePopEnterTransition,
        popExitTransition = slidePopExitTransition,
    ) {
        InsightScreen(navController)
    }

    composable(
        route = "year_in_music",
        enterTransition = slideEnterTransition,
        exitTransition = slideExitTransition,
        popEnterTransition = slidePopEnterTransition,
        popExitTransition = slidePopExitTransition,
    ) {
        YearInMusicScreen(navController)
    }



    composable(
        route = "search/{query}",
        arguments = listOf(
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
        arguments = listOf(
            navArgument("albumId") {
                type = NavType.StringType
            },
        ),
        enterTransition = slideEnterTransition,
        exitTransition = slideExitTransition,
        popEnterTransition = slidePopEnterTransition,
        popExitTransition = slidePopExitTransition,
    ) {
        AlbumScreen(navController, scrollBehavior)
    }

    composable(
        route = "artist/{artistId}",
        arguments = listOf(
            navArgument("artistId") {
                type = NavType.StringType
            },
        ),
        enterTransition = slideEnterTransition,
        exitTransition = slideExitTransition,
        popEnterTransition = slidePopEnterTransition,
        popExitTransition = slidePopExitTransition,
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
        arguments = listOf(
            navArgument("artistId") {
                type = NavType.StringType
            },
        ),
        enterTransition = slideEnterTransition,
        exitTransition = slideExitTransition,
        popEnterTransition = slidePopEnterTransition,
        popExitTransition = slidePopExitTransition,
    ) {
        ArtistSongsScreen(navController, scrollBehavior)
    }

    composable(
        route = "artist/{artistId}/items?browseId={browseId}&params={params}",
        arguments = listOf(
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
        enterTransition = slideEnterTransition,
        exitTransition = slideExitTransition,
        popEnterTransition = slidePopEnterTransition,
        popExitTransition = slidePopExitTransition,
    ) {
        ArtistItemsScreen(navController, scrollBehavior)
    }

    composable(
        route = "online_playlist/{playlistId}",
        arguments = listOf(
            navArgument("playlistId") {
                type = NavType.StringType
            },
        ),
        enterTransition = slideEnterTransition,
        exitTransition = slideExitTransition,
        popEnterTransition = slidePopEnterTransition,
        popExitTransition = slidePopExitTransition,
    ) {
        OnlinePlaylistScreen(navController, scrollBehavior)
    }

    composable(
        route = "local_playlist/{playlistId}",
        arguments = listOf(
            navArgument("playlistId") {
                type = NavType.StringType
            },
        ),
        enterTransition = slideEnterTransition,
        exitTransition = slideExitTransition,
        popEnterTransition = slidePopEnterTransition,
        popExitTransition = slidePopExitTransition,
    ) {
        LocalPlaylistScreen(navController, scrollBehavior)
    }

    composable(
        route = "auto_playlist/{playlist}",
        arguments = listOf(
            navArgument("playlist") {
                type = NavType.StringType
            },
        ),
        enterTransition = slideEnterTransition,
        exitTransition = slideExitTransition,
        popEnterTransition = slidePopEnterTransition,
        popExitTransition = slidePopExitTransition,
    ) {
        AutoPlaylistScreen(navController, scrollBehavior)
    }

    composable(
        route = "cache_playlist/{playlist}",
        arguments = listOf(
            navArgument("playlist") {
                type = NavType.StringType
            },
        ),
        enterTransition = slideEnterTransition,
        exitTransition = slideExitTransition,
        popEnterTransition = slidePopEnterTransition,
        popExitTransition = slidePopExitTransition,
    ) {
        CachePlaylistScreen(navController, scrollBehavior)
    }

    composable(
        route = "top_playlist/{top}",
        arguments = listOf(
            navArgument("top") {
                type = NavType.StringType
            },
        ),
        enterTransition = slideEnterTransition,
        exitTransition = slideExitTransition,
        popEnterTransition = slidePopEnterTransition,
        popExitTransition = slidePopExitTransition,
    ) {
        TopPlaylistScreen(navController, scrollBehavior)
    }

    composable(
        route = "youtube_browse/{browseId}?params={params}",
        arguments = listOf(
            navArgument("browseId") {
                type = NavType.StringType
                nullable = true
            },
            navArgument("params") {
                type = NavType.StringType
                nullable = true
            },
        ),
        enterTransition = slideEnterTransition,
        exitTransition = slideExitTransition,
        popEnterTransition = slidePopEnterTransition,
        popExitTransition = slidePopExitTransition,
    ) {
        YouTubeBrowseScreen(navController)
    }

    composable(
        route = "settings",
        enterTransition = fadeEnterTransition,
        exitTransition = fadeExitTransition,
        popEnterTransition = fadePopEnterTransition,
        popExitTransition = fadePopExitTransition,
    ) {
        val latestVersion by mutableLongStateOf(BuildConfig.VERSION_CODE.toLong())
        SettingsScreen(latestVersion, navController, scrollBehavior)
    }

    composable(
        route = "settings/appearance",
        enterTransition = fadeEnterTransition,
        exitTransition = fadeExitTransition,
        popEnterTransition = fadePopEnterTransition,
        popExitTransition = fadePopExitTransition,
    ) {
        AppearanceSettings(navController, scrollBehavior)
    }

    composable(
        route = "settings/dynamic_island",
        enterTransition = fadeEnterTransition,
        exitTransition = fadeExitTransition,
        popEnterTransition = fadePopEnterTransition,
        popExitTransition = fadePopExitTransition,
    ) {
        com.darkxvenom.airbeats.ui.screens.settings.DynamicIslandSettings(navController, scrollBehavior)
    }



    composable(
        route = "settings/account",
        enterTransition = fadeEnterTransition,
        exitTransition = fadeExitTransition,
        popEnterTransition = fadePopEnterTransition,
        popExitTransition = fadePopExitTransition,
    ) {
        AccountSettings(navController, scrollBehavior)
    }

    composable(
        route = "settings/content",
        enterTransition = fadeEnterTransition,
        exitTransition = fadeExitTransition,
        popEnterTransition = fadePopEnterTransition,
        popExitTransition = fadePopExitTransition,
    ) {
        ContentSettings(navController, scrollBehavior)
    }

    composable(
        route = "settings/player",
        enterTransition = fadeEnterTransition,
        exitTransition = fadeExitTransition,
        popEnterTransition = fadePopEnterTransition,
        popExitTransition = fadePopExitTransition,
    ) {
        PlayerSettings(navController, scrollBehavior)
    }

    composable(
        route = "settings/storage",
        enterTransition = fadeEnterTransition,
        exitTransition = fadeExitTransition,
        popEnterTransition = fadePopEnterTransition,
        popExitTransition = fadePopExitTransition,
    ) {
        StorageSettings(navController, scrollBehavior)
    }

    composable(
        route = "settings/privacy",
        enterTransition = fadeEnterTransition,
        exitTransition = fadeExitTransition,
        popEnterTransition = fadePopEnterTransition,
        popExitTransition = fadePopExitTransition,
    ) {
        PrivacySettings(navController, scrollBehavior)
    }

    composable(
        route = "settings/backup_restore",
        enterTransition = fadeEnterTransition,
        exitTransition = fadeExitTransition,
        popEnterTransition = fadePopEnterTransition,
        popExitTransition = fadePopExitTransition,
    ) {
        BackupAndRestore(navController, scrollBehavior)
    }

    composable(
        route = "settings/discord",
        enterTransition = fadeEnterTransition,
        exitTransition = fadeExitTransition,
        popEnterTransition = fadePopEnterTransition,
        popExitTransition = fadePopExitTransition,
    ) {
        DiscordSettings(navController, scrollBehavior)
    }

    composable(
        route = "settings/about",
        enterTransition = fadeEnterTransition,
        exitTransition = fadeExitTransition,
        popEnterTransition = fadePopEnterTransition,
        popExitTransition = fadePopExitTransition,
    ) {
        AboutScreen(navController, scrollBehavior)
    }

    composable(
        route = "login",
        enterTransition = slideEnterTransition,
        exitTransition = slideExitTransition,
        popEnterTransition = slidePopEnterTransition,
        popExitTransition = slidePopExitTransition,
    ) {
        HomeScreen(navController = navController, onSearchClick = onSearchClick)
    }

    composable(
        route = "contributor/{username}",
        enterTransition = slideEnterTransition,
        exitTransition = slideExitTransition,
        popEnterTransition = slidePopEnterTransition,
        popExitTransition = slidePopExitTransition,
    ) { backStackEntry ->
        val username = backStackEntry.arguments?.getString("username") ?: return@composable
        ContributorProfileScreen(navController, username)
    }
}
