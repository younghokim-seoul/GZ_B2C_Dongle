package com.hoho.android.usbserial.core;

public enum DongleState {
    NONE,
    AT_MODE,
    BLE_SCAN_START,
    BLE_SCAN_FINISHED,
    BLE_CONNECTING,
    BLE_CONNECTED,
    BLE_CONNECT_FAIL,
    BLE_DISCONNECTED,
    DT_MODE
}
