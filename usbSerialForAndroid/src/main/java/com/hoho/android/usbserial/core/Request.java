package com.hoho.android.usbserial.core;

public class Request {

    String type;
    String packet;
    int timeout;

    int maxCount;

    int packetSize;
    RequestListener requestListener;









    public Request(String type, String packet, int timeout, int maxCount,int packetSize ,RequestListener requestListener) {
        this.type = type;
        this.packet = packet;
        this.timeout = timeout;
        this.maxCount = maxCount;
        this.packetSize = packetSize;
        this.requestListener = requestListener;

    }

    @Override
    public String toString() {
        return "Request{" +
                "type='" + type + '\'' +
                ", packet='" + packet + '\'' +
                ", timeout=" + timeout +
                ", maxCount=" + maxCount +
                ", packetSize=" + packetSize +
                ", requestListener=" + requestListener +
                '}';
    }
}
