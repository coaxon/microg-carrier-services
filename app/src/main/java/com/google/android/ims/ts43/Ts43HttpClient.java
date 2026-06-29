package com.google.android.ims.ts43;

import android.util.Log;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Ts43HttpClient {
    private static final String TAG = "Ts43HttpClient";
    
    private static final String EAP_RELAY_ACCEPT = "application/vnd.gsma.eap-relay.v1.0+json";
    private static final int TS43_TIMEOUT_MS = 15000;

    /**
     * Phase 1: EAP-AKA authentication via JSON body relay.
     */
    public static String performEapAkaAuth(String entitlementUrl, String eapId, Ts43Orchestrator.EapAkaHandler handler) throws IOException {
        String separator = entitlementUrl.contains("?") ? "&" : "?";
        String initialUrl = entitlementUrl + separator + "EAP_ID=" + java.net.URLEncoder.encode(eapId, "UTF-8");

        Map<String, String> cookies = new LinkedHashMap<>();

        HttpURLConnection conn = (HttpURLConnection) new URL(initialUrl).openConnection();
        conn.setConnectTimeout(TS43_TIMEOUT_MS);
        conn.setReadTimeout(TS43_TIMEOUT_MS);
        conn.setRequestMethod("GET");
        conn.setRequestProperty("Accept", EAP_RELAY_ACCEPT);

        int postsRemaining = 3;
        while (true) {
            applyCookies(conn, cookies);
            int responseCode = conn.getResponseCode();
            collectCookies(conn, cookies);

            if (responseCode != 200) {
                Log.w(TAG, "EAP: unexpected HTTP " + responseCode);
                conn.disconnect();
                return null;
            }

            String body = readStream(conn.getInputStream());
            conn.disconnect();

            String token = extractToken(body);
            if (token != null) {
                Log.i(TAG, "EAP: auth token received");
                return token;
            }

            if (postsRemaining <= 0) {
                Log.w(TAG, "EAP-AKA auth: exceeded max rounds");
                return null;
            }

            String eapRelayPacket = extractEapRelayPacket(body);
            if (eapRelayPacket == null) return null;

            // Delegate SIM logic to orchestrator (abstracted away)
            String eapResponse = handler.handleEapChallenge(eapRelayPacket);
            if (eapResponse == null) return null;

            conn = (HttpURLConnection) new URL(entitlementUrl).openConnection();
            conn.setConnectTimeout(TS43_TIMEOUT_MS);
            conn.setReadTimeout(TS43_TIMEOUT_MS);
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Accept", EAP_RELAY_ACCEPT + ", text/vnd.wap.connectivity-xml");
            conn.setRequestProperty("Content-Type", EAP_RELAY_ACCEPT);
            conn.setDoOutput(true);
            
            String postBody = "{\"eap-relay-packet\":\"" + eapResponse + "\"}";
            conn.getOutputStream().write(postBody.getBytes(StandardCharsets.UTF_8));
            postsRemaining--;
        }
    }
    
    /**
     * Phase 2: ODSA request with auth token. Using simple Maps for parameters instead of Protobufs.
     */
    public static String performOdsaRequest(String entitlementUrl, String authToken, Map<String, String> params) {
        try {
            StringBuilder urlBuilder = new StringBuilder(entitlementUrl);
            urlBuilder.append(entitlementUrl.contains("?") ? "&" : "?");
            urlBuilder.append("token=").append(java.net.URLEncoder.encode(authToken, "UTF-8"));
            
            if (params != null) {
                for (Map.Entry<String, String> param : params.entrySet()) {
                    urlBuilder.append("&").append(param.getKey()).append("=")
                              .append(java.net.URLEncoder.encode(param.getValue(), "UTF-8"));
                }
            }

            HttpURLConnection conn = (HttpURLConnection) new URL(urlBuilder.toString()).openConnection();
            conn.setConnectTimeout(TS43_TIMEOUT_MS);
            conn.setReadTimeout(TS43_TIMEOUT_MS);
            conn.setRequestMethod("GET");
            conn.setRequestProperty("Accept", "application/json");
            
            if (conn.getResponseCode() == 200) {
                String body = readStream(conn.getInputStream());
                conn.disconnect();
                return body;
            }
            conn.disconnect();
        } catch (Exception e) {
            Log.e(TAG, "ODSA error", e);
        }
        return null;
    }

    private static void applyCookies(HttpURLConnection conn, Map<String, String> cookies) {
        if (!cookies.isEmpty()) {
            StringBuilder cookieHeader = new StringBuilder();
            for (Map.Entry<String, String> entry : cookies.entrySet()) {
                if (cookieHeader.length() > 0) cookieHeader.append("; ");
                cookieHeader.append(entry.getKey()).append("=").append(entry.getValue());
            }
            conn.setRequestProperty("Cookie", cookieHeader.toString());
        }
    }

    private static void collectCookies(HttpURLConnection conn, Map<String, String> cookies) {
        List<String> setCookies = conn.getHeaderFields().get("Set-Cookie");
        if (setCookies != null) {
            for (String sc : setCookies) {
                String[] parts = sc.split(";")[0].split("=", 2);
                if (parts.length == 2) cookies.put(parts[0].trim(), parts[1].trim());
            }
        }
    }

    private static String extractEapRelayPacket(String body) {
        try {
            JSONObject json = new JSONObject(body);
            String packet = json.optString("eap-relay-packet", null);
            if (packet != null && !packet.isEmpty()) return packet;
        } catch (Exception ignored) {}
        return null;
    }

    private static String extractToken(String responseBody) {
        try {
            JSONObject json = new JSONObject(responseBody);
            JSONObject tokenObj = json.optJSONObject("Token");
            if (tokenObj != null) {
                String token = tokenObj.optString("token", null);
                if (token != null && !token.isEmpty()) return token;
            }
        } catch (Exception ignored) {}

        try {
            Matcher m = Pattern.compile("<parm[^>]+name=\"token\"[^>]+value=\"([^\"]+)\"").matcher(responseBody);
            if (m.find()) return m.group(1);
        } catch (Exception ignored) {}

        return null;
    }

    private static String readStream(InputStream in) throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8));
        StringBuilder sb = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            sb.append(line);
        }
        return sb.toString();
    }
}
