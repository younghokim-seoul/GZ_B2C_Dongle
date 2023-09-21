package com.hoho.android.usbserial.core;

import com.hoho.android.usbserial.GolfzonLogger;
import com.hoho.android.usbserial.driver.UsbSerialPort;

public class RequestManager {

    private RequestThread requestThread;

    private ResponseManager responseManager;
    private CheckResponseThread checkResponseThread;

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

    private void addQueueReqeustPacket(String type, String packet, RequestListener listener, int retryCount, int... timeout) {
        GolfzonLogger.e(">>>>>>main = " + type);
        GolfzonLogger.e(">>>>>>sub = " + packet);

        if (packet == null) {
            listener.onResult(ResultCode.FAIL, null);
            return;
        }

        requestThread.addRequestList(new Request(type, packet, timeout[0], retryCount, listener));
        requestThread.start();
    }

    public void sendPacket(Feature feature, RequestListener listener) {
        GolfzonLogger.e("[Request Feature] : " + feature + " packet => " + feature.getReqMsg());
        addQueueReqeustPacket(feature.name(), feature.getReqMsg(), new RequestListener() {
            @Override
            public void onResult(int result, Object object) {
                GolfzonLogger.e("feature = " + feature + ", callback = " + listener + ", getRequestThread() = " + requestThread);

                if (requestThread != null) {
                    GolfzonLogger.e(">>>>>>>>>>>>>>>>>");
                    requestThread.checkRetry();

                    addQueueReqeustPacket(Feature.REQ_IS_MASTER.name(), Feature.REQ_IS_MASTER.getReqMsg(), new RequestListener() {
                        @Override
                        public void onResult(int result, Object object) {
                            GolfzonLogger.e("feature = REQ_IS_MASTER" + ", callback = " + listener + ", getRequestThread() = " + requestThread);

                            if (requestThread != null) {
                                GolfzonLogger.e(">>>>>>>>>>>>>>>>>");
                                requestThread.checkRetry();
                            }
                        }
                    }, 3, 1500);
                }
            }
        }, 3, 1500);

    }


    public void isConnected() {
        addQueueReqeustPacket(Feature.REQ_IS_CONNECTED.name(), Feature.REQ_IS_CONNECTED.getReqMsg(), new RequestListener() {
            @Override
            public void onResult(int result, Object object) {
//                GolfzonLogger.e("feature = REQ_IS_CONNECTED" +", callback = "+ listener +", getRequestThread() = " + requestThread);
//
//                if(requestThread != null){
//                    GolfzonLogger.e(">>>>>>>>>>>>>>>>>");
//                    requestThread.checkRetry();
//                }
            }
        }, 3, 500);
    }

    public void connect(String address) {
        addQueueReqeustPacket(Feature.REQ_SET_CONNECTED.name(), Feature.REQ_SET_CONNECTED.getReqMsg() + address, new RequestListener() {
            @Override
            public void onResult(int result, Object object) {
                GolfzonLogger.e("feature = REQ_SET_CONNECTED");

            }
        }, 3, 100000);
    }


}
