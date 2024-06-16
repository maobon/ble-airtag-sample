package com.sample.airtagger.ble

import android.Manifest
import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.bluetooth.le.BluetoothLeScanner
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.activity.result.ActivityResultCaller
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import com.sample.airtagger.utils.hasPermission

class BluetoothUtil(
    private val context: Context
) {

    private val mBluetoothAdapter: BluetoothAdapter by lazy {
        val bluetoothManager =
            context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        bluetoothManager.adapter
    }

    val mLeScanner: BluetoothLeScanner by lazy {
        mBluetoothAdapter.bluetoothLeScanner
    }

    fun isBluetoothEnable() = mBluetoothAdapter.isEnabled

    /**
     * Prompts the user to enable Bluetooth via a system dialog.
     *
     * For Android 12+, [Manifest.permission.BLUETOOTH_CONNECT] is required to use
     * the [BluetoothAdapter.ACTION_REQUEST_ENABLE] intent.
     */
    fun enableBluetooth(enablingBluetooth: ActivityResultLauncher<Intent>) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && !(context.hasPermission(Manifest.permission.BLUETOOTH_CONNECT))) {
            // Insufficient permission to prompt for Bluetooth enabling
            Log.d(TAG, "Insufficient permission to prompt for Bluetooth enabling")
            return
        }

        Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE).apply {
            enablingBluetooth.launch(this)
        }
    }

    fun createBluetoothEnablingResult(
        caller: ActivityResultCaller,
        onUnexpected: (() -> Unit)? = null,
        onExpect: () -> Unit
    ) =
        caller.registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            if (it.resultCode == Activity.RESULT_OK) {
                Log.d(TAG, "getBluetoothEnablingResult: result is ok")
                onExpect()
            } else {
                Log.d(TAG, "getBluetoothEnablingResult: result is not ok")
                onUnexpected?.invoke()
            }
        }

    companion object {
        private const val TAG = "BluetoothUtil"
    }
}