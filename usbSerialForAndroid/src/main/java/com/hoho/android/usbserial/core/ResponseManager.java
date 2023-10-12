package com.hoho.android.usbserial.core;

import android.util.Pair;

import com.hoho.android.usbserial.GolfzonLogger;
import com.hoho.android.usbserial.util.SerialInputOutputManager;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class ResponseManager implements SerialInputOutputManager.Listener, RealTimeDataChecker.DataCheckerCallback {

    private final RequestThread requestThread;
    private final DongleManager dongleManager;

    private final RealTimeDataChecker checker;
    private DongleState dongleState = DongleState.Disconnect;

    private DongleNoti dongleNoti = DongleNoti.NONE;
    private SerialDataListener serialDataListener;


    private String macAddress = "";


    public void stop() {
        //data check stop
        checker.stop();
    }

    public void setSerialDataListener(SerialDataListener serialDataListener) {
        this.serialDataListener = serialDataListener;
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

//            GolfzonLogger.e(":::dongleState " + dongleState);
//            GolfzonLogger.e(":::receivedData " + receivedData);

            if (dongleState == DongleState.DataGathering) {
                //data mode
                if (serialDataListener != null) serialDataListener.onResult(data);
            } else {
                //connect mode;
                if (dongleManager.getRequestThread().getRequestTypeList().size() > 0) {

                    Request request = dongleManager.getRequestThread().getRequestTypeList().getFirst();
                    checker.setCurrentRequest(request);
                    checker.setTimeoutMs(request.timeout);
                    checker.onReceiveData(receivedData);
                }
            }
        } catch (Exception e) {
            GolfzonLogger.e("[onNewData] error => " + e);
        }

    }

    @Override
    public void onRunError(Exception e) {
        GolfzonLogger.e("[onRunError] error => " + e);
    }


    public void broadCastDongleState(DongleNoti state) {
        if (serialDataListener != null) serialDataListener.onDongleState(state);
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
                    GolfzonLogger.i("DATA MODE -> AT MODE 전환");
                    dongleNoti = DongleNoti.AT_MODE;
                    break;
                case REQ_DT_MODE:
                    GolfzonLogger.i("DATA 모드로 전환");
                    dongleNoti = DongleNoti.DT_MODE;
                    dongleState = DongleState.DataGathering;
                    requestThread.checkRetry();
                    break;
                case REQ_IS_CONNECTED:
                    dongleNoti = DongleNoti.BLE_CONNECT_CHECK;
                    GolfzonLogger.i("연결 상태 확인");

                    String[] data = result.second.split(" ");

                    requestThread.checkRetry();
                    if (data.length == 3) {
                        GolfzonLogger.i("수신(연결 안되있을시)");
                        dongleState = DongleState.Disconnect;
                        dongleManager.isMasterCheck();
                    } else {
                        GolfzonLogger.i("수신(연결 되있을시)");
                        String[] connectDevice = data[data.length - 2].split(",");
                        macAddress = connectDevice[connectDevice.length-1];
                        dongleState = DongleState.Connect;
                        dongleManager.setATtoDT();
                    }

                    break;
                case REQ_IS_MASTER:
                    dongleNoti = DongleNoti.MASTER_CHECK;
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
                    dongleNoti = DongleNoti.BLE_SCAN_FINISHED;
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
                    GolfzonLogger.i("BLE MAC 어드레스로 접속 시도 " + macAddress);

                    if (result.second.contains(macAddress)) {
                        dongleNoti = DongleNoti.BLE_CONNECTED;
                        dongleState = DongleState.Connect;
                        requestThread.checkRetry();
                        //ble connect success
                        GolfzonLogger.i("연결 성공");
                        dongleManager.setATtoDT();
                    } else {
                        //ble connect fail
                        GolfzonLogger.i("연결 실패");
                        dongleState = DongleState.Disconnect;
                        dongleNoti = DongleNoti.BLE_CONNECT_FAIL;
                    }
                    break;
                default:
            }

            broadCastDongleState(dongleNoti);

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
