package com.hoho.android.usbserial.examples;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.FragmentManager;

import com.hoho.android.usbserial.GolfzonLogger;
import com.hoho.android.usbserial.core.RealTimeDataChecker;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeoutException;


public class MainActivity extends AppCompatActivity implements FragmentManager.OnBackStackChangedListener {


    RealTimeDataChecker realTimeDataChecker;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        GolfzonLogger.i("::::MainActivity onCreate");
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportFragmentManager().addOnBackStackChangedListener(this);

        if (savedInstanceState == null)
            getSupportFragmentManager().beginTransaction().add(R.id.fragment, new DevicesFragment(), "devices").commit();
        else
            onBackStackChanged();




        DataGenerator dataGenerator = new DataGenerator();
        dataGenerator.startDataGeneration(); // 데이터 생성 시작



        RealTimeDataChecker checker = new RealTimeDataChecker();





            // DataGenerator 클래스에서 생성된 데이터를 지속적으로 검사
            while (true) {
                try{
                    BlockingQueue<String> sharedDataQueue = dataGenerator.getDataQueue();
                    String result = checker.checkRealTimeData(sharedDataQueue,700);
                    handleDataCheckResult(result,dataGenerator);

                }catch (TimeoutException e){
                    GolfzonLogger.e(":::TimeoutException => " + e);
                }catch (Exception e){
                    GolfzonLogger.e(":::e => " + e);
                }finally {
//                    checker.shutdownExecutor();
                }

            }


    }

    private void handleDataCheckResult(String result, DataGenerator dataGenerator) {
        if ("OK".equals(result)) {
            // 데이터가 정상적으로 검사되었을 때 처리
            // 예: 데이터를 화면에 표시하거나 다음 작업 수행
            GolfzonLogger.e("데이터가 정상적으로 검사되었을 때 처리");
            dataGenerator.clearQueue();
        } else {
            // 타임아웃 또는 검사 실패 시 처리
            // 예: 에러 메시지 출력 또는 다른 작업 수행
            GolfzonLogger.e("타임아웃 또는 검사 실패 시 처리");
        }
    }





    @Override
    public void onBackStackChanged() {
        getSupportActionBar().setDisplayHomeAsUpEnabled(getSupportFragmentManager().getBackStackEntryCount() > 0);
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    @Override
    protected void onNewIntent(Intent intent) {
        if ("android.hardware.usb.action.USB_DEVICE_ATTACHED".equals(intent.getAction())) {
            TerminalFragment terminal = (TerminalFragment) getSupportFragmentManager().findFragmentByTag("terminal");
            if (terminal != null) terminal.status("USB device detected");
        }
        super.onNewIntent(intent);
    }


}
