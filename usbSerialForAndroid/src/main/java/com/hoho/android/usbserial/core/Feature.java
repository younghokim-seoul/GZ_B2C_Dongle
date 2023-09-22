package com.hoho.android.usbserial.core;

import java.util.HashMap;

public enum Feature {
    REQ_AT_MODE("+++","+++"),
    REQ_DT_MODE("ATO1\r\n","ATO1"),
    REQ_IS_MASTER("AT+UBTLE?\r\n","AT+UBTLE?"),
    REQ_SET_MASTER( "AT+UBTLE=1\r\n", "AT+UBTLE=1"),
    REQ_RESET("AT+CPWROFF\r\n","AT+CPWROFF"),
    REQ_SCAN_DEVICE("AT+UBTD=4,1\r\n","AT+UBTD=4,1"),

    REQ_SET_CONNECTED("AT+UDCP=sps://","AT+UDCP=sps://"),
    REQ_IS_CONNECTED("AT+UDLP?\r\n","AT+UDLP?");


    static private final HashMap<String,Feature> sFeatureMap;


    static {
        sFeatureMap = new HashMap<>(Feature.values().length);

        for(Feature type : Feature.values()){
            sFeatureMap.put(type.resMsg,type);
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

    public static Feature byType(String key){
        return sFeatureMap.get(key);
    }
}
