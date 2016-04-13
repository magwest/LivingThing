package com.example.magnus.livingthing.sync;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;



public class AuthenticatorService extends Service {

    private Authenticator mAuthenticator;


    @Override
    public void onCreate() {
        mAuthenticator = new Authenticator(this);
    }

    /*
     * When the system binds to this Service to make the RPC call
     * return the authenticator's IBinder.
     */
    @Override
    public IBinder onBind(Intent intent) {
        return mAuthenticator.getIBinder();
    }
}
