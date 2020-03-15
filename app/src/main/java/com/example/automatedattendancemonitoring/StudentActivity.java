package com.example.automatedattendancemonitoring;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.bluetooth.le.AdvertiseCallback;
import android.bluetooth.le.AdvertiseData;
import android.bluetooth.le.AdvertiseSettings;
import android.bluetooth.le.BluetoothLeAdvertiser;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.ParcelUuid;
import android.os.Parcelable;
import android.util.Log;
import android.view.View;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.LinkedList;

public class StudentActivity extends AppCompatActivity {
    private BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_student);

        registerReceiver(bluetoothStateChanged, new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED));
    }

    @Override
    protected void onDestroy() {
        bluetoothAdapter.disable();
        unregisterReceiver(bluetoothStateChanged);
        super.onDestroy();
    }

    public void markMe(View v) {
        //TODO: discover if already enabled
        bluetoothAdapter.enable();
//        Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
//        startActivityForResult(enableBtIntent, 0);
    }

    private final BroadcastReceiver bluetoothStateChanged = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            if (intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, -1) == BluetoothAdapter.STATE_ON) {
                //TODO: bypass asking user?
//                int MY_PERMISSIONS_REQUEST_ACCESS_COARSE_LOCATION = 1;
//                ActivityCompat.requestPermissions(StudentActivity.this,
//                        new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},
//                        MY_PERMISSIONS_REQUEST_ACCESS_COARSE_LOCATION);

                BluetoothLeAdvertiser advertiser = bluetoothAdapter.getBluetoothLeAdvertiser();
                AdvertiseSettings settings = new AdvertiseSettings.Builder()
                        .setAdvertiseMode(AdvertiseSettings.ADVERTISE_MODE_LOW_LATENCY)
                        .setTxPowerLevel(AdvertiseSettings.ADVERTISE_TX_POWER_HIGH)
                        .setConnectable(false)
                        .build();

                AdvertiseData data = new AdvertiseData.Builder()
                        .setIncludeDeviceName(false)
                        .addServiceUuid(MainActivity.BLUETOOTH_UUID)
                        .addServiceData(MainActivity.BLUETOOTH_UUID, ((EditText) findViewById(R.id.fullname_input)).getText().toString().getBytes(StandardCharsets.UTF_8)) //TODO: length limit
                        .build();
                Log.d("student", data.toString());

                advertiser.startAdvertising(settings, data, advertisingCallback);
            }
        }
    };

    private AdvertiseCallback advertisingCallback = new AdvertiseCallback() {
        @Override
        public void onStartSuccess(AdvertiseSettings settingsInEffect) {
            super.onStartSuccess(settingsInEffect);
        }

        @Override
        public void onStartFailure(int errorCode) {
            Log.e( "student", "Advertising onStartFailure: " + errorCode );
            super.onStartFailure(errorCode);
        }
    };
}
