package com.hoho.android.usbserial.core;

import android.util.Pair;

import com.hoho.android.usbserial.GolfzonLogger;
import com.hoho.android.usbserial.util.SerialInputOutputManager;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ResponseManager implements SerialInputOutputManager.Listener, RealTimeDataChecker.DataCheckerCallback {

    private final RequestThread requestThread;
    private final DongleManager dongleManager;

    private final RealTimeDataChecker checker;
    private DongleState dongleState = DongleState.DISCONNECT;

    private DongleNoti dongleNoti = DongleNoti.NONE;
    private SerialDataListener serialDataListener;

    private String macAddress = "";


    public void setSerialDataListener(SerialDataListener serialDataListener) {
        this.serialDataListener = serialDataListener;
    }

    public void setDongleState(DongleState dongleState) {
        this.dongleState = dongleState;
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

            String receivedData = new String(data);

            if (dongleState == DongleState.DATA_GATHERING) {
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
        dongleState = DongleState.DISCONNECT;
    }


    public void broadCastDongleNoti(DongleNoti noti) {
        if (serialDataListener != null) serialDataListener.onDongleState(noti);
    }

    private void handleDataCheckResult(Pair<String, String> result) throws Exception {

        GolfzonLogger.i("[handleDataCheckResult] Data Check Result: OK");

        String[] packet = result.second.split(" ");
        String responseMessage = packet[0];



//        Optional<Feature> feature = Arrays.stream(Feature.values()).filter(featureEnum -> {
//            String key = responseMessage.startsWith(Feature.REQ_SET_CONNECTED.getKey()) ? Feature.REQ_SET_CONNECTED.getKey() : responseMessage;
//            return featureEnum.getKey().equalsIgnoreCase(key);
//        }).findFirst();

        String key = responseMessage.startsWith(Feature.REQ_SET_CONNECTED.getKey()) ? Feature.REQ_SET_CONNECTED.getKey() : responseMessage;
        Feature filterFeature = Feature.byKey(key);



        if (filterFeature != null) {
            switch (filterFeature) {
                case REQ_AT_MODE:
                    GolfzonLogger.i("DATA MODE -> AT MODE 전환");
                    dongleNoti = DongleNoti.AT_MODE;
                    break;
                case REQ_DT_MODE:
                    GolfzonLogger.i("DATA 모드로 전환");
                    dongleNoti = DongleNoti.DT_MODE;
                    dongleState = DongleState.DATA_GATHERING;
                    requestThread.checkRetry();
                    break;
                case REQ_IS_CONNECTED:
                    dongleNoti = DongleNoti.BLE_CONNECT_CHECK;
                    GolfzonLogger.i("연결 상태 확인");

                    String[] data = result.second.split(" ");

                    requestThread.checkRetry();
                    if (data.length == 3) {
                        GolfzonLogger.i("수신(연결 안되있을시)");
                        dongleState = DongleState.DISCONNECT;
                        dongleManager.isMasterCheck();
                    } else {
                        GolfzonLogger.i("수신(연결 되있을시)");
                        String[] connectDevice = data[data.length - 2].split(",");
                        macAddress = connectDevice[connectDevice.length - 1];
                        dongleState = DongleState.CONNECT;
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
                    GolfzonLogger.i("스캔 시작..");
                    dongleNoti = DongleNoti.BLE_SCAN_FINISHED;
                    String[] visionHomeFilter = result.second.split(" ");

//                    List<String[]> scanResult = Arrays.stream(visionHomeFilter).filter(s -> s.contains("VisionHome")).map(s -> s.split(",")).collect(Collectors.toList());

                    List<String[]> scanResult = new ArrayList<>();

                    for(int i =0 ; i < visionHomeFilter.length ;i ++){
                        String s = visionHomeFilter[i];
                        if(s.contains("VisionHome")){
                            String[] deviceInfo = s.split(",");
                            scanResult.add(deviceInfo);
                        }
                    }

                    if (!scanResult.isEmpty()) {
                        GolfzonLogger.i("::::VisionHome 검색 결과 있음");
                        requestThread.checkRetry();
//                        String[] nearVisionHome = scanResult.stream().findFirst().get();
                        String[] nearVisionHome = scanResult.get(0);
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
                        dongleState = DongleState.CONNECT;
                        requestThread.checkRetry();
                        //ble connect success
                        GolfzonLogger.i("연결 성공");
                        dongleManager.setATtoDT();
                    } else {
                        //ble connect fail
                        GolfzonLogger.i("연결 실패");
                        dongleState = DongleState.DISCONNECT;
                        dongleNoti = DongleNoti.BLE_CONNECT_FAIL;
                    }
                    break;
                case REQ_SET_DISCONNECTED:
                    GolfzonLogger.i("스캔 종료..");
                    requestThread.success(null);
                    dongleState = DongleState.DISCONNECT;
                    dongleNoti = DongleNoti.BLE_DISCONNECTED;
                    break;
                default:
            }

            broadCastDongleNoti(dongleNoti);

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
    public void onTimeout(String packetType) {
        GolfzonLogger.e("[timeOut] =>>>>> " + packetType);
    }
}
