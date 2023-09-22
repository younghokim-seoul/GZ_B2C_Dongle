package com.hoho.android.usbserial.core;

public class Request {

    String type;
    String packet;
    int timeout;

    int maxCount;
    RequestListener requestListener;

    PacketCheckListener packetCheckListener;







    public Request(String type, String packet, int timeout, int maxCount, RequestListener requestListener,PacketCheckListener packetCheckListener) {
        this.type = type;
        this.packet = packet;
        this.timeout = timeout;
        this.maxCount = maxCount;
        this.requestListener = requestListener;
        this.packetCheckListener = packetCheckListener;
    }

    @Override
    public String toString() {
        return "Request{" +
                "type='" + type + '\'' +
                ", packet='" + packet + '\'' +
                ", timeout=" + timeout +
                ", maxCount=" + maxCount +
                ", requestListener=" + requestListener +
                ", packetCheckListener=" + packetCheckListener +
                '}';
    }
}
