package com.sample.airtagger.ble

import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCharacteristic

interface ConnectionStateListener {

    fun onConnected(gatt: BluetoothGatt)

    fun onGetCharacteristics(tx: BluetoothGattCharacteristic, rx: BluetoothGattCharacteristic)

}