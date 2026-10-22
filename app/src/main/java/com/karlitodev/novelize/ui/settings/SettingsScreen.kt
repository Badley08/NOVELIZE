package com.karlitodev.novelize.ui.settings

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.RecordVoiceOver
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.SystemUpdate
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.karlitodev.novelize.R
import com.karlitodev.novelize.audio.VoicePreset
import com.karlitodev.novelize.data.ReaderTheme
import com.karlitodev.novelize.data.UserPreferences
import com.karlitodev.novelize.theme.BrandAmber
import com.karlitodev.novelize.theme.BrandEmerald
import com.karlitodev.novelize.theme.BrandPurple
import com.karlitodev.novelize.update.UpdateManager
import com.karlitodev.novelize.update.UpdateState
import kotlinx.coroutines.launch

/**
 * Settings screen configuring themes, 4 native TTS voice presets,
 * UI language, and author information.
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

    val isFr = preferences.appLanguage == "fr"

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = { Text(if (isFr) "Paramètres" else "Settings", fontWeight = FontWeight.Bold) },
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
            // UI Language Section
            SettingsSectionHeader(
                icon = Icons.Default.Language,
                title = if (isFr) "Langue de l'Interface" else "UI Language"
            )

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    listOf("fr" to "Français", "en" to "English").forEach { (code, label) ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onPreferencesChange(preferences.copy(appLanguage = code)) }
                                .padding(vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = preferences.appLanguage == code,
                                onClick = { onPreferencesChange(preferences.copy(appLanguage = code)) },
                                colors = RadioButtonDefaults.colors(selectedColor = BrandPurple)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(label, fontWeight = if (preferences.appLanguage == code) FontWeight.Bold else FontWeight.Normal)
                        }
                    }
                }
            }

            // Auto-Updates from GitHub Releases
            SettingsSectionHeader(
                icon = Icons.Default.SystemUpdate,
                title = if (isFr) "Mises à jour (GitHub Releases)" else "App Updates (GitHub Releases)"
            )

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
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                if (isFr) "Version actuelle : v${updateManager.currentVersion}"
                                else "Current Version: v${updateManager.currentVersion}",
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp
                            )
                            Text(
                                if (isFr) "Seules les versions signées par Luberisse Karl Brad sont acceptées."
                                else "Only updates signed by Luberisse Karl Brad are permitted.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Spacer(modifier = Modifier.width(8.dp))

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
                                Icon(Icons.Default.SystemUpdate, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(if (isFr) "Vérifier" else "Check")
                            }
                        }
                    }

                    when (val state = updateState) {
                        is UpdateState.UpToDate -> {
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                text = if (isFr) "✓ L'application est à jour (v${state.currentVersion})"
                                else "✓ App is up to date (v${state.currentVersion})",
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
                                        text = "${if (isFr) "Nouvelle version disponible" else "New Version Available"}: ${state.release.tagName}",
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
                                        Text(if (isFr) "Télécharger & Installer" else "Download & Install Update")
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
                                    Text(
                                        if (isFr) "Téléchargement de l'APK..." else "Downloading update APK...",
                                        fontSize = 12.sp
                                    )
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
                                Text(if (isFr) "Installer l'APK téléchargé" else "Install Downloaded APK")
                            }
                        }
                        is UpdateState.SecurityRejected -> {
                            Spacer(modifier = Modifier.height(10.dp))
                            Card(
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Security, contentDescription = null, tint = MaterialTheme.colorScheme.error)
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Text(
                                        text = state.reason,
                                        color = MaterialTheme.colorScheme.onErrorContainer,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
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
            SettingsSectionHeader(
                icon = Icons.Default.Palette,
                title = if (isFr) "Confort de Lecture" else "Reading Comfort"
            )

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(if (isFr) "Thème du lecteur" else "Reader Theme", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                    Spacer(modifier = Modifier.height(10.dp))
                    ReaderTheme.entries.forEach { theme ->
                        val displayName = when (theme) {
                            ReaderTheme.LIGHT -> if (isFr) "Clair" else "Light"
                            ReaderTheme.SEPIA -> "Sépia"
                            ReaderTheme.NIGHT -> if (isFr) "Nuit" else "Night"
                            ReaderTheme.DYNAMIC -> if (isFr) "Dynamique (selon couverture)" else "Dynamic Cover"
                        }

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
                            Text(displayName)
                        }
                    }
                }
            }

            // Native TTS Voice selection (4 voices: 2 Female, 2 Male)
            SettingsSectionHeader(
                icon = Icons.Default.RecordVoiceOver,
                title = if (isFr) "Voix Text-To-Speech (4 Voix Optimisées)" else "TTS Voices (4 Optimized Voices)"
            )

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = if (isFr) "Choisissez votre profil vocal préféré pour la lecture :"
                        else "Choose your preferred narration voice profile:",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    VoicePreset.entries.forEach { preset ->
                        val isSelected = preferences.selectedVoicePack == preset.id ||
                            (preferences.selectedVoicePack.startsWith("piper") && preset == VoicePreset.FEMALE_1)

                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                                .clickable { onPreferencesChange(preferences.copy(selectedVoicePack = preset.id)) },
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = if (isSelected) BrandPurple.copy(alpha = 0.12f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                            )
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                RadioButton(
                                    selected = isSelected,
                                    onClick = { onPreferencesChange(preferences.copy(selectedVoicePack = preset.id)) },
                                    colors = RadioButtonDefaults.colors(selectedColor = BrandPurple)
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(preset.displayName, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Surface(
                                            color = if (preset.gender == "F") BrandPurpleLight.copy(alpha = 0.2f) else BrandAmber.copy(alpha = 0.2f),
                                            shape = RoundedCornerShape(6.dp)
                                        ) {
                                            Text(
                                                text = if (preset.gender == "F") "Féminine" else "Masculine",
                                                color = if (preset.gender == "F") BrandPurple else BrandAmber,
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Bold,
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                            )
                                        }
                                    }
                                    Text(
                                        text = preset.description,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // About Developer section (Luberisse Karl Brad)
            SettingsSectionHeader(icon = Icons.Default.Info, title = if (isFr) "À Propos & Développeur" else "About & Developer")

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Developer Photo (karlito.png)
                    Image(
                        painter = painterResource(id = R.drawable.karlito),
                        contentDescription = "Luberisse Karl Brad",
                        modifier = Modifier
                            .size(110.dp)
                            .clip(CircleShape)
                            .border(3.dp, BrandPurple, CircleShape),
                        contentScale = ContentScale.Crop
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    Text(
                        text = "Created By: Luberisse Karl Brad",
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 18.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Text(
                        text = "NoveLize Developer & Creator",
                        fontSize = 13.sp,
                        color = BrandPurple,
                        fontWeight = FontWeight.SemiBold
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // WhatsApp Contact Button
                    Button(
                        onClick = {
                            try {
                                val url = "https://wa.me/18293393248"
                                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url)).apply {
                                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                                }
                                context.startActivity(intent)
                            } catch (_: Exception) {
                                val dialIntent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:+18293393248"))
                                context.startActivity(dialIntent)
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF25D366)),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.Phone, contentDescription = null, tint = Color.White)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "WhatsApp: +1 (829) 339-3248",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "NoveLize v1.0.0 — GPL-3.0 License",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
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
