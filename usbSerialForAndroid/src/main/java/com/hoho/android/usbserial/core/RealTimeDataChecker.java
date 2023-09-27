package com.hoho.android.usbserial.core;

import android.util.Pair;

import com.hoho.android.usbserial.GolfzonLogger;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;


public class RealTimeDataChecker {

    private final ExecutorService taskExecutor; // 스레드 풀
    private final BlockingQueue<String> dataQueue;
    private final ExecutorService timerExecutor;

    private DataCheckerCallback callback;

    private int timeoutMs= -1;


    private interface DataCheckerCallback {
        void onDataCheckResult(Pair<String,String> result);
    }


    public RealTimeDataChecker() {
        this.dataQueue = new LinkedBlockingQueue<>();
        this.taskExecutor = Executors.newSingleThreadExecutor();
        this.timerExecutor = Executors.newSingleThreadExecutor();
    }

    public void setCallback(DataCheckerCallback callback) {
        this.callback = callback;
    }

    public void setTimeoutMs(int timeoutMs) {
        this.timeoutMs = timeoutMs;
    }

    public void onReceiveData(String data){
        dataQueue.add(data);
    }

    public void start(){

        timerExecutor.execute(() -> {
            while (true){
                try {
                    if(timeoutMs != -1) {
                        Pair<String, String> result = checkRealTimeData(timeoutMs);
                        GolfzonLogger.i(":::result " + result.first);
                        timeoutMs = -1;
                        notify(result);
                    }

                }catch (InterruptedException | ExecutionException e){
                    GolfzonLogger.i(":::[error] " + e);
                    notify(new Pair<>("Error",e.getMessage()));
                }
            }
        });
    }

    public void notify(Pair<String, String> result){
        if(callback != null) callback.onDataCheckResult(result);
    }

    public void stop(){
        timerExecutor.shutdownNow();
    }

    // 실시간 데이터 검사를 수행하는 메서드
    public Pair<String,String> checkRealTimeData(int timeout) throws InterruptedException, ExecutionException {

        Future<Pair<String, String>> future = taskExecutor.submit(new DataCheckerTask(dataQueue, timeout));

        return future.get();

//        try {
//            Callable<Pair<String,String>> dataCheckTask = () -> {
//
//                long startTime = System.currentTimeMillis();
//                StringBuilder dataBuilder = new StringBuilder();
//                while (true) {
//                    String data = dataQueue.poll();
//
//                    if (data != null) {
//                        GolfzonLogger.i("::::검사필요한 데이터 -> " + data);
////                        GolfzonLogger.i("before -> " + dataBuilder.toString());
//                        dataBuilder.append(data);
////                        GolfzonLogger.i("after -> " + dataBuilder.toString());
//
//                        String removeNewLine = parseSerialData(dataBuilder.toString());
//                        GolfzonLogger.i("removeNewLine = " + removeNewLine);
//                        if (removeNewLine.endsWith("OK")) {
//
//                            GolfzonLogger.e(":::::ok 데이터 " + removeNewLine);
//                            return new Pair<>("OK",removeNewLine);
//                        }
//                    }
//
//                    long currentTime = System.currentTimeMillis();
//                    if (currentTime - startTime >= timeoutMs) {
//                        dataBuilder.setLength(0);
//                        throw new TimeoutException("데이터가 시간 내에 도착하지 않았습니다.");
//                    }
//                }
//            };
//            // 데이터 검사 태스크를 스레드 풀에 제출하고 결과를 기다림
//            Future<Pair<String,String>> future = executor.submit(dataCheckTask);
//            return future.get(timeoutMs, TimeUnit.MILLISECONDS);
//        } catch (InterruptedException | ExecutionException e) {
//            // 오류가 발생하면 Exception을 throw하도록 수정
//            GolfzonLogger.e("::대체 무슨 에러예요? " + e);
//            throw new RuntimeException("오류 발생", e);
//        } finally {
////            executor.shutdown();
//        }
    }


    private static String parseSerialData(String requireAscii) {

        if (requireAscii == null || requireAscii.length() == 0) {
            return requireAscii;
        }

        String filterNewLine = requireAscii.replaceAll("(\r\n|\r|\n|\n\r)", " ");
        String filterSplace = filterNewLine.replaceAll("\\s+", " ");
        return filterSplace.replaceFirst(".$", "");

    }

    private static class DataCheckerTask implements Callable<Pair<String,String>>{

        private final BlockingQueue<String> dataQueue;
        private final int timeoutMs;


        public DataCheckerTask(BlockingQueue<String> dataQueue, int timeoutMs) {
            this.dataQueue = dataQueue;
            this.timeoutMs = timeoutMs;
        }

        @Override
        public Pair<String, String> call() throws Exception {

            long startTime = System.currentTimeMillis();
            StringBuilder dataBuilder = new StringBuilder();
            while (true) {
                String data = dataQueue.poll();

                if (data != null) {
                    dataBuilder.append(data);
                    String removeNewLine = parseSerialData(dataBuilder.toString());
//                    GolfzonLogger.i("removeNewLine = " + removeNewLine);
                    if (removeNewLine.endsWith("OK")) {

                        GolfzonLogger.e(":::::ok 데이터 " + removeNewLine);
                        return new Pair<>("OK",removeNewLine);
                    }
                }

                long currentTime = System.currentTimeMillis();
                if (currentTime - startTime >= timeoutMs) {
                    dataBuilder.setLength(0);
                    return new Pair<>("Timeout","timeout occurred");
                }
            }
        }
    }

}
