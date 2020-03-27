package com.example.automatedattendancemonitoring;

import android.app.AlertDialog;
import android.bluetooth.le.AdvertiseCallback;
import android.bluetooth.le.AdvertiseSettings;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class StudentActivity extends AppCompatActivity {
    private AdvertiseCallback advertiseCallback;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_student);
    }

    @Override
    protected void onDestroy() {
        if (advertiseCallback != null) {
            BluetoothHelper.ADAPTER.getBluetoothLeAdvertiser().stopAdvertising(advertiseCallback);
        }
        super.onDestroy();
    }

    public void markMe(View v) {
        findViewById(R.id.markMeButton).setEnabled(false);
        findViewById(R.id.fullnameInput).setEnabled(false);

        Toast.makeText(StudentActivity.this, R.string.trying_to_start_marking, Toast.LENGTH_LONG).show();

        EditText fullnameInput = findViewById(R.id.fullnameInput);
        advertiseCallback = BluetoothHelper.advertise(
                StudentActivity.this,
                fullnameInput.getText().toString(),
                this::advertisementStarted,
                this::advertisementFailed
        );
    }

    private void advertisementStarted(AdvertiseSettings settingsInEffect) {
        Toast.makeText(StudentActivity.this, R.string.marking_started, Toast.LENGTH_LONG).show();
    }

    private void advertisementFailed(int errorCode) {
        findViewById(R.id.markMeButton).setEnabled(true);
        findViewById(R.id.fullnameInput).setEnabled(true);

        new AlertDialog.Builder(StudentActivity.this)
                .setTitle(R.string.marking_failed_title)
                .setMessage(R.string.marking_failed_message)
                .show();

        Log.e("student", "advertising failed: " + errorCode);
    }
}
