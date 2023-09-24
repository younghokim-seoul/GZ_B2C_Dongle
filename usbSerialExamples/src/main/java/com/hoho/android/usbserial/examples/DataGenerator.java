package com.hoho.android.usbserial.examples;

import com.hoho.android.usbserial.GolfzonLogger;
import com.hoho.android.usbserial.core.RealTimeDataChecker;

import java.util.Random;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class DataGenerator {
    private final ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();
    private final BlockingQueue<String> dataQueue = new LinkedBlockingQueue<>();
    private final Random random = new Random();

    public void startDataGeneration() {
        executor.scheduleAtFixedRate(() -> {
            // 랜덤한 문자열 생성 (가끔 "ok" 제외)
            StringBuilder dataBuilder = new StringBuilder();
            int length = random.nextInt(10) + 1; // 1~10 길이의 문자열 생성
            for (int i = 0; i < length; i++) {
                char randomChar = (char) (random.nextInt(26) + 'a'); // 소문자 알파벳
                dataBuilder.append(randomChar);
            }

            // "ok"를 끝에 추가 (가끔 제외)
            if (random.nextInt(10) < 8) { // 80% 확률로 "ok" 추가
                dataBuilder.append("ok");
            }

            String data = dataBuilder.toString();
            GolfzonLogger.i("Generated Data: " + data);

            // 생성된 데이터를 큐에 추가
            dataQueue.add(data);

        }, 0, 200, TimeUnit.MILLISECONDS); // 100ms마다 데이터 생성 및 큐에 추가
    }

    public void stopDataGeneration() {
        executor.shutdown();
    }

    public BlockingQueue<String> getDataQueue() {
        return dataQueue;
    }

    public void clearQueue(){
        dataQueue.clear();
    }
}
