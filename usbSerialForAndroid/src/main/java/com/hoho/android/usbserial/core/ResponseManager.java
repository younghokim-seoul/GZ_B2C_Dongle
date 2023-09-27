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
import java.util.concurrent.TimeoutException;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class ResponseManager implements SerialInputOutputManager.Listener {

    private RequestThread requestThread;
    private RequestManager requestManager;

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
            RequestManager requestManager,
            RequestThread requestThread
    ) {
        this.requestManager = requestManager;
        this.requestThread = requestThread;
        this.checker = new RealTimeDataChecker();
        this.checker.start();

    }

    @Override
    public void onNewData(byte[] data) {
        // response....
        try {

            // 수신한 데이터를 큐에 추가

            Request request = requestManager.getRequestThread().getRequestTypeList().getFirst();

            if (request.type.equalsIgnoreCase(Feature.REQ_AT_MODE.name())) {
                return;
            }

            String receivedData = new String(data, StandardCharsets.UTF_8);
            GolfzonLogger.e(":::큐 집어넣기전 데이터.. " + receivedData);

            checker.setTimeoutMs(request.timeout);
            checker.onReceiveData(receivedData);

//            dataQueue.add(receivedData);
//            // 데이터 검사
//            Pair<String, String> result = checker.checkRealTimeData(request.timeout);
//            // 검사 결과 처리
//            handleDataCheckResult(request, result);


            // RealTimeDataChecker 클래스를 사용하여 데이터 검사
//            try {
//                dataQueue.add(receivedData);
//                // 데이터 검사
//                String result = checker.checkRealTimeData(dataQueue, request.timeout);
//                // 검사 결과 처리
//                handleDataCheckResult(request, result);
//            } catch (TimeoutException | RuntimeException e) {
//                GolfzonLogger.e(":::=> [ERROR] => " + e);
//            }


//            if(!isDtMode){
//                Request request = requestManager.getRequestThread().getRequestTypeList().getFirst();
//                packetBuffer.add(data);
//                packetCheckThread.start(request);
//            }else{
//               if(rawDataListener != null) rawDataListener.onResult(data);
//            }
//
//


//            if(isScanning){
//                scanDeviceList.add(data);
//                return;
//            }
//
//            GolfzonLogger.i("::::task... " + requestManager.getRequestThread().getRequestTypeList().getFirst().toString());
//            String receivceData = HexDump.dumpHexString(data);
//            GolfzonLogger.i("receivceData " +receivceData);
//            String[] serialData = parseSerialData(data);
//            String eventType = serialData[0];
//            Feature feature = Feature.byType(eventType);
//            switch (feature) {
//                case REQ_IS_MASTER:
//                    GolfzonLogger.i("동글 마스터 설정 확인");
//                    boolean isMaster = serialData[1].equalsIgnoreCase("+UBTLE:1");
////                    requestThread.success(null);
//
//                    GolfzonLogger.i(":::동글이 마스터인지 여부 isMaster -> " + isMaster);
//
//                    if(isMaster){
//                        GolfzonLogger.i("연결된 정보가 없으므로, 스캔 시작");
//                        requestManager.sendPacket(Feature.REQ_SCAN_DEVICE, null);
//                    }else{
//                        GolfzonLogger.i("동굴이 마스터가 아닙니다.");
//                    }
//                    break;
//                case REQ_SCAN_DEVICE:
//                    GolfzonLogger.i("주변 BLE 신호 검색후 VisionHome 검색");
////                    scanDeviceList.clear();
////                    scanDeviceList.add(data);
//                    requestThread.checkRetry();
//                    isScanning = true;
//
//                    handler.postDelayed(() -> {
////                        GolfzonLogger.i(":::::블루투스 검색 종료 " + scanDeviceList.size());
//
//                        isScanning = false;
//
//
////                        StringBuilder sb = new StringBuilder();
////                        for(int i = 0 ; i < scanDeviceList.size() ; i++){
////                            sb.append(new String(scanDeviceList.get(i)));
////                        }
////
////                        String[] visionHomeFilter = sb.toString().split("\r\n");
////
////                        List<String[]> scanResult = Arrays.stream(visionHomeFilter)
////                                .filter(s -> s.contains("VisionHome")).map(s -> s.split(",")).collect(Collectors.toList());
////
////                        GolfzonLogger.i(":::scanResult " + scanResult.size());
////
////                        if(!scanResult.isEmpty()){
////                            String[] nearVisionHome = scanResult.stream().findFirst().get();
////
////                            macAddress = nearVisionHome[0].split(":")[1];
////
////                            requestManager.connect(macAddress);
////                            GolfzonLogger.i(nearVisionHome[0] + nearVisionHome[1] + nearVisionHome[2] + nearVisionHome[3] + nearVisionHome[4]);
////                        }else{
////                            GolfzonLogger.i(":::::VisionHome 검색 안됨");
////                        }
//
//                    },10000);
//                    break;
//                case REQ_SET_CONNECTED:
//                    GolfzonLogger.i("BLE MAC 어드레스 접속 시도 ");
//                    break;
//                default:
//                    GolfzonLogger.i("::::데이티? " + new String(data));
//                    break;
//
//            }

        } catch (Exception e) {
            GolfzonLogger.e("[onNewData] error => " + e);
        }

    }

    @Override
    public void onRunError(Exception e) {
        GolfzonLogger.e("[onRunError] error => " + e);
    }


    private void handleDataCheckResult(Request request, Pair<String, String> result) throws Exception {
        if ("OK".equals(result.first)) {
            // 데이터가 정상적으로 검사되었을 때 처리
            // 예: 데이터를 화면에 표시하거나 다음 작업 수행
            GolfzonLogger.i("[handleDataCheckResult] Data Check Result: OK");

            String[] packet = result.second.split(" ");
            String mainMsg = packet[0];

            Optional<Feature> feature = Arrays.stream(Feature.values()).filter(feature1 -> feature1.getResMsg().equalsIgnoreCase(mainMsg)).findFirst();

            if (feature.isPresent()) {
                switch (feature.get()) {
                    case REQ_AT_MODE:
                        break;
                    case REQ_DT_MODE:
                        break;
                    case REQ_IS_MASTER:


                        GolfzonLogger.i("동글 마스터 설정 확인");
                        String content[] = packet[1].split(":");
                        boolean isMaster = content[1].equalsIgnoreCase("1");

                        GolfzonLogger.i(":::동글이 마스터인지 여부 isMaster -> " + isMaster);

                        if (isMaster) {
                           requestManager.setScanDevice();
                        } else {

                        }

                        break;
                    case REQ_IS_CONNECTED:
                        break;
                    case REQ_SCAN_DEVICE:
                        String[] visionHomeFilter = result.second.split("\r\n");

                        List<String[]> scanResult = Arrays.stream(visionHomeFilter).filter(s -> s.contains("VisionHome")).map(s -> s.split(",")).collect(Collectors.toList());

                        if (!scanResult.isEmpty()) {
                            String[] nearVisionHome = scanResult.stream().findFirst().get();
                            macAddress = nearVisionHome[0].split(":")[1];
                            GolfzonLogger.i("DEVICE INFO => " + nearVisionHome[0] + nearVisionHome[1] + nearVisionHome[2] + nearVisionHome[3] + nearVisionHome[4]);
                        } else {
                            GolfzonLogger.i(":::::VisionHome 검색 안됨");
                        }
                        break;

                    default:
                }
            }

        } else {
            // 타임아웃 또는 검사 실패 시 처리
            // 예: 에러 메시지 출력 또는 다른 작업 수행
            System.out.println("Data Check Result: " + result);
        }
    }
}
