package com.darkxvenom.airbeats.ui.screens.settings

import android.annotation.SuppressLint
import android.app.DownloadManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.*
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.platform.UriHandler
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.core.net.toUri
import androidx.navigation.NavController
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.darkxvenom.airbeats.innertube.utils.parseCookieString
import com.darkxvenom.airbeats.BuildConfig
import com.darkxvenom.airbeats.LocalPlayerAwareWindowInsets
import com.darkxvenom.airbeats.LocalPlayerConnection
import com.darkxvenom.airbeats.R
import com.darkxvenom.airbeats.constants.AccountNameKey
import com.darkxvenom.airbeats.constants.InnerTubeCookieKey
import com.darkxvenom.airbeats.ui.component.AvatarPreferenceManager
import com.darkxvenom.airbeats.ui.component.AvatarSelection
import com.darkxvenom.airbeats.ui.component.ChangelogScreen
import com.darkxvenom.airbeats.ui.component.UpdateAvailableDialog
import com.darkxvenom.airbeats.utils.UpdateInfo
import com.darkxvenom.airbeats.utils.Updater
import com.darkxvenom.airbeats.utils.rememberPreference
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.URL

// ==================== DIVIDER COMPONENT ====================

@Composable
fun SettingsDivider(
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f),
    thickness: Dp = 0.5.dp
) {
    HorizontalDivider(
        modifier = modifier.fillMaxWidth(),
        thickness = thickness,
        color = color
    )
}

// ==================== SETTINGS CATEGORY COMPONENTS ====================

data class SettingsCategoryItem(
    val icon: androidx.compose.ui.graphics.painter.Painter,
    val title: @Composable () -> Unit,
    val trailingContent: @Composable (() -> Unit)? = null,
    val onClick: () -> Unit
)

@Composable
fun SettingsCategory(
    title: String,
    items: List<SettingsCategoryItem>,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Text(
            text = title,
            modifier = Modifier.padding(start = 20.dp, bottom = 8.dp),
            style = MaterialTheme.typography.titleSmall.copy(
                fontWeight = FontWeight.SemiBold,
                letterSpacing = 0.5.sp
            ),
            color = MaterialTheme.colorScheme.primary
        )

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceContainer
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
        ) {
            Column(
                modifier = Modifier.padding(vertical = 4.dp)
            ) {
                items.forEachIndexed { index, item ->
                    SettingsCategoryItemContent(
                        item = item,
                        isLast = index == items.lastIndex
                    )
                }
            }
        }
    }
}

@Composable
fun SettingsCategoryItemContent(
    item: SettingsCategoryItem,
    isLast: Boolean
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .clickable(onClick = item.onClick)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Icon container
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(
                    MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f)
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                painter = item.icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(20.dp)
            )
        }

        Spacer(modifier = Modifier.width(16.dp))

        // Title
        Box(modifier = Modifier.weight(1f)) {
            item.title()
        }

        // Trailing content
        if (item.trailingContent != null) {
            item.trailingContent()
        } else {
            Icon(
                painter = painterResource(R.drawable.arrow_forward),
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                modifier = Modifier.size(18.dp)
            )
        }
    }

    if (!isLast) {
        HorizontalDivider(
            modifier = Modifier.padding(start = 72.dp, end = 16.dp),
            thickness = 0.5.dp,
            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)
        )
    }
}

// ==================== ORIGINAL FUNCTIONS ====================

@SuppressLint("ObsoleteSdkInt")
@RequiresApi(Build.VERSION_CODES.TIRAMISU)
fun getAppVersion(context: Context): String {
    return try {
        val packageInfo = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            context.packageManager.getPackageInfo(
                context.packageName,
                PackageManager.PackageInfoFlags.of(0)
            )
        } else {
            @Suppress("DEPRECATION")
            context.packageManager.getPackageInfo(
                context.packageName,
                0
            )
        }
        packageInfo.versionName ?: "Unknown"
    } catch (e: PackageManager.NameNotFoundException) {
        "Unknown"
    }
}

