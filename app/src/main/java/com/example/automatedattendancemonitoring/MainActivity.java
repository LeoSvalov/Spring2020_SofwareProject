package com.example.automatedattendancemonitoring;

import androidx.appcompat.app.AppCompatActivity;

import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.os.Bundle;
import android.os.Parcel;
import android.os.ParcelUuid;
import android.view.View;

import java.util.UUID;

public class MainActivity extends AppCompatActivity {
    public static final ParcelUuid BLUETOOTH_UUID = ParcelUuid.fromString("00000000-0000-1000-8000-00805F9B34FB");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        BluetoothAdapter.getDefaultAdapter().disable();
    }

    @Override
    protected void onDestroy() {
        BluetoothAdapter.getDefaultAdapter().disable();
        super.onDestroy();
    }

    public void showStudentActivity(View v) {
        startActivity(new Intent(MainActivity.this, StudentActivity.class));
    }

    public void showTeacherActivity(View v) {
        startActivity(new Intent(MainActivity.this, TeacherActivity.class));
    }
}
