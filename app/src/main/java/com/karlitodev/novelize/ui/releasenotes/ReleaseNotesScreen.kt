package com.karlitodev.novelize.ui.releasenotes

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.karlitodev.novelize.theme.BrandAmber
import com.karlitodev.novelize.theme.BrandPurple

// ---------------------------------------------------------------------------
// Data model
// ---------------------------------------------------------------------------

private data class ChangeEntry(val text: String)

private data class ChangeSection(val title: String, val entries: List<ChangeEntry>)

private data class ReleaseBlock(
    val version: String,
    val date: String,
    val sections: List<ChangeSection>
)

// ---------------------------------------------------------------------------
// Static changelog — mirrors CHANGELOG.md exactly, without emojis.
// Update this list whenever a new version is released.
// ---------------------------------------------------------------------------

private val CHANGELOG: List<ReleaseBlock> = listOf(

    ReleaseBlock(
        version = "1.4",
        date = "2026-10-21",
        sections = listOf(
            ChangeSection(
                title = "New Features",
                entries = listOf(
                    ChangeEntry("In-app Release Notes screen integrated directly in Settings, showing the complete version history without any emojis."),
                    ChangeEntry("Automatic deletion of old GitHub Actions builds: only the 10 most recent runs are kept, all older runs are removed via the GitHub API.")
                )
            ),
            ChangeSection(
                title = "Bug Fixes and Optimizations",
                entries = listOf(
                    ChangeEntry("Fixed R8 build failure caused by the missing class com.gemalto.jp2.JP2Decoder (optional JPX filter of PDFBox-Android, not present on Android). Resolved via ProGuard -dontwarn rules."),
                    ChangeEntry("Added -keep rules for ONNX Runtime and ML Kit to prevent accidental removal by the R8 minifier."),
                    ChangeEntry("Fixed hardcoded version constant in the library screen to correctly sync the new-version badge on startup."),
                    ChangeEntry("Release Notes screen now displays in English by default.")
                )
            )
        )
    ),

    ReleaseBlock(
        version = "1.3",
        date = "2026-10-21",
        sections = listOf(
            ChangeSection(
                title = "New Features",
                entries = listOf(
                    ChangeEntry("Multilingual character gender detection via regular expressions (Regex) for French (FR), Japanese (JP), Korean (KR), and English (EN)."),
                    ChangeEntry("Dynamic assignment of 4 distinct character voices (2 male: MALE_1 and MALE_2, 2 female: FEMALE_1 and FEMALE_2) in addition to the narrator, with differentiated acoustic settings for pitch and speech rate."),
                    ChangeEntry("Extension of the local neural AI voice engine (Piper) to support secondary male and female voice models in French and English.")
                )
            ),
            ChangeSection(
                title = "Bug Fixes and Optimizations",
                entries = listOf(
                    ChangeEntry("Fixed the import of the PDFBox-Android library (com.tom_roush.pdfbox) resolving the continuous integration build error.")
                )
            )
        )
    ),

    ReleaseBlock(
        version = "1.2",
        date = "2026-10-21",
        sections = listOf(
            ChangeSection(
                title = "New Features",
                entries = listOf(
                    ChangeEntry("Full media controls in the status bar, notification drawer, and Android lock screen via MediaSessionCompat and NotificationCompat.MediaStyle (play, pause, previous sentence, next sentence)."),
                    ChangeEntry("Native PDF document support with text extraction and automatic cover generation via PdfRenderer and PDFBox-Android."),
                    ChangeEntry("Native CBZ comic and manga archive support with cover extraction."),
                    ChangeEntry("Native Markdown file support (.md, .markdown) with chapter title hierarchy."),
                    ChangeEntry("Improved EPUB support with structured parsing of container.xml manifest and spine reading order (.opf)."),
                    ChangeEntry("Automatic extraction and display of the first page or embedded cover for all supported formats."),
                    ChangeEntry("Ability for users to manually set and replace the cover of any book from the image gallery."),
                    ChangeEntry("Direct book deletion option from the library."),
                    ChangeEntry("Crash and incident handler (CrashHandler) generating a detailed crash.txt technical report, shareable directly to developers via WhatsApp."),
                    ChangeEntry("Intelligent SFX sync from GitHub with integrity verification to prevent re-downloading already present files."),
                    ChangeEntry("Personalized welcome and info messages based on the user language."),
                    ChangeEntry("Smart reading resume: blinking highlight on the last stopped sentence and temporary screen dim to guide attention.")
                )
            ),
            ChangeSection(
                title = "Bug Fixes and Optimizations",
                entries = listOf(
                    ChangeEntry("Removed 32 Python-generated synthetic sound effects from local resources and the GitHub repository to improve audio quality."),
                    ChangeEntry("Retained and integrated 18 real studio-quality sound effects (combat, magic, interactions)."),
                    ChangeEntry("Replaced the synthetic voice synthesizer with Android's native TextToSpeech engine, customized per vocal role (distinct pitch and rate for narrator, hero, female and male characters)."),
                    ChangeEntry("Redesigned sound effect detection rules to prevent false positives on common everyday words."),
                    ChangeEntry("Added a cooldown period preventing the same sound from playing again within 8 paragraphs."),
                    ChangeEntry("Direct APK release on GitHub Releases without ZIP compression for seamless in-app automatic updates."),
                    ChangeEntry("Fixed splash screen animation resize on startup.")
                )
            )
        )
    ),

    ReleaseBlock(
        version = "1.1",
        date = "2026-10-21",
        sections = listOf(
            ChangeSection(
                title = "Additions",
                entries = listOf(
                    ChangeEntry("First-run setup wizard executed once on first launch."),
                    ChangeEntry("Automatic silent update check via GitHub Releases."),
                    ChangeEntry("Improved stability of the background audio playback service.")
                )
            )
        )
    )
)

