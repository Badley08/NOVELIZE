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
 * Configures appearance, neural voice profiles, and optimal use guidelines.
 */
@Composable
fun WizardScreen(
    onComplete: (ReaderTheme, Float) -> Unit,
    modifier: Modifier = Modifier
) {
    var step by remember { mutableIntStateOf(0) }
    var selectedTheme by remember { mutableStateOf(ReaderTheme.DYNAMIC) }
    var fontSizeSp by remember { mutableFloatStateOf(18f) }
    var selectedLanguage by remember { mutableStateOf("en") }

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
                    // Step indicators
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        repeat(3) { index ->
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
                            if (step < 2) {
                                step++
                            } else {
                                onComplete(selectedTheme, fontSizeSp)
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = BrandPurple),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(if (step == 2) "Start Reading" else "Continue")
                        Spacer(modifier = Modifier.width(8.dp))
                        Icon(
                            imageVector = if (step == 2) Icons.Default.Check else Icons.AutoMirrored.Filled.ArrowForward,
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
                0 -> AppearanceStep(
                    selectedTheme = selectedTheme,
                    onThemeSelected = { selectedTheme = it },
                    fontSize = fontSizeSp,
                    onFontSizeChanged = { fontSizeSp = it }
                )
                1 -> VoicesStep(
                    selectedLanguage = selectedLanguage,
                    onLanguageSelected = { selectedLanguage = it }
                )
                2 -> OptimalUseStep()
            }
        }
    }
}

@Composable
private fun AppearanceStep(
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
            text = "Customize Your Reader",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )
        Text(
            text = "Select your preferred visual style and reading comfort.",
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
                        text = theme.displayName,
                        fontSize = 13.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                        color = if (theme == ReaderTheme.NIGHT) Color.White else Color(0xFF2D3436)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Font size adjustment
        Text(
            text = "Font Size: ${fontSize.toInt()} sp",
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
                text = "“The whispering forest shaded the mossy trail, illuminated by an ethereal glow.”",
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
    selectedLanguage: String,
    onLanguageSelected: (String) -> Unit
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
            text = "Multi-Voice Narration",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )
        Text(
            text = "NoveLize dynamically maps up to 5 distinct voice profiles to characters and the narrator.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 8.dp, bottom = 24.dp)
        )

        // Primary narration language
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Primary Language",
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp
                )
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    listOf("en" to "English", "fr" to "Français").forEach { (code, label) ->
                        val isSelected = selectedLanguage == code
                        OutlinedButton(
                            onClick = { onLanguageSelected(code) },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp),
                            colors = if (isSelected) ButtonDefaults.outlinedButtonColors(containerColor = BrandPurple.copy(alpha = 0.1f)) else ButtonDefaults.outlinedButtonColors(),
                            border = ButtonDefaults.outlinedButtonBorder.copy(
                                brush = Brush.linearGradient(listOf(if (isSelected) BrandPurple else MaterialTheme.colorScheme.outlineVariant, if (isSelected) BrandPurple else MaterialTheme.colorScheme.outlineVariant))
                            )
                        ) {
                            Text(label, color = if (isSelected) BrandPurple else MaterialTheme.colorScheme.onSurface)
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // On-device privacy assurance badge
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
                        text = "100% On-Device & Private",
                        fontWeight = FontWeight.Bold,
                        color = BrandEmerald,
                        fontSize = 14.sp
                    )
                    Text(
                        text = "Voice synthesis and translation run strictly locally. Zero audio or book content leaves your device.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
private fun OptimalUseStep() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Visual badge representing 3D headphones
        Box(
            modifier = Modifier
                .size(100.dp)
                .clip(CircleShape)
                .background(
                    Brush.radialGradient(
                        listOf(BrandPurpleLight, BrandPurple)
                    )
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Headphones,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(54.dp)
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Optimal Performance Notice",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = "On-device speech synthesis and real-time sound design are resource-intensive. To ensure optimal smoothness:",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(20.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                GuidelineItem("🎧", "Wear headphones for spatial soundscapes and immersive dialogue.")
                GuidelineItem("⚡", "Avoid resource-heavy background tasks during long narration sessions.")
                GuidelineItem("🌙", "Turn off your screen for background listening to conserve battery and RAM.")
            }
        }
    }
}

@Composable
private fun GuidelineItem(icon: String, text: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(text = icon, fontSize = 20.sp)
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text = text,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}
