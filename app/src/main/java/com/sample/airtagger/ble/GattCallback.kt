package com.sample.airtagger.ble

import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCallback
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothProfile
import android.util.Log
import com.sample.airtagger.utils.data.BytesUtil

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
            // todo ..... core .....
            Log.w(TAG, "Error $status encountered for $deviceAddress! Disconnecting...")
            gatt.close()

            // reconnect  ....
            // if (status == 19){
            //     Log.d(TAG, "onConnectionStateChange: .... 19 ....")
            //     gatt.connect()
            // }
        }
    }

    override fun onServicesDiscovered(gatt: BluetoothGatt, status: Int) {
        with(gatt) {
            Log.w(TAG, "Discovered ${services.size} services for ${device.address}")

            // See implementation just above this section
            printGattTable()
            // Consider connection setup as complete here

            // iBeacon
            // get service
            // get characteristics
            val service = getService(BeaconConst.SERVICE_UUID)
            val writeCharacteristic = service.run {
                getCharacteristic(BeaconConst.CHARACTERISTIC_WRITE_UUID)
            }

            val notifyCharacteristic = service.run {
                getCharacteristic(BeaconConst.CHARACTERISTIC_NOTIFY_UUID)
            }

            mStatusListener.onGetCharacteristics(writeCharacteristic, notifyCharacteristic)
        }
    }

    @Deprecated("Deprecated in Java")
    override fun onCharacteristicRead(
        gatt: BluetoothGatt, characteristic: BluetoothGattCharacteristic, status: Int
    ) {
        with(characteristic) {
            when (status) {
                BluetoothGatt.GATT_SUCCESS -> {
                    Log.i(TAG, "Read characteristic --> $uuid:\n${BytesUtil.bytesToHex(value)}")
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
            BluetoothGatt.GATT_SUCCESS -> {
                val hexString = BytesUtil.bytesToHex(value)
                Log.i(TAG, "Read characteristic $uuid:\n $hexString")
            }

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
            Log.i(TAG, "Characteristic $uuid changed | value: ${BytesUtil.bytesToHex(value)}")
            processCharacteristicChanged(characteristic, value)
        }
    }

    override fun onCharacteristicChanged(
        gatt: BluetoothGatt, characteristic: BluetoothGattCharacteristic, value: ByteArray
    ) {
        val newValueHex = BytesUtil.bytesToHex(value)
        with(characteristic) {
            Log.i(TAG, "Characteristic $uuid changed | value: $newValueHex")
            processCharacteristicChanged(characteristic, value)
        }
    }

    /**
     * process with receive
     */
    private fun processCharacteristicChanged(
        characteristic: BluetoothGattCharacteristic,
        value: ByteArray
    ) {
        if (characteristic.uuid == BeaconConst.CHARACTERISTIC_NOTIFY_UUID) {
            if (value.isNotEmpty()) {
                mStatusListener.onCharacteristicChanged(value)
            }
        }
    }

    companion object {
        private const val TAG = "GattCallback"
    }
}