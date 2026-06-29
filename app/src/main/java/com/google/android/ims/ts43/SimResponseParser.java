package com.google.android.ims.ts43;

import android.util.Base64;
import android.util.Log;

import java.util.Arrays;

public class SimResponseParser {
    private static final String TAG = "SimResponseParser";

    public static class SimAuthResult {
        public byte[] res;
        public byte[] ck;
        public byte[] ik;
        public byte[] auts;
    }

    /**
     * Parse SIM EAP-AKA authentication response (3GPP TS 31.102 Section 7.1.2).
     *
     * Success format: 0xDB [len_RES] [RES] [len_CK] [CK] [len_IK] [IK]
     * Sync failure:   0xDC [len_AUTS] [AUTS]
     */
    public static SimAuthResult parse(String responseBase64) {
        if (responseBase64 == null) return null;
        byte[] data = Base64.decode(responseBase64, Base64.NO_WRAP);
        if (data == null || data.length < 2) return null;

        SimAuthResult result = new SimAuthResult();
        int tag = data[0] & 0xFF;
        int offset = 1;

        if (tag == 0xDB) {
            // Success: sequential LV for RES, CK, IK
            result.res = extractLv(data, offset);
            if (result.res == null) {
                Log.w(TAG, "Failed to extract RES from SIM response");
                return null;
            }
            offset += 1 + result.res.length;

            result.ck = extractLv(data, offset);
            if (result.ck == null) {
                Log.w(TAG, "Failed to extract CK from SIM response");
                return null;
            }
            offset += 1 + result.ck.length;

            result.ik = extractLv(data, offset);
            if (result.ik == null) {
                Log.w(TAG, "Failed to extract IK from SIM response");
                return null;
            }

            Log.d(TAG, "SIM auth success: RES=" + result.res.length + "B, CK=" + result.ck.length + "B, IK=" + result.ik.length + "B");
        } else if (tag == 0xDC) {
            // Sync failure: LV for AUTS
            result.auts = extractLv(data, offset);
            if (result.auts == null) {
                Log.w(TAG, "Failed to extract AUTS from SIM response");
                return null;
            }
            Log.d(TAG, "SIM auth sync failure: AUTS=" + result.auts.length + "B");
        } else {
            Log.w(TAG, "Unknown SIM response tag: 0x" + Integer.toHexString(tag));
            return null;
        }

        return result;
    }

    private static byte[] extractLv(byte[] data, int offset) {
        if (offset >= data.length) return null;
        int len = data[offset] & 0xFF;
        int valueStart = offset + 1;
        int valueEnd = valueStart + len;
        if (valueEnd > data.length) return null;
        return Arrays.copyOfRange(data, valueStart, valueEnd);
    }
}
