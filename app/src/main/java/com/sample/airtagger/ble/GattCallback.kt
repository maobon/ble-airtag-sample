package com.sample.airtagger.ble

import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCallback
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothProfile
import android.util.Log

@Suppress("all")
class GattCallback(
    private val mStatusListener: ConnectionStateListener
) : BluetoothGattCallback() {

    override fun onConnectionStateChange(gatt: BluetoothGatt, status: Int, newState: Int) {
        val deviceAddress = gatt.device.address

        if (status == BluetoothGatt.GATT_SUCCESS) {
            if (newState == BluetoothProfile.STATE_CONNECTED) {
                Log.w(TAG, "Successfully connected to $deviceAddress")

                // stash BluetoothGatt instance ...
                // mBluetoothGatt = gatt
                mStatusListener.onConnected(gatt)

            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                Log.w(TAG, "Successfully disconnected from $deviceAddress")

                gatt.close()
            }

        } else {
            Log.w(TAG, "Error $status encountered for $deviceAddress! Disconnecting...")
            gatt.close()
        }
    }

    override fun onServicesDiscovered(gatt: BluetoothGatt, status: Int) {
        with(gatt) {
            Log.w(TAG, "Discovered ${services.size} services for ${device.address}")

            // See implementation just above this section
            printGattTable()
            // Consider connection setup as complete here

            // xiaomi using for test
            // val xiaomiService = getService(XIAOMI_ENV_SENSOR_SERVICE_UUID)
            // val xiaomiCharacteristic = xiaomiService.run {
            //     getCharacteristic(XIAOMI_ENV_SENSOR_CHARACTERISTIC_UUID)
            // }
            // mStatusListener.onGetCharacteristics(xiaomiCharacteristic, xiaomiCharacteristic)

            // tesla
            // val teslaService = getService(TESLA_SERVICE_UUID)
            // val characteristicTx = teslaService.run {
            //     getCharacteristic(TESLA_TX_CHARACTERISTIC_UUID)
            // }
            // val characteristicRx = teslaService.run {
            //     getCharacteristic(TESLA_RX_CHARACTERISTIC_UUID)
            // }
            // mStatusListener.onGetCharacteristics(characteristicTx, characteristicRx)
        }
    }

    @Deprecated("Deprecated in Java")
    override fun onCharacteristicRead(
        gatt: BluetoothGatt, characteristic: BluetoothGattCharacteristic, status: Int
    ) {

        with(characteristic) {
            when (status) {
                BluetoothGatt.GATT_SUCCESS -> {
                    // Log.i(TAG, "Read characteristic --> $uuid:\n${value.toHexString()}")
                }

                BluetoothGatt.GATT_READ_NOT_PERMITTED -> {
                    Log.e(TAG, "Read not permitted for $uuid!")
                }

                else -> {
                    Log.e(TAG, "Characteristic read failed for $uuid, error: $status")
                }
            }
        }
    }

    override fun onCharacteristicRead(
        gatt: BluetoothGatt,
        characteristic: BluetoothGattCharacteristic,
        value: ByteArray,
        status: Int
    ) {
        val uuid = characteristic.uuid
        when (status) {
            // BluetoothGatt.GATT_SUCCESS -> {
            //     val hexString = value.toHexString()
            //     Log.i(TAG, "Read characteristic $uuid:\n $hexString")
            // }

            BluetoothGatt.GATT_READ_NOT_PERMITTED -> {
                Log.e(TAG, "Read not permitted for $uuid!")
            }

            else -> {
                Log.e(TAG, "Characteristic read failed for $uuid, error: $status")
            }
        }
    }

    @Deprecated("Deprecated in Java")
    override fun onCharacteristicChanged(
        gatt: BluetoothGatt, characteristic: BluetoothGattCharacteristic
    ) {
        with(characteristic) {
            // Log.i(TAG, "Characteristic $uuid changed | value: ${value.toHexString()}")
            processReceiveMsg(characteristic, value)
        }
    }

    override fun onCharacteristicChanged(
        gatt: BluetoothGatt, characteristic: BluetoothGattCharacteristic, value: ByteArray
    ) {
        // val newValueHex = value.toHexString()
        // with(characteristic) {
        //     Log.i(TAG, "Characteristic $uuid changed | value: $newValueHex")
        //     processReceiveMsg(characteristic, value)
        // }
    }

    private fun processReceiveMsg(characteristic: BluetoothGattCharacteristic, value: ByteArray) {
        // if (characteristic.uuid == TESLA_RX_CHARACTERISTIC_UUID) {
        //     val fromVCSECMessage: vcsec.FromVCSECMessage? = MessageUtil.autoChaCha(value)
        //     Log.d(TAG, "received content from vehicle: processReceiveMsg:$fromVCSECMessage")
        //     if (fromVCSECMessage != null)
        //         mStatusListener.onVehicleResponse(fromVCSECMessage)
        // }

        // xiaomi using for test
        // else if (characteristic.uuid == XIAOMI_ENV_SENSOR_CHARACTERISTIC_UUID) {
        //    mStatusListener.onVehicleResponse(value)
        // }
    }

    companion object {
        private const val TAG = "GattCallback"
    }
}