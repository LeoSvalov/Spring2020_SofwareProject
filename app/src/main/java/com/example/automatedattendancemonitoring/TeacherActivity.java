package com.example.automatedattendancemonitoring;

import android.app.AlertDialog;
import android.bluetooth.le.ScanCallback;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TableLayout;

import androidx.appcompat.app.AppCompatActivity;

import java.util.HashMap;
import java.util.Map;

public class TeacherActivity extends AppCompatActivity {
    private ScanCallback scanCallback;
    private Map<String, Student> names = new HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_teacher);
    }

    @Override
    protected void onDestroy() {
        if (scanCallback != null) {
            BluetoothHelper.ADAPTER.getBluetoothLeScanner().stopScan(scanCallback);
        }
        super.onDestroy();
    }

    public void gatherAttendance(View v) {
        Button toggleButton = (Button) v;
        toggleButton.setText(R.string.stop_gathering_attendance);
        toggleButton.setOnClickListener(this::stopGatheringAttendance);

        scanCallback = BluetoothHelper.scan(
                TeacherActivity.this,
                this::addStudentToTheTable,
                this::gatheringFailed
        );
    }

    public void stopGatheringAttendance(View v) {
        Button toggleButton = (Button) v;
        toggleButton.setText(R.string.gather_attendance);
        toggleButton.setOnClickListener(this::gatherAttendance);

        if (scanCallback != null) {
            BluetoothHelper.ADAPTER.getBluetoothLeScanner().stopScan(scanCallback);
            scanCallback = null;
        }
    }

    private void addStudentToTheTable(String name) {
        if (names.containsKey(name)) return;
        Student student = new Student(TeacherActivity.this, name);
        names.put(name, student);
        ((TableLayout) findViewById(R.id.attendanceTable)).addView(student.getRowView());
    }

    private void gatheringFailed(int errorCode) {
        Button toggleButton = findViewById(R.id.gatherAttendanceButton);
        toggleButton.setText(R.string.gather_attendance);
        toggleButton.setOnClickListener(this::gatherAttendance);

        new AlertDialog.Builder(TeacherActivity.this)
                .setTitle(R.string.gathering_attendance_failed_title)
                .setMessage(R.string.gathering_attendance_failed_message)
                .show();

        Log.e("teacher", "scan failed: " + errorCode);
    }
}
