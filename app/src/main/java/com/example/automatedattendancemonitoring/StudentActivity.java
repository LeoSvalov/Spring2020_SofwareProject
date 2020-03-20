package com.example.automatedattendancemonitoring;

import android.bluetooth.le.AdvertiseCallback;
import android.bluetooth.le.AdvertiseSettings;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;

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
            BluetoothHelper.ADVERTISER.stopAdvertising(advertiseCallback);
        }
        super.onDestroy();
    }

    public void markMe(View v) {
        findViewById(R.id.markMeButton).setEnabled(false);
        findViewById(R.id.fullnameInput).setEnabled(false);

        //TODO: add loading indicator

        BluetoothHelper.enableAndExecute(
                StudentActivity.this,
                () -> {
                    EditText fullnameInput = findViewById(R.id.fullnameInput);
                    advertiseCallback = BluetoothHelper.advertise(
                            fullnameInput.getText().toString(),
                            this::advertisementStarted,
                            this::advertisementFailed
                    );
                }
        );
    }

    private void advertisementStarted(AdvertiseSettings settingsInEffect) {
        //TODO: add indicator
    }

    private void advertisementFailed(int errorCode) {
        findViewById(R.id.markMeButton).setEnabled(true);
        findViewById(R.id.fullnameInput).setEnabled(true);

        //TODO: show error dialog

        Log.e("student", "advertising failed: " + errorCode);
    }
}
