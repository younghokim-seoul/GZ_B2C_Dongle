package com.hoho.android.usbserial.core;

import com.hoho.android.usbserial.GolfzonLogger;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class RealTimeDataChecker {

    private ExecutorService executor; // 스레드 풀


    public RealTimeDataChecker() {
        this.executor = Executors.newSingleThreadExecutor();
    }


    // 실시간 데이터 검사를 수행하는 메서드
    public String checkRealTimeData(BlockingQueue<String> dataQueue, int timeoutMs) throws TimeoutException {


        try {
            Callable<String> dataCheckTask = () -> {

                long startTime = System.currentTimeMillis();
                StringBuilder dataBuilder = new StringBuilder();
                while (true) {
                    String data = dataQueue.poll();

                    if (data != null) {
                        GolfzonLogger.i("::::검사필요한 데이터 -> " + data);
                        GolfzonLogger.i("before -> " + dataBuilder.toString());
                        dataBuilder.append(data);
                        GolfzonLogger.i("after -> " + dataBuilder.toString());

                        String removeNewLine = parseSerialData(dataBuilder.toString());
                        GolfzonLogger.i("removeNewLine = " + removeNewLine);
                        if (removeNewLine.endsWith("OK")) {
                            GolfzonLogger.e(":::::ok 데이터 "  + removeNewLine);
                            return "OK";
                        }
                    }

                    long currentTime = System.currentTimeMillis();
                    if (currentTime - startTime >= timeoutMs) {
                        dataBuilder.setLength(0);
                        throw new TimeoutException("데이터가 시간 내에 도착하지 않았습니다.");
                    }
                }
            };
            // 데이터 검사 태스크를 스레드 풀에 제출하고 결과를 기다림
            Future<String> future = executor.submit(dataCheckTask);
            return future.get(timeoutMs, TimeUnit.MILLISECONDS);
        } catch (InterruptedException | ExecutionException e) {
            // 오류가 발생하면 Exception을 throw하도록 수정
            GolfzonLogger.e("::대체 무슨 에러예요? " + e);
            throw new RuntimeException("오류 발생", e);
        } finally {
//            executor.shutdown();
        }
    }

    public void shutdownExecutor() {
        executor.shutdown();
    }


    private String parseSerialData(String requireAscii) throws Exception {

        if(requireAscii == null || requireAscii.length() == 0){
            return requireAscii;
        }

        String filterNewLine = requireAscii.replaceAll("(\r\n|\r|\n|\n\r)", " ");
        GolfzonLogger.i("filterNewLine => " + filterNewLine);
        return  filterNewLine.replaceFirst(".$", "");

    }

}
