package com.hoho.android.usbserial.core;

import com.hoho.android.usbserial.GolfzonLogger;
import com.hoho.android.usbserial.driver.UsbSerialPort;

public class DongleManager {

    private RequestThread requestThread;

    private ResponseManager responseManager;

    private UsbSerialPort usbSerialPort;



    public void init() {

        if (requestThread == null) {
            requestThread = new RequestThread(this);
        }


        if (responseManager == null) {
            responseManager = new ResponseManager(this, requestThread);
        }
    }

    public ResponseManager getResponseManager() {
        return responseManager;
    }

    public UsbSerialPort getUsbSerialPort() {
        return usbSerialPort;
    }

    public void setUsbSerialPort(UsbSerialPort usbSerialPort) {
        this.usbSerialPort = usbSerialPort;

    }

    public RequestThread getRequestThread() {
        return requestThread;
    }

    private void addQueueReqeustPacket(String type, String packet, RequestListener requestListener, int retryCount, int... timeout) {
        GolfzonLogger.e(">>>>>>main = " + type);
        GolfzonLogger.e(">>>>>>sub = " + packet);
        GolfzonLogger.e(">>>>>>timeout  = " + timeout[0]);
        GolfzonLogger.e(">>>>>>retryCount  = " + retryCount);
        if (packet == null) {
            requestListener.onResult(ResultCode.FAIL, null);
            return;
        }


        requestThread.addRequestList(new Request(type, packet, timeout[0], retryCount, requestListener));
        requestThread.start();
    }


    public void setAtMode() {

        addQueueReqeustPacket(Feature.REQ_AT_MODE.name(), Feature.REQ_AT_MODE.getReqMsg(), (result, object) -> {
            if (requestThread != null) {
                GolfzonLogger.e(">>>>>>>>>>>>>>>>>");
                requestThread.checkRetry();
                isMasterCheck();
            }
        }, 3, 1000);

    }


    public void isMasterCheck() {
        addQueueReqeustPacket(Feature.REQ_IS_MASTER.name(), Feature.REQ_IS_MASTER.getReqMsg(), (result, object) -> {

        }, 3, 1000);
    }


    public void setScanDevice() {
        addQueueReqeustPacket(Feature.REQ_SCAN_DEVICE.name(), Feature.REQ_SCAN_DEVICE.getReqMsg(), (result, object) -> {
                GolfzonLogger.e("feature = REQ_SCAN_DEVICE");

        }, 3, 10000);
    }


    public void isConnected() {
        addQueueReqeustPacket(Feature.REQ_AT_MODE.name(), Feature.REQ_AT_MODE.getReqMsg(), (result, object) -> {
            if (requestThread != null) {
                GolfzonLogger.e(">>>>>>>>>>>>>>>>>");
                requestThread.checkRetry();

                addQueueReqeustPacket(Feature.REQ_IS_CONNECTED.name(), Feature.REQ_IS_CONNECTED.getReqMsg(), new RequestListener() {
                    @Override
                    public void onResult(int result, Object object) {


                    }
                }, 1, 500);
            }
        },  3, 1000);
    }

    public void setConnect(String address) {
        addQueueReqeustPacket(Feature.REQ_SET_CONNECTED.name(), Feature.REQ_SET_CONNECTED.getReqMsg() + address, new RequestListener() {
            @Override
            public void onResult(int result, Object object) {
                GolfzonLogger.e("feature = REQ_SET_CONNECTED");

            }
        }, 3, 20000);
    }

    public void setATtoDT(){
        addQueueReqeustPacket(Feature.REQ_DT_MODE.name(), Feature.REQ_DT_MODE.getReqMsg(), new RequestListener() {
            @Override
            public void onResult(int result, Object object) {
                GolfzonLogger.e("feature = REQ_DT_MODE");


            }
        }, 3, 1000);
    }


}
