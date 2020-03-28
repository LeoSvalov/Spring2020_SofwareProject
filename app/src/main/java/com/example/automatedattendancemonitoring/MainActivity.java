package com.example.automatedattendancemonitoring;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (BluetoothHelper.ADAPTER == null) {
            new AlertDialog.Builder(MainActivity.this)
                    .setTitle(R.string.no_bluetooth_title)
                    .setMessage(R.string.no_bluetooth_message)
                    .setOnDismissListener(dialog -> finish())
                    .show();
        }
    }

    public void showStudentActivity(View v) {
        startActivity(new Intent(MainActivity.this, StudentActivity.class));
    }

    public void showTeacherActivity(View v) {
        startActivity(new Intent(MainActivity.this, TeacherActivity.class));
    }
}
