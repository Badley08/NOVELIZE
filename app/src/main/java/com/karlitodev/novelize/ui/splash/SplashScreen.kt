package com.karlitodev.novelize.ui.splash

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.PowerManager
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoStories
import androidx.compose.material.icons.filled.BatteryAlert
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Headphones
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.RecordVoiceOver
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.karlitodev.novelize.theme.BrandAmber
import com.karlitodev.novelize.theme.BrandCoral
import com.karlitodev.novelize.theme.BrandEmerald
import com.karlitodev.novelize.theme.BrandPurple
import com.karlitodev.novelize.theme.BrandPurpleLight
import com.karlitodev.novelize.theme.BrandTeal
import kotlinx.coroutines.delay
import kotlin.math.cos
import kotlin.math.sin

/**
 * Animated splash screen with physics-based spring animations and floating orbital particles.
 * On completion, requests storage access and battery optimization exemption before
 * transitioning to the main navigation graph.
 */
@Composable
fun SplashScreen(
    onSplashComplete: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    // Phase state machine: 0=animation, 1=permission requests, 2=done
    var phase by remember { mutableIntStateOf(0) }

    // Permission tracking
    var storageGranted by remember { mutableStateOf(isStoragePermissionGranted(context)) }
    var batteryOptimizationDisabled by remember { mutableStateOf(isBatteryOptimizationDisabled(context)) }

    // Storage permission launcher
    val storagePermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        storageGranted = isGranted
    }

    // Battery optimization intent launcher
    val batteryIntentLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) {
        batteryOptimizationDisabled = isBatteryOptimizationDisabled(context)
    }

    when (phase) {
        0 -> {
            PhysicsAnimatedSplash(
                onAnimationEnd = { phase = 1 },
                modifier = modifier
            )
        }
        1 -> {
            PermissionRequestScreen(
                storageGranted = storageGranted,
                batteryOptimizationDisabled = batteryOptimizationDisabled,
                onRequestStorage = {
                    val permission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        Manifest.permission.READ_MEDIA_AUDIO
                    } else {
                        Manifest.permission.READ_EXTERNAL_STORAGE
                    }
                    storagePermissionLauncher.launch(permission)
                },
                onRequestBattery = {
                    val intent = Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS).apply {
                        data = Uri.parse("package:${context.packageName}")
                    }
                    batteryIntentLauncher.launch(intent)
                },
                onContinue = {
                    phase = 2
                    onSplashComplete()
                },
                modifier = modifier
            )
        }
    }
}

/**
 * Physics-based animated launch screen with spring-driven logo entrance,
 * orbiting feature particles, and pulsing sound wave rings.
 */
@Composable
private fun PhysicsAnimatedSplash(
    onAnimationEnd: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Spring-driven logo scale with overshoot bounce
    val logoScale = remember { Animatable(0f) }
    // Spring-driven logo vertical offset (drops from above and settles)
    val logoOffsetY = remember { Animatable(-200f) }
    // Spring-driven title alpha
    val titleAlpha = remember { Animatable(0f) }
    // Spring-driven subtitle alpha
    val subtitleAlpha = remember { Animatable(0f) }

    // Infinite orbital rotation for floating feature icons
    val infiniteTransition = rememberInfiniteTransition(label = "orbital_spin")
    val orbitalAngle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 8000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "orbital_angle"
    )

    // Pulsing sound wave ring scale
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.8f,
        targetValue = 1.6f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "pulse_scale"
    )
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.6f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "pulse_alpha"
    )

    // Second pulse ring (offset timing)
    val pulseScale2 by infiniteTransition.animateFloat(
        initialValue = 0.6f,
        targetValue = 1.8f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "pulse_scale2"
    )
    val pulseAlpha2 by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "pulse_alpha2"
    )

    LaunchedEffect(Unit) {
        // Spring animation for logo scale with high-stiffness overshoot bounce
        logoScale.animateTo(
            targetValue = 1f,
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessLow
            )
        )
        // Spring animation for logo vertical drop with settling bounce
        logoOffsetY.animateTo(
            targetValue = 0f,
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioLowBouncy,
                stiffness = Spring.StiffnessMediumLow
            )
        )
        // Title fade in with spring feel
        titleAlpha.animateTo(
            targetValue = 1f,
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioNoBouncy,
                stiffness = Spring.StiffnessVeryLow
            )
        )
        // Subtitle fade
        subtitleAlpha.animateTo(
            targetValue = 1f,
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioNoBouncy,
                stiffness = Spring.StiffnessVeryLow
            )
        )
        delay(1800)
        onAnimationEnd()
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(Color(0xFF13111C), Color(0xFF1E1B2E), Color(0xFF2D1F5E))
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        // Background floating particles canvas
        Canvas(modifier = Modifier.fillMaxSize()) {
            val centerX = size.width / 2
            val centerY = size.height / 2

            // Draw pulsing sound wave rings
            drawCircle(
                color = BrandPurpleLight,
                radius = 100f * pulseScale,
                center = Offset(centerX, centerY),
                alpha = pulseAlpha
            )
            drawCircle(
                color = BrandTeal,
                radius = 80f * pulseScale2,
                center = Offset(centerX, centerY),
                alpha = pulseAlpha2
            )

            // Orbital feature dots
            val orbitRadius = 180f
            val iconPositions = listOf(0f, 60f, 120f, 180f, 240f, 300f)
            val colors = listOf(BrandAmber, BrandTeal, BrandCoral, BrandEmerald, BrandPurpleLight, BrandAmber)

            iconPositions.forEachIndexed { index, baseAngle ->
                val angle = Math.toRadians((baseAngle + orbitalAngle).toDouble())
                val x = centerX + orbitRadius * cos(angle).toFloat()
                val y = centerY + orbitRadius * sin(angle).toFloat()
                drawCircle(
                    color = colors[index],
                    radius = 6f,
                    center = Offset(x, y),
                    alpha = 0.7f
                )
            }
        }

        // Main content column with spring-animated elements
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.offset(y = logoOffsetY.value.dp)
        ) {
            // Pulsing logo badge
            Box(
                modifier = Modifier
                    .scale(logoScale.value)
                    .size(130.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.radialGradient(
                            listOf(Color(0xFF2D1F5E), Color(0xFF15102A), Color(0xFF0D0A1A))
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                androidx.compose.foundation.Image(
                    painter = androidx.compose.ui.res.painterResource(id = com.karlitodev.novelize.R.drawable.ic_launcher_foreground),
                    contentDescription = "NoveLize Logo",
                    modifier = Modifier.size(110.dp)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "NoveLize",
                fontSize = 36.sp,
                fontWeight = FontWeight.ExtraBold,
                color = Color.White,
                modifier = Modifier.alpha(titleAlpha.value)
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Immersive Light Novel Audio Drama",
                fontSize = 14.sp,
                color = BrandPurpleLight,
                modifier = Modifier.alpha(subtitleAlpha.value)
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Animated feature icon row appearing with spring
            Row(
                horizontalArrangement = Arrangement.spacedBy(20.dp),
                modifier = Modifier.alpha(subtitleAlpha.value)
            ) {
                FeatureChip(icon = Icons.Default.RecordVoiceOver, label = "Multi-Voice")
                FeatureChip(icon = Icons.Default.MusicNote, label = "Sound FX")
                FeatureChip(icon = Icons.Default.Headphones, label = "Offline")
            }
        }
    }
}

@Composable
private fun FeatureChip(icon: ImageVector, label: String) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(CircleShape)
                .background(BrandPurple.copy(alpha = 0.3f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = BrandPurpleLight,
                modifier = Modifier.size(22.dp)
            )
        }
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = label,
            fontSize = 10.sp,
            color = Color.White.copy(alpha = 0.8f),
            fontWeight = FontWeight.Medium
        )
    }
}

