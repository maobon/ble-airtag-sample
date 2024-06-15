package com.sample.airtagger.utils

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.DialogInterface.OnClickListener
import android.content.pm.PackageManager
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.sample.airtagger.R

const val RUNTIME_PERMISSIONS_REQUEST_CODE = 100

private val BLUETOOTH_PERMISSION_BELOW_S = arrayOf(
    Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION
)

@RequiresApi(Build.VERSION_CODES.S)
private val BLUETOOTH_PERMISSIONS_ABOVE_S = arrayOf(
    Manifest.permission.BLUETOOTH_SCAN,
    Manifest.permission.BLUETOOTH_CONNECT,
    Manifest.permission.ACCESS_COARSE_LOCATION,
    Manifest.permission.ACCESS_FINE_LOCATION
)

/**
 * Determine whether the current [Context] has been granted the relevant [Manifest.permission].
 */
fun Context.hasPermission(permissionType: String): Boolean {
    return ContextCompat.checkSelfPermission(
        this, permissionType
    ) == PackageManager.PERMISSION_GRANTED
}

/**
 * Determine whether the current [Context] has been granted the relevant permissions to perform
 * Bluetooth operations depending on the mobile device's Android version.
 */
fun Context.hasRequiredBluetoothPermissions(): Boolean {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        hasPermission(Manifest.permission.BLUETOOTH_SCAN) && hasPermission(Manifest.permission.BLUETOOTH_CONNECT)
    } else {
        hasPermission(Manifest.permission.ACCESS_FINE_LOCATION)
    }
}

fun Activity.requestRelevantRuntimePermissions() {
    if (hasRequiredBluetoothPermissions()) return

    with(this) {
        when {
            Build.VERSION.SDK_INT < Build.VERSION_CODES.S -> {
                createDialogToRequestRuntimePermissions(
                    this, getString(R.string.title_alert_dialog), getString(R.string.prompt_below_s)
                ) { _, _ ->
                    ActivityCompat.requestPermissions(
                        this, BLUETOOTH_PERMISSION_BELOW_S, RUNTIME_PERMISSIONS_REQUEST_CODE
                    )
                }
            }

            Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
                createDialogToRequestRuntimePermissions(
                    this, getString(R.string.title_alert_dialog), getString(R.string.prompt_above_s)
                ) { _, _ ->
                    ActivityCompat.requestPermissions(
                        this, BLUETOOTH_PERMISSIONS_ABOVE_S, RUNTIME_PERMISSIONS_REQUEST_CODE
                    )
                }
            }
        }
    }

}

private fun createDialogToRequestRuntimePermissions(
    activity: Activity, title: String, message: String, onClickListener: OnClickListener
) = with(activity) {
    runOnUiThread {
        AlertDialog.Builder(activity).setTitle(title).setMessage(message).setCancelable(false)
            .setPositiveButton(android.R.string.ok, onClickListener).show()
    }
}

fun Activity.onRequestPermissionsResults(
    requestCode: Int,
    permissions: Array<out String>,
    grantResults: IntArray,
    unexpected: (() -> Unit)? = null,
    expected: () -> Unit
) {
    if (requestCode != RUNTIME_PERMISSIONS_REQUEST_CODE) return

    // first param: permission name
    // second param: request result
    val containsPermanentDenial = permissions.zip(grantResults.toTypedArray()).any {
        it.second == PackageManager.PERMISSION_DENIED && !ActivityCompat.shouldShowRequestPermissionRationale(
            this, it.first
        )
    }

    val containsDenial = grantResults.any {
        it == PackageManager.PERMISSION_DENIED
    }

    val allGranted = grantResults.all {
        it == PackageManager.PERMISSION_GRANTED
    }

    when {
        containsPermanentDenial -> {
            // TODO: Handle permanent denial (e.g., show AlertDialog with justification)
            // Note: The user will need to navigate to App Settings and manually grant
            // permissions that were permanently denied
        }

        containsDenial -> {
            requestRelevantRuntimePermissions()
        }

        allGranted && hasRequiredBluetoothPermissions() -> {
            // todo core method ...
            // sendMessage(sMessenger, ACTION_CONNECTING)
            // rootView.btnTest1.isEnabled = false

            expected()
        }

        else -> {
            // Unexpected scenario encountered when handling permissions
            // recreate()

            if (unexpected != null) {
                unexpected()
            }
        }
    }
}

