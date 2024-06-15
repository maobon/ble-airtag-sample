package com.sample.airtagger.ble;

import com.sample.airtagger.utils.data.ConversionUtils;

import java.util.UUID;

public class BeaconConst {

    public static final int MANUFACTURER_ID = 76;

    public static final String MANUFACTURE_DATA_UUID = "74278bda-b644-4520-8f0c-720eaf059935";

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
        // System.arraycopy(ConversionUtils.integerToByteArray(11488), 0, manufacturerData, 18, 2);

        // copy minor into data array
        // System.arraycopy(ConversionUtils.integerToByteArray(24252), 0, manufacturerData, 20, 2);

        return manufacturerData;
    }

    public static byte[] getManufactureDataMask() {
        return manufacturerDataMask;
    }
}
