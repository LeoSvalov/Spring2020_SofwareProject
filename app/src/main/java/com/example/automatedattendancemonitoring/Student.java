package com.example.automatedattendancemonitoring;

import android.app.Activity;
import android.widget.Button;
import android.widget.TableRow;
import android.widget.TextView;

/**
 * This class handles student's parameters and its representation in GUI
 */
class Student {

    final TableRow row;
    private final Activity activity;
    final int databaseId;

    Student(TeacherActivity activity, String name, int databaseId) {
        this.databaseId = databaseId;
        this.activity = activity;

        TextView nameElement = new TextView(activity);
        nameElement.setText(name);

        pointsElement = new TextView(activity);
        pointsElement.setText(String.valueOf(points));

        Button pointsMinusOneButton = new Button(activity);
        pointsMinusOneButton.setText("-");
        pointsMinusOneButton.setOnClickListener(v -> changePoints(-1));

        Button pointsPlusOneButton = new Button(activity);
        pointsPlusOneButton.setText("+");
        pointsPlusOneButton.setOnClickListener(v -> changePoints(1));

        Button removeButton = new Button(activity);
        removeButton.setText("X");
        removeButton.setOnClickListener(v -> activity.removeStudent(name));

        row = new TableRow(activity);
        row.addView(nameElement);
        row.addView(pointsMinusOneButton);
        row.addView(pointsElement);
        row.addView(pointsPlusOneButton);
        row.addView(removeButton);
    }



    // To handle participation points

    private int points = 0;

    private final TextView pointsElement;

    private void changePoints(int d) {
        int newPoins = points + d;
        DatabaseHelper.updateRecord(activity, databaseId, newPoins, () -> {
            System.out.println("test");
            points = newPoins;
            pointsElement.setText(String.valueOf(points));
        });
    }

}