/**
 * Permission request screen displayed after splash animation.
 * Requests storage access and battery optimization exemption.
 */
@Composable
private fun PermissionRequestScreen(
    storageGranted: Boolean,
    batteryOptimizationDisabled: Boolean,
    onRequestStorage: () -> Unit,
    onRequestBattery: () -> Unit,
    onContinue: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(Color(0xFF13111C), Color(0xFF1E1B2E))
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Security,
                contentDescription = null,
                tint = BrandPurple,
                modifier = Modifier.size(48.dp)
            )

            Text(
                text = "Required Permissions",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                textAlign = TextAlign.Center
            )

            Text(
                text = "NoveLize needs a few permissions to read your imported novels and deliver uninterrupted narration in the background.",
                fontSize = 14.sp,
                color = Color.White.copy(alpha = 0.7f),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Storage access permission card
            PermissionCard(
                icon = Icons.Default.Folder,
                title = "Storage Access",
                description = "Read your local EPUB, TXT, PDF, and DOCX novel files.",
                isGranted = storageGranted,
                onRequest = onRequestStorage
            )

            // Battery optimization exemption card
            PermissionCard(
                icon = Icons.Default.BatteryAlert,
                title = "Unrestricted Background",
                description = "Prevent the system from pausing narration during long listening sessions with the screen off.",
                isGranted = batteryOptimizationDisabled,
                onRequest = onRequestBattery
            )

            Spacer(modifier = Modifier.height(12.dp))

            Button(
                onClick = onContinue,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = BrandPurple)
            ) {
                Text(
                    text = if (storageGranted && batteryOptimizationDisabled) "All Set — Continue" else "Continue Anyway",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
            }

            if (!storageGranted || !batteryOptimizationDisabled) {
                Text(
                    text = "You can grant these later in Settings.",
                    fontSize = 12.sp,
                    color = Color.White.copy(alpha = 0.5f)
                )
            }
        }
    }
}

@Composable
private fun PermissionCard(
    icon: ImageVector,
    title: String,
    description: String,
    isGranted: Boolean,
    onRequest: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isGranted) BrandEmerald.copy(alpha = 0.15f) else Color.White.copy(alpha = 0.08f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = if (isGranted) Icons.Default.CheckCircle else icon,
                contentDescription = null,
                tint = if (isGranted) BrandEmerald else BrandPurpleLight,
                modifier = Modifier.size(32.dp)
            )
            Spacer(modifier = Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = Color.White
                )
                Text(
                    text = description,
                    fontSize = 12.sp,
                    color = Color.White.copy(alpha = 0.6f)
                )
            }
            if (!isGranted) {
                Spacer(modifier = Modifier.width(8.dp))
                OutlinedButton(
                    onClick = onRequest,
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = BrandPurpleLight)
                ) {
                    Text("Grant", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }
            }
        }
    }
}

private fun isStoragePermissionGranted(context: Context): Boolean {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        ContextCompat.checkSelfPermission(
            context, Manifest.permission.READ_MEDIA_AUDIO
        ) == PackageManager.PERMISSION_GRANTED
    } else {
        ContextCompat.checkSelfPermission(
            context, Manifest.permission.READ_EXTERNAL_STORAGE
        ) == PackageManager.PERMISSION_GRANTED
    }
}

private fun isBatteryOptimizationDisabled(context: Context): Boolean {
    val pm = context.getSystemService(Context.POWER_SERVICE) as PowerManager
    return pm.isIgnoringBatteryOptimizations(context.packageName)
}
