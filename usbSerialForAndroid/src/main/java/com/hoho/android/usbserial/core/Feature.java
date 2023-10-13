package com.hoho.android.usbserial.core;

import java.util.HashMap;

public enum Feature {
    REQ_AT_MODE("+++","ok",0),
    REQ_DT_MODE("ATO1\r\n","ATO1",0),
    REQ_IS_MASTER("AT+UBTLE?\r\n","AT+UBTLE?",0),

    REQ_SET_MASTER( "AT+UBTLE=1\r\n", "AT+UBTLE=1",0),

    REQ_RESET("AT+CPWROFF\r\n","AT+CPWROFF",3),
    REQ_SCAN_DEVICE("AT+UBTD=4,1\r\n","AT+UBTD=4,1",0),
    REQ_SET_CONNECTED("AT+UDCP=sps://","AT+UDCP=sps://",5),
    REQ_IS_CONNECTED("AT+UDLP?\r\n","AT+UDLP?",0),
    REQ_SET_DISCONNECTED("AT+UDCPC=1\\r\\n", "AT+UDCPC=1",0);




    final String reqMsg;
    final String resMsg;

    final int contentSize;



    Feature(String reqMsg, String resMsg,int contentSize) {
        this.reqMsg = reqMsg;
        this.resMsg = resMsg;
        this.contentSize = contentSize;
    }

    public String getReqMsg() {
        return reqMsg;
    }

    public String getResMsg() {
        return resMsg;
    }


    public String getKey() {
        return resMsg;
    }

    public static Feature byKey(String key) {
        for (Feature feature : values()) {
            if (feature.getKey().equals(key)) {
                return feature;
            }
        }
        return null;
    }


}
