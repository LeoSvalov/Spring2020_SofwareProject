package com.example.automatedattendancemonitoring;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.ParcelUuid;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public class TeacherActivity extends AppCompatActivity {
    private BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_teacher);

        registerReceiver(bluetoothStateChanged, new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED));
    }

    @Override
    protected void onDestroy() {
        bluetoothAdapter.disable();
        unregisterReceiver(bluetoothStateChanged);
        super.onDestroy();
    }

    public void gatherAttendance(View v) {
//        bluetoothAdapter.enable();
        //TODO: if already enabled
        //TODO: bypass asking user?
        Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
        startActivityForResult(enableBtIntent, 0);
        Intent discoverableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
        discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 300);
        startActivity(discoverableIntent);

        Button toggleButton = (Button) v;
        toggleButton.setText(R.string.stop_gathering_attendance);
        toggleButton.setOnClickListener(this::stopGatheringAttendance);
    }

    private final BroadcastReceiver bluetoothStateChanged = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
        if (intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, -1) == BluetoothAdapter.STATE_ON) {
            BluetoothLeScanner scanner = bluetoothAdapter.getBluetoothLeScanner();

            ScanFilter filter = new ScanFilter.Builder()
                    .setServiceUuid(MainActivity.BLUETOOTH_UUID)
                    .build();
            List<ScanFilter> filters = new LinkedList<>();
            filters.add( filter );

            ScanSettings settings = new ScanSettings.Builder()
                    .setScanMode( ScanSettings.SCAN_MODE_LOW_LATENCY )
                    .build();

            Log.d("teacher", "scanning");
            scanner.startScan(filters, settings, mScanCallback);
        }
        }
    };

    private Set<String> addresses = new HashSet<>();

    private ScanCallback mScanCallback = new ScanCallback() {
        @Override
        public void onScanResult(int callbackType, ScanResult result) {
            super.onScanResult(callbackType, result);
            Log.d("teacher", "got result");
            if (result != null) Log.d("teacher", result.toString());
            if( result == null || result.getDevice() == null ) return;
            Log.d("teacher", result.toString());

            String address = result.getDevice().getAddress();
            if (addresses.contains(address)) {
                return;
                //TODO: notify about cheating
            } else {
                addresses.add(address);
            }

            Log.d("teacher", result.toString());
            String data = new String(result.getScanRecord().getServiceData(MainActivity.BLUETOOTH_UUID), StandardCharsets.UTF_8);
            Log.d("teacher", data);

            TextView discoveredDevices = findViewById(R.id.discovered_devices);
            discoveredDevices.setText( data + discoveredDevices.getText());
        }

        @Override
        public void onScanFailed(int errorCode) {
            Log.e( "BLE", "Discovery onScanFailed: " + errorCode );
            super.onScanFailed(errorCode);
        }
    };

    public void stopGatheringAttendance(View v) {
        bluetoothAdapter.disable();

        Button toggleButton = (Button) v;
        toggleButton.setText(R.string.gather_attendance);
        toggleButton.setOnClickListener(this::gatherAttendance);
    }
}
