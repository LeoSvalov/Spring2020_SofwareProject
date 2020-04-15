package com.example.automatedattendancemonitoring;

import android.app.AlertDialog;
import android.bluetooth.le.AdvertiseCallback;
import android.bluetooth.le.AdvertiseSettings;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class StudentActivity extends AppCompatActivity {
    private AdvertiseCallback advertiseCallback;
    private String fullname;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_student);

        fullname = getIntent().getStringExtra("fullname");
        if (fullname == null) {
            Log.e("student", "No fullname provided");
            Toast.makeText(StudentActivity.this, R.string.login_error, Toast.LENGTH_LONG).show();
            finish();
        }
        ((TextView) findViewById(R.id.fullnameTextView)).setText(fullname);
    }

    @Override
    protected void onDestroy() {
        if (advertiseCallback != null && BluetoothHelper.ADAPTER.getBluetoothLeAdvertiser() != null) {
            BluetoothHelper.ADAPTER.getBluetoothLeAdvertiser().stopAdvertising(advertiseCallback);
        }
        super.onDestroy();
    }

    public void markMe(View v) {
        findViewById(R.id.markMeButton).setEnabled(false);

        Toast.makeText(StudentActivity.this, R.string.trying_to_start_marking, Toast.LENGTH_LONG).show();

        advertiseCallback = BluetoothHelper.advertise(
                StudentActivity.this,
                fullname,
                this::advertisementStarted,
                this::advertisementFailed
        );
    }

    private void advertisementStarted(AdvertiseSettings settingsInEffect) {
        Toast.makeText(StudentActivity.this, R.string.marking_started, Toast.LENGTH_LONG).show();
    }

    private void advertisementFailed(int errorCode) {
        findViewById(R.id.markMeButton).setEnabled(true);

        new AlertDialog.Builder(StudentActivity.this)
                .setTitle(R.string.marking_failed_title)
                .setMessage(R.string.marking_failed_message)
                .show();

        Log.e("student", "advertising failed: " + errorCode);
    }
}
