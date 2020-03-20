package com.example.automatedattendancemonitoring;

import android.bluetooth.le.ScanCallback;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.util.HashSet;
import java.util.Set;

public class TeacherActivity extends AppCompatActivity {
    private ScanCallback scanCallback;
    private Set<String> names = new HashSet<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_teacher);
    }

    @Override
    protected void onDestroy() {
        if (scanCallback != null) {
            BluetoothHelper.SCANNER.stopScan(scanCallback);
        }
        super.onDestroy();
    }

    public void gatherAttendance(View v) {
        Button toggleButton = (Button) v;
        toggleButton.setText(R.string.stop_gathering_attendance);
        toggleButton.setOnClickListener(this::stopGatheringAttendance);

        BluetoothHelper.enableAndExecute(
                TeacherActivity.this,
                () -> scanCallback = BluetoothHelper.scan(
                        TeacherActivity.this,
                        this::addStudentToTheList,
                        this::gatheringFailed
                )
        );
    }

    public void stopGatheringAttendance(View v) {
        Button toggleButton = (Button) v;
        toggleButton.setText(R.string.gather_attendance);
        toggleButton.setOnClickListener(this::gatherAttendance);

        if (scanCallback != null) {
            BluetoothHelper.SCANNER.stopScan(scanCallback);
            scanCallback = null;
        }
    }

    private void addStudentToTheList(String name) {
        if (names.contains(name)) return;
        names.add(name);

        TextView list = findViewById(R.id.attendanceList);
        list.setText(name + "\n" + list.getText());
    }

    private void gatheringFailed(int errorCode) {
        Button toggleButton = findViewById(R.id.gatherAttendanceButton);
        toggleButton.setText(R.string.gather_attendance);
        toggleButton.setOnClickListener(this::gatherAttendance);

        //TODO: show error dialog
    }
}
