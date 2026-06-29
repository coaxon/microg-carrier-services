package com.google.android.ims.ts43;

import android.util.Base64;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertEquals;

@RunWith(RobolectricTestRunner.class)
public class Ts43OrchestratorTest {

    @Test
    public void testTs43HandshakeFlow() {
        // Mock SIM response components
        byte[] res = new byte[16];
        byte[] ck = new byte[16];
        byte[] ik = new byte[16];
        for (int i = 0; i < 16; i++) {
            res[i] = 1;
            ck[i] = 2;
            ik[i] = 3;
        }

        // Construct mock DB tag SIM response
        byte[] simResponse = new byte[1 + 1 + 16 + 1 + 16 + 1 + 16];
        int pos = 0;
        simResponse[pos++] = (byte) 0xDB;
        simResponse[pos++] = 16;
        System.arraycopy(res, 0, simResponse, pos, 16);
        pos += 16;
        simResponse[pos++] = 16;
        System.arraycopy(ck, 0, simResponse, pos, 16);
        pos += 16;
        simResponse[pos++] = 16;
        System.arraycopy(ik, 0, simResponse, pos, 16);

        final String simResponseBase64 = Base64.encodeToString(simResponse, Base64.NO_WRAP);

        Ts43Orchestrator.IccAuthenticator mockAuthenticator = new Ts43Orchestrator.IccAuthenticator() {
            @Override
            public String getIccAuthentication(String authData) {
                return simResponseBase64;
            }
        };

        Ts43Orchestrator orchestrator = new Ts43Orchestrator(mockAuthenticator, "012345678901234@nai.epc.mnc001.mcc001.3gppnetwork.org");
        Ts43Orchestrator.EapAkaHandler handler = orchestrator.getEapAkaHandler();

        // Construct mock EAP-AKA Request (Challenge)
        // ID = 42, Type = 23 (AKA), Subtype = 1 (Challenge)
        // AT_RAND (1) len=5 (20 bytes), AT_AUTN (2) len=5 (20 bytes)
        int totalLen = 8 + 20 + 20;
        byte[] eapPacket = new byte[totalLen];
        int eapPos = 0;
        eapPacket[eapPos++] = 1; // Request
        eapPacket[eapPos++] = 42; // ID
        eapPacket[eapPos++] = (byte) (totalLen >> 8);
        eapPacket[eapPos++] = (byte) totalLen;
        eapPacket[eapPos++] = 23; // EAP-AKA
        eapPacket[eapPos++] = 1; // Challenge
        eapPacket[eapPos++] = 0;
        eapPacket[eapPos++] = 0;

        // AT_RAND
        eapPacket[eapPos++] = 1;
        eapPacket[eapPos++] = 5;
        eapPacket[eapPos++] = 0;
        eapPacket[eapPos++] = 0;
        for (int i = 0; i < 16; i++) {
            eapPacket[eapPos++] = 8;
        }

        // AT_AUTN
        eapPacket[eapPos++] = 2;
        eapPacket[eapPos++] = 5;
        eapPacket[eapPos++] = 0;
        eapPacket[eapPos++] = 0;
        for (int i = 0; i < 16; i++) {
            eapPacket[eapPos++] = 9;
        }

        String eapRelayBase64 = Base64.encodeToString(eapPacket, Base64.NO_WRAP);

        String resultBase64 = handler.handleEapChallenge(eapRelayBase64);

        assertNotNull("EAP-AKA Response should not be null", resultBase64);
        
        byte[] resultPacket = Base64.decode(resultBase64, Base64.NO_WRAP);
        assertNotNull(resultPacket);
        assertTrue(resultPacket.length > 8);
        // Code should be 2 (Response)
        assertEquals(2, resultPacket[0]);
        // ID should be 42
        assertEquals(42, resultPacket[1]);
        // Type should be 23 (EAP-AKA)
        assertEquals(23, resultPacket[4]);
        // Subtype should be 1 (Challenge)
        assertEquals(1, resultPacket[5]);
        
        System.out.println("TS.43 EAP-AKA handshake simulation successful!");
        System.out.println("Response packet base64: " + resultBase64);
    }
}
