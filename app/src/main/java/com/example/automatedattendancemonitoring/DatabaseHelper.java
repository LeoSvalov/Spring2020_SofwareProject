package com.example.automatedattendancemonitoring;

import android.app.Activity;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.core.util.Consumer;

import com.android.volley.Request;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

/**
 * Class with static helper functions for more convenient work with remote database
 */
@SuppressWarnings({"WeakerAccess", "UnusedReturnValue", "unused"})
public final class DatabaseHelper {

    /**
     * To prevent creating instances of this class
     */
    private DatabaseHelper() {
    }

    /**
     * Address of database api
     */
    public static final String apiAddress = "https://immense-bayou-90265.herokuapp.com/api/attendances";

    /**
     * Sends request to add a record to the database.
     * In case of error, shows {@link Toast} error message.
     * In case of success, passes id of record to {@code callback}
     *
     * @param activity  activity, which will be used to create {@link Toast} messages
     * @param teacherId google id of the teacher
     * @param lessonId  id of the lesson
     * @param fullname  full name of a student
     * @param subject   name of a subject
     * @param type      type of a lesson
     * @param callback  function to execute after successful record insertion
     */
    public static void addRecord(Activity activity, String teacherId, String lessonId, String fullname, String subject, String type, Consumer<Integer> callback) {
        Volley.newRequestQueue(activity).add(new StringRequest(Request.Method.POST, apiAddress,
                response -> {
                    try {
                        int id = new JSONObject(response).getInt("id");
                        callback.accept(id);
                    } catch (JSONException e) {
                        Toast.makeText(activity, String.format(activity.getString(R.string.add_student_error), fullname), Toast.LENGTH_LONG).show();
                        Log.e("DB", "Error parsing response");
                    }
                },
                error -> {
                    Toast.makeText(activity, String.format(activity.getString(R.string.add_student_error), fullname), Toast.LENGTH_LONG).show();
                    Log.e("DB", "Error adding record: " + error.toString());
                }
        ) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("t_google_id", teacherId);
                params.put("lesson_id", lessonId);
                params.put("student_fullname", fullname);
                params.put("subject", subject);
                params.put("type", type);
                return params;
            }
        });
    }

    /**
     * Sends request to delete a record to the database.
     * In case of error, shows {@link Toast} error message.
     * In case of success, executes {@code callback}
     *
     * @param activity activity, which will be used to create {@link Toast} messages
     * @param id       id of record to delete
     * @param callback function to execute after successful record deletion
     */
    public static void removeRecord(Activity activity, int id, Runnable callback) {
        Volley.newRequestQueue(activity).add(new StringRequest(Request.Method.DELETE, apiAddress + "/" + id,
                response -> callback.run(),
                error -> {
                    Toast.makeText(activity, activity.getString(R.string.remove_student_error), Toast.LENGTH_LONG).show();
                    Log.e("DB", "Error deleting record: " + error.toString());
                }
        ));
    }

    /**
     * Sends request to update a record to the database.
     * In case of error, shows {@link Toast} error message.
     * In case of success, executes {@code callback}
     *
     * @param activity activity, which will be used to create {@link Toast} messages
     * @param id       id of record to update
     * @param points   new amount of points
     * @param callback function to execute after successful record deletion
     */
    public static void updateRecord(Activity activity, int id, int points, Runnable callback) {
        Volley.newRequestQueue(activity).add(new StringRequest(Request.Method.PUT, apiAddress + "/" + id,
                response -> callback.run(),
                error -> {
                    Toast.makeText(activity, activity.getString(R.string.update_points_error), Toast.LENGTH_LONG).show();
                    Log.e("DB", "Error updating record: " + error.toString());
                }
        ) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("points", String.valueOf(points));
                return params;
            }
        });
    }

    /**
     * Sets appropriate adapter to the {@code spinner}, using {@code items}
     * to identify which items spinner should have.<br>
     * Currently, {@code items} is used only to get data from local file,
     * but it may be used to get data from the database
     *
     * @param activity activity, which will be used to get array from resources
     *                 and/or to connect to the database
     * @param spinner  spinner, which will have its items changed
     * @param items    identifier, which will be used to get items for spinner
     */
    public static void setSpinnerItems(Activity activity, Spinner spinner, String items) {
        int resId = activity.getResources().getIdentifier(items, "array", activity.getPackageName());
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                activity,
                resId,
                android.R.layout.simple_spinner_dropdown_item
        );
        spinner.setAdapter(adapter);
    }

}
