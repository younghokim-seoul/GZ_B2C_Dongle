package com.hoho.android.usbserial.core;

import com.hoho.android.usbserial.GolfzonLogger;
import com.hoho.android.usbserial.util.HexDump;
import com.hoho.android.usbserial.util.SerialInputOutputManager;

public class ResponseManager implements SerialInputOutputManager.Listener {

    private RequestThread requestThread;

    public ResponseManager(RequestThread requestThread) {
        this.requestThread = requestThread;
    }

    @Override
    public void onNewData(byte[] data) {
        // response....
        try {
            String receivceData = HexDump.dumpHexString(data);
            GolfzonLogger.i(":::receivceData>>>>>> " + receivceData);
        } catch (Exception e) {
            GolfzonLogger.e("[onNewData] error => " + e);
        }

    }

    @Override
    public void onRunError(Exception e) {
        GolfzonLogger.e("[onRunError] error => " + e);
    }
}
