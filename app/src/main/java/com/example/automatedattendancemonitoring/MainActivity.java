package com.example.automatedattendancemonitoring;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    public void showStudentActivity(View v) {
        startActivity(new Intent(MainActivity.this, StudentActivity.class));
    }

    public void showTeacherActivity(View v) {
        startActivity(new Intent(MainActivity.this, TeacherActivity.class));
    }
}
