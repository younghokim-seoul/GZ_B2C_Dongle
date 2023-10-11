package com.hoho.android.usbserial.core;

import android.util.Pair;

import com.hoho.android.usbserial.GolfzonLogger;
import com.hoho.android.usbserial.util.SerialInputOutputManager;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.stream.Collectors;

public class ResponseManager implements SerialInputOutputManager.Listener, RealTimeDataChecker.DataCheckerCallback {

    private RequestThread requestThread;
    private DongleManager dongleManager;

    private List<byte[]> packetBuffer = new ArrayList<>();

    private String macAddress = "";

    private boolean isDtMode = false;

    private RawDataListener rawDataListener;

    private BlockingQueue<String> dataQueue = new LinkedBlockingQueue<>();

    private RealTimeDataChecker checker;


    public void setDtMode(boolean dtMode) {
        GolfzonLogger.i(":>>>>>>setDtMode " + dtMode);
        isDtMode = dtMode;
    }


    public void setRawDataListener(RawDataListener rawDataListener) {
        this.rawDataListener = rawDataListener;
    }

    public ResponseManager(
            DongleManager dongleManager,
            RequestThread requestThread
    ) {
        this.dongleManager = dongleManager;
        this.requestThread = requestThread;
        this.checker = new RealTimeDataChecker();
        this.checker.start();
        this.checker.setCallback(this);

    }

    @Override
    public void onNewData(byte[] data) {

        try {
            // 수신한 데이터를 큐에 추가

            String receivedData = new String(data, StandardCharsets.UTF_8);
            GolfzonLogger.e(":::큐 집어넣기전 데이터.. " + receivedData);


            if(isDtMode){

            }else{
                Request request = dongleManager.getRequestThread().getRequestTypeList().getFirst();

                if (request.type.equalsIgnoreCase(Feature.REQ_AT_MODE.name())) {
                    return;
                }
                checker.setTimeoutMs(request.timeout);
                checker.onReceiveData(receivedData);
            }
        } catch (Exception e) {
            GolfzonLogger.e("[onNewData] error => " + e);
        }

    }

    @Override
    public void onRunError(Exception e) {
        GolfzonLogger.e("[onRunError] error => " + e);
    }


    private void handleDataCheckResult(Pair<String, String> result) throws Exception {

        GolfzonLogger.i("[handleDataCheckResult] Data Check Result: OK");

        String[] packet = result.second.split(" ");
        String responseMessage = packet[0];



        Optional<Feature> feature = Arrays.stream(Feature.values()).filter(featureEnum -> {
            String key = responseMessage.startsWith(Feature.REQ_SET_CONNECTED.getKey()) ? Feature.REQ_SET_CONNECTED.getKey() : responseMessage;
            return featureEnum.getKey().equalsIgnoreCase(key);
        }).findFirst();

        if (feature.isPresent()) {
            switch (feature.get()) {
                case REQ_AT_MODE:
                    break;
                case REQ_DT_MODE:
                    GolfzonLogger.i("REQ_DT_MODE");
                    setDtMode(true);
                    break;
                case REQ_IS_MASTER:
                    GolfzonLogger.i("동글 마스터 설정 확인");
                    String[] content = packet[1].split(":");
                    boolean isMaster = content[1].equalsIgnoreCase("1");
                    GolfzonLogger.i(":::동글이 마스터인지 여부 isMaster -> " + isMaster);
                    requestThread.checkRetry();
                    if (isMaster) {
                        dongleManager.setScanDevice();
                    } else {
                        GolfzonLogger.i("동글 마스터로 재설정후 리셋필요.");
                    }
                    break;
                case REQ_SCAN_DEVICE:
                    GolfzonLogger.i("REQ_SCAN_DEVICE");

                    String[] visionHomeFilter = result.second.split(" ");

                    List<String[]> scanResult = Arrays.stream(visionHomeFilter).filter(s -> s.contains("VisionHome")).map(s -> s.split(",")).collect(Collectors.toList());

                    if (!scanResult.isEmpty()) {
                        GolfzonLogger.i("::::VisionHome 검색 결과 있음");
                        requestThread.checkRetry();
                        String[] nearVisionHome = scanResult.stream().findFirst().get();
                        macAddress = nearVisionHome[0].split(":")[1];
                        GolfzonLogger.i("DEVICE INFO => " + nearVisionHome[0] + nearVisionHome[1] + nearVisionHome[2] + nearVisionHome[3] + nearVisionHome[4]);
                        dongleManager.setConnect(macAddress);
                    } else {
                        GolfzonLogger.i(":::::VisionHome 검색 안됨 처음부터 다시 시도");
//                        dongleManager.setAtMode();
                    }
                    break;

                case REQ_SET_CONNECTED:
                    GolfzonLogger.i("REQ_SET_CONNECTED");
                    requestThread.checkRetry();

                    dongleManager.setATtoDT();
                    break;
                default:
            }
        }


    }

    @Override
    public void onDataCheckResult(Pair<String, String> result) {
        try {
            handleDataCheckResult(result);
        } catch (Exception e) {
            GolfzonLogger.e("[onDataCheckResult] => " + e);
        }

    }

    @Override
    public void onTimeout() {
        GolfzonLogger.e("[timeOut] =>>>>> ");
    }
}
