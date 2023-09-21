package com.hoho.android.usbserial.core;

import com.hoho.android.usbserial.GolfzonLogger;

import java.util.List;
import java.util.function.Consumer;

public class CheckResponseThread {

    private PoolWorker threads;

    private ResponseManager responseManager;

    private Request request;





    public CheckResponseThread(ResponseManager responseManager) {
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


//    public void success(Object item) {
//        GolfzonLogger.i("success(), callback : " + requestListener);
//        if (requestListener != null) requestListener.onResult(ResultCode.SUCCESS, item);
//    }
//
//    public void fail(int error) {
//        GolfzonLogger.d("mSDKRequest : " + requestListener + "");
//        if (requestListener != null) {
//            if (ResultCode.REQUEST_TIMEOUT_ERROR == error) {
////                checkRetry();
//            }
//            requestListener.onResult(error, null);
//        }
//    }



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
                        List<byte[]> packetItems  = responseManager.getPacketBuffer();

                        packetItems.forEach(bytes -> packetStr += new String(bytes));

                        GolfzonLogger.e("before packetStr => " + packetStr);



                        String filterNewLine = packetStr.replaceAll("(\r\n|\r|\n|\n\r)", "");
                        GolfzonLogger.i("filterNewLine => " + filterNewLine);

                        String response = filterNewLine.substring(filterNewLine.length() -2);

                        GolfzonLogger.e("response => " + response);

                        if(response.equalsIgnoreCase("ok")){
                            GolfzonLogger.e("ok Filter");
                            close();
                        }

                        wait(request.timeout / 2);

                        GolfzonLogger.e("after packetStr => " + packetStr);
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
