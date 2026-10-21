package com.karlitodev.novelize.ui.library

import android.graphics.BitmapFactory
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.AutoStories
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.Headphones
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material3.*
import androidx.compose.runtime.*
import kotlinx.coroutines.launch
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.karlitodev.novelize.model.Book
import com.karlitodev.novelize.theme.BrandAmber
import com.karlitodev.novelize.theme.BrandPurple
import com.karlitodev.novelize.theme.BrandPurpleLight
import com.karlitodev.novelize.theme.BrandTeal
import java.io.File

/**
 * Main bookshelf displaying imported novels, reading progress, and quick resume actions.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LibraryScreen(
    books: List<Book>,
    onBookClick: (Book) -> Unit,
    onImportUri: (Uri) -> Unit,
    onImportDemoClick: () -> Unit,
    onSettingsClick: () -> Unit,
    onPickCover: (String, Uri) -> Unit = { _, _ -> },
    onDeleteBook: (String) -> Unit = {},
    lastSeenVersion: String = "",
    onAcknowledgeChangelog: (String) -> Unit = {},
    appLanguage: String = "fr",
    modifier: Modifier = Modifier
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val coroutineScope = androidx.compose.runtime.rememberCoroutineScope()
    val updateManager = remember { com.karlitodev.novelize.update.UpdateManager(context) }
    val updateState by updateManager.state.collectAsState()
    val sfxDownloadManager = remember { com.karlitodev.novelize.sfx.SfxDownloadManager(context) }
    val isFr = appLanguage == "fr"

    val currentVersion = "1.2"
    var showChangelog by remember { mutableStateOf(lastSeenVersion.isNotEmpty() && lastSeenVersion != currentVersion) }
    var pendingCrashReport by remember {
        mutableStateOf(if (com.karlitodev.novelize.crash.CrashHandler.hasCrashReport(context)) com.karlitodev.novelize.crash.CrashHandler.readCrashReport(context) else null)
    }
    var newSfxNotice by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        updateManager.checkForUpdates()
        coroutineScope.launch {
            val newlyDownloaded = sfxDownloadManager.syncMissingSfx()
            if (newlyDownloaded > 0) {
                newSfxNotice = if (isFr) {
                    "Yo ! Y a de nouveaux sons, viens les tester et me dire ce que tu en penses !"
                } else {
                    "Yo! There are new sounds, come test them and let me know what you think!"
                }
            }
        }
    }

    var selectedBookForCover by remember { mutableStateOf<Book?>(null) }
    val coverPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        val targetBook = selectedBookForCover ?: return@rememberLauncherForActivityResult
        if (uri != null) {
            onPickCover(targetBook.id, uri)
        }
        selectedBookForCover = null
    }

    val documentPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri != null) {
            onImportUri(uri)
        }
    }

    // WhatsApp Crash Report Dialog
    pendingCrashReport?.let { report ->
        AlertDialog(
            onDismissRequest = {
                com.karlitodev.novelize.crash.CrashHandler.clearCrashReport(context)
                pendingCrashReport = null
            },
            title = {
                Text(
                    text = if (isFr) "Rapport d'incident détecté" else "Crash Report Detected",
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text(
                    text = if (isFr)
                        "Un problème inattendu s'est produit. Voulez-vous envoyer le fichier crash.txt par WhatsApp pour analyse ?"
                    else
                        "An unexpected crash occurred. Would you like to send crash.txt via WhatsApp for developer analysis?"
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        com.karlitodev.novelize.crash.CrashHandler.sendCrashReportViaWhatsApp(context, report)
                        com.karlitodev.novelize.crash.CrashHandler.clearCrashReport(context)
                        pendingCrashReport = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = BrandPurple)
                ) {
                    Text(if (isFr) "Envoyer sur WhatsApp" else "Send via WhatsApp")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        com.karlitodev.novelize.crash.CrashHandler.clearCrashReport(context)
                        pendingCrashReport = null
                    }
                ) {
                    Text(if (isFr) "Ignorer" else "Dismiss")
                }
            }
        )
    }

    // New SFX Welcome notification
    newSfxNotice?.let { notice ->
        AlertDialog(
            onDismissRequest = { newSfxNotice = null },
            title = { Text(text = "Yo !", fontWeight = FontWeight.Bold, fontSize = 18.sp) },
            text = { Text(text = notice, fontSize = 14.sp) },
            confirmButton = {
                Button(
                    onClick = { newSfxNotice = null },
                    colors = ButtonDefaults.buttonColors(containerColor = BrandPurple)
                ) {
                    Text("Top !")
                }
            }
        )
    }

    // Startup Changelog Dialog without emojis
    if (showChangelog) {
        AlertDialog(
            onDismissRequest = {
                showChangelog = false
                onAcknowledgeChangelog(currentVersion)
            },
            title = {
                Text(
                    text = "Nouveautes de la version $currentVersion",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "- Controles de lecture integres dans la barre de notification et l'ecran de verrouillage Android (MediaSession & MediaStyle).",
                        fontSize = 13.sp
                    )
                    Text(
                        text = "- Lecture native des documents PDF avec extraction automatique des chapitres et de la couverture.",
                        fontSize = 13.sp
                    )
                    Text(
                        text = "- Support complet des fichiers EPUB avec respect de l'ordre de lecture et extraction de couverture.",
                        fontSize = 13.sp
                    )
                    Text(
                        text = "- Support des bandes dessinees et mangas au format CBZ.",
                        fontSize = 13.sp
                    )
                    Text(
                        text = "- Synthese vocale Android TextToSpeech personnalisee selon les personnages en cas d'indisponibilite du modele IA.",
                        fontSize = 13.sp
                    )
                    Text(
                        text = "- Reprise de lecture intelligente avec mise en surbrillance clignotante et assombrissement de l'interface.",
                        fontSize = 13.sp
                    )
                    Text(
                        text = "- Filtrage et limitation de repetition des effets sonores (SFX).",
                        fontSize = 13.sp
                    )
                    Text(
                        text = "- Possibilite de personnaliser la couverture de n'importe quel livre depuis votre galerie.",
                        fontSize = 13.sp
                    )
                    Text(
                        text = "- Suppression de livres de la bibliotheque.",
                        fontSize = 13.sp
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        showChangelog = false
                        onAcknowledgeChangelog(currentVersion)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = BrandPurple)
                ) {
                    Text("Compris")
                }
            }
        )
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.AutoStories,
                            contentDescription = null,
                            tint = BrandPurple,
                            modifier = Modifier.size(28.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "NoveLize",
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 22.sp
                        )
                    }
                },
                actions = {
                    IconButton(onClick = onSettingsClick) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Settings"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = {
                    documentPicker.launch(
                        arrayOf(
                            "application/epub+zip",
                            "application/pdf",
                            "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
                            "application/x-cbz",
                            "application/vnd.comicbook+zip",
                            "text/plain",
                            "text/markdown",
                            "*/*"
                        )
                    )
                },
                containerColor = BrandPurple,
                contentColor = Color.White,
                icon = { Icon(Icons.Default.Add, contentDescription = null) },
                text = { Text("Importer (EPUB, PDF, CBZ, TXT)", fontWeight = FontWeight.SemiBold) }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .padding(horizontal = 16.dp)
        ) {
            // GitHub Releases Update Banner
            (updateState as? com.karlitodev.novelize.update.UpdateState.UpdateAvailable)?.let { update ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 10.dp, bottom = 4.dp),
                    colors = CardDefaults.cardColors(containerColor = BrandPurple),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Update Available: ${update.release.tagName}", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            Text("New version published on GitHub", color = Color.White.copy(alpha = 0.8f), fontSize = 11.sp)
                        }
                        FilledTonalButton(
                            onClick = { coroutineScope.launch { updateManager.downloadAndInstall(update.release) } },
                            colors = ButtonDefaults.filledTonalButtonColors(containerColor = Color.White, contentColor = BrandPurple),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("Update", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                    }
                }
            }
            if (books.isEmpty()) {
                // Empty bookshelf hero state
                EmptyBookshelfCard(
                    onBrowseFiles = {
                        documentPicker.launch(arrayOf("application/epub+zip", "text/plain", "*/*"))
                    },
                    onLoadDemo = onImportDemoClick
                )
            } else {
                // Featured resume banner if books exist
                val latestBook = books.first()
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp)
                        .clickable { onBookClick(latestBook) },
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Decorative cover thumbnail
                        Box(
                            modifier = Modifier
                                .size(64.dp, 84.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(
                                    Brush.verticalGradient(
                                        listOf(
                                            Color(latestBook.dominantColorHex ?: 0xFF6C5CE7),
                                            Color(0xFF241E38)
                                        )
                                    )
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Book,
                                contentDescription = null,
                                tint = Color.White.copy(alpha = 0.8f),
                                modifier = Modifier.size(28.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(16.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            val bannerPercent = (latestBook.readingProgressPercent * 100).toInt()
                            Text(
                                text = if (bannerPercent > 0) "CONTINUE READING • $bannerPercent%" else "START READING",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = BrandPurple
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = latestBook.title,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = "By ${latestBook.author}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            LinearProgressIndicator(
                                progress = { latestBook.readingProgressPercent.coerceIn(0f, 1f) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(6.dp)
                                    .clip(RoundedCornerShape(3.dp)),
                                color = BrandPurple,
                                trackColor = MaterialTheme.colorScheme.outlineVariant
                            )
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        FilledIconButton(
                            onClick = { onBookClick(latestBook) },
                            colors = IconButtonDefaults.filledIconButtonColors(containerColor = BrandPurple)
                        ) {
                            Icon(Icons.Default.PlayArrow, contentDescription = "Play")
                        }
                    }
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp, bottom = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Your Bookshelf (${books.size})",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    TextButton(onClick = onImportDemoClick) {
                        Text("+ Sample Novel", color = BrandPurple, fontSize = 12.sp)
                    }
                }

                // Bookshelf grid
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    horizontalArrangement = Arrangement.spacedBy(14.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    contentPadding = PaddingValues(bottom = 90.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(books) { book ->
                        BookCard(
                            book = book,
                            onClick = { onBookClick(book) },
                            onPickCoverClick = {
                                selectedBookForCover = book
                                coverPickerLauncher.launch("image/*")
                            },
                            onDeleteClick = {
                                onDeleteBook(book.id)
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun EmptyBookshelfCard(
    onBrowseFiles: () -> Unit,
    onLoadDemo: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 32.dp),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(28.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(BrandPurple.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Headphones,
                    contentDescription = null,
                    tint = BrandPurple,
                    modifier = Modifier.size(44.dp)
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = "Votre bibliothèque est vide",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Importez des fichiers EPUB, PDF, CBZ, DOCX, MD ou TXT pour profiter de la lecture immersive.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                lineHeight = 20.sp
            )

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = onBrowseFiles,
                colors = ButtonDefaults.buttonColors(containerColor = BrandPurple),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.FolderOpen, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Sélectionner un fichier", fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedButton(
                onClick = onLoadDemo,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Charger l'histoire de démonstration", fontWeight = FontWeight.SemiBold)
            }
        }
    }
}

@Composable
private fun BookCard(
    book: Book,
    onClick: () -> Unit,
    onPickCoverClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    val coverBitmap = remember(book.coverPath) {
        if (!book.coverPath.isNullOrBlank()) {
            val file = java.io.File(book.coverPath)
            if (file.exists()) {
                BitmapFactory.decodeFile(file.absolutePath)
            } else null
        } else null
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column {
            // Book cover representation
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
                    .background(
                        Brush.verticalGradient(
                            listOf(
                                Color(book.dominantColorHex ?: 0xFF6C5CE7),
                                Color(0xFF1E1B2E)
                            )
                        )
                    )
            ) {
                if (coverBitmap != null) {
                    Image(
                        bitmap = coverBitmap.asImageBitmap(),
                        contentDescription = book.title,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                    // Gradient scrim over the bottom of the cover for readability
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.verticalGradient(
                                    colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.75f)),
                                    startY = 100f
                                )
                            )
                    )
                }

                // Top badges & actions row
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Format tag
                    Surface(
                        color = Color.Black.copy(alpha = 0.6f),
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text(
                            text = book.format.name,
                            color = Color.White,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }

                    // Quick action icons (Change cover, delete)
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        IconButton(
                            onClick = onPickCoverClick,
                            modifier = Modifier
                                .size(28.dp)
                                .clip(RoundedCornerShape(14.dp))
                                .background(Color.Black.copy(alpha = 0.5f))
                        ) {
                            Icon(
                                imageVector = Icons.Default.AddPhotoAlternate,
                                contentDescription = "Changer la couverture",
                                tint = Color.White,
                                modifier = Modifier.size(16.dp)
                            )
                        }

                        IconButton(
                            onClick = onDeleteClick,
                            modifier = Modifier
                                .size(28.dp)
                                .clip(RoundedCornerShape(14.dp))
                                .background(Color.Black.copy(alpha = 0.5f))
                        ) {
                            Icon(
                                imageVector = Icons.Default.DeleteOutline,
                                contentDescription = "Supprimer le livre",
                                tint = Color.White,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }

                // Title and Author overlay at bottom
                Column(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(10.dp)
                ) {
                    Text(
                        text = book.title,
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = book.author,
                        color = Color.White.copy(alpha = 0.75f),
                        fontSize = 11.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            // Progress footer
            Column(modifier = Modifier.padding(10.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "${book.totalChapters} chapitres",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    val percentInt = (book.readingProgressPercent * 100).toInt()
                    Text(
                        text = if (percentInt > 0) "$percentInt%" else "Prêt",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = BrandPurple
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                LinearProgressIndicator(
                    progress = { book.readingProgressPercent.coerceIn(0f, 1f) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(4.dp)
                        .clip(RoundedCornerShape(2.dp)),
                    color = BrandPurple,
                    trackColor = MaterialTheme.colorScheme.outlineVariant
                )
            }
        }
    }
}
