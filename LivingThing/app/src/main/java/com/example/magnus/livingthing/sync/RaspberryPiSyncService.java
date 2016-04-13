package com.example.magnus.livingthing.sync;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;


public class RaspberryPiSyncService extends Service {

    private static final Object sSyncAdapterLock = new Object();
    private static RaspberryPiSyncAdapter sSyncAdapter = null;

    @Override
    public void onCreate() {
        synchronized (sSyncAdapterLock) {
            if (sSyncAdapter == null) {
                sSyncAdapter = new RaspberryPiSyncAdapter(getApplicationContext(), true);
            }
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return sSyncAdapter.getSyncAdapterBinder();
    }
}
