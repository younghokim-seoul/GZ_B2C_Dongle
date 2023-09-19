package com.hoho.android.usbserial.core;

public class Request {

    String type;
    String packet;
    int timeout;

    int maxCount;
    RequestListener listener;


    public Request(String type, String packet, int timeout, int maxCount, RequestListener listener) {
        this.type = type;
        this.packet = packet;
        this.timeout = timeout;
        this.maxCount = maxCount;
        this.listener = listener;
    }
}