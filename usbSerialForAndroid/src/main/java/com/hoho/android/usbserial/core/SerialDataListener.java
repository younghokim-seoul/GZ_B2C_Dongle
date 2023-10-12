package com.hoho.android.usbserial.core;

public interface SerialDataListener {
    void onResult(byte[] raw);

    void onDongleState(DongleNoti state);
}
