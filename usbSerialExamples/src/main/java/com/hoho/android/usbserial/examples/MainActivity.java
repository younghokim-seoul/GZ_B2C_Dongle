package com.hoho.android.usbserial.examples;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothClass;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.FragmentManager;

import com.hoho.android.usbserial.GolfzonLogger;
import com.hoho.android.usbserial.core.Feature;
import com.hoho.android.usbserial.core.RealTimeDataChecker;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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


        String inputString = "AT+UDCP=sps://A1DPFDAS356p";
//
//        // 정규식 패턴을 정의합니다.
//        String regexPattern = "sps://([A-Za-z0-9]+)";
//
//        // 패턴을 컴파일합니다.
//        Pattern pattern = Pattern.compile(regexPattern);
//
//        // 입력 문자열에서 패턴과 매치되는 부분을 찾습니다.
//        Matcher matcher = pattern.matcher(inputString);
//
//        // 매치가 발견되었을 때 추출합니다.
//        if (matcher.find()) {
//            String result = matcher.group(1); // 첫 번째 그룹을 가져옵니다.
//            String type = matcher.group(0); // 첫 번째 그룹을 가져옵니다.
//            GolfzonLogger.i(":::result.. " + result );
//            GolfzonLogger.i(":::type.. " + type );
//        } else {
//            GolfzonLogger.i("Pattern not found.");
//        }

        replyCallToSender(this);


    }


    public static void replyCallToSender(Context context) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                Runtime runtime = Runtime.getRuntime();
                Process process;
                try {
                    String cmd = "getprop ro.build.version.sdk";
                    GolfzonLogger.i("cmd with call... = " + cmd);
                    process = runtime.exec(cmd);
                    BufferedReader br = new BufferedReader(new InputStreamReader(process.getInputStream()));
                    String line = "";
                    StringBuffer sb = new StringBuffer();
                    while ((line = br.readLine()) != null) {
                        GolfzonLogger.i("::line => " + line);
                        sb.append(line + "\n");
                    }
                    // Bluetooth 버전 정보를 가져옵니다.
                    GolfzonLogger.e("sb: " + sb);
                    int index = sb.indexOf("Version:");
                    String version = sb.substring(index + 8).trim();
                    GolfzonLogger.e("[Succes] Bluetooth Version: " + version);
                    br.close();



                } catch (Exception e) {
                    e.printStackTrace();
                    GolfzonLogger.e("Unable to execute top command " + e);
                }
            }
        }).start();

    }



    private void filterFeatureType(final String responseMessage) {



        Optional<Feature> feature = Arrays.stream(Feature.values()).filter(featureEnum -> {
            String key = responseMessage.startsWith(Feature.REQ_SET_CONNECTED.getKey()) ? Feature.REQ_SET_CONNECTED.getKey() : responseMessage;
            return featureEnum.getKey().equalsIgnoreCase(key);
        }).findFirst();


        if (feature.isPresent()) {
            Feature feature1 = feature.get();

            GolfzonLogger.i(":::feature1 : " + feature1);
            GolfzonLogger.e(":::mainMsg : " + responseMessage);
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
