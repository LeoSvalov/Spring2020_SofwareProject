package com.example.automatedattendancemonitoring;

import android.app.AlertDialog;
import android.bluetooth.le.ScanCallback;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TableLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.util.HashMap;
import java.util.Map;

public class TeacherActivity extends AppCompatActivity {

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


    //Automatic gathering

    public void gatherAttendance(View v) {
        changeGatheringButton(true);

        scanCallback = BluetoothHelper.scan(
                TeacherActivity.this,
                this::addStudentToTheTable,
                this::gatheringFailed
        );
    }

    public void stopGatheringAttendance(View v) {
        changeGatheringButton(false);

        if (scanCallback != null) {
            BluetoothHelper.ADAPTER.getBluetoothLeScanner().stopScan(scanCallback);
            scanCallback = null;
        }
    }

    private void gatheringFailed(int errorCode) {
        changeGatheringButton(false);

        new AlertDialog.Builder(TeacherActivity.this)
                .setTitle(R.string.gathering_attendance_failed_title)
                .setMessage(R.string.gathering_attendance_failed_message)
                .show();

        Log.e("teacher", "scan failed: " + errorCode);
    }

    private void changeGatheringButton(boolean isGathering) {
        Button toggleButton = findViewById(R.id.gatherAttendanceButton);
        if (isGathering) {
            toggleButton.setText(R.string.stop_gathering_attendance);
            toggleButton.setOnClickListener(this::stopGatheringAttendance);
        } else {
            toggleButton.setText(R.string.gather_attendance);
            toggleButton.setOnClickListener(this::gatherAttendance);
        }
    }


    // Table management

    private ScanCallback scanCallback;
    private Map<String, Student> students = new HashMap<>();

    public void addStudentManually(View v) {
        TextView fullnameInput = findViewById(R.id.addStudentManuallyFullnameInput);
        addStudentToTheTable(fullnameInput.getText().toString());
        fullnameInput.setText("");
    }

    private void addStudentToTheTable(String name) {
        if (students.containsKey(name)) return;
        Student student = new Student(TeacherActivity.this, name);
        students.put(name, student);
        ((TableLayout) findViewById(R.id.attendanceTable)).addView(student.getRowView());
    }

    public void removeStudentFromTheTable(String name) {
        Student student = students.get(name);
        if (student == null) return;
        students.remove(name);
        ((TableLayout) findViewById(R.id.attendanceTable)).removeView(student.getRowView());
    }
}
