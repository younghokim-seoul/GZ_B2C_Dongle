package com.hoho.android.usbserial.core;

import com.hoho.android.usbserial.GolfzonLogger;

import java.util.List;
import java.util.function.Consumer;

public class CheckResponseThread {

    private PoolWorker threads;

    private ResponseManager responseManager;




    public CheckResponseThread(ResponseManager responseManager) {
        this.responseManager = responseManager;
    }

    public synchronized void start() {
        GolfzonLogger.d("threads : " + threads);
        if (threads == null) {
            GolfzonLogger.d("response Check Thread start");
            threads = new PoolWorker("GZTimer:#");
            threads.setStart(true);
            synchronized (this.threads) {
                threads.start();
            }
        }
    }

    public void close() {
        GolfzonLogger.i("close");
        try {


            if (threads != null) {
                threads.setStart(false);
                GolfzonLogger.i("interrrupt");
                threads.interrupt();
            } else {
                GolfzonLogger.i("threads is null");
            }

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            threads = null;
        }
    }


    private class PoolWorker extends Thread {
        private boolean isStart;
        private String packetStr;

        public PoolWorker(String threadName) {
            super(threadName);
        }

        public void setStart(boolean is) {
            isStart = is;
        }

        @Override
        public void run() {
            GolfzonLogger.e(":isStart = " + isStart);
            while (isStart) {
                synchronized (this) {
                    try {
                        GolfzonLogger.e("thread 테스트...");
                        List<byte[]> packetItems  = responseManager.getPacketBuffer();

                        packetItems.forEach(bytes -> packetStr += new String(bytes));

                        GolfzonLogger.e("packetStr => " + packetStr);
                        wait(300);
                        close();
                    } catch (InterruptedException e) {
                        GolfzonLogger.i("InterruptedException, isStart : " + isStart);
//                        e.printStackTrace();
                        if (!isStart) {
                            break;
                        } else {
                            continue;
                        }
                    } catch (Exception e1) {
//                        e1.printStackTrace();
                        break;
                    }
                }
                GolfzonLogger.i("Request Thread is dead.");
            }

        }
    }

}
