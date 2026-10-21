package com.karlitodev.novelize

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.PowerManager
import android.provider.Settings
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.karlitodev.novelize.theme.NoveLizeTheme
import com.karlitodev.novelize.update.UpdateManager
import kotlinx.coroutines.launch

/**
 * Root activity for the NoveLize application.
 * Handles edge-to-edge layout initialization, runtime permission requests,
 * battery optimization exemption, and background update checks.
 */
class MainActivity : ComponentActivity() {

    /**
     * Launcher for requesting notification permission on Android 13+.
     */
    private val notificationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { /* Permission result handled silently */ }

    /**
     * Launcher for the battery optimization exemption settings screen.
     * Re-checks result on return so background narration stays uninterrupted.
     */
    private val batteryOptimizationLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) {
        val pm = getSystemService(POWER_SERVICE) as PowerManager
        if (!pm.isIgnoringBatteryOptimizations(packageName)) {
            Toast.makeText(
                this,
                "Battery optimization is still active. Background narration may be interrupted.",
                Toast.LENGTH_LONG
            ).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        com.karlitodev.novelize.crash.CrashHandler.init(this)

        enableEdgeToEdge()

        // Request runtime permissions (storage / audio)
        requestAppPermissions()

        // Request battery optimization exemption for uninterrupted background playback
        requestBatteryOptimizationExemption()

        // Schedule a silent background check for new releases
        checkForUpdates()

        setContent {
            NoveLizeTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    MainNavigation()
                }
            }
        }
    }

    /**
     * Requests notification permission on Android 13+ (API 33) for background narration controls.
     */
    private fun requestAppPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED
            ) {
                notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }

    /**
     * Asks the system to exclude NoveLize from battery optimization.
     * This prevents Doze mode from pausing background TTS narration or SFX playback.
     */
    private fun requestBatteryOptimizationExemption() {
        val pm = getSystemService(POWER_SERVICE) as PowerManager
        if (!pm.isIgnoringBatteryOptimizations(packageName)) {
            val intent = Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS).apply {
                data = Uri.parse("package:$packageName")
            }
            try {
                batteryOptimizationLauncher.launch(intent)
            } catch (_: Exception) {
                // Some OEMs remove this activity; silently fail
            }
        }
    }

    /**
     * Silently checks GitHub Releases for a newer APK version.
     * If a new version is found, the update dialog will be presented
     * when the user navigates to Settings or the update screen.
     */
    private fun checkForUpdates() {
        lifecycleScope.launch {
            try {
                val manager = UpdateManager(this@MainActivity)
                manager.checkForUpdates()
            } catch (_: Exception) {
                // Update check failures are silent; the user is not blocked
            }
        }
    }
}
