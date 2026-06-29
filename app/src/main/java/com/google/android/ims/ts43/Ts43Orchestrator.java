package com.google.android.ims.ts43;

import android.util.Base64;
import android.util.Log;

public class Ts43Orchestrator {
    private static final String TAG = "Ts43Orchestrator";
    
    private static final int EAP_AKA_TYPE = 23;
    private static final int EAP_AKA_SUBTYPE_CHALLENGE = 1;
    private static final int AT_RAND = 1;
    private static final int AT_AUTN = 2;

    public interface IccAuthenticator {
        /**
         * Simulates TelephonyManager.getIccAuthentication(APPTYPE_USIM, AUTHTYPE_EAP_AKA, authData)
         * @param authData Base64 encoded byte array representing the EAP challenge.
         * @return Base64 encoded SIM response (length-value encoded RES, CK, IK or AUTS).
         */
        String getIccAuthentication(String authData);
    }

    public interface EapAkaHandler {
        String handleEapChallenge(String eapRelayBase64);
    }

    private final IccAuthenticator authenticator;
    private final String identity; // The NAI, e.g., 0<IMSI>@nai.epc.mnc<MNC>.mcc<MCC>.3gppnetwork.org

    public Ts43Orchestrator(IccAuthenticator authenticator, String identity) {
        this.authenticator = authenticator;
        this.identity = identity;
    }

    public EapAkaHandler getEapAkaHandler() {
        return new EapAkaHandler() {
            @Override
            public String handleEapChallenge(String eapRelayBase64) {
                return processEapPacket(eapRelayBase64);
            }
        };
    }

    private String processEapPacket(String eapRelayBase64) {
        byte[] eapPayload = Base64.decode(eapRelayBase64, Base64.NO_WRAP);
        if (eapPayload == null || eapPayload.length < 8) {
            Log.e(TAG, "Invalid EAP packet: too short");
            return null;
        }

        if (eapPayload[0] != 1 || eapPayload[4] != EAP_AKA_TYPE) {
            Log.e(TAG, "Not an EAP-AKA Request");
            return null;
        }

        int id = eapPayload[1] & 0xFF;
        int subtype = eapPayload[5] & 0xFF;
        if (subtype != EAP_AKA_SUBTYPE_CHALLENGE) {
            Log.e(TAG, "Unexpected EAP-AKA subtype: " + subtype);
            return null;
        }

        byte[] rand = null, autn = null;
        int offset = 8;
        while (offset + 1 < eapPayload.length) {
            int attrType = eapPayload[offset] & 0xFF;
            int attrLen = (eapPayload[offset + 1] & 0xFF) * 4;
            if (attrLen < 4 || offset + attrLen > eapPayload.length) break;

            if (attrType == AT_RAND) {
                rand = new byte[16];
                System.arraycopy(eapPayload, offset + 4, rand, 0, 16);
            } else if (attrType == AT_AUTN) {
                autn = new byte[16];
                System.arraycopy(eapPayload, offset + 4, autn, 0, 16);
            }
            offset += attrLen;
        }

        if (rand == null || autn == null) {
            Log.e(TAG, "Missing RAND or AUTN in EAP-AKA challenge");
            return null;
        }

        byte[] authData = new byte[1 + rand.length + 1 + autn.length];
        authData[0] = (byte) rand.length;
        System.arraycopy(rand, 0, authData, 1, rand.length);
        authData[1 + rand.length] = (byte) autn.length;
        System.arraycopy(autn, 0, authData, 1 + rand.length + 1, autn.length);
        
        String authDataStr = Base64.encodeToString(authData, Base64.NO_WRAP);
        
        // This is the hardware boundary call, abstracted via the interface!
        String simResponseBase64 = authenticator.getIccAuthentication(authDataStr);
        if (simResponseBase64 == null) {
            Log.e(TAG, "IccAuthenticator returned null");
            return null;
        }

        SimResponseParser.SimAuthResult authResult = SimResponseParser.parse(simResponseBase64);
        if (authResult == null) return null;

        if (authResult.auts != null) {
            return EapAkaCrypto.constructSyncFailure(id, authResult.auts);
        }

        return EapAkaCrypto.constructEapAkaResponse(id, authResult, identity);
    }
}
