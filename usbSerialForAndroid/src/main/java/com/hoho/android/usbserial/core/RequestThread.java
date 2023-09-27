package com.hoho.android.usbserial.core;

import android.text.TextUtils;

import com.hoho.android.usbserial.GolfzonLogger;

import java.util.LinkedList;

public class RequestThread {
    public static int TIME_OUT = 1000;
    public int WAIT_TIME_OUT = 1000;

    private int MAX_COUNT = 3;
    private int RETRY_COUNT = 0;

    private final LinkedList<Request> requestTypeList = new LinkedList<>();

    public DongleManager requestManager;

    private PoolWorker threads;

    private RequestListener requestListener;



    public RequestThread(DongleManager requestManager) {
        this.requestManager = requestManager;
    }

    public synchronized void start() {
        GolfzonLogger.d("threads : " + threads);
        if (threads == null) {
            GolfzonLogger.d("threads start");
            threads = new PoolWorker("GZQueue:#");
            threads.setStart(true);
            synchronized (this.threads) {
                threads.start();
            }
        }
    }


    public LinkedList<Request> getRequestTypeList() {
        return requestTypeList;
    }

    public void addRequestList(Request request) {

        GolfzonLogger.i(":::>>>addRequestList");

        synchronized (requestTypeList) {
            requestTypeList.add(request);
        }
        GolfzonLogger.i("addRequestList : " + request.type + ", size : " + requestTypeList.size());
    }


    private void request() {
        if (!requestTypeList.isEmpty()) {
            try {
                synchronized (requestTypeList) {
                    if (!requestTypeList.isEmpty()) {
                        String type = requestTypeList.get(0).type;
                        String packet = requestTypeList.get(0).packet;
                        requestListener = requestTypeList.get(0).requestListener;
                        MAX_COUNT = requestTypeList.get(0).maxCount;
                        WAIT_TIME_OUT = requestTypeList.get(0).timeout;

                        GolfzonLogger.e("request, type : " + type + ", packet : " + packet + ", requestListener = " + requestListener + " MAX_COUNT " + MAX_COUNT + " WAIT_TIME_OUT " + WAIT_TIME_OUT);

                        if (TextUtils.isEmpty(packet)) {
                            checkRetry();
                            return;
                        }


                        byte[] data = (packet + '\n').getBytes();


                        requestManager.getUsbSerialPort().write(data,500);

                        if(type.equalsIgnoreCase(Feature.REQ_AT_MODE.name())){
                            GolfzonLogger.i(":::>>>after request REQ_AT_MODE");
                            success(null);
                        }

                    }
                }
            } catch (Exception e) {
                GolfzonLogger.e("request Exception... " + e.getMessage());
            }
        }
    }

    public void checkRetry() {
        if (requestManager != null) {
            try {
                if (requestTypeList.size() > 0) {
                    GolfzonLogger.d("thread is running, retry count -> " + RETRY_COUNT + ", Max count -> " + MAX_COUNT + ", type : " + requestTypeList.get(0).type);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }



        remove();

        if (requestTypeList.size() > 0) {
            RETRY_COUNT = 0;
            if (threads != null && threads.isAlive()) {
                synchronized (this.threads) {
                    if (threads.getState() == Thread.State.TIMED_WAITING || threads.getState() == Thread.State.WAITING) {
                        GolfzonLogger.e("notify");
                        threads.notify();
                    } else {
                        GolfzonLogger.e("next start " + threads.getState());
                        if (threads.getState() != Thread.State.RUNNABLE) {
                            close();
                            start();
                        }
                    }
                }
            }
        } else {
            close();
        }

    }


    public void afterCheckRetry() {
        GolfzonLogger.e("afterCheckRetry");

        if (requestTypeList.size() > 0) {
            RETRY_COUNT = 0;
            GolfzonLogger.i("checkRetry, threads :" + threads);
            if (threads != null && threads.isAlive()) {
                synchronized (this.threads) {
                    if (threads.getState() == Thread.State.TIMED_WAITING || threads.getState() == Thread.State.WAITING) {
                        GolfzonLogger.e("notify");
                        threads.notify();
                    } else {
                        GolfzonLogger.e("next start");
                        close();
                        start();
                    }
                }
            }
        } else {
            GolfzonLogger.e("checkRetry ----> close");
            close();
        }
    }

    public void closeAll() {
        GolfzonLogger.i("closeAll : " + threads);
        synchronized (requestTypeList) {
            close();
            requestTypeList.clear();
        }
    }

    public void close() {
        GolfzonLogger.i("close");
        try {
            RETRY_COUNT = 0;

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

    private void remove() {
        RETRY_COUNT = 0;
        synchronized (requestTypeList) {
            if (!requestTypeList.isEmpty()) {
                Request req = requestTypeList.remove(0);
                GolfzonLogger.e("==========remove msg = " + req.type + ", mRequestTypeList size = " + requestTypeList.size());
            }
        }
    }

    public void success(Object item) {
        GolfzonLogger.i("success(), callback : " + requestListener);
        if (requestListener != null) requestListener.onResult(ResultCode.SUCCESS, item);
    }

    public void fail(int error) {
        GolfzonLogger.d("mSDKRequest : " + requestListener + "");
        if (requestListener != null) {
            if (ResultCode.REQUEST_TIMEOUT_ERROR == error) {
                checkRetry();
            }
            requestListener.onResult(error, null);
        }
    }


    private class PoolWorker extends Thread {
        private boolean isStart;

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


                        try {
                            GolfzonLogger.d("thread is running, retry count -> " + RETRY_COUNT + ", Max count -> " + MAX_COUNT + ", type : " + requestTypeList.get(0).type);
                        } catch (Exception e) {
                            GolfzonLogger.e("log error");
                            e.printStackTrace();

                        }



                        if (RETRY_COUNT++ < MAX_COUNT) {
                            request();
                            GolfzonLogger.i("wait : " + WAIT_TIME_OUT);
                            wait(WAIT_TIME_OUT);
                        } else {
                            GolfzonLogger.i("REQUEST TIME OUT!!!!!!");
                            if (!requestTypeList.isEmpty()) {
                                fail(ResultCode.REQUEST_TIMEOUT_ERROR);
                            } else {
                                GolfzonLogger.i("REQUEST TIME OUT!!!!!!");
                                fail(ResultCode.REQUEST_TIMEOUT_ERROR);

                            }

                            break;
                        }
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
