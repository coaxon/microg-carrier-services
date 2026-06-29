package com.google.android.ims.ui;

import android.app.Activity;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.util.Base64;
import android.util.Log;
import android.widget.TextView;
import android.widget.LinearLayout;
import android.view.ViewGroup;

public class TestAkaActivity extends Activity {
    private static final String TAG = "AkaTest";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        TextView textView = new TextView(this);
        textView.setText("Testing EAP-AKA Authentication...");
        layout.addView(textView, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        setContentView(layout);

        testAkaAuth(textView);
    }

    private void testAkaAuth(TextView textView) {
        TelephonyManager tm = getSystemService(TelephonyManager.class);
        if (tm == null) {
            logAndDisplay(textView, "TelephonyManager is null");
            return;
        }

        try {
            // A dummy challenge, EAP-AKA challenge is typically a base64 encoded byte array (e.g. 16 bytes RAND + 16 bytes AUTN)
            byte[] dummyChallenge = new byte[32]; // all zeros dummy
            String base64Challenge = Base64.encodeToString(dummyChallenge, Base64.NO_WRAP);
            
            logAndDisplay(textView, "Calling getIccAuthentication...");
            
            // Note: getIccAuthentication was deprecated in API 30 in favor of getIccAuthentication with appType, authType, and data
            // We use the modern approach
            String result = tm.getIccAuthentication(
                TelephonyManager.APPTYPE_USIM,
                TelephonyManager.AUTHTYPE_EAP_AKA,
                base64Challenge
            );
            
            logAndDisplay(textView, "Result: " + (result != null ? result : "null (No Response/Error)"));
        } catch (SecurityException e) {
            logAndDisplay(textView, "SecurityException: Permission denied!\n" + e.getMessage());
        } catch (Exception e) {
            logAndDisplay(textView, "Exception: " + e.getMessage());
        }
    }

    private void logAndDisplay(TextView tv, String msg) {
        Log.i(TAG, msg);
        tv.append("\n" + msg);
    }
}
