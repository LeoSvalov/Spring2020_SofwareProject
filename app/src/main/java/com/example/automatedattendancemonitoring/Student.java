package com.example.automatedattendancemonitoring;

import android.widget.Button;
import android.widget.TableRow;
import android.widget.TextView;

/**
 * This class handles student's parameters and its representation in GUI
 */
class Student {

    Student(TeacherActivity activity, String name) {
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
        removeButton.setOnClickListener(v -> activity.removeStudentFromTheTable(name));

        row = new TableRow(activity);
        row.addView(nameElement);
        row.addView(pointsMinusOneButton);
        row.addView(pointsElement);
        row.addView(pointsPlusOneButton);
        row.addView(removeButton);
    }


    // The row in table with all necessary columns

    private final TableRow row;

    TableRow getRowView() {
        return row;
    }


    // To handle participation points

    private int points = 0;

    private final TextView pointsElement;

    private void changePoints(int d) {
        points += d;
        pointsElement.setText(String.valueOf(points));
    }

}
