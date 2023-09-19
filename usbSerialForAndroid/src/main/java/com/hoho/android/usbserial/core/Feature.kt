package com.hoho.android.usbserial.core

enum class Feature(val req: String, val res: String, val timeout: Int = 1000, val retryCnt: Int = 2) {
    REQ_AT_MODE(req = "+++", res = ""),
    REQ_IS_MASTER(req = "AT+UBTLE?\r\n", res = "AT+UBTLE?"),
    REQ_SET_MASTER(req = "AT+UBTLE=1\r\n", res = "AT+UBTLE=1"),
    REQ_RESET(req ="AT+CPWROFF\r\n", res = "AT+CPWROFF"),
    REQ_IS_CONNECTED(req = "AT+UBTLE=1\r\n", res = "AT+UBTLE=1")



}