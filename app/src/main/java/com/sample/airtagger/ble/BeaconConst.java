package com.sample.airtagger.ble;

import com.sample.airtagger.utils.data.ConversionUtils;

import java.util.UUID;

public class BeaconConst {

    public static final int MANUFACTURER_ID = 76;

    // beacon manufacture data
    public static final String MANUFACTURE_DATA_UUID = "74278bda-b644-4520-8f0c-720eaf059935";
    public static final int MANUFACTURE_DATA_MAJOR = 1;
    public static final int MANUFACTURE_DATA_MINOR = 2;

    // service
    private static final String SERVICE = "00001803-494c-4f47-4943-544543480000";
    private static final String CHARACTERISTIC_WRITE = "00001805-494c-4f47-4943-544543480000";
    private static final String CHARACTERISTIC_NOTIFY = "00001804-494c-4f47-4943-544543480000";
    private static final String CLIENT_CHARACTERISTIC_DESCRIPTOR = "00002902-0000-1000-8000-00805f9b34fb";

    public static final UUID SERVICE_UUID = UUID.fromString(SERVICE);
    public static final UUID CHARACTERISTIC_WRITE_UUID = UUID.fromString(CHARACTERISTIC_WRITE);
    public static final UUID CHARACTERISTIC_NOTIFY_UUID = UUID.fromString(CHARACTERISTIC_NOTIFY);
    public static final UUID CLIENT_CHARACTERISTIC_DESCRIPTOR_UUID = UUID.fromString(CLIENT_CHARACTERISTIC_DESCRIPTOR);


    // the manufacturer data byte is the filter!
    private static final byte[] manufacturerData = new byte[]
            {
                    0, 0,

                    // uuid
                    0, 0, 0, 0,
                    0, 0,
                    0, 0,
                    0, 0, 0, 0, 0, 0, 0, 0,

                    // major
                    0, 0,

                    // minor
                    0, 0,

                    0
            };

    // the mask tells what bytes in the filter need to match, 1 if it has to match, 0 if not
    private static final byte[] manufacturerDataMask = new byte[]
            {
                    0, 0,

                    // uuid
                    1, 1, 1, 1,
                    1, 1,
                    1, 1,
                    1, 1, 1, 1, 1, 1, 1, 1,

                    // major
                    0, 0,

                    // minor
                    0, 0,

                    0
            };

    // copy UUID (with no dashes) into data array
    public static byte[] getManufactureData() {

        UUID teslaUUID = UUID.fromString(MANUFACTURE_DATA_UUID);
        System.arraycopy(ConversionUtils.UuidToByteArray(teslaUUID), 0, manufacturerData, 2, 16);

        // copy major into data array
        System.arraycopy(ConversionUtils.integerToByteArray(MANUFACTURE_DATA_MAJOR), 0, manufacturerData, 18, 2);

        // copy minor into data array
        System.arraycopy(ConversionUtils.integerToByteArray(MANUFACTURE_DATA_MINOR), 0, manufacturerData, 20, 2);

        return manufacturerData;
    }

    public static byte[] getManufactureDataMask() {
        return manufacturerDataMask;
    }

}
