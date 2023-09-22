package com.hoho.android.usbserial.core;

import com.hoho.android.usbserial.GolfzonLogger;
import com.hoho.android.usbserial.driver.UsbSerialPort;

public class RequestManager {

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

    private void addQueueReqeustPacket(String type, String packet, RequestListener requestListener, PacketCheckListener packetCheckListener, int retryCount, int... timeout) {
        GolfzonLogger.e(">>>>>>main = " + type);
        GolfzonLogger.e(">>>>>>sub = " + packet);
        GolfzonLogger.e(">>>>>>timeout  = " + timeout[0]);
        GolfzonLogger.e(">>>>>>retryCount  = " + retryCount);
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

        }, (result, object) -> {
            if(result == ResultCode.SUCCESS){
                requestThread.checkRetry();
                getResponseManager().getPacketCheckThread().close();
                setScanDevice();
            }else{
                requestThread.checkRetry();
            }

        }, 3, 1000);
    }


    public void setScanDevice() {

        getResponseManager().getPacketCheckThread().close();
        addQueueReqeustPacket(Feature.REQ_SCAN_DEVICE.name(), Feature.REQ_SCAN_DEVICE.getReqMsg(), (result, object) -> {
                GolfzonLogger.e("feature = REQ_SCAN_DEVICE");

        }, (result, object) -> {
            GolfzonLogger.i("::::::setScanDevice result = >  " + result);
            if(result == ResultCode.SUCCESS){
                String sb = (String) object;
                requestThread.checkRetry();
                getResponseManager().getPacketCheckThread().close();

                setConnect("C8C7B9F67698r");

            }else{
                requestThread.checkRetry();
            }
        }, 1, 10000);
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
                }, new PacketCheckListener() {
                    @Override
                    public void onResult(int result, Object object) {
                        requestThread.checkRetry();
                        getResponseManager().getPacketCheckThread().close();
                    }
                } , 1, 500);
            }
        }, null, 3, 1000);
    }

    public void setConnect(String address) {
        addQueueReqeustPacket(Feature.REQ_SET_CONNECTED.name(), Feature.REQ_SET_CONNECTED.getReqMsg() + address, new RequestListener() {
            @Override
            public void onResult(int result, Object object) {
                GolfzonLogger.e("feature = REQ_SET_CONNECTED");

            }
        }, (result, object) -> {
            requestThread.checkRetry();
            getResponseManager().getPacketCheckThread().close();
            setATtoDT();
        }, 3, 20000);
    }

    public void setATtoDT(){
        addQueueReqeustPacket(Feature.REQ_DT_MODE.name(), Feature.REQ_DT_MODE.getReqMsg(), new RequestListener() {
            @Override
            public void onResult(int result, Object object) {
                GolfzonLogger.e("feature = REQ_DT_MODE");


            }
        }, (result, object) -> {
            requestThread.checkRetry();
            getResponseManager().getPacketCheckThread().close();
            getResponseManager().setDtMode(true);
        }, 3, 1000);
    }


}
