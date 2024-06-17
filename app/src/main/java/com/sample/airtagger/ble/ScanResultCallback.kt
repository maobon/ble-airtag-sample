package com.sample.airtagger.ble

import android.annotation.SuppressLint
import android.bluetooth.BluetoothDevice
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.content.Context
import android.os.Build
import android.util.Log

class ScanResultCallback(
    private val mContext: Context,
    private val mGattCallback: GattCallback
) : ScanCallback() {

    override fun onScanResult(callbackType: Int, result: ScanResult) {
        super.onScanResult(callbackType, result)

        // iBeacon
        // get advertising data
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            // connectTargetDevice(result.device)

            val rssi = result.rssi
            val txPower = result.txPower
            Log.d(TAG, "onScanResult: rssi=$rssi txPower=$txPower")


            // result.scanRecord?.advertisingDataMap?.get(9).let {
            //     val localName = java.lang.String(it)
            //     Log.i(TAG, "onScanResult: completeLocalName=$localName")
            //     // if (TESLA_BLUETOOTH_BEACON_LOCAL_NAME.equals(localName)) {
            //     //     Log.d(TAG, "onScanResult: === FIND MY CAR ===")
            //     //     if (mScanning) stopBleScan()
            //     //     connectTargetDevice(result.device)
            //     // }
            // }
        }
    }

    override fun onScanFailed(errorCode: Int) {
        super.onScanFailed(errorCode)

        // todo .....
    }

    @SuppressLint("MissingPermission")
    private fun connectTargetDevice(bluetoothDevice: BluetoothDevice) {
        bluetoothDevice.connectGatt(mContext, false, mGattCallback, BluetoothDevice.TRANSPORT_LE)
    }

    companion object{
        private const val TAG = "ScanResultCallback"
    }
}