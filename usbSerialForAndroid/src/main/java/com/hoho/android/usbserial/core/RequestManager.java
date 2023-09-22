package com.hoho.android.usbserial.core;

import com.hoho.android.usbserial.GolfzonLogger;
import com.hoho.android.usbserial.driver.UsbSerialPort;

public class RequestManager {

    private RequestThread requestThread;

    private ResponseManager responseManager;

    private UsbSerialPort usbSerialPort;

    public RequestManager() {

    }

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

    private void addQueueReqeustPacket(String type, String packet, RequestListener requestListener, PacketCheckListener packetCheckListener, int retryCount, int... timeout) {
        GolfzonLogger.e(">>>>>>main = " + type);
        GolfzonLogger.e(">>>>>>sub = " + packet);

        if (packet == null) {
            requestListener.onResult(ResultCode.FAIL, null);
            return;
        }


        requestThread.addRequestList(new Request(type, packet, timeout[0], retryCount, requestListener, packetCheckListener));
        requestThread.start();
    }


    public void setAtMode(Feature feature) {
        GolfzonLogger.e("[Request Feature] : " + feature + " packet => " + feature.getReqMsg());
        addQueueReqeustPacket(feature.name(), feature.getReqMsg(), (result, object) -> {
            if (requestThread != null) {
                GolfzonLogger.e(">>>>>>>>>>>>>>>>>");
                requestThread.checkRetry();
                isMasterCheck();
            }
        }, null, 3, 1000);

    }


    public void isMasterCheck() {
        addQueueReqeustPacket(Feature.REQ_IS_MASTER.name(), Feature.REQ_IS_MASTER.getReqMsg(), (result, object) -> {
            if (requestThread != null) {
                GolfzonLogger.e(">>>>>>>>>>>>>>>>>");
                requestThread.checkRetry();
            }
        }, (result, object) -> {

            if(result == ResultCode.SUCCESS){

            }else{

            }

        }, 0, 1000);
    }


    public void setScanDevice() {
        addQueueReqeustPacket(Feature.REQ_SCAN_DEVICE.name(), Feature.REQ_SCAN_DEVICE.getReqMsg(), (result, object) -> {
//                GolfzonLogger.e("feature = REQ_IS_CONNECTED" +", callback = "+ listener +", getRequestThread() = " + requestThread);
//
//                if(requestThread != null){
//                    GolfzonLogger.e(">>>>>>>>>>>>>>>>>");
//                    requestThread.checkRetry();
//                }
        }, (result, object) -> {

        }, 3, 500);
    }


    public void isConnected() {
        addQueueReqeustPacket(Feature.REQ_IS_CONNECTED.name(), Feature.REQ_IS_CONNECTED.getReqMsg(), (result, object) -> {
//                GolfzonLogger.e("feature = REQ_IS_CONNECTED" +", callback = "+ listener +", getRequestThread() = " + requestThread);
//
//                if(requestThread != null){
//                    GolfzonLogger.e(">>>>>>>>>>>>>>>>>");
//                    requestThread.checkRetry();
//                }
        }, (result, object) -> {

        }, 3, 500);
    }

    public void setConnect(String address) {
        addQueueReqeustPacket(Feature.REQ_SET_CONNECTED.name(), Feature.REQ_SET_CONNECTED.getReqMsg() + address, new RequestListener() {
            @Override
            public void onResult(int result, Object object) {
                GolfzonLogger.e("feature = REQ_SET_CONNECTED");

            }
        }, (result, object) -> {

        }, 3, 100000);
    }


}
