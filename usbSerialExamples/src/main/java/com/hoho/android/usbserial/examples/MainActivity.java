package com.hoho.android.usbserial.examples;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.FragmentManager;

import com.hoho.android.usbserial.GolfzonLogger;
import com.hoho.android.usbserial.core.Feature;
import com.hoho.android.usbserial.core.RealTimeDataChecker;

import java.util.Arrays;
import java.util.Optional;
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

        String test1= "AT+UDLP? +UDLP:0,\"com\",\"com1\",\"\" OK";
        String test2= "AT+UDLP? +UDLP:0,\"com\",\"com1\",\"\" +UDLP:1,\"sps\",\"D4CA6EF5932Ap\",\"D4CA63F2C8F1p\" OK";
        GolfzonLogger.i("::test1 => " + test1);

        GolfzonLogger.i("::test2 => " + test2);

        String[]test1Split = test1.split(" ");
        String[]test2Split = test2.split(" ");

        GolfzonLogger.i("::test1Split => " + test1Split.length);
        GolfzonLogger.i("::test2Split => " + test2Split.length);



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
