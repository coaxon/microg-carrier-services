package com.google.android.ims.services;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

public class CarrierMessagingServiceStub extends Service {
    @Override
    public IBinder onBind(Intent intent) {
        // Return null to act as a silent stub. 
        // Google Messages will receive a null binder but won't crash from an unresolvable intent.
        return null;
    }
}