// ---------------------------------------------------------------------------
// Screen
// ---------------------------------------------------------------------------

/**
 * Displays the complete version history of NoveLize.
 * No emojis used anywhere per project convention.
 * Defaults to English; pass isFr=true from the navigation host when the user
 * has selected French in Settings.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReleaseNotesScreen(
    isFr: Boolean = false,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = if (isFr) "Notes de Version" else "Release Notes",
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = if (isFr) "Retour" else "Back"
                        )
                    }
                }
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(vertical = 16.dp)
        ) {
            items(CHANGELOG) { release ->
                ReleaseCard(release = release)
            }
        }
    }
}

// ---------------------------------------------------------------------------
// Sub-composables
// ---------------------------------------------------------------------------

@Composable
private fun ReleaseCard(release: ReleaseBlock) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(18.dp)) {

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "v${release.version}",
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 20.sp,
                    color = BrandPurple
                )
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = BrandAmber.copy(alpha = 0.15f)
                ) {
                    Text(
                        text = release.date,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = BrandAmber
                    )
                }
            }

            HorizontalDivider(
                modifier = Modifier.padding(vertical = 12.dp),
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
            )

            release.sections.forEach { section ->
                SectionBlock(section = section)
                Spacer(modifier = Modifier.height(12.dp))
            }
        }
    }
}

@Composable
private fun SectionBlock(section: ChangeSection) {
    Column {
        Text(
            text = section.title.uppercase(),
            fontWeight = FontWeight.Bold,
            fontSize = 11.sp,
            letterSpacing = 1.2.sp,
            color = BrandPurple.copy(alpha = 0.8f),
            modifier = Modifier.padding(bottom = 8.dp)
        )
        section.entries.forEach { entry ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 6.dp),
                verticalAlignment = Alignment.Top
            ) {
                Text(
                    text = "-",
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    color = BrandPurple,
                    modifier = Modifier
                        .padding(top = 1.dp)
                        .width(14.dp)
                )
                Text(
                    text = entry.text,
                    fontSize = 13.sp,
                    lineHeight = 19.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}
