package com.example.sensorapp.presentation.permission

import android.Manifest
import android.content.Intent
import android.os.Build
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.core.app.ActivityCompat
import com.example.sensorapp.domain.model.SensorType
import kotlinx.coroutines.launch

@Composable
fun rememberPermissionHandler(
    snackbarHostState: SnackbarHostState
): PermissionHandlerState {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var pendingSensorType by remember { mutableStateOf<SensorType?>(null) }
    var pendingCallback by remember { mutableStateOf<(() -> Unit)?>(null) }
    var showDialog by remember { mutableStateOf(false) }
    var permissionToRequest by remember { mutableStateOf("") }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        val sensorType = pendingSensorType
        val callback = pendingCallback
        pendingCallback = null
        pendingSensorType = null
        if (granted) {
            callback?.invoke()
        } else {
            scope.launch {
                val result = snackbarHostState.showSnackbar(
                    message = "${sensorType?.displayName ?: "This sensor"} needs permission. You can grant it in Settings.",
                    actionLabel = "Open Settings",
                    duration = SnackbarDuration.Long
                )
                if (result == androidx.compose.material3.SnackbarResult.ActionPerformed) {
                    context.startActivity(
                        Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                            data = android.net.Uri.fromParts("package", context.packageName, null)
                        }
                    )
                }
            }
        }
    }

    fun checkAndRequest(sensorType: SensorType, onGranted: () -> Unit) {
        val permission = when (sensorType) {
            SensorType.STEP_COUNTER -> {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    Manifest.permission.ACTIVITY_RECOGNITION
                } else null
            }
            else -> null
        }

        if (permission == null) {
            onGranted()
            return
        }

        if (ActivityCompat.checkSelfPermission(context, permission) ==
            android.content.pm.PackageManager.PERMISSION_GRANTED
        ) {
            onGranted()
            return
        }

        pendingSensorType = sensorType
        pendingCallback = onGranted
        permissionToRequest = permission
        showDialog = true
    }

    return PermissionHandlerState(
        showDialog = showDialog,
        onDismissDialog = { showDialog = false; pendingSensorType = null; pendingCallback = null },
        onConfirmDialog = {
            showDialog = false
            launcher.launch(permissionToRequest)
        },
        checkAndRequest = { sensorType, onGranted ->
            checkAndRequest(sensorType, onGranted)
        },
        pendingSensorType = pendingSensorType
    )
}

data class PermissionHandlerState(
    val showDialog: Boolean,
    val onDismissDialog: () -> Unit,
    val onConfirmDialog: () -> Unit,
    val checkAndRequest: (SensorType, () -> Unit) -> Unit,
    val pendingSensorType: SensorType?
)

@Composable
fun PermissionDialog(
    sensorType: SensorType?,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    if (sensorType == null) return

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(text = "Permission needed", fontWeight = FontWeight.Bold)
        },
        text = {
            Text(
                "To use the ${sensorType.displayName}, SensorApp needs access to your " +
                        "physical activity. This data stays on your device and is never uploaded."
            )
        },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text("Allow")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Not now")
            }
        }
    )
}
