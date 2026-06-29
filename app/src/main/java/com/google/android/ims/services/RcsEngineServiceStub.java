package com.google.android.ims.services;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

public class RcsEngineServiceStub extends Service {
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
