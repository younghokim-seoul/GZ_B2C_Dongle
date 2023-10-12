package com.hoho.android.usbserial.core;

import com.hoho.android.usbserial.GolfzonLogger;
import com.hoho.android.usbserial.driver.UsbSerialPort;

public class DongleManager {

    private RequestThread requestThread;

    private ResponseManager responseManager;

    private UsbSerialPort usbSerialPort;


    public void init() {

        GolfzonLogger.i(":::requestThread " + requestThread + " responseManager " + responseManager);


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

    private void addQueueReqeustPacket(Feature feature, RequestListener requestListener, int retryCount, int... timeout) {
        GolfzonLogger.e(">>>>>>main = " + feature.name());
        GolfzonLogger.e(">>>>>>sub = " + feature.getReqMsg());
        GolfzonLogger.e(">>>>>>timeout  = " + timeout[0]);
        GolfzonLogger.e(">>>>>>retryCount  = " + retryCount);
        if (feature.getReqMsg() == null) {
            requestListener.onResult(ResultCode.FAIL, null);
            return;
        }
        requestThread.addRequestList(new Request(feature.name(), feature.getReqMsg(), timeout[0], retryCount, feature.contentSize, requestListener));
        requestThread.start();
    }

    private void addQueueReqeustPacket(Feature feature, String address, RequestListener requestListener, int retryCount, int... timeout) {
        GolfzonLogger.e(">>>>>>main = " + feature.name());
        GolfzonLogger.e(">>>>>>sub = " + feature.getReqMsg());
        GolfzonLogger.e(">>>>>>timeout  = " + timeout[0]);
        GolfzonLogger.e(">>>>>>retryCount  = " + retryCount);
        if (feature.getReqMsg() == null) {
            requestListener.onResult(ResultCode.FAIL, null);
            return;
        }

        requestThread.addRequestList(new Request(feature.name(), feature.getReqMsg() + address, timeout[0], retryCount, feature.contentSize, requestListener));
        requestThread.start();
    }


    public void setAtMode() {
        responseManager.broadCastDongleState(DongleNoti.AT_MODE);
        addQueueReqeustPacket(Feature.REQ_AT_MODE, (result, object) -> {
            GolfzonLogger.e("REQ_AT_MODE >>>>>> " + result);
            if (requestThread != null) {
                GolfzonLogger.e(">>>>>>>>>>>>>>>>>");
                requestThread.checkRetry();
                isConnected();
            }
        }, 3, 3000);

    }


    public void isMasterCheck() {
        addQueueReqeustPacket(Feature.REQ_IS_MASTER, (result, object) -> {

        }, 3, 1000);
    }


    public void setScanDevice() {
        responseManager.broadCastDongleState(DongleNoti.BLE_SCAN_START);
        addQueueReqeustPacket(Feature.REQ_SCAN_DEVICE, (result, object) -> {
            GolfzonLogger.e("feature = REQ_SCAN_DEVICE");

        }, 3, 10000);
    }


    public void isConnected() {
        addQueueReqeustPacket(Feature.REQ_IS_CONNECTED, new RequestListener() {
            @Override
            public void onResult(int result, Object object) {
                GolfzonLogger.e("feature = REQ_IS_CONNECTED");
                if(result == ResultCode.SUCCESS){
                    if (requestThread != null) {
                        GolfzonLogger.e(">>>>>>>>>>>>>>>>>");
                        requestThread.checkRetry();
                        isMasterCheck();
                    }
                }

            }
        }, 3, 500);
    }

    public void setConnect(String address) {
        responseManager.broadCastDongleState(DongleNoti.BLE_CONNECTING);
        addQueueReqeustPacket(Feature.REQ_SET_CONNECTED, address + "\r\n", new RequestListener() {
            @Override
            public void onResult(int result, Object object) {
                GolfzonLogger.e("feature = REQ_SET_CONNECTED");
                if (result == ResultCode.REQUEST_TIMEOUT_ERROR) {
                    setScanDevice();
                } else {
                    requestThread.checkRetry();
                }

            }
        }, 3, 5000);
    }

    public void setATtoDT() {
        addQueueReqeustPacket(Feature.REQ_DT_MODE, new RequestListener() {
            @Override
            public void onResult(int result, Object object) {
                GolfzonLogger.e("feature = REQ_DT_MODE");

            }
        }, 3, 1000);
    }


}
