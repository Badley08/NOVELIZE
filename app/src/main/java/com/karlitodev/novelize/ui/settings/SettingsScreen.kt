package com.karlitodev.novelize.ui.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.SystemUpdate
import androidx.compose.material.icons.filled.Translate
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.karlitodev.novelize.data.ReaderTheme
import com.karlitodev.novelize.data.UserPreferences
import com.karlitodev.novelize.theme.BrandAmber
import com.karlitodev.novelize.theme.BrandEmerald
import com.karlitodev.novelize.theme.BrandPurple
import com.karlitodev.novelize.update.UpdateManager
import com.karlitodev.novelize.update.UpdateState
import kotlinx.coroutines.launch

/**
 * Settings screen configuring themes, sound design, multi-voice engine, and GitHub auto-updates.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    preferences: UserPreferences,
    onPreferencesChange: (UserPreferences) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val updateManager = remember { UpdateManager(context) }
    val updateState by updateManager.state.collectAsState()

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = { Text("Settings", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Auto-Updates from GitHub Releases
            SettingsSectionHeader(icon = Icons.Default.SystemUpdate, title = "App Updates (GitHub Releases)")

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Current Version: v${updateManager.currentVersion}", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                            Text(
                                "Auto-detects new APK releases on GitHub",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Button(
                            onClick = {
                                coroutineScope.launch {
                                    updateManager.checkForUpdates()
                                }
                            },
                            enabled = updateState !is UpdateState.Checking && updateState !is UpdateState.Downloading,
                            colors = ButtonDefaults.buttonColors(containerColor = BrandPurple),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            if (updateState is UpdateState.Checking) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(16.dp),
                                    color = MaterialTheme.colorScheme.onPrimary,
                                    strokeWidth = 2.dp
                                )
                            } else {
                                Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Check")
                            }
                        }
                    }

                    when (val state = updateState) {
                        is UpdateState.UpToDate -> {
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                text = "✓ App is up to date (v${state.currentVersion})",
                                color = BrandEmerald,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                        is UpdateState.UpdateAvailable -> {
                            Spacer(modifier = Modifier.height(12.dp))
                            Card(
                                colors = CardDefaults.cardColors(containerColor = BrandPurple.copy(alpha = 0.1f)),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Text(
                                        text = "🎉 New Version Available: ${state.release.tagName}",
                                        fontWeight = FontWeight.Bold,
                                        color = BrandPurple,
                                        fontSize = 14.sp
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = state.release.body,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        maxLines = 4
                                    )
                                    Spacer(modifier = Modifier.height(10.dp))
                                    Button(
                                        onClick = {
                                            coroutineScope.launch {
                                                updateManager.downloadAndInstall(state.release)
                                            }
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = BrandPurple),
                                        modifier = Modifier.fillMaxWidth(),
                                        shape = RoundedCornerShape(8.dp)
                                    ) {
                                        Icon(Icons.Default.CloudDownload, contentDescription = null)
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text("Download & Install Update")
                                    }
                                }
                            }
                        }
                        is UpdateState.Downloading -> {
                            Spacer(modifier = Modifier.height(12.dp))
                            Column {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("Downloading update APK...", fontSize = 12.sp)
                                    Text("${state.progressPercent}%", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = BrandPurple)
                                }
                                Spacer(modifier = Modifier.height(6.dp))
                                LinearProgressIndicator(
                                    progress = { state.progressPercent / 100f },
                                    modifier = Modifier.fillMaxWidth(),
                                    color = BrandPurple
                                )
                            }
                        }
                        is UpdateState.ReadyToInstall -> {
                            Spacer(modifier = Modifier.height(10.dp))
                            Button(
                                onClick = { updateManager.triggerApkInstall(state.apkFile) },
                                colors = ButtonDefaults.buttonColors(containerColor = BrandEmerald),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("Install Downloaded APK")
                            }
                        }
                        is UpdateState.Error -> {
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                text = state.message,
                                color = MaterialTheme.colorScheme.error,
                                fontSize = 12.sp
                            )
                        }
                        else -> {}
                    }
                }
            }

            // Theme preference section
            SettingsSectionHeader(icon = Icons.Default.Palette, title = "Reading Comfort")

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Reader Theme", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                    Spacer(modifier = Modifier.height(10.dp))
                    ReaderTheme.entries.forEach { theme ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onPreferencesChange(preferences.copy(theme = theme)) }
                                .padding(vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = preferences.theme == theme,
                                onClick = { onPreferencesChange(preferences.copy(theme = theme)) },
                                colors = RadioButtonDefaults.colors(selectedColor = BrandPurple)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(theme.displayName)
                        }
                    }
                }
            }

            // Audio & SFX settings
            SettingsSectionHeader(icon = Icons.Default.VolumeUp, title = "Sound Design & Voices")

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Contextual SFX Audio", fontWeight = FontWeight.SemiBold)
                        Switch(
                            checked = preferences.sfxEnabled,
                            onCheckedChange = { onPreferencesChange(preferences.copy(sfxEnabled = it)) },
                            colors = SwitchDefaults.colors(checkedThumbColor = BrandPurple)
                        )
                    }

                    if (preferences.sfxEnabled) {
                        Spacer(modifier = Modifier.height(12.dp))
                        Text("SFX Volume: ${(preferences.sfxVolume * 100).toInt()}%", fontSize = 13.sp)
                        Slider(
                            value = preferences.sfxVolume,
                            onValueChange = { onPreferencesChange(preferences.copy(sfxVolume = it)) },
                            valueRange = 0f..1f,
                            colors = SliderDefaults.colors(thumbColor = BrandPurple, activeTrackColor = BrandPurple)
                        )

                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Ambient Volume: ${(preferences.ambientVolume * 100).toInt()}%", fontSize = 13.sp)
                        Slider(
                            value = preferences.ambientVolume,
                            onValueChange = { onPreferencesChange(preferences.copy(ambientVolume = it)) },
                            valueRange = 0f..1f,
                            colors = SliderDefaults.colors(thumbColor = BrandPurple, activeTrackColor = BrandPurple)
                        )
                    }
                }
            }

            // Offline Translation section
            SettingsSectionHeader(icon = Icons.Default.Translate, title = "Offline Translation")

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("English ⇄ French", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                    Text(
                        "On-device vocabulary and ML models active without network connection.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }

            // About & License notice
            SettingsSectionHeader(icon = Icons.Default.Info, title = "About NoveLize")

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("NoveLize v1.0.0", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                    Text(
                        "GPL-3.0 License. Interactive light novel reader with on-device multi-voice narration.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun SettingsSectionHeader(icon: ImageVector, title: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)
    ) {
        Icon(icon, contentDescription = null, tint = BrandPurple, modifier = Modifier.size(20.dp))
        Spacer(modifier = Modifier.width(8.dp))
        Text(title, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = BrandPurple)
    }
}
