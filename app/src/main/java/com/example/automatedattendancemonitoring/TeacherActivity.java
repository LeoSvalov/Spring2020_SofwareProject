package com.example.automatedattendancemonitoring;

import android.app.AlertDialog;
import android.bluetooth.le.ScanCallback;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TableLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

public class TeacherActivity extends AppCompatActivity {
    private final String lessonId = UUID.randomUUID().toString();
    private String teacherId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_teacher);

        teacherId = getIntent().getStringExtra("googleId");
        if (teacherId == null) {
            Log.e("teacher", "No google id");
            Toast.makeText(TeacherActivity.this, R.string.login_error, Toast.LENGTH_LONG).show();
            finish();
        }
    }

    @Override
    protected void onDestroy() {
        if (scanCallback != null && BluetoothHelper.ADAPTER.getBluetoothLeScanner() != null) {
            BluetoothHelper.ADAPTER.getBluetoothLeScanner().stopScan(scanCallback);
        }
        super.onDestroy();
    }


    //Automatic gathering

    public void gatherAttendance(View v) {
        changeGatheringButton(true);

        scanCallback = BluetoothHelper.scan(
                TeacherActivity.this,
                this::addStudent,
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
        addStudent(fullnameInput.getText().toString());
        fullnameInput.setText("");
    }

    private void addStudent(String name) {
        if (Objects.equals(name, "")) return;
        if (students.containsKey(name)) return;

        // to database
        DatabaseHelper.addRecord(TeacherActivity.this, teacherId, lessonId, name, id -> {
            // to list
            Student student = new Student(TeacherActivity.this, name, id);
            students.put(name, student);

            // to table
            ((TableLayout) findViewById(R.id.attendanceTable)).addView(student.row);
        });
    }

    public void removeStudent(String name) {
        Student student = students.get(name);
        if (student == null) return;

        // from database
        DatabaseHelper.removeRecord(TeacherActivity.this, student.databaseId, () -> {
            // from list
            students.remove(name);

            // from table
            ((TableLayout) findViewById(R.id.attendanceTable)).removeView(student.row);
        });
    }
}
