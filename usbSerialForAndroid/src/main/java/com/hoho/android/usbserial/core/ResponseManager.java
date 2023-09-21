package com.hoho.android.usbserial.core;

import android.os.Handler;
import android.os.Looper;

import com.hoho.android.usbserial.GolfzonLogger;
import com.hoho.android.usbserial.util.HexDump;
import com.hoho.android.usbserial.util.SerialInputOutputManager;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class ResponseManager implements SerialInputOutputManager.Listener {

    private RequestThread requestThread;
    private RequestManager requestManager;

    private CheckResponseThread checkResponseThread;

    private List<byte[]> packetBuffer = new ArrayList<>();

    private boolean isScanning = false;
    private long scanTime = 5000;

    private Handler handler = new Handler(Looper.getMainLooper());

    private String macAddress = "";





    public ResponseManager(
            RequestManager requestManager,
            RequestThread requestThread
    ) {
        this.requestManager = requestManager;
        this.requestThread = requestThread;
        this.checkResponseThread = new CheckResponseThread(this);
    }

    @Override
    public void onNewData(byte[] data) {
        // response....
        try {
            GolfzonLogger.i("::::task... " + requestManager.getRequestThread().getRequestTypeList().getFirst().toString());
            String receivceData = HexDump.dumpHexString(data);
            GolfzonLogger.i("receivceData " +new String(receivceData));
            packetBuffer.add(data);
            checkResponseThread.start();

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

    private String[] parseSerialData(byte[] data) throws Exception {
        String convertToAscii = new String(data);
        GolfzonLogger.i("::::ascii " + convertToAscii);
        String filterNewLine = convertToAscii.replaceAll("(\r\n|\r|\n|\n\r)", " ");
        GolfzonLogger.i("filterNewLine => " + filterNewLine);
        return removeLastChar(filterNewLine.replaceAll("\\s+", "-")).split("-");

    }

    private String removeLastChar(String str) {
        if (str == null) {
            return null;
        }
        return str.replaceFirst(".$", "");
    }

    public List<byte[]> getPacketBuffer() {
        return packetBuffer;
    }
}
