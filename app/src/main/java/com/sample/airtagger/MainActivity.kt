package com.sample.airtagger

import android.Manifest
import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Build
import android.os.Bundle
import android.os.IBinder
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.sample.airtagger.ble.BluetoothUtil
import com.sample.airtagger.databinding.ActivityMainBinding
import com.sample.airtagger.utils.hasPermission
import com.sample.airtagger.utils.hasRequiredBluetoothPermissions
import com.sample.airtagger.utils.onRequestPermissionsResults
import com.sample.airtagger.utils.requestRelevantRuntimePermissions

class MainActivity : AppCompatActivity() {

    private lateinit var activityMainBinding: ActivityMainBinding
    private var mService: BleService? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        activityMainBinding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(activityMainBinding.root)

        initViews()
    }

    private fun initViews() {
        activityMainBinding.btnTest.setOnClickListener {
            scan()
        }
    }

    override fun onResume() {
        super.onResume()

        bindService(Intent(this, BleService::class.java), object : ServiceConnection {
            override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
                mService = (service as BleService.LocalBinder).getService()
            }

            override fun onServiceDisconnected(name: ComponentName?) {
                mService = null
            }
        }, Context.BIND_AUTO_CREATE)

        isBluetoothEnabled {
            scan()
        }
    }

    private fun scan() {
        if (!hasRequiredBluetoothPermissions()) {
            requestRelevantRuntimePermissions()
        } else {
            startBluetoothScan()
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<out String>, grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        onRequestPermissionsResults(requestCode, permissions, grantResults, null) {
            startBluetoothScan()
        }
    }

    private fun startBluetoothScan(){
        mService?.startBleScan()
    }

    /**
     * Prompts the user to enable Bluetooth via a system dialog.
     *
     * For Android 12+, [Manifest.permission.BLUETOOTH_CONNECT] is required to use
     * the [BluetoothAdapter.ACTION_REQUEST_ENABLE] intent.
     */
    private fun isBluetoothEnabled(unexpected: (() -> Unit)? = null, expect: () -> Unit) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S
            && !hasPermission(Manifest.permission.BLUETOOTH_CONNECT)
        ) {
            // Insufficient permission to prompt for Bluetooth enabling
            // Log.d(TAG, "Insufficient permission to prompt for Bluetooth enabling")
            unexpected?.invoke()
            return
        }

        if (!BluetoothUtil(this@MainActivity).isBluetoothEnable()) {
            Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE).apply {
                registerForActivityResult(
                    ActivityResultContracts.StartActivityForResult()
                ) {
                    if (it.resultCode == Activity.RESULT_OK) {
                        expect()
                    } else {
                        unexpected?.invoke()
                    }
                }.launch(this)
            }
        }
    }
}