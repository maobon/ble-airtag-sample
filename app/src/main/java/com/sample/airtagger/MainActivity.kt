package com.sample.airtagger

import android.content.BroadcastReceiver
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.ServiceConnection
import android.os.Bundle
import android.os.IBinder
import android.util.Log
import androidx.activity.result.ActivityResultLauncher
import androidx.appcompat.app.AppCompatActivity
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.sample.airtagger.ble.BluetoothUtil
import com.sample.airtagger.databinding.ActivityMainBinding
import com.sample.airtagger.utils.enableLocation
import com.sample.airtagger.utils.hasRequiredBluetoothPermissions
import com.sample.airtagger.utils.isLocationEnable
import com.sample.airtagger.utils.onRequestPermissionsResults
import com.sample.airtagger.utils.requestRelevantRuntimePermissions
import com.sample.airtagger.utils.toast

class MainActivity : AppCompatActivity() {

    private lateinit var activityMainBinding: ActivityMainBinding
    private lateinit var enablingBluetooth: ActivityResultLauncher<Intent>
    private var mService: BleService? = null

    private val bluetoothUtil by lazy {
        BluetoothUtil(this@MainActivity)
    }

    private val localBroadcastManager by lazy {
        LocalBroadcastManager.getInstance(this@MainActivity)
    }

    private val broadcastReceiver by lazy {
        InnerBroadcastReceiver(this@MainActivity)
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        activityMainBinding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(activityMainBinding.root)

        initViews()
        initPermissions()
        initBroadcastReceiver()
    }

    private fun initBroadcastReceiver() {
        IntentFilter().apply {
            addAction(Action.GATT_CHARACTERISTIC_CHANGED)
            localBroadcastManager.registerReceiver(broadcastReceiver, this)
        }
    }

    /**
     * Update UI
     */
    internal class InnerBroadcastReceiver(
        private val activity: MainActivity
    ) : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            val data = intent?.getStringExtra(Action.GATT_CHARACTERISTIC_CHANGED_KEY)
            Log.w(TAG, "onReceive: data=$data")

            when (data) {
                "passcode" -> {
                    val bytes = "123456".toByteArray(Charsets.UTF_8)
                    activity.mService?.writePasscode(bytes)
                }

                "successful" -> {
                    activity.toast("Auth Successful")
                }
            }
        }
    }

    private fun initPermissions() {
        enablingBluetooth = bluetoothUtil.createBluetoothEnablingResult(this, null) {
            if (!isLocationEnable()) {
                Log.e(TAG, "location is not enable.")
                enableLocation(this@MainActivity)
            }
        }
    }

    private fun initViews() {
        activityMainBinding.btnTest.setOnClickListener {
            launchScanning()
        }
    }

    override fun onResume() {
        super.onResume()

        bindService(Intent(this, BleService::class.java), object : ServiceConnection {
            override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
                Log.d(TAG, "bluetooth LE service bind successfully")
                mService = (service as BleService.LocalBinder).getService()
            }

            override fun onServiceDisconnected(name: ComponentName?) {
                Log.d(TAG, "bluetooth LE service unbind")
                mService = null
            }
        }, Context.BIND_AUTO_CREATE)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<out String>, grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        onRequestPermissionsResults(requestCode, permissions, grantResults, null) {
            Log.d(TAG, "onRequestPermissionsResult: runtime permissions is ready.")
            checkBluetoothAndLocationSwitches()
        }
    }

    private fun launchScanning() {
        if (!hasRequiredBluetoothPermissions()) {
            Log.d(TAG, "scan: request runtime permissions")
            requestRelevantRuntimePermissions()
        } else {
            checkBluetoothAndLocationSwitches()
        }
    }

    private fun checkBluetoothAndLocationSwitches() {
        if (!bluetoothUtil.isBluetoothEnable()) {
            Log.e(TAG, "enableSwitches: bluetooth is not enable.")
            bluetoothUtil.enableBluetooth(enablingBluetooth)
            return
        }

        if (!isLocationEnable()) {
            Log.e(TAG, "enableSwitches: location is not enable")
            enableLocation(this@MainActivity)
            return
        }

        startBluetoothScan()
    }

    private fun startBluetoothScan() {
        if (bluetoothUtil.isBluetoothEnable() && isLocationEnable()) {
            mService?.startBleScan()
        } else {
            Log.e(
                TAG, "startBluetoothScan: bluetooth or location switch not turn on. " +
                        "scanning progress canceled."
            )
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        localBroadcastManager.unregisterReceiver(broadcastReceiver)
    }

    companion object {
        private const val TAG = "MainActivity"
    }
}