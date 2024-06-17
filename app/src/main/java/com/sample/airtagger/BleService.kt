package com.sample.airtagger

import android.Manifest
import android.annotation.SuppressLint
import android.app.Service
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.le.ScanFilter
import android.bluetooth.le.ScanSettings
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Binder
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.util.Log
import androidx.core.app.ActivityCompat
import com.sample.airtagger.ble.BeaconConst
import com.sample.airtagger.ble.BluetoothUtil
import com.sample.airtagger.ble.ConnectionStateListener
import com.sample.airtagger.ble.GattCallback
import com.sample.airtagger.ble.GattOperation
import com.sample.airtagger.ble.ScanResultCallback

class BleService : Service() {

    private var mScanning = false
    private var mGattOperation: GattOperation? = null

    private val mBluetoothUtil by lazy {
        BluetoothUtil(this@BleService)
    }

    private val mLocalBinder by lazy {
        LocalBinder()
    }

    open inner class LocalBinder : Binder() {
        fun getService(): BleService {
            return this@BleService
        }
    }

    override fun onBind(intent: Intent): IBinder {
        return mLocalBinder
    }

    // step1 ....................
    // scan and connect .... every time ....
    @SuppressLint("MissingPermission")
    fun startBleScan() {
        Log.i(TAG, "startBleScan: ")

        if (mScanning) {
            stopBleScan()
        } else {
            mScanning = true

            val filters = mutableListOf<ScanFilter>().apply {
                add(
                    ScanFilter.Builder().setManufacturerData(
                        BeaconConst.MANUFACTURER_ID,
                        BeaconConst.getManufactureData(),
                        BeaconConst.getManufactureDataMask()
                    ).build()
                )
            }
            mBluetoothUtil.mLeScanner.startScan(filters, getScanSettings(), mScanResultCallback)

            // stop scan ...
            Handler(Looper.getMainLooper()).postDelayed({
                stopBleScan()
            }, 10 * 1000L)
        }
    }

    @SuppressLint("MissingPermission")
    private fun stopBleScan() {
        if (mScanning) {
            Log.d(TAG, "stopBleScan: stop scan")
            mScanning = false
            mBluetoothUtil.mLeScanner.stopScan(mScanResultCallback)
        }
    }

    private fun getScanSettings(): ScanSettings {
        return ScanSettings.Builder().setScanMode(ScanSettings.SCAN_MODE_LOW_POWER)
            .setReportDelay(0).build()
    }

    private val mScanResultCallback by lazy {
        ScanResultCallback(this@BleService, mGattCallback)
    }

    // bluetooth gatt callback result: onConnectionStateChange ...
    private val mGattCallback by lazy {

        // custom interface function
        GattCallback(object : ConnectionStateListener {
            override fun onConnected(gatt: BluetoothGatt) {
                Log.d(TAG, "onConnected: iBeacon connected successfully")
                val operation = GattOperation(gatt)
                operation.discoveryServices()
                this@BleService.mGattOperation = operation
            }

            override fun onGetCharacteristics(
                write: BluetoothGattCharacteristic,
                notify: BluetoothGattCharacteristic
            ) {
                Log.d(TAG, "onGetCharacteristics: get write and notify characteristics")

                this@BleService.mGattOperation?.enableNotifications(
                    notify,
                    BeaconConst.CLIENT_CHARACTERISTIC_DESCRIPTOR_UUID
                )
            }
        })
    }

    companion object {
        private const val TAG = "BleService"
    }

}