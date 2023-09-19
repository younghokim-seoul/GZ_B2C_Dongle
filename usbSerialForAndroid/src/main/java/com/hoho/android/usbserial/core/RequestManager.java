package com.hoho.android.usbserial.core;

import com.hoho.android.usbserial.GolfzonLogger;
import com.hoho.android.usbserial.driver.UsbSerialPort;

public class RequestManager {

    private RequestThread requestThread;

    private UsbSerialPort usbSerialPort;

    public RequestManager() {

    }

    public void init() {
        if (requestThread == null) {
            requestThread = new RequestThread(this);
        }
    }

    public RequestThread getRequestThread() {
        return requestThread;
    }

    public UsbSerialPort getUsbSerialPort() {
        return usbSerialPort;
    }

    public void setUsbSerialPort(UsbSerialPort usbSerialPort) {
        this.usbSerialPort = usbSerialPort;

    }

    private void addQueueReqeustPacket(String type, String packet, RequestListener listener, int retryCount, int... timeout) {
        GolfzonLogger.e(">>>>>>main = " + type);
        GolfzonLogger.e(">>>>>>sub = " + packet);

        if(packet == null){
            listener.onResult(ResultCode.FAIL, null);
            return;
        }

        requestThread.addRequestList(new Request(type, packet, timeout[0], retryCount, listener));
        requestThread.start();
    }

    public void sendPacket(Feature feature, RequestListener listener){
        GolfzonLogger.e("[Request Feature] : "+ feature + " packet => " + feature.getReq());
        addQueueReqeustPacket(feature.name(), feature.getReq(), new RequestListener() {
            @Override
            public void onResult(int result, Object object) {
                GolfzonLogger.e("feature = "+ feature +", callback = "+ listener +", getRequestThread() = " + requestThread);

                if(requestThread != null){
                    GolfzonLogger.e(">>>>>>>>>>>>>>>>>");
                }
            }
        }, 3,feature.getTimeout());

    }




}
