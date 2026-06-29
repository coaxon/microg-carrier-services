package com.google.android.ims.ts43;

import android.util.Base64;
import android.util.Log;

import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

public class EapAkaCrypto {
    private static final String TAG = "EapAkaCrypto";
    
    private static final int EAP_AKA_TYPE = 23;
    private static final int AT_RES = 3;
    private static final int AT_MAC = 11;

    /**
     * FIPS 186-2 Change Notice 1 PRF (Appendix 3.1).
     * Used by EAP-AKA (RFC 4187 Section 7) to derive key material from MK.
     * Produces 160 bytes: K_encr(16) + K_aut(16) + MSK(64) + EMSK(64).
     */
    public static byte[] fips186Prf(byte[] xKey) {
        final int[] H = {0x67452301, 0xEFCDAB89, 0x98BADCFE, 0x10325476, 0xC3D2E1F0};

        byte[] result = new byte[160]; // 8 iterations * 20 bytes each
        byte[] xKeyPadded = new byte[64];
        System.arraycopy(xKey, 0, xKeyPadded, 0, Math.min(xKey.length, 64));

        for (int iter = 0; iter < 8; iter++) {
            int[] w = new int[80];
            for (int i = 0; i < 16; i++) {
                w[i] = ((xKeyPadded[i * 4] & 0xFF) << 24)
                      | ((xKeyPadded[i * 4 + 1] & 0xFF) << 16)
                      | ((xKeyPadded[i * 4 + 2] & 0xFF) << 8)
                      | (xKeyPadded[i * 4 + 3] & 0xFF);
            }
            for (int i = 16; i < 80; i++) {
                w[i] = Integer.rotateLeft(w[i - 3] ^ w[i - 8] ^ w[i - 14] ^ w[i - 16], 1);
            }

            int a = H[0], b = H[1], c = H[2], d = H[3], e = H[4];
            for (int i = 0; i < 80; i++) {
                int f, k;
                if (i < 20) { f = (b & c) | (~b & d); k = 0x5A827999; }
                else if (i < 40) { f = b ^ c ^ d; k = 0x6ED9EBA1; }
                else if (i < 60) { f = (b & c) | (b & d) | (c & d); k = 0x8F1BBCDC; }
                else { f = b ^ c ^ d; k = 0xCA62C1D6; }
                int temp = Integer.rotateLeft(a, 5) + f + e + k + w[i];
                e = d; d = c; c = Integer.rotateLeft(b, 30); b = a; a = temp;
            }

            int[] out = {H[0] + a, H[1] + b, H[2] + c, H[3] + d, H[4] + e};
            for (int i = 0; i < 5; i++) {
                int off = iter * 20 + i * 4;
                result[off]     = (byte) (out[i] >> 24);
                result[off + 1] = (byte) (out[i] >> 16);
                result[off + 2] = (byte) (out[i] >> 8);
                result[off + 3] = (byte) out[i];
            }

            long carry = 1;
            int outStart = iter * 20;
            for (int i = 19; i >= 0; i--) {
                carry += (xKeyPadded[i] & 0xFFL) + (result[outStart + i] & 0xFFL);
                xKeyPadded[i] = (byte) carry;
                carry >>= 8;
            }
        }

        return result;
    }

    public static byte[] calculateMac(byte[] k_aut, byte[] packet) {
        try {
            Mac mac = Mac.getInstance("HmacSHA1");
            mac.init(new SecretKeySpec(k_aut, "HmacSHA1"));
            return mac.doFinal(packet);
        } catch (NoSuchAlgorithmException | InvalidKeyException e) {
            Log.e(TAG, "Failed to calculate MAC", e);
            return null;
        }
    }

    /**
     * Constructs the EAP-AKA AT_RES and AT_MAC response packet based on SIM response.
     */
    public static String constructEapAkaResponse(int id, SimResponseParser.SimAuthResult authResult, String identity) {
        if (authResult.res == null || authResult.ck == null || authResult.ik == null) {
            Log.e(TAG, "SIM response missing RES, CK, or IK");
            return null;
        }

        byte[] identityBytes = identity.getBytes(StandardCharsets.UTF_8);
        byte[] mkInput = new byte[identityBytes.length + authResult.ik.length + authResult.ck.length];
        int pos = 0;
        System.arraycopy(identityBytes, 0, mkInput, pos, identityBytes.length); pos += identityBytes.length;
        System.arraycopy(authResult.ik, 0, mkInput, pos, authResult.ik.length); pos += authResult.ik.length;
        System.arraycopy(authResult.ck, 0, mkInput, pos, authResult.ck.length);

        byte[] mk;
        try {
            MessageDigest sha1 = MessageDigest.getInstance("SHA-1");
            mk = sha1.digest(mkInput);
        } catch (NoSuchAlgorithmException e) {
            Log.e(TAG, "SHA-1 not supported", e);
            return null;
        }

        byte[] keyMaterial = fips186Prf(mk);
        if (keyMaterial == null) return null;
        byte[] k_aut = Arrays.copyOfRange(keyMaterial, 16, 32);

        int resLen = authResult.res.length;
        int resPad = (4 - (resLen % 4)) % 4;
        int atResLen = 4 + resLen + resPad;
        int atMacLen = 20;
        int totalLen = 8 + atResLen + atMacLen;
        
        byte[] packet = new byte[totalLen];
        pos = 0;
        
        packet[pos++] = 0x02; // Code: Response
        packet[pos++] = (byte) id;
        packet[pos++] = (byte) (totalLen >> 8);
        packet[pos++] = (byte) totalLen;
        packet[pos++] = (byte) EAP_AKA_TYPE;
        packet[pos++] = 0x01; // Subtype: Challenge
        packet[pos++] = 0x00;
        packet[pos++] = 0x00;
        
        packet[pos++] = (byte) AT_RES;
        packet[pos++] = (byte) (atResLen / 4);
        int resBits = resLen * 8;
        packet[pos++] = (byte) (resBits >> 8);
        packet[pos++] = (byte) resBits;
        System.arraycopy(authResult.res, 0, packet, pos, resLen);
        pos += resLen;
        pos += resPad;
        
        int macOffset = pos;
        packet[pos++] = (byte) AT_MAC;
        packet[pos++] = 0x05; // Length: 20 bytes (5 * 4)
        packet[pos++] = 0x00;
        packet[pos++] = 0x00;
        pos += 16;
        
        byte[] mac = calculateMac(k_aut, packet);
        if (mac == null) return null;
        
        System.arraycopy(mac, 0, packet, macOffset + 4, 16);
        
        return Base64.encodeToString(packet, Base64.NO_WRAP);
    }

    public static String constructSyncFailure(int id, byte[] auts) {
        int atAutsLen = 16;
        int totalLen = 8 + atAutsLen;
        
        byte[] packet = new byte[totalLen];
        int pos = 0;
        
        packet[pos++] = 0x02;
        packet[pos++] = (byte) id;
        packet[pos++] = (byte) (totalLen >> 8);
        packet[pos++] = (byte) totalLen;
        packet[pos++] = (byte) EAP_AKA_TYPE;
        packet[pos++] = 0x04; // Subtype: Sync-Failure
        packet[pos++] = 0x00;
        packet[pos++] = 0x00;
        
        packet[pos++] = 0x04; // AT_AUTS
        packet[pos++] = 0x04; // Length: 16 bytes
        System.arraycopy(auts, 0, packet, pos, auts.length);
        
        return Base64.encodeToString(packet, Base64.NO_WRAP);
    }
}
