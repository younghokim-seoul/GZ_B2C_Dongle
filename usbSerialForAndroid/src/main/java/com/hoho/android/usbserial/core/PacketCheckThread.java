package com.hoho.android.usbserial.core;

import com.hoho.android.usbserial.GolfzonLogger;

import java.util.List;

public class PacketCheckThread {

    private PoolWorker threads;

    private ResponseManager responseManager;

    private Request request;

    private PacketCheckListener listener;





    public PacketCheckThread(ResponseManager responseManager) {
        this.responseManager = responseManager;
    }

    public synchronized void start(Request request) {
        GolfzonLogger.d("threads : " + threads);
        if (threads == null) {
            GolfzonLogger.d("response Check Thread start");
            this.request = request;
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
                responseManager.bufferClear();
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
            request = null;
        }
    }


    public void success(Object item) {

        if (listener != null) listener.onResult(ResultCode.SUCCESS, item);
    }

    public void fail() {
        if (listener != null) {
            listener.onResult(ResultCode.FAIL, null);
        }
    }



    private class PoolWorker extends Thread {
        private boolean isStart;
        private String packetStr ="";

        public PoolWorker(String threadName) {
            super(threadName);
        }

        public void setStart(boolean is) {
            isStart = is;
        }


        @Override
        public void run() {
            GolfzonLogger.e(":isStart = " + isStart + " request " + request);
            while (isStart) {
                synchronized (this) {
                    try {
                        GolfzonLogger.e("thread 테스트...");

                        listener = request.packetCheckListener;

                        List<byte[]> packetItems  = responseManager.getPacketBuffer();

                        packetItems.forEach(bytes -> packetStr += new String(bytes));

                        GolfzonLogger.e("before packetStr => " + packetStr);

                        String filterNewLine = packetStr.replaceAll("(\r\n|\r|\n|\n\r)", "");

                        GolfzonLogger.i("filterNewLine => " + filterNewLine);


                        //실시간 체크....
                        if(isCheckSum(filterNewLine)){
                            success(null);
                            close();
                        }

                        wait(request.timeout / 2);

                        // wait 후... 재검사..

                        GolfzonLogger.e("after packetStr => " + filterNewLine);

                        if(isCheckSum(filterNewLine)){
                            //마지노선 이후.. 들어왔다면?
                            success(null);
                        }else{
                            fail();
                        }
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

        private boolean isCheckSum(String filterNewLine){
            String response = filterNewLine.substring(filterNewLine.length() -2);

            if(response.equalsIgnoreCase("ok")){
                GolfzonLogger.e("ok Filter");
                return true;
            }else{
                return false;
            }
        }
    }

}
