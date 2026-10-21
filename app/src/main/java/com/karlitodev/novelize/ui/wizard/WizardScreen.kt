package com.karlitodev.novelize.ui.wizard

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Headphones
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.RecordVoiceOver
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.karlitodev.novelize.data.ReaderTheme
import com.karlitodev.novelize.theme.BrandEmerald
import com.karlitodev.novelize.theme.BrandPurple
import com.karlitodev.novelize.theme.BrandPurpleLight

/**
 * First-run onboarding wizard.
 * Configures application UI/UX language (Français / English), appearance,
 * neural voice profiles, and optimal use guidelines.
 */
@Composable
fun WizardScreen(
    onComplete: (ReaderTheme, Float, String) -> Unit,
    modifier: Modifier = Modifier
) {
    var step by remember { mutableIntStateOf(0) }
    var selectedTheme by remember { mutableStateOf(ReaderTheme.DYNAMIC) }
    var fontSizeSp by remember { mutableFloatStateOf(18f) }
    var selectedLanguage by remember { mutableStateOf("fr") }

    val isFr = selectedLanguage == "fr"

    Scaffold(
        modifier = modifier.fillMaxSize(),
        bottomBar = {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                tonalElevation = 8.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Step indicators (4 steps: Language, Appearance, Voices, Guidelines)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        repeat(4) { index ->
                            Box(
                                modifier = Modifier
                                    .size(if (index == step) 24.dp else 10.dp, 10.dp)
                                    .clip(RoundedCornerShape(5.dp))
                                    .background(
                                        if (index == step) BrandPurple else MaterialTheme.colorScheme.outlineVariant
                                    )
                            )
                        }
                    }

                    Button(
                        onClick = {
                            if (step < 3) {
                                step++
                            } else {
                                onComplete(selectedTheme, fontSizeSp, selectedLanguage)
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = BrandPurple),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            if (step == 3) {
                                if (isFr) "Commencer la lecture" else "Start Reading"
                            } else {
                                if (isFr) "Continuer" else "Continue"
                            }
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Icon(
                            imageVector = if (step == 3) Icons.Default.Check else Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = null
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        AnimatedContent(
            targetState = step,
            transitionSpec = { fadeIn() togetherWith fadeOut() },
            label = "wizard_step_transition",
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
        ) { currentStep ->
            when (currentStep) {
                0 -> LanguageStep(
                    selectedLanguage = selectedLanguage,
                    onLanguageSelected = { selectedLanguage = it }
                )
                1 -> AppearanceStep(
                    isFr = isFr,
                    selectedTheme = selectedTheme,
                    onThemeSelected = { selectedTheme = it },
                    fontSize = fontSizeSp,
                    onFontSizeChanged = { fontSizeSp = it }
                )
                2 -> VoicesStep(
                    isFr = isFr,
                    selectedLanguage = selectedLanguage
                )
                3 -> OptimalUseStep(isFr = isFr)
            }
        }
    }
}

@Composable
private fun LanguageStep(
    selectedLanguage: String,
    onLanguageSelected: (String) -> Unit
) {
    val isFr = selectedLanguage == "fr"

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = Icons.Default.Language,
            contentDescription = null,
            tint = BrandPurple,
            modifier = Modifier.size(54.dp)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = if (isFr) "Langue de l'Application" else "Application Language",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )
        Text(
            text = if (isFr) "Choisissez la langue de l'interface et des modèles IA."
            else "Choose your preferred UI and neural voice model language.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 8.dp, bottom = 28.dp)
        )

        // Language Choice Cards
        listOf(
            Triple("fr", "Français", "Interface et narration optimisées pour les light novels en français"),
            Triple("en", "English", "UI and neural narration optimized for English light novels")
        ).forEach { (code, title, desc) ->
            val isSelected = selectedLanguage == code
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
                    .clickable { onLanguageSelected(code) }
                    .border(
                        width = if (isSelected) 2.dp else 1.dp,
                        color = if (isSelected) BrandPurple else MaterialTheme.colorScheme.outlineVariant,
                        shape = RoundedCornerShape(16.dp)
                    ),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (isSelected) BrandPurple.copy(alpha = 0.12f) else MaterialTheme.colorScheme.surface
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(
                        selected = isSelected,
                        onClick = { onLanguageSelected(code) },
                        colors = RadioButtonDefaults.colors(selectedColor = BrandPurple)
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = title,
                            fontWeight = FontWeight.Bold,
                            fontSize = 17.sp,
                            color = if (isSelected) BrandPurple else MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = desc,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun AppearanceStep(
    isFr: Boolean,
    selectedTheme: ReaderTheme,
    onThemeSelected: (ReaderTheme) -> Unit,
    fontSize: Float,
    onFontSizeChanged: (Float) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = Icons.Default.Palette,
            contentDescription = null,
            tint = BrandPurple,
            modifier = Modifier.size(48.dp)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = if (isFr) "Personnalisation du Lecteur" else "Customize Your Reader",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )
        Text(
            text = if (isFr) "Sélectionnez votre thème et le confort de lecture souhaité."
            else "Select your preferred visual style and reading comfort.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 8.dp, bottom = 24.dp)
        )

        // Theme choices
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            ReaderTheme.entries.forEach { theme ->
                val isSelected = theme == selectedTheme
                val themeName = when (theme) {
                    ReaderTheme.LIGHT -> if (isFr) "Clair" else "Light"
                    ReaderTheme.SEPIA -> "Sépia"
                    ReaderTheme.NIGHT -> if (isFr) "Nuit" else "Night"
                    ReaderTheme.DYNAMIC -> if (isFr) "Dynamique" else "Dynamic"
                }

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(12.dp))
                        .border(
                            width = if (isSelected) 2.dp else 1.dp,
                            color = if (isSelected) BrandPurple else MaterialTheme.colorScheme.outlineVariant,
                            shape = RoundedCornerShape(12.dp)
                        )
                        .background(
                            when (theme) {
                                ReaderTheme.LIGHT -> Color(0xFFF8F9FA)
                                ReaderTheme.SEPIA -> Color(0xFFFBF0D9)
                                ReaderTheme.NIGHT -> Color(0xFF1E1E1E)
                                ReaderTheme.DYNAMIC -> BrandPurpleLight.copy(alpha = 0.2f)
                            }
                        )
                        .clickable { onThemeSelected(theme) }
                        .padding(vertical = 14.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = themeName,
                        fontSize = 12.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                        color = if (theme == ReaderTheme.NIGHT) Color.White else Color(0xFF2D3436)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(28.dp))

        // Font size adjustment
        Text(
            text = if (isFr) "Taille de police : ${fontSize.toInt()} sp" else "Font Size: ${fontSize.toInt()} sp",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold
        )
        Slider(
            value = fontSize,
            onValueChange = onFontSizeChanged,
            valueRange = 14f..28f,
            steps = 7,
            colors = SliderDefaults.colors(thumbColor = BrandPurple, activeTrackColor = BrandPurple),
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
        )

        // Live preview card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = when (selectedTheme) {
                    ReaderTheme.LIGHT -> Color(0xFFFFFFFF)
                    ReaderTheme.SEPIA -> Color(0xFFF4E4C1)
                    ReaderTheme.NIGHT -> Color(0xFF1A1A1A)
                    ReaderTheme.DYNAMIC -> Color(0xFF241E38)
                }
            )
        ) {
            Text(
                text = if (isFr) "« La forêt murmurait sous la brise nocturne, baignée d'une lueur éthérée. »"
                else "“The whispering forest shaded the mossy trail, illuminated by an ethereal glow.”",
                fontSize = fontSize.sp,
                lineHeight = (fontSize * 1.35f).sp,
                color = when (selectedTheme) {
                    ReaderTheme.NIGHT, ReaderTheme.DYNAMIC -> Color(0xFFE2E8F0)
                    ReaderTheme.SEPIA -> Color(0xFF433422)
                    ReaderTheme.LIGHT -> Color(0xFF2D3436)
                },
                modifier = Modifier.padding(20.dp),
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun VoicesStep(
    isFr: Boolean,
    selectedLanguage: String
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = Icons.Default.RecordVoiceOver,
            contentDescription = null,
            tint = BrandPurple,
            modifier = Modifier.size(48.dp)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = if (isFr) "Modèles Vocaux IA" else "Neural AI Voice Models",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )
        Text(
            text = if (isFr) "NoveLize utilise de vrais modèles IA neuronaux pour cartographier distinctement le narrateur et les dialogues."
            else "NoveLize uses real neural AI models to distinctively map narrator and character dialogue.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 8.dp, bottom = 24.dp)
        )

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = if (isFr) "Modèles Sélectionnés (${if (selectedLanguage == "fr") "Français" else "English"})"
                    else "Active Models (${if (selectedLanguage == "fr") "French" else "English"})",
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = if (isFr) "• Narrateur : Modèle équilibré haute clarté\n• Personnage Principal : Timbre héroïque\n• Voix 1 & 2 : Profils féminins et masculins distincts\n• Prise en charge stricte : Français et Anglais uniquement"
                    else "• Narrator: High-clarity neural model\n• Lead Character: Resonant heroic timbre\n• Roles 1 & 2: Distinct male/female profiles\n• Supported Prose: French & English strictly",
                    style = MaterialTheme.typography.bodySmall,
                    lineHeight = 22.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Privacy badge
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = BrandEmerald.copy(alpha = 0.1f)),
            shape = RoundedCornerShape(16.dp)
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Security,
                    contentDescription = null,
                    tint = BrandEmerald,
                    modifier = Modifier.size(32.dp)
                )
                Spacer(modifier = Modifier.width(16.dp))
                Column {
                    Text(
                        text = if (isFr) "100% Local & Sécurisé" else "100% On-Device & Private",
                        fontWeight = FontWeight.Bold,
                        color = BrandEmerald,
                        fontSize = 14.sp
                    )
                    Text(
                        text = if (isFr) "La synthèse neuronale s'exécute sur votre appareil sans aucune transmission cloud."
                        else "Neural speech synthesis runs locally with zero cloud telemetry.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
private fun OptimalUseStep(isFr: Boolean) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = Icons.Default.Headphones,
            contentDescription = null,
            tint = BrandPurple,
            modifier = Modifier.size(48.dp)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = if (isFr) "Prêt pour l'Aventure" else "Ready for the Adventure",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )
        Text(
            text = if (isFr) "Conseils pour une expérience immersive maximale."
            else "Tips for the best audio drama light novel experience.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 8.dp, bottom = 24.dp)
        )

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                GuidelineRow(
                    icon = Icons.Default.Headphones,
                    title = if (isFr) "Casque ou Écouteurs" else "Use Headphones",
                    desc = if (isFr) "Les soundscapes et effets sonores sont spatialisés pour une immersion totale."
                    else "Soundscapes and SFX cues are balanced for spatial immersion."
                )
                GuidelineRow(
                    icon = Icons.Default.VolumeUp,
                    title = if (isFr) "Effets Sonores Pixabay" else "Pixabay Sound Effects",
                    desc = if (isFr) "Téléchargez vos sons Pixabay favoris et placez-les dans le dossier SFX de l'application."
                    else "Download your favourite Pixabay SFX files into the app's SFX folder."
                )
            }
        }
    }
}

@Composable
private fun GuidelineRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    desc: String
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, contentDescription = null, tint = BrandPurple, modifier = Modifier.size(24.dp))
        Spacer(modifier = Modifier.width(14.dp))
        Column {
            Text(title, fontWeight = FontWeight.Bold, fontSize = 14.sp)
            Text(desc, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}
