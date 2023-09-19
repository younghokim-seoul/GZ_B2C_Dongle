package com.hoho.android.usbserial.core;

import java.util.HashMap;

public enum Feature {
    REQ_AT_MODE("+++",""),
    REQ_IS_MASTER("AT+UBTLE?\r\n","AT+UBTLE?"),
    REQ_SET_MASTER( "AT+UBTLE=1\r\n", "AT+UBTLE=1"),
    REQ_RESET("AT+CPWROFF\r\n","AT+CPWROFF"),
    REQ_IS_CONNECTED("AT+UBTLE=1\r\n","AT+UBTLE=1");


    static private final HashMap<String,Feature> sFeatureMap;


    static {
        sFeatureMap = new HashMap<>(Feature.values().length);

        for(Feature type : Feature.values()){
            sFeatureMap.put(type.name(),type);
        }
    }


    final String reqMsg;
    final String resMsg;



    Feature(String reqMsg, String resMsg) {
        this.reqMsg = reqMsg;
        this.resMsg = resMsg;
    }

    public String getReqMsg() {
        return reqMsg;
    }

    public String getResMsg() {
        return resMsg;
    }
}
