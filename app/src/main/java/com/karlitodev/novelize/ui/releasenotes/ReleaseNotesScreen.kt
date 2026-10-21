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
        version = "1.3",
        date = "2026-10-21",
        sections = listOf(
            ChangeSection(
                title = "Nouveautes",
                entries = listOf(
                    ChangeEntry("Detection multilingue par expressions regulieres (Regex) du genre des personnages (masculin et feminin) pour les langues francaise (FR), japonaise (JP), coreenne (KR) et anglaise (EN)."),
                    ChangeEntry("Attribution dynamique de 4 voix de personnages distinctes (2 voix masculines : MALE_1 et MALE_2, 2 voix feminines : FEMALE_1 et FEMALE_2) en plus du narrateur, avec reglages acoustiques differencies de hauteur tonale (pitch) et de cadence."),
                    ChangeEntry("Extension du moteur vocal neural local et IA (Piper) pour supporter les modeles de voix secondaires masculins et feminins en francais et en anglais.")
                )
            ),
            ChangeSection(
                title = "Corrections et Optimisations",
                entries = listOf(
                    ChangeEntry("Correction de l'importation de la bibliotheque PDFBox-Android (com.tom_roush.pdfbox) resolvant l'erreur de compilation en integration continue.")
                )
            )
        )
    ),

    ReleaseBlock(
        version = "1.2",
        date = "2026-10-21",
        sections = listOf(
            ChangeSection(
                title = "Nouveautes",
                entries = listOf(
                    ChangeEntry("Controles multimedia complets dans la barre d'etat, la zone de notification et l'ecran de verrouillage Android via MediaSessionCompat et NotificationCompat.MediaStyle."),
                    ChangeEntry("Support natif des documents PDF avec extraction de texte et generation automatique de couverture via PdfRenderer et PDFBox-Android."),
                    ChangeEntry("Support natif des bandes dessinees et mangas au format archive CBZ avec extraction de couverture."),
                    ChangeEntry("Support natif des fichiers texte Markdown (.md, .markdown) avec hierarchisation des titres de chapitres."),
                    ChangeEntry("Support ameliore des fichiers EPUB avec analyse structuree du fichier de manifeste container.xml et de l'ordre de lecture du spine (.opf)."),
                    ChangeEntry("Extraction et affichage automatique de la premiere page ou de la couverture integree pour tous les formats compatibles."),
                    ChangeEntry("Possibilite pour l'utilisateur de definir et remplacer manuellement la couverture de n'importe quel livre a partir de la galerie d'images."),
                    ChangeEntry("Option de suppression directe des livres importes de la bibliotheque."),
                    ChangeEntry("Module de gestion des incidents et crashs (CrashHandler) generant un rapport technique detaille crash.txt et permettant son envoi direct par WhatsApp aux developpeurs."),
                    ChangeEntry("Synchronisation intelligente des effets sonores (SFX) depuis le depot GitHub avec verification d'integrite pour eviter tout re-telechargement de fichiers deja presents."),
                    ChangeEntry("Messages d'information et d'accueil personnalises selon la langue utilisateur."),
                    ChangeEntry("Reprise de lecture intelligente : mise en surbrillance clignotante du texte ou la lecture s'est arretee et assombrissement temporaire de l'interface pour guider l'attention.")
                )
            ),
            ChangeSection(
                title = "Corrections et Optimisations",
                entries = listOf(
                    ChangeEntry("Suppression des 32 effets sonores synthetiques generes par python dans les ressources locales afin d'ameliorer la qualite audio."),
                    ChangeEntry("Conservation et integration des 18 effets sonores reels de qualite studio (combat, magie, interactions)."),
                    ChangeEntry("Remplacement du synthetiseur vocal synthetique par le moteur TextToSpeech systeme natif d'Android, personnalise par role vocal."),
                    ChangeEntry("Refonte des regles de detection des effets sonores pour empecher les faux positifs sur les mots du quotidien."),
                    ChangeEntry("Ajout d'une periode de reinitialisation (cooldown) empechant la repetition abusive d'un meme bruitage a moins de 8 paragraphes d'intervalle."),
                    ChangeEntry("Publication directe de l'application au format APK sur GitHub Releases sans compression ZIP pour permettre la mise a jour automatique integree."),
                    ChangeEntry("Correction du redimensionnement de l'animation de presentation au demarrage.")
                )
            )
        )
    ),

    ReleaseBlock(
        version = "1.1",
        date = "2026-10-21",
        sections = listOf(
            ChangeSection(
                title = "Ajouts",
                entries = listOf(
                    ChangeEntry("Integration du processus d'installation assistee (Wizard) a executer une seule fois a la premiere ouverture."),
                    ChangeEntry("Verification automatique et silencieuse des mises a jour disponibles via GitHub Releases."),
                    ChangeEntry("Amelioration de la stabilite du service de lecture audio en arriere-plan.")
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
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReleaseNotesScreen(
    isFr: Boolean = true,
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
                ReleaseCard(release = release, isFr = isFr)
            }
        }
    }
}

// ---------------------------------------------------------------------------
// Sub-composables
// ---------------------------------------------------------------------------

@Composable
private fun ReleaseCard(release: ReleaseBlock, isFr: Boolean) {
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