@RequiresApi(Build.VERSION_CODES.TIRAMISU)
@Composable
fun VersionCard(uriHandler: UriHandler) {
    val context = LocalContext.current
    val appVersion = remember { getAppVersion(context) }

    Spacer(Modifier.height(16.dp))

    Column(modifier = Modifier.padding(horizontal = 16.dp)) {
        Text(
            text = stringResource(R.string.app_info),
            modifier = Modifier.padding(start = 20.dp, bottom = 8.dp),
            style = MaterialTheme.typography.titleSmall.copy(
                fontWeight = FontWeight.SemiBold,
                letterSpacing = 0.5.sp
            ),
            color = MaterialTheme.colorScheme.primary
        )

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceContainer
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
        ) {
            Column(
                modifier = Modifier.padding(vertical = 4.dp)
            ) {
                // Version item
                SettingsCategoryItemContent(
                    item = SettingsCategoryItem(
                        icon = painterResource(R.drawable.info),
                        title = {
                            Column {
                                Text(
                                    text = "AirBeats Lite",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Text(
                                    text = "v$appVersion",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontFamily = FontFamily.Monospace
                                )
                            }
                        },
                        trailingContent = {
                            Icon(
                                painter = painterResource(R.drawable.arrow_forward),
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                                modifier = Modifier.size(18.dp)
                            )
                        },
                        onClick = { uriHandler.openUri("https://github.com/drkvenom786/airbeats-lite/releases/latest") }
                    ),
                    isLast = false
                )

                // Website item
                SettingsCategoryItemContent(
                    item = SettingsCategoryItem(
                        icon = painterResource(R.drawable.resource_public),
                        title = {
                            Text(
                                text = "Official Website",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        },
                        trailingContent = {
                            Icon(
                                painter = painterResource(R.drawable.arrow_forward),
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                                modifier = Modifier.size(18.dp)
                            )
                        },
                        onClick = { uriHandler.openUri("https://airbeats.app") }
                    ),
                    isLast = true
                )
            }
        }
    }
}

@Composable
fun UpdateCard(latestVersion: String = "") {
    val context = LocalContext.current
    var showUpdateCard by remember { mutableStateOf(false) }
    var currentLatestVersion by remember { mutableStateOf(latestVersion) }
    var updateInfoState by remember { mutableStateOf<UpdateInfo?>(null) }
    var showDownloadDialog by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        Updater.getLatestUpdateInfo().onSuccess { info ->
            if (info.versionName.isNotBlank() && isNewerVersion(info.versionName, BuildConfig.VERSION_NAME)) {
                showUpdateCard = true
                currentLatestVersion = info.versionName
                updateInfoState = info
            }
        }.onFailure {
            val newVersion = checkForUpdates()
            if (newVersion != null && isNewerVersion(newVersion, BuildConfig.VERSION_NAME)) {
                showUpdateCard = true
                currentLatestVersion = newVersion
                updateInfoState = UpdateInfo(versionName = newVersion)
            }
        }
    }

    if (showDownloadDialog) {
        updateInfoState?.let { info ->
            UpdateAvailableDialog(
                updateInfo = info,
                onDismiss = { showDownloadDialog = false }
            )
        } ?: UpdateDownloadDialog(
            latestVersion = currentLatestVersion,
            onDismiss = { showDownloadDialog = false }
        )
    }

    if (showUpdateCard) {
        Spacer(Modifier.height(16.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
        ) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.4f)
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(24.dp))
                        .clickable { showDownloadDialog = true }
                        .padding(20.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.secondaryContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.update),
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.secondary,
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = stringResource(R.string.NewVersion) + ": $currentLatestVersion",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.secondary,
                            fontWeight = FontWeight.Bold
                        )

                        Spacer(modifier = Modifier.height(2.dp))

                        Text(
                            text = stringResource(R.string.tap_to_update),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Icon(
                        painter = painterResource(R.drawable.download),
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.secondary,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun UpdateDownloadDialog(
    latestVersion: String,
    onDismiss: () -> Unit
) {
    val uriHandler = LocalUriHandler.current
    var downloadStatus by remember { mutableStateOf(DownloadStatus.NOT_STARTED) }

    Dialog(onDismissRequest = {
        if (downloadStatus != DownloadStatus.REDIRECTING) {
            onDismiss()
        }
    }) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(28.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceContainer
            )
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = stringResource(id = R.string.update_version, latestVersion),
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(16.dp))

                when (downloadStatus) {
                    DownloadStatus.NOT_STARTED -> {
                        Text(
                            stringResource(R.string.download_question),
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(24.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            OutlinedButton(
                                onClick = onDismiss,
                                modifier = Modifier.weight(1f)
                            ) {
                                Text(stringResource(R.string.cancel))
                            }

                            Button(
                                onClick = {
                                    downloadStatus = DownloadStatus.REDIRECTING
                                    val downloadUrl = if (com.darkxvenom.airbeats.BuildConfig.IS_NIGHTLY) {
                                        "https://github.com/drkvenom786/airbeats-lite/releases/download/v${latestVersion}-nightly/Airbeats-v${latestVersion}-Nightly.apk"
                                    } else {
                                        "https://github.com/drkvenom786/airbeats-lite/releases/download/v$latestVersion/AirBeats-Lite-v${latestVersion}.apk"
                                    }
                                    uriHandler.openUri(downloadUrl)
                                    downloadStatus = DownloadStatus.COMPLETED
                                    onDismiss()
                                },
                                modifier = Modifier.weight(1f)
                            ) {
                                Text(stringResource(R.string.download))
                            }
                        }
                    }

                    DownloadStatus.REDIRECTING -> {
                        Text(
                            stringResource(R.string.opening_browser),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        CircularProgressIndicator(
                            modifier = Modifier.size(48.dp),
                            strokeWidth = 4.dp,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }

                    DownloadStatus.COMPLETED -> {
                        Text(
                            stringResource(R.string.download_started),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(
                            onClick = onDismiss,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(stringResource(R.string.close))
                        }
                    }

                    DownloadStatus.ERROR -> {
                        Text(
                            stringResource(R.string.download_errorup),
                            color = MaterialTheme.colorScheme.error
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(
                            onClick = onDismiss,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(stringResource(R.string.close))
                        }
                    }
                }
            }
        }
    }
}

enum class DownloadStatus {
    NOT_STARTED,
    REDIRECTING,
    COMPLETED,
    ERROR
}

suspend fun checkForUpdates(): String? = withContext(Dispatchers.IO) {
    try {
        if (com.darkxvenom.airbeats.BuildConfig.IS_NIGHTLY) {
            val url = java.net.URL("https://api.github.com/repos/drkvenom786/airbeats-lite/releases")
            val connection = url.openConnection()
            connection.connect()
            val json = connection.getInputStream().bufferedReader().use { it.readText() }
            val jsonArray = org.json.JSONArray(json)
            for (i in 0 until jsonArray.length()) {
                val release = jsonArray.getJSONObject(i)
                if (release.getBoolean("prerelease")) {
                    val tagName = release.getString("tag_name")
                    return@withContext tagName.removePrefix("v").removeSuffix("-nightly").trim()
                }
            }
            return@withContext null
        } else {
            val url = java.net.URL("https://api.github.com/repos/drkvenom786/airbeats-lite/releases/latest")
            val connection = url.openConnection()
            connection.connect()
            val json = connection.getInputStream().bufferedReader().use { it.readText() }
            val jsonObject = org.json.JSONObject(json)
            val tagName = jsonObject.getString("tag_name")
            return@withContext tagName.removePrefix("v").trim()
        }
    } catch (e: Exception) {
        e.printStackTrace()
        return@withContext null
    }
}

fun isNewerVersion(remoteVersion: String, currentVersion: String): Boolean {
    fun normalize(version: String): List<Int> {
        return version
            .replace(Regex("[^0-9.]"), "")
            .split(".")
            .map { it.toIntOrNull() ?: 0 }
    }

    val remote = normalize(remoteVersion)
    val current = normalize(currentVersion)

    for (i in 0 until maxOf(remote.size, current.size)) {
        val r = remote.getOrNull(i) ?: 0
        val c = current.getOrNull(i) ?: 0
        if (r > c) return true
        if (r < c) return false
    }

    return false
}

// ==================== MAIN SETTINGS SCREEN ====================
@RequiresApi(Build.VERSION_CODES.TIRAMISU)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    latestVersion: Long,
    navController: NavController,
    scrollBehavior: TopAppBarScrollBehavior,
) {
    val uriHandler = LocalUriHandler.current
    var showTranslateDialog by remember { mutableStateOf(false) }
    var showChangelogSheet by remember { mutableStateOf(false) }

    Box(modifier = Modifier.fillMaxSize()) {
        // Main Scaffold with TopAppBar that scrolls
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            containerColor = MaterialTheme.colorScheme.background,
            topBar = {
                // U-Shaped TopAppBar that scrolls with content
                TopAppBar(
                    title = {
                        Box(
                            modifier = Modifier.fillMaxWidth(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = stringResource(R.string.settings),
                                fontSize = 20.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    },
                    navigationIcon = {
                        Spacer(modifier = Modifier.width(48.dp))
                    },
                    actions = {
                        Spacer(modifier = Modifier.width(48.dp))
                    },
                    modifier = Modifier
                        .clip(
                            RoundedCornerShape(
                                bottomStart = 30.dp,
                                bottomEnd = 30.dp
                            )
                        )
                        .background(
                            Brush.verticalGradient(
                                listOf(
                                    MaterialTheme.colorScheme.surface.copy(alpha = 0.85f),
                                    MaterialTheme.colorScheme.surface.copy(alpha = 0.65f)
                                )
                            )
                        )
                        .border(
                            width = 0.6.dp,
                            brush = Brush.horizontalGradient(
                                listOf(
                                    Color.White.copy(alpha = 0.3f),
                                    Color.White.copy(alpha = 0.1f),
                                    Color.White.copy(alpha = 0.3f)
                                )
                            ),
                            shape = RoundedCornerShape(
                                bottomStart = 30.dp,
                                bottomEnd = 30.dp
                            )
                        ),
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Color.Transparent,
                        scrolledContainerColor = Color.Transparent
                    ),
                    scrollBehavior = scrollBehavior
                )
            }
        ) { innerPadding ->
            // Content with proper padding from Scaffold
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding) // VERY IMPORTANT
                    .verticalScroll(rememberScrollState())
                    .windowInsetsPadding(
                        LocalPlayerAwareWindowInsets.current.only(
                            WindowInsetsSides.Horizontal + WindowInsetsSides.Bottom
                        )
                    )
            )

            {
                // Add a small top padding to separate from header

                val context = LocalContext.current
                val avatarManager = remember { AvatarPreferenceManager(context) }
                val currentSelection by avatarManager.getAvatarSelection.collectAsState(initial = AvatarSelection.Default)
                val accountName by rememberPreference(AccountNameKey, "")
                val innerTubeCookie by rememberPreference(InnerTubeCookieKey, "")
                val isLoggedIn = remember(innerTubeCookie) {
                    "SAPISID" in parseCookieString(innerTubeCookie)
                }

                // Profile Section
                ProfileSection(
                    isLoggedIn = isLoggedIn,
                    accountName = accountName,
                    currentSelection = currentSelection
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Settings Categories
                Column(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // General Settings
                    SettingsCategory(
                        title = stringResource(R.string.general_settings),
                        items = listOf(
                            SettingsCategoryItem(
                                icon = painterResource(R.drawable.palette),
                                title = {
                                    Text(
                                        stringResource(R.string.appearance),
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                },
                                onClick = { navController.navigate("settings/appearance") }
                            ),
                            
                            SettingsCategoryItem(
                                icon = painterResource(R.drawable.person),
                                title = {
                                    Text(
                                        stringResource(R.string.account),
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                },
                                onClick = { navController.navigate("settings/account") }
                            ),
                            SettingsCategoryItem(
                                icon = painterResource(R.drawable.language),
                                title = {
                                    Text(
                                        stringResource(R.string.content),
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                },
                                onClick = { navController.navigate("settings/content") }
                            ),
                            SettingsCategoryItem(
                                icon = painterResource(R.drawable.play),
                                title = {
                                    Text(
                                        stringResource(R.string.player_and_audio),
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                },
                                onClick = { navController.navigate("settings/player") }
                            ),
                            
                            
                            SettingsCategoryItem(
                                icon = painterResource(R.drawable.storage),
                                title = {
                                    Text(
                                        stringResource(R.string.storage),
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                },
                                onClick = { navController.navigate("settings/storage") }
                            ),
                            SettingsCategoryItem(
                                icon = painterResource(R.drawable.security),
                                title = {
                                    Text(
                                        stringResource(R.string.privacy),
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                },
                                onClick = { navController.navigate("settings/privacy") }
                            ),
                            SettingsCategoryItem(
                                icon = painterResource(R.drawable.restore),
                                title = {
                                    Text(
                                        stringResource(R.string.backup_restore),
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                },
                                onClick = { navController.navigate("settings/backup_restore") }
                            ),
                            SettingsCategoryItem(
                                icon = painterResource(R.drawable.bug_report),
                                title = {
                                    Text(
                                        "Experimental Settings",
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                },
                                onClick = { navController.navigate("settings/experimental") }
                            )
                        )
                    )

                    // About & Community
                    SettingsCategory(
                        title = stringResource(R.string.community),
                        items = listOf(
                            SettingsCategoryItem(
                                icon = painterResource(R.drawable.info),
                                title = {
                                    Text(
                                        stringResource(R.string.about),
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                },
                                onClick = { navController.navigate("settings/about") }
                            ),
                            SettingsCategoryItem(
                                icon = painterResource(R.drawable.translate), // Assuming R.drawable.translate exists. If not, I can use R.drawable.language
                                title = {
                                    Text(
                                        "Help Translate",
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                },
                                onClick = { uriHandler.openUri("https://crowdin.com/project/airbeats") }
                            ),
                            SettingsCategoryItem(
                                icon = painterResource(R.drawable.schedule),
                                title = {
                                    Text(
                                        stringResource(R.string.Changelog),
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                },
                                onClick = { showChangelogSheet = true }
                            )
                        )
                    )
                }

                // Update Card
                UpdateCard()

                // Version Card
                VersionCard(uriHandler)

                Spacer(Modifier.height(32.dp))
            }
        }

        // Dialogs and Bottom Sheets
        if (showTranslateDialog) {
            Dialog(onDismissRequest = { showTranslateDialog = false }) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    shape = RoundedCornerShape(28.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainer
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = stringResource(R.string.Redirección),
                            style = MaterialTheme.typography.headlineSmall,
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        Text(
                            text = stringResource(R.string.poeditor_redirect),
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center
                        )

                        Spacer(modifier = Modifier.height(24.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            OutlinedButton(
                                onClick = { showTranslateDialog = false },
                                modifier = Modifier.weight(1f)
                            ) {
                                Text(stringResource(R.string.cancel))
                            }

                            Button(
                                onClick = {
                                    showTranslateDialog = false
                                    uriHandler.openUri("https://poeditor.com/join/project/208BwCVazA")
                                },
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("OK")
                            }
                        }
                    }
                }
            }
        }

        if (showChangelogSheet) {
            ModalBottomSheet(
                onDismissRequest = { showChangelogSheet = false },
                sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
                containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
                contentColor = MaterialTheme.colorScheme.onSurface,
                dragHandle = {
                    BottomSheetDefaults.DragHandle()
                }
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 24.dp, vertical = 12.dp)
                ) {
                    Text(
                        text = "AirBeats Lite " + stringResource(R.string.Changelog),
                        style = MaterialTheme.typography.headlineSmall,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )

                    ChangelogScreen()

                    Spacer(Modifier.height(24.dp))

                    Button(
                        onClick = { showChangelogSheet = false },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(stringResource(R.string.close))
                    }

                    Spacer(Modifier.height(16.dp))
                }
            }
        }
    }
}

@Composable
fun ProfileSection(
    isLoggedIn: Boolean,
    accountName: String,
    currentSelection: AvatarSelection
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 8.dp, bottom = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (isLoggedIn) {
            var imageLoadError by remember { mutableStateOf(false) }
            var isImageLoading by remember { mutableStateOf(false) }

            // Avatar container (Material 3 style)
            Box(
                modifier = Modifier
                    .size(96.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surfaceContainerHigh)
                    .border(
                        width = 2.dp,
                        color = MaterialTheme.colorScheme.primaryContainer,
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                when {
                    currentSelection is AvatarSelection.Custom && !imageLoadError -> {
                        AsyncImage(
                            model = ImageRequest.Builder(LocalContext.current)
                                .data((currentSelection as AvatarSelection.Custom).uri.toUri())
                                .crossfade(true)
                                .listener(
                                    onStart = { isImageLoading = true },
                                    onSuccess = { _, _ ->
                                        isImageLoading = false
                                        imageLoadError = false
                                    },
                                    onError = { _, _ ->
                                        isImageLoading = false
                                        imageLoadError = true
                                    }
                                )
                                .build(),
                            contentDescription = "Avatar",
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(CircleShape),
                            contentScale = ContentScale.Crop
                        )

                        if (isImageLoading) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.7f)),
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(24.dp),
                                    strokeWidth = 2.dp,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }

                    currentSelection is AvatarSelection.DiceBear && !imageLoadError -> {
                        AsyncImage(
                            model = ImageRequest.Builder(LocalContext.current)
                                .data((currentSelection as AvatarSelection.DiceBear).url)
                                .crossfade(true)
                                .listener(
                                    onStart = { isImageLoading = true },
                                    onSuccess = { _, _ ->
                                        isImageLoading = false
                                        imageLoadError = false
                                    },
                                    onError = { _, _ ->
                                        isImageLoading = false
                                        imageLoadError = true
                                    }
                                )
                                .build(),
                            contentDescription = "Avatar",
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(CircleShape),
                            contentScale = ContentScale.Crop
                        )

                        if (isImageLoading) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.7f)),
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(24.dp),
                                    strokeWidth = 2.dp,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }

                    else -> {
                        val initials = remember(accountName) {
                            val cleanName = accountName.replace("@", "").trim()
                            when {
                                cleanName.isEmpty() -> "?"
                                cleanName.contains(" ") -> {
                                    val parts = cleanName.split(" ")
                                    "${parts.first().firstOrNull()?.uppercase() ?: ""}${
                                        parts.last().firstOrNull()?.uppercase() ?: ""
                                    }"
                                }
                                else -> cleanName.take(2).uppercase()
                            }
                        }

                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(MaterialTheme.colorScheme.primaryContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = initials,
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Username
            AnimatedContent(
                targetState = accountName.replace("@", "").takeIf { it.isNotBlank() } ?: "",
                transitionSpec = { fadeIn() togetherWith fadeOut() },
                label = "username"
            ) { name ->
                Text(
                    text = name,
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold
                    ),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "AirBeats Lite",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.primary
            )

        } else {
            // Not logged in state
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Logo
                Surface(
                    modifier = Modifier.size(80.dp),
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.surfaceContainerHigh,
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp,
                        MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                    )
                ) {
                    Box(
                        modifier = Modifier.padding(18.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.airbeats_monochrome),
                            contentDescription = "AirBeats Lite Logo",
                            modifier = Modifier.fillMaxSize(),
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = buildAnnotatedString {
                                withStyle(
                                    style = SpanStyle(
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                ) {
                                    append("Air")
                                }

                                withStyle(
                                    style = SpanStyle(
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                ) {
                                    append("Beats ")
                                }

                                withStyle(
                                    style = SpanStyle(
                                        color = MaterialTheme.colorScheme.secondary,
                                        fontWeight = FontWeight.Normal
                                    )
                                ) {
                                    append("Lite")
                                }
                            },
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Text(
                        text = "Dev By DxV STUDIO 亗",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}
