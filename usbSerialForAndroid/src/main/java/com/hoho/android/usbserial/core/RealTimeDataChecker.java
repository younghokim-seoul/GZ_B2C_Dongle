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

    private final ExecutorService taskExecutor;
    private final BlockingQueue<String> dataQueue;
    private final ExecutorService timerExecutor;

    private DataCheckerCallback callback;

    private int timeoutMs = -1;

    private Request currentRequest;


    public interface DataCheckerCallback {
        void onDataCheckResult(Pair<String, String> result);

        void onTimeout(String type);
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
    public void setCurrentRequest(Request currentRequest) {
        this.currentRequest = currentRequest;
    }

    public void onReceiveData(String data) {
        dataQueue.add(data);
    }

    public void start() {

        timerExecutor.execute(() -> {
            while (true) {
                try {
                    if (timeoutMs > 0) {
                        Pair<String, String> result = checkRealTimeData(timeoutMs,currentRequest.packetSize);

                        timeoutMs = -1;

                        if (result == null) {
                            callback.onTimeout(currentRequest.type);
                        } else {
                            GolfzonLogger.i(":::result " + result.first);
                            notify(result);
                        }
                    }

                } catch (InterruptedException | ExecutionException e) {
                    GolfzonLogger.i(":::[error] " + e);
                    notify(null);
                }
            }
        });
    }

    public void notify(Pair<String, String> result) {
        if (callback != null) callback.onDataCheckResult(result);
    }

    public void stop() {
        GolfzonLogger.i("::::executor shutdown");
        timerExecutor.shutdown();
        taskExecutor.shutdown();

    }

    // 실시간 데이터 검사를 수행하는 메서드
    public Pair<String, String> checkRealTimeData(int timeout, int packetSize) throws InterruptedException, ExecutionException {
        Future<Pair<String, String>> future = taskExecutor.submit(new DataCheckerTask(dataQueue, timeout,packetSize));
        return future.get();
    }


    private static String parseSerialData(String requireAscii) {

        if (requireAscii == null || requireAscii.length() == 0) {
            return requireAscii;
        }

        String filterNewLine = requireAscii.replaceAll("(\r\n|\r|\n|\n\r)", " ");
        String filterSplace = filterNewLine.replaceAll("\\s+", " ");
        return filterSplace.replaceFirst(".$", "");

    }

    private static class DataCheckerTask implements Callable<Pair<String, String>> {

        private final BlockingQueue<String> dataQueue;
        private final int timeoutMs;

        private final int packetSize;


        public DataCheckerTask(BlockingQueue<String> dataQueue, int timeoutMs, int packetSize) {
            this.dataQueue = dataQueue;
            this.timeoutMs = timeoutMs;
            this.packetSize = packetSize;
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

                    if(packetSize >= 3){
                        if (removeNewLine.endsWith("244")) {
                            return new Pair<>("OK", removeNewLine);
                        }
                    }else{
                        if (removeNewLine.endsWith("OK")) {
                            return new Pair<>("OK", removeNewLine);
                        }
                    }

                }

                long currentTime = System.currentTimeMillis();
                if (currentTime - startTime >= timeoutMs) {
                    dataBuilder.setLength(0);
                    return null;
                }
            }
        }
    }

}
