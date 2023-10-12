package com.hoho.android.usbserial.examples;

import android.app.Application;

import com.hoho.android.usbserial.core.DongleManager;
import com.hoho.android.usbserial.core.DongleState;

import co.golfzon.visionHome.core.HGS_ClientManager;
import co.golfzon.visionHome.core.interfaces.HGS_Client;

public class App extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        ServiceLocator.register(HGS_Client.class, HGS_ClientManager.getInstance().create(this));
        ServiceLocator.register(DongleState.class,new DongleManager());
    }
}
