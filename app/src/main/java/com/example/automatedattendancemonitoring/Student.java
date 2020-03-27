package com.example.automatedattendancemonitoring;

import android.app.Activity;
import android.widget.TableRow;
import android.widget.TextView;

/**
 * This class handles student's parameters and its representation in GUI
 */
class Student {

    private final TableRow row;

    Student(Activity activity, String name) {
        TextView nameElement = new TextView(activity);
        nameElement.setText(name);

        TextView pointsElement = new TextView(activity);
        pointsElement.setText("0");

        row = new TableRow(activity);
        row.addView(nameElement);
        row.addView(pointsElement);
    }

    TableRow getRowView() {
        return row;
    }

}
